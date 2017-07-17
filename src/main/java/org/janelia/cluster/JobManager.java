package org.janelia.cluster;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

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
    private static final int DEFAULT_CHECK_INTERVAL_SECONDS = 15;
    private static final int DEFAULT_KEEP_COMPLETED_MINUTES = 10;
    private static final int DEFAULT_KEEP_ZOMBIES_MINUTES = 30;

    // Configuration
    private final JobSyncApi jobSyncApi;
    private final int checkIntervalSeconds;
    private final int keepCompletedMinutes;
    private final int keepZombiesMinutes;

    // State
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean checkRunning = new AtomicBoolean();
    private final ConcurrentHashMap<Integer, JobMetadata> jobMetadataMap = new ConcurrentHashMap<>();
    private ScheduledFuture<?> jobChecker;
    private boolean started = false;

    public JobManager(JobSyncApi jobSyncApi) {
        this(jobSyncApi, DEFAULT_CHECK_INTERVAL_SECONDS, DEFAULT_KEEP_COMPLETED_MINUTES, DEFAULT_KEEP_ZOMBIES_MINUTES);
    }

    public JobManager(JobSyncApi jobSyncApi, int checkIntervalSeconds, int keepCompletedMinutes, int keepZombiesMinutes) {
        this.jobSyncApi = jobSyncApi;
        this.checkIntervalSeconds = checkIntervalSeconds;
        this.keepCompletedMinutes = keepCompletedMinutes;
        this.keepZombiesMinutes = keepZombiesMinutes;
    }

    /**
     * Clear the job map and begin monitoring the cluster after the initial check interval. 
     * Any submitted jobs will have their futures completed if they finish while the monitor is running.
     */
    public synchronized void start() {
        if (!started) {
            jobMetadataMap.clear();
            jobChecker = scheduler.scheduleAtFixedRate(() -> checkJobs(), checkIntervalSeconds, checkIntervalSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * Stop monitoring the cluster. Call start() to begin monitoring again.
     */
    public synchronized void stop() {
        if (started) {
            jobChecker.cancel(true);
            started = false;
        }
    }

    /**
     * Submit the job described by the given JobTemplate to the cluster. 
     * @param jt job template
     * @return a future collection containing the completed JobInfo
     * @throws Exception if there is an error submitting the job
     */
    public JobFuture submitJob(JobTemplate jt) throws Exception {
        start();
        JobInfo info = jobSyncApi.submitJob(jt);
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
    public Future<Collection<JobInfo>> submitJob(JobTemplate jt, int start, int end) throws Exception {
        start();
        JobInfo info = jobSyncApi.submitJobs(jt, start, end);
        return recordInfo(info);
    }

    private JobFuture recordInfo(JobInfo info) {
        JobFuture future = new JobFuture(info.getJobId());
        JobMetadata metadata = new JobMetadata(false, null, null, future);
        jobMetadataMap.put(info.getJobId(), metadata);
        return future;
    }

    /**
     * Returns the job ids of any jobs which were being monitored and are now complete.
     * @return collection of job ids
     */
    public Collection<Integer> getCompletedJobIds() {  
        return jobMetadataMap.entrySet().stream()
                .filter((entry) -> entry.getValue().isDone())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Returns the job ids of any jobs which are currently running and being monitored.
     * @return collection of job ids
     */
    public Collection<Integer> getRunningJobIds() {  
        return jobMetadataMap.entrySet().stream()
                .filter((entry) -> !entry.getValue().isDone())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Returns the latest JobInfos for the given job.
     * @param jobId job id
     * @return collection of the JobInfos
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
     * @return JobInfo
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

    private void checkJobs() {

        if (!checkRunning.compareAndSet(false, true)) {
            log.info("Job check already running");
            return;
        }

        try {
            log.info("Checking for jobs");
            try {
                // Query cluster for new job info
                List<JobInfo> jobs = jobSyncApi.getJobInfo();
                Multimap<Integer, JobInfo> jobMap = Utils.getJobMap(jobs);
                Date now = new Date();
                
                for (Map.Entry<Integer, JobMetadata> entry : jobMetadataMap.entrySet()) {
                    Integer jobId = entry.getKey();
                    JobMetadata currMetadata = entry.getValue();
                    
                    if (currMetadata.isDone()) {
                        // Job completion was already processed, no need to update anything
                        
                        // But let's check to see if we've held onto this job for long enough
                        if (Utils.getDateDiff(currMetadata.getLastUpdated(), now, TimeUnit.MINUTES) > keepCompletedMinutes) {
                            log.info("Removing finished job: {}", jobId);
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
                                log.info("Marking job as completed: {}", jobId);
                                currMetadata.getFuture().put(newInfos);
                            }
                            else {
                                log.info("Updating running job {} with {} updated infos", jobId, newInfos.size());
                            }
                        }
                        else {
                            // No new info about this job means that it's a zombie
                            if (Utils.getDateDiff(currMetadata.getLastUpdated(), now, TimeUnit.MINUTES) > keepZombiesMinutes) {
                                log.info("Removing zombie job: {}", jobId);
                                jobMetadataMap.remove(jobId);
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
