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
    public JobInfo submitJob(JobTemplate jt) throws IOException;
    
    /**
     * Submit the given job array for execution on the cluster.
     * @param jt JobtTemplate
     * @param start start index of the job array
     * @param end end index of the job array
     * @return initial JobINfo containing the job array's main id on the cluster
     * @throws IOException
     */
    public JobInfo submitJobs(JobTemplate jt, Integer start, Integer end) throws IOException;
    
    /**
     * Returns fresh job info for the current user from the cluster.
     * @return
     * @throws IOException
     */
    public List<JobInfo> getJobInfo() throws IOException;
    
    /**
     * Returns fresh job info for the given user from the cluster.
     * @param user
     * @return
     * @throws IOException
     */
    public List<JobInfo> getJobInfo(String user) throws IOException;
}
