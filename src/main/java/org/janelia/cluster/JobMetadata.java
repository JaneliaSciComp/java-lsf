package org.janelia.cluster;

import java.util.Collection;
import java.util.Date;

/**
 * Metadata about a batch job that was submitted for execution using the JobManager.
 *
 * This object represents a point-in-time record of the job status, and as such it is immutable. As the status is
 * updated, new JobMetadata objects will be created.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class JobMetadata {

    private final boolean done;
    private final Date lastUpdated;
    private final Collection<JobInfo> lastInfos;
    private final JobFuture future;

    JobMetadata(boolean done, Date lastUpdated, Collection<JobInfo> lastInfos, JobFuture future) {
        this.done = done;
        this.lastUpdated = lastUpdated;
        this.lastInfos = lastInfos;
        this.future = future;
    }

    public boolean isDone() {
        return done;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public Collection<JobInfo> getLastInfos() {
        return lastInfos;
    }

    public JobFuture getFuture() {
        return future;
    }
}