package org.janelia.cluster;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A JobFuture represents the future completion of a single job or job array on the cluster. Either way,
 * the job id can be retrieved.
 * 
 * The future result will be a collection of all the final JobInfo objects for all the jobs in the job array.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public final class JobFuture implements Future<Collection<JobInfo>> {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final Integer jobId;
    private Collection<JobInfo> infos;

    public JobFuture(Integer jobId) {
        this.jobId = jobId;
    }
    
    public Integer getJobId() {
        return jobId;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // TODO: support killing the job 
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return latch.getCount() == 0;
    }

    @Override
    public Collection<JobInfo> get() throws InterruptedException {
        latch.await();
        return infos;
    }

    @Override
    public Collection<JobInfo> get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (latch.await(timeout, unit)) {
            return infos;
        }
        else {
            throw new TimeoutException();
        }
    }

    void put(Collection<JobInfo> result) {
        if (infos!=null) {
            throw new IllegalStateException("Result for this job was already set");
        }
        infos = result;
        latch.countDown();
    }
}
