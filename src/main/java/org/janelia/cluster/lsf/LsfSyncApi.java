package org.janelia.cluster.lsf;

import java.io.IOException;
import java.util.List;

import org.janelia.cluster.JobInfo;
import org.janelia.cluster.JobSyncApi;
import org.janelia.cluster.JobTemplate;

/**
 * LSF-based implementation of a synchronous job API. Relies on local access to LSF 
 * command-line tools such as bjobs and bsub. The host executing this code must be a
 * LSF submit host.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class LsfSyncApi implements JobSyncApi {

    private final LsfSubCommand subCmd;
    private final LsfJobsCommand jobsCmd;
    
    public LsfSyncApi() {
        this(new LsfSubCommand(), new LsfJobsCommand());
    }
    
    public LsfSyncApi(LsfSubCommand subCmd, LsfJobsCommand jobsCmd) {
        this.subCmd = subCmd;
        this.jobsCmd = jobsCmd;
    }

    @Override
    public List<JobInfo> getJobInfo() throws IOException {
        return jobsCmd.execute();
    }

    @Override
    public List<JobInfo> getJobInfo(String user) throws IOException {
        return jobsCmd.execute(user);
    }

    @Override
    public JobInfo submitJob(JobTemplate jt) throws IOException {
        return subCmd.execute(jt);
    }

    @Override
    public JobInfo submitJobs(JobTemplate jt, Integer start, Integer end) throws IOException {
        return subCmd.execute(jt, start, end);
    }
}
