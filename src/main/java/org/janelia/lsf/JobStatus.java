package org.janelia.lsf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum JobStatus {
    
    /** The job is pending, that is, it has not yet been started. */
    PEND(false, false), 
    
    /** The job is currently running. */
    RUN(true, false), 
    
    /** The job has terminated with status of 0. */
    DONE(true, true), 
    
    /** The job has terminated with a non-zero status - it may have been
       aborted due to an error in its execution, or killed by its owner
       or the LSF administrator. */
    EXIT(true, true),
    
    /** The job has been suspended, either by its owner or the LSF
    administrator, while pending. */
    PSUSP(true, false),  
    
    /** The job has been suspended, either by its owner or the LSF
       administrator, while running. */
    USUSP(true, false), 
    
    /** The job has been suspended by LSF due to either of the following
       two causes: 1) The load conditions on the execution host or hosts
       have exceeded a threshold according to the loadStop vector
       defined for the host or queue or 2) the run window of the job's
       queue is closed. */
    SSUSP(true, false),
    
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

    public static JobStatus parse(String status) {
        try {
            return valueOf(status);
        }
        catch (IllegalArgumentException e) {
            Logger log = LoggerFactory.getLogger(JobStatus.class);
            log.error("Error parsing job status: "+status, e);
            return OTHER;
        }
    }
    
}
