package org.janelia.lsf;

import java.time.LocalDateTime;

/**
 * Job info reported from LSF. 
 * 
 * For batch jobs, each job in a batch is reported separately, and the arrayIndex is populated with the
 * sub-job's array index. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class JobInfo {

    private Integer jobId;
    private Integer arrayIndex;
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
    
    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Integer getArrayIndex() {
        return arrayIndex;
    }

    public void setArrayIndex(Integer arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFromHost() {
        return fromHost;
    }

    public void setFromHost(String fromHost) {
        this.fromHost = fromHost;
    }

    public String getExecHost() {
        return execHost;
    }

    public void setExecHost(String execHost) {
        this.execHost = execHost;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public Integer getReqSlot() {
        return reqSlot;
    }

    public void setReqSlot(Integer reqSlot) {
        this.reqSlot = reqSlot;
    }

    public Integer getAllocSlot() {
        return allocSlot;
    }

    public void setAllocSlot(Integer allocSlot) {
        this.allocSlot = allocSlot;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    public String getMaxMem() {
        return maxMem;
    }

    public void setMaxMem(String maxMem) {
        this.maxMem = maxMem;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public String toString() {
        return "JobInfo[jobId=" + jobId + ", arrayIndex=" + arrayIndex + ", name=" + name + ", fromHost=" + fromHost
                + ", execHost=" + execHost + ", status=" + status + ", queue=" + queue + ", project=" + project
                + ", reqSlot=" + reqSlot + ", allocSlot=" + allocSlot + ", submitTime=" + submitTime + ", startTime="
                + startTime + ", finishTime=" + finishTime + ", maxMem=" + maxMem + ", exitCode=" + exitCode + "]";
    }

    public boolean isStarted() {
        return status!=null && status.isStarted();
    }
    
    public boolean isComplete() {
        return status!=null && status.isDone();
    }

}
