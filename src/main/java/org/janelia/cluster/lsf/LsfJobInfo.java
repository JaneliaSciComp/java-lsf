package org.janelia.cluster.lsf;

import org.janelia.cluster.JobInfo;
import org.janelia.cluster.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LSF-specific job info from bjobs.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class LsfJobInfo extends JobInfo {

    private String statusString;

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
        
        if ("PEND".equals(statusString)) {
            setStatus(JobStatus.PENDING);
        }
        else if ("RUN".equals(statusString)) {
            setStatus(JobStatus.RUNNING);
        }
        else if ("DONE".equals(statusString)) {
            setStatus(JobStatus.DONE);
        }
        else if ("EXIT".equals(statusString)) {
            setStatus(JobStatus.EXIT);
        }
        else if ("PSUSP".equals(statusString)) {
            setStatus(JobStatus.SUSPENDED);
        }
        else if ("USUSP".equals(statusString)) {
            setStatus(JobStatus.SUSPENDED);
        }
        else if ("SSUSP".equals(statusString)) {
            setStatus(JobStatus.SUSPENDED);
        }
        else {
            setStatus(JobStatus.OTHER);
            Logger log = LoggerFactory.getLogger(JobStatus.class);
            log.error("Unknown LSF job status: "+statusString);
        }
    }
    
}
