package org.janelia.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A monitor which runs wraps a JobManager to periodically call its checkJobs() method in a background thread.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class JobMonitor {

    private static final Logger log = LoggerFactory.getLogger(JobMonitor.class);

    // Constants
    private static final int DEFAULT_CHECK_INTERVAL_SECONDS = 15;

    // Configuration
    private final int checkIntervalSeconds;

    // State
    private JobManager jobManager;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> jobChecker;
    private boolean started = false;


    public JobMonitor(JobManager jobManager) {
        this(jobManager, DEFAULT_CHECK_INTERVAL_SECONDS);
    }

    public JobMonitor(JobManager jobManager, int checkIntervalSeconds) {
        this.jobManager = jobManager;
        this.checkIntervalSeconds = checkIntervalSeconds;
    }

    /**
     * Clear the job map and begin monitoring the cluster after the initial check interval.
     * Any submitted jobs will have their futures completed if they finish while the monitor is running.
     * If the monitor is already running, calling this method does nothing.
     */
    public synchronized void start() {
        if (!started) {
            log.debug("Starting job monitoring");
            jobManager.clear();
            jobChecker = scheduler.scheduleAtFixedRate(() -> jobManager.checkJobs(),
                    checkIntervalSeconds, checkIntervalSeconds, TimeUnit.SECONDS);
            this.started = true;
        }
    }

    /**
     * Stop monitoring the cluster. After calling stop(), the client may call start() to begin monitoring again.
     * If the monitor is not running, calling this method does nothing.
     */
    public synchronized void stop() {
        if (started) {
            log.debug("Stopping job monitoring");
            jobChecker.cancel(true);
            started = false;
        }
    }
}
