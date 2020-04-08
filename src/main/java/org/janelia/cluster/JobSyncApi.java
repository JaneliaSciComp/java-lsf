package org.janelia.cluster;

import java.io.IOException;
import java.util.List;

/**
 * A simple synchronous API for job submission and monitoring. 
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface JobSyncApi {

    /**
     * Submit the given job for execution on the cluster.
     * @param jt JobTemplate
     * @return initial JobInfo containing the job's id on the cluster
     * @throws IOException
     */
    JobInfo submitJob(JobTemplate jt) throws IOException;
    
    /**
     * Submit the given job array for execution on the cluster.
     * @param jt JobtTemplate
     * @param start start index of the job array
     * @param end end index of the job array
     * @return initial JobINfo containing the job array's main id on the cluster
     * @throws IOException
     */
    JobInfo submitJobs(JobTemplate jt, Long start, Long end) throws IOException;
    
    /**
     * Returns fresh job info for the current user from the cluster.
     * @return
     * @throws IOException
     */
    List<JobInfo> getJobInfo() throws IOException;
    
    /**
     * Returns fresh job info for the given user from the cluster.
     * @param user
     * @return
     * @throws IOException
     */
    List<JobInfo> getJobInfo(String user) throws IOException;

    /**
     * Returns fresh job info for the id from the cluster.
     * @param jobId
     * @return
     * @throws IOException
     */
    List<JobInfo> getJobInfo(Long jobId) throws IOException;

    /**
     * Kills the given job.
     * @param jobId
     * @throws IOException
     */
    void killJobById(Long jobId) throws IOException;

    /**
     * Kills the given job.
     * @param jobName
     * @throws IOException
     */
    void killJobByName(String jobName) throws IOException;
}
