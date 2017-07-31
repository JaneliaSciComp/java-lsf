package org.janelia.cluster;

import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Manager for submitting and monitoring cluster jobs. During construction, a concrete JobSyncApi implementation 
 * must be provided to describe how the cluster should be accessed. 
 *  
 * After construction, call start() to begin polling the cluster for updates. 
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class JobManager {

    private static final Logger log = LoggerFactory.getLogger(JobManager.class);

    // Constants
    private static final int DEFAULT_KEEP_COMPLETED_MINUTES = 10;
    private static final int DEFAULT_KEEP_ZOMBIES_MINUTES = 30;

    // Configuration
    private final JobSyncApi jobSyncApi;
    private final int keepCompletedMinutes;
    private final int keepZombiesMinutes;

    // State
    private final AtomicBoolean checkRunning = new AtomicBoolean();
    private final ConcurrentHashMap<Long, JobMetadata> jobMetadataMap = new ConcurrentHashMap<>();

    public JobManager(JobSyncApi jobSyncApi) {
        this(jobSyncApi, DEFAULT_KEEP_COMPLETED_MINUTES, DEFAULT_KEEP_ZOMBIES_MINUTES);
    }

    public JobManager(JobSyncApi jobSyncApi, int keepCompletedMinutes, int keepZombiesMinutes) {
        this.jobSyncApi = jobSyncApi;
        this.keepCompletedMinutes = keepCompletedMinutes;
        this.keepZombiesMinutes = keepZombiesMinutes;
    }

    /**
     * Submit the job described by the given JobTemplate to the cluster.
     * @param jt job template
     * @return a future collection containing the completed JobInfo
     * @throws Exception if there is an error submitting the job
     */
    public JobFuture submitJob(JobTemplate jt) throws Exception {
        JobInfo info = jobSyncApi.submitJob(jt);
        log.debug("Submitted job {}", info.getJobId());
        return recordInfo(info);
    }

    /**
     * Submit a job array described by the given JobTemplate to the cluster.
     * @param jt job array template 
     * @param start starting array index
     * @param end ending array index
     * @return a future collection containing the completed JobInfos
     * @throws Exception if there is an error submitting the jobs
     */
    public JobFuture submitJob(JobTemplate jt, int start, int end) throws Exception {
        JobInfo info = jobSyncApi.submitJobs(jt, start, end);
        log.debug("Submitted job array {} ({}-{})", info.getJobId(), start, end);
        return recordInfo(info);
    }

    private JobFuture recordInfo(JobInfo info) {
        JobFuture future = new JobFuture(info.getJobId());
        JobMetadata metadata = new JobMetadata(false, new Date(), null, future);
        jobMetadataMap.put(info.getJobId(), metadata);
        return future;
    }

    /**
     * Returns the job ids of any jobs which were being monitored and are now complete.
     * @return collection of job ids
     */
    public Collection<Long> getCompletedJobIds() {
        return jobMetadataMap.entrySet().stream()
                .filter((entry) -> entry.getValue().isDone())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Returns the job ids of any jobs which are currently running and being monitored.
     * @return collection of job ids
     */
    public Collection<Long> getRunningJobIds() {
        return jobMetadataMap.entrySet().stream()
                .filter((entry) -> !entry.getValue().isDone())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Returns the latest JobInfos for the given job.
     * @param jobId job id
     * @return collection of the JobInfos, or null if we have no information for that job
     */
    public Collection<JobInfo> getJobInfo(Integer jobId) {
        JobMetadata metadata = jobMetadataMap.get(jobId);
        if (metadata != null) {
            return metadata.getLastInfos();
        }
        return null;
    }

    /**
     * Returns the latest JobInfo for the given job.
     * @param jobId job id
     * @param arrayIndex array index 
     * @return JobInfo, or null if we have no information for that job
     */
    public JobInfo getJobInfo(Integer jobId, Integer arrayIndex) {
        JobMetadata metadata = jobMetadataMap.get(jobId);
        if (metadata != null) {
            for (JobInfo jobInfo : metadata.getLastInfos()) {
                if (arrayIndex.equals(jobInfo.getArrayIndex())) {
                    return jobInfo;
                }
            }
        }
        return null;
    }

    /**
     * Reset this manager instance. If there are currently running jobs, their futures will all throw exceptions.
     */
    public void clear() {
        log.trace("Resetting job metadata map");
        if (!jobMetadataMap.isEmpty()) {

            // Make sure all job futures are completed
            for (Map.Entry<Long, JobMetadata> entry : jobMetadataMap.entrySet()) {
                Long jobId = entry.getKey();
                JobMetadata currMetadata = entry.getValue();
                Exception e = new Exception("Job "+jobId+" was abandoned");
                currMetadata.getFuture().completeExceptionally(e);
            }

            jobMetadataMap.clear();
        }
    }

    /**
     * This method is called periodically if start() is called. You could also manually schedule this method to be
     * called, e.g. if you are running in a manager container with designated timer threads.
     */
    public void checkJobs() {

        log.trace("checkJobs");

        // Ensure we only run one check at at time
        if (!checkRunning.compareAndSet(false, true)) {
            log.trace("Job check already running");
            return;
        }

        try {
            // Are there any jobs to monitor? 
            if (jobMetadataMap.isEmpty()) {
                log.trace("No jobs are being monitored");
                return;
            }
            
            try {
                // Query cluster for new job info
                
                List<JobInfo> jobs = new ArrayList<>();
                try {
                    jobs = jobSyncApi.getJobInfo();
                }
                catch (Throwable t) {
                    // Catch any exceptions so that the code below can run and jobs can be retired 
                    // as zombies if we can't retrieve job information for long enough
                    log.error("Error getting job information", t);
                }
                
                Multimap<Long, JobInfo> jobMap = Utils.getJobMap(jobs);
                Date now = new Date();

                if (log.isDebugEnabled()) {
                    log.debug("Monitoring jobs: {}", getRunningJobIds());
                }
                else {
                    log.info("Monitoring {} jobs", getRunningJobIds().size());
                }
                
                for (Map.Entry<Long, JobMetadata> entry : jobMetadataMap.entrySet()) {
                    Long jobId = entry.getKey();
                    JobMetadata currMetadata = entry.getValue();
                    
                    if (currMetadata.isDone()) {
                        // Job completion was already processed, no need to update anything
                        
                        // But let's check to see if we've held onto this job for long enough
                        if (Utils.getDateDiff(currMetadata.getLastUpdated(), now, TimeUnit.MINUTES) > keepCompletedMinutes) {
                            log.debug("Job {} is done and will be removed from monitoring", jobId);
                            jobMetadataMap.remove(jobId);
                        }
                    }
                    else {
                        Collection<JobInfo> newInfos = jobMap.get(jobId);
                        if (newInfos!=null && !newInfos.isEmpty()) {
                            boolean allDone = Utils.allDone(newInfos);
                            
                            // Update the map with new metadata
                            JobMetadata newMetadata = new JobMetadata(allDone, now, newInfos, currMetadata.getFuture());
                            jobMetadataMap.put(jobId, newMetadata);
                            
                            // Complete the future, if all jobs in the job array are done
                            if (allDone) {
                                log.debug("Job {} has completed", jobId);
                                currMetadata.getFuture().complete(newInfos);
                            }
                            else {
                                log.trace("Updating running job {}", jobId);
                            }
                        }
                        else {
                            // No new info about this job means that it's a zombie
                            if (Utils.getDateDiff(currMetadata.getLastUpdated(), now, TimeUnit.MINUTES) > keepZombiesMinutes) {
                                log.warn("Removing zombie job: {}", jobId);
                                jobMetadataMap.remove(jobId);
                                Exception e = new Exception("Job "+jobId+" was identified as a zombie and removed");
                                currMetadata.getFuture().completeExceptionally(e);
                            }
                        }
                    }
                }
    
                // TODO: monitor other jobs that were not submitted through this manager
            }
            catch (Throwable e) {
                log.error("Error checking job status", e);
            }
        }
        finally {
            checkRunning.set(false);
        }
    }

    private class JobMetadata {

        private final boolean done;
        private final Date lastUpdated;
        private final Collection<JobInfo> lastInfos;
        private final JobFuture future;

        public JobMetadata(boolean done, Date lastUpdated, Collection<JobInfo> lastInfos, JobFuture future) {
            this.done = done;
            this.lastUpdated = lastUpdated;
            this.lastInfos = lastInfos;
            this.future = future;
        }

        public boolean isDone() {
            return done;
        }
        
        public Date getLastUpdated() {
            return lastUpdated;
        }

        public Collection<JobInfo> getLastInfos() {
            return lastInfos;
        }

        public JobFuture getFuture() {
            return future;
        }
    }
}
