package org.janelia.cluster.lsf;

import java.io.IOException;
import java.util.List;

import org.janelia.cluster.JobCmdFlag;
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
    private final LsfKillCommand killCmd;

    public LsfSyncApi() {
        this(new LsfSubCommand(), new LsfJobsCommand(), new LsfKillCommand());
    }
    
    public LsfSyncApi(LsfSubCommand subCmd, LsfJobsCommand jobsCmd, LsfKillCommand killCmd) {
        this.subCmd = subCmd;
        this.jobsCmd = jobsCmd;
        this.killCmd = killCmd;
    }

    @Override
    public List<JobInfo> getJobInfo() throws IOException {
        return jobsCmd.execute();
    }

    @Override
    public List<JobInfo> getJobInfo(String user) throws IOException {
        return jobsCmd.execute(user, null);
    }

    @Override
    public List<JobInfo> getJobInfo(Long jobId) throws IOException {
        return jobsCmd.execute(null, jobId);
    }

    @Override
    public JobInfo submitJob(JobTemplate jt) throws IOException {
        return subCmd.execute(jt);
    }

    @Override
    public JobInfo submitJobs(JobTemplate jt, Long start, Long end) throws IOException {
        return subCmd.execute(jt, start, end);
    }

    @Override
    public void killJobById(Long jobId, JobCmdFlag... flags) throws IOException {
        killCmd.executeWithJobId(jobId, flags);
    }

    @Override
    public void killJobByName(String jobName, JobCmdFlag... flags) throws IOException {
        killCmd.executeWithJobName(jobName, flags);
    }
}
