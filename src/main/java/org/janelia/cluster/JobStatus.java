package org.janelia.cluster;

public enum JobStatus {
    
    /** The job is pending, that is, it has not yet been started. */
    PENDING(false, false), 
    
    /** The job is currently running. */
    RUNNING(true, false), 
    
    /** The job has terminated with status of 0. */
    DONE(true, true), 
    
    /** The job has terminated with a non-zero status. */
    EXIT(true, true),
    
    /** The job has been suspended. */
    SUSPENDED(true, false),  
    
    /** Unknown recognized, we consider it over */
    OTHER(false, true);
    
    private boolean isStarted;
    private boolean isDone;
    JobStatus(boolean isStarted, boolean isDone) {
        this.isStarted = isStarted;
        this.isDone = isDone;
    }
    
    public boolean isStarted() {
        return isStarted;
    }
    
    public boolean isDone() {
        return isDone;
    }
    
}
