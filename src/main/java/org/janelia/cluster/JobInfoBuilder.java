package org.janelia.cluster;

import java.time.LocalDateTime;

/**
 * Job info reported by the cluster implementation. 
 * 
 * For job arrays, each individual job is reported separately, and the arrayIndex is populated with the
 * sub-job's array index. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class JobInfoBuilder {

    private Long jobId;
    private String name;
    private JobStatus status;

    public JobInfoBuilder setJobId(Long jobId) {
        this.jobId = jobId;
        return this;
    }

    public JobInfoBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public JobInfoBuilder setStatus(JobStatus status) {
        this.status = status;
        return this;
    }

    public JobInfo build() {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobId(jobId);
        jobInfo.setName(name);
        jobInfo.setStatus(status);
        return jobInfo;
    }
}
