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
public class JobInfo {

    private Long jobId;
    private Long arrayIndex;
    private String name;
    private String fromHost;
    private String execHost;
    private JobStatus status;
    private String queue;
    private String project;
    private Integer reqSlot;
    private Integer allocSlot;
    private LocalDateTime submitTime;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private String maxMem;
    private Integer exitCode;
    private String exitReason;
    
    /**
     * Job identifier for the job or job array.
     */
    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    /**
     * Index of the job, if its part of a job array. Null otherwise.
     */
    public Long getArrayIndex() {
        return arrayIndex;
    }

    public void setArrayIndex(Long arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    /**
     * Job name, as defined by the user during job submission.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Hostname of the submitting host.
     */
    public String getFromHost() {
        return fromHost;
    }

    public void setFromHost(String fromHost) {
        this.fromHost = fromHost;
    }

    /**
     * Hostname of the host on which the job is executing.
     */
    public String getExecHost() {
        return execHost;
    }

    public void setExecHost(String execHost) {
        this.execHost = execHost;
    }

    /**
     * Last known status of the job.
     */
    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    /**
     * Queue where the job will run, or ran.
     */
    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    /**
     * Project associated with the job.
     */
    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    /**
     * Number of slots requested for the job.
     */
    public Integer getReqSlot() {
        return reqSlot;
    }

    public void setReqSlot(Integer reqSlot) {
        this.reqSlot = reqSlot;
    }

    /**
     * Number of slots that were allocated for the job.
     */
    public Integer getAllocSlot() {
        return allocSlot;
    }

    public void setAllocSlot(Integer allocSlot) {
        this.allocSlot = allocSlot;
    }

    /**
     * Local time at which the job was submitted.
     */
    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    /**
     * Local time at which the job started running. Null if the job is pending.
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Local time at which the job finished running. Null if the job is pending or running.
     */
    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    /**
     * Maximum amount of memory used by the job during its execution.
     */
    public String getMaxMem() {
        return maxMem;
    }

    public void setMaxMem(String maxMem) {
        this.maxMem = maxMem;
    }

    /**
     * Final exit code of the job. Null if the job has not finished.
     */
    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Final exit reason for the job.
     * @return
     */
    public String getExitReason() {
        return exitReason;
    }

    public void setExitReason(String exitReason) {
        this.exitReason = exitReason;
    }

    /**
     * Returns true if the job has started executing.
     */
    public boolean isStarted() {
        return status!=null && status.isStarted();
    }
    
    /**
     * Returns true if the job is has finished executing.
     */
    public boolean isComplete() {
        return status!=null && status.isDone();
    }

    @Override
    public String toString() {
        return "JobInfo[jobId=" + jobId + ", arrayIndex=" + arrayIndex + ", name=" + name + ", fromHost=" + fromHost
                + ", execHost=" + execHost + ", status=" + status + ", queue=" + queue + ", project=" + project
                + ", reqSlot=" + reqSlot + ", allocSlot=" + allocSlot + ", submitTime=" + submitTime
                + ", startTime=" + startTime + ", finishTime=" + finishTime + ", maxMem=" + maxMem
                + ", exitCode=" + exitCode + ", exitReason=" + exitReason + "]";
    }
}
