package org.janelia.cluster.lsf;

import org.janelia.cluster.JobInfo;
import org.janelia.cluster.JobStatus;

public class TestUtils {
    
    public static JobInfo newInfo(Long jobId, JobStatus jobStatus) {
        return newInfo(jobId, jobStatus, null);
    }
    
    public static JobInfo newInfo(Long jobId, JobStatus jobStatus, Integer exitCode) {
        return newInfo(jobId, jobStatus, exitCode, null);
    }

    public static JobInfo newInfo(Long jobId, JobStatus jobStatus, Integer exitCode, Long arrayIndex) {
        JobInfo info = new JobInfo();
        info.setJobId(jobId);
        info.setStatus(jobStatus);
        info.setExitCode(exitCode);
        info.setArrayIndex(arrayIndex);
        return info;
    }
    
}
