package org.janelia.cluster.lsf.mock;

import org.apache.commons.io.FileUtils;
import org.janelia.cluster.*;
import org.janelia.cluster.lsf.LsfJobsCommand;
import org.janelia.cluster.lsf.LsfKillCommand;
import org.janelia.cluster.lsf.LsfSubCommand;
import org.janelia.cluster.lsf.TestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockJobManagerTests {

    private LsfSubCommand subCmd;
    private LsfJobsCommand jobsCmd;
    private LsfKillCommand killCmd;
    private JobSyncApi syncApi;
    
    private JobManager mgr;
    private JobMonitor monitor;
    private Path rootPath;
    private Path inputDirPath;
    private Path scriptDirPath;
    private Path outputDirPath;

    public JobSyncApi createSyncApi() {
        subCmd = mock(LsfSubCommand.class);
        jobsCmd = mock(LsfJobsCommand.class);
        killCmd = mock(LsfKillCommand.class);
        syncApi = new JobSyncApi() {

            @Override
            public List<JobInfo> getJobInfo() throws IOException {
                return jobsCmd.execute();
            }

            @Override
            public List<JobInfo> getJobInfo(String user) throws IOException {
                return jobsCmd.execute(user, null);
            }

            @Override
            public List<JobInfo> getJobInfo(Long jobId) throws IOException {
                return jobsCmd.execute(null, jobId);
            }

            @Override
            public JobInfo submitJob(JobTemplate jt) throws IOException {
                return subCmd.execute(jt);
            }

            @Override
            public JobInfo submitJobs(JobTemplate jt, Long start, Long end) throws IOException {
                return subCmd.execute(jt, start, end);
            }

            @Override
            public void killJob(Long jobId) throws IOException {
                killCmd.execute(jobId);
            }
        };
        return syncApi;
    }

    @Before
    public void setupDirs() throws IOException {
        
        rootPath = Paths.get(".").toAbsolutePath().normalize();
        scriptDirPath = rootPath.resolve("src/test/bash");
        inputDirPath = rootPath.resolve("src/test/resources");
        outputDirPath = rootPath.resolve("target/test");
        
        // Recreate the output directory
        FileUtils.deleteDirectory(outputDirPath.toFile());
        outputDirPath.toFile().mkdirs();
    }
    
    @Before 
    public void createManager() throws IOException {
        this.mgr = new JobManager(createSyncApi(), 1, 0);
        this.monitor = new JobMonitor(mgr, 1);
        monitor.start();
    }

    @After
    public void destroyManager() {
        this.monitor.stop();
    }

    @Test
    public void testArraySub() throws Exception {
        
        String scriptPath = scriptDirPath.resolve("test.sh").toString();
        
        JobTemplate jt = new JobTemplate();
        jt.setRemoteCommand("bash");
        jt.setArgs(Arrays.asList(scriptPath));
        jt.setJobName("testApi");
        jt.setInputPath(inputDirPath+"/input.1");
        jt.setOutputPath(outputDirPath+"/out.1");
        jt.setErrorPath(outputDirPath+"/err.1");
        jt.setNativeSpecification(Arrays.asList("-W 1", "-n 2"));

        Long jobId = 10000L;
        
        when(subCmd.execute(jt, 1L, 4L))
            .thenReturn(TestUtils.newInfo(jobId, JobStatus.PENDING));
        
        when(jobsCmd.execute())
            .thenReturn(Arrays.asList(TestUtils.newInfo(jobId, JobStatus.PENDING)))
            .thenReturn(Arrays.asList(TestUtils.newInfo(jobId, JobStatus.RUNNING, 0)))
            .thenReturn(Arrays.asList(TestUtils.newInfo(jobId, JobStatus.DONE, 0)));

        JobInfo info1 = TestUtils.newInfo(jobId, JobStatus.RUNNING, null, 1L);
        JobInfo info2 = TestUtils.newInfo(jobId, JobStatus.RUNNING, null, 2L);
        JobInfo info3 = TestUtils.newInfo(jobId, JobStatus.RUNNING, null, 3L);
        JobInfo info4 = TestUtils.newInfo(jobId, JobStatus.RUNNING, null, 4L);
        
        JobInfo info1Done = TestUtils.newInfo(jobId, JobStatus.DONE, 0, 1L);
        JobInfo info2Done = TestUtils.newInfo(jobId, JobStatus.DONE, 0, 2L);
        JobInfo info3Done = TestUtils.newInfo(jobId, JobStatus.DONE, 0, 3L);
        JobInfo info4Done = TestUtils.newInfo(jobId, JobStatus.DONE, 0, 4L);
        
        when(jobsCmd.execute())
            .thenReturn(Arrays.asList(info1, info2, info3, info4))
            .thenReturn(Arrays.asList(info1Done, info2Done, info3Done, info4Done));
        
        JobFuture future = mgr.submitJob(jt, 1, 4);
        
        Assert.assertNotNull(future);
        Assert.assertNotNull(future.getJobId());

        boolean hadErrors = false;
        for (JobInfo jobInfo : future.get()) {
            Assert.assertTrue(jobInfo.isStarted());
            Assert.assertTrue(jobInfo.isComplete());
            Assert.assertEquals(future.getJobId(), jobInfo.getJobId());
            Assert.assertTrue(jobInfo.getExitCode()==0);
        }
        
        Assert.assertFalse("Jobs had errors", hadErrors);
    }

    @Test
    public void testJobArraySub() throws Exception {
        
        String scriptPath = scriptDirPath.resolve("test.sh").toString();
        
        JobTemplate jt = new JobTemplate();
        jt.setRemoteCommand("bash");
        jt.setArgs(Arrays.asList(scriptPath));
        jt.setJobName("testApi");
        jt.setInputPath(inputDirPath+"/input.1");
        jt.setOutputPath(outputDirPath+"/out.1");
        jt.setErrorPath(outputDirPath+"/err.1");
        jt.setNativeSpecification(Arrays.asList("-W 1", "-n 2"));

        Long jobId = 100001L;
        when(subCmd.execute(jt))
            .thenReturn(TestUtils.newInfo(jobId, JobStatus.PENDING));
        
        when(jobsCmd.execute())
            .thenReturn(Arrays.asList(TestUtils.newInfo(jobId, JobStatus.PENDING)))
            .thenReturn(Arrays.asList(TestUtils.newInfo(jobId, JobStatus.RUNNING, 0)))
            .thenReturn(Arrays.asList(TestUtils.newInfo(jobId, JobStatus.DONE, 0)));
        
        JobFuture future = mgr.submitJob(jt);
        Assert.assertNotNull(future);
        Assert.assertNotNull(future.getJobId());

        boolean hadErrors = false;
        for (JobInfo jobInfo : future.get()) {
            Assert.assertTrue(jobInfo.isStarted());
            Assert.assertTrue(jobInfo.isComplete());
            Assert.assertEquals(future.getJobId(), jobInfo.getJobId());
            Assert.assertTrue(jobInfo.getExitCode()==0);
        }
        
        Assert.assertFalse("Jobs had errors", hadErrors);
    }
    
    
}
