package org.janelia.cluster.lsf;

import org.janelia.cluster.JobInfo;
import org.janelia.cluster.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parsing for LSF-specific job info from bjobs.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class LsfJobInfo extends JobInfo {

    private static final Logger log = LoggerFactory.getLogger(LsfJobInfo.class);
    
    private String lsfJobName;
    private String lsfJobStatus;

    /**
     * Returns the full LSF job name, including the array index, if any.
     */
    public String getLsfJobName() {
        return lsfJobName;
    }

    public void setLsfJobName(String lsfJobName) {
        this.lsfJobName = lsfJobName;
        try {
            int b1 = lsfJobName.indexOf('[');
            int b2 = lsfJobName.indexOf(']');
            if (b1>0 && b2>0 && b2==lsfJobName.length()-1) {
                setArrayIndex(LsfUtils.parseInt(lsfJobName.substring(b1+1, b2)));
                setName(lsfJobName.substring(0, b1));
            }
            else {
                setName(lsfJobName);
            }
        }
        catch (Exception e) {
            log.warn("Problem parsing LSF job name: "+lsfJobName, e);
            setName(lsfJobName);
        }
    }

    /**
     * Returns the LSF status string. 
     * @return
     */
    public String getLsfJobStatus() {
        return lsfJobStatus;
    }

    public void setLsfJobStatus(String lsfJobStatus) {
        this.lsfJobStatus = lsfJobStatus;
        
        if ("PEND".equals(lsfJobStatus)) {
            setStatus(JobStatus.PENDING);
        }
        else if ("RUN".equals(lsfJobStatus)) {
            setStatus(JobStatus.RUNNING);
        }
        else if ("DONE".equals(lsfJobStatus)) {
            setStatus(JobStatus.DONE);
        }
        else if ("EXIT".equals(lsfJobStatus)) {
            setStatus(JobStatus.EXIT);
        }
        else if ("PSUSP".equals(lsfJobStatus)) {
            setStatus(JobStatus.SUSPENDED);
        }
        else if ("USUSP".equals(lsfJobStatus)) {
            setStatus(JobStatus.SUSPENDED);
        }
        else if ("SSUSP".equals(lsfJobStatus)) {
            setStatus(JobStatus.SUSPENDED);
        }
        else {
            setStatus(JobStatus.OTHER);
            Logger log = LoggerFactory.getLogger(JobStatus.class);
            log.error("Unknown LSF job status: "+lsfJobStatus);
        }
    }

    @Override
    public void setExecHost(String execHost) {
        try {
            if (execHost!=null) {
                int s1 = execHost.indexOf('*');
                if (s1>0) {
                    execHost = execHost.substring(s1+1);
                }
            }
        }
        catch (Exception e) {
            log.warn("Problem parsing LSF exec host: "+execHost, e);
        }
        super.setExecHost(execHost);
    }
    
}
