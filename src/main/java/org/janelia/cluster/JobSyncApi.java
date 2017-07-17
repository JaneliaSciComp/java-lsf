package org.janelia.cluster;

import java.io.IOException;
import java.util.List;

/**
 * A simple synchronous API for job submission and monitoring. 
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface JobSyncApi {

    public JobInfo submitJob(JobTemplate jt) throws IOException;
    
    public JobInfo submitJobs(JobTemplate jt, Integer start, Integer end) throws IOException;
    
    public List<JobInfo> getJobInfo() throws IOException;
    
    public List<JobInfo> getJobInfo(String user) throws IOException;
}
