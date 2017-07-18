package org.janelia.cluster.lsf.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.janelia.cluster.JobFuture;
import org.janelia.cluster.JobInfo;
import org.janelia.cluster.JobManager;
import org.janelia.cluster.JobStatus;
import org.janelia.cluster.JobSyncApi;
import org.janelia.cluster.JobTemplate;
import org.janelia.cluster.lsf.LsfJobsCommand;
import org.janelia.cluster.lsf.LsfSubCommand;
import org.janelia.cluster.lsf.TestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MockJobManagerTests {

    private LsfSubCommand subCmd;
    private LsfJobsCommand jobsCmd;
    private JobSyncApi syncApi;
    
    private JobManager mgr;
    private Path rootPath;
    private Path inputDirPath;
    private Path scriptDirPath;
    private Path outputDirPath;

    public JobSyncApi createSyncApi() {
        subCmd = mock(LsfSubCommand.class);
        jobsCmd = mock(LsfJobsCommand.class);
        syncApi = new JobSyncApi() {

            @Override
            public List<JobInfo> getJobInfo() throws IOException {
                return jobsCmd.execute();
            }

            @Override
            public List<JobInfo> getJobInfo(String user) throws IOException {
                return jobsCmd.execute(user);
            }

            @Override
            public JobInfo submitJob(JobTemplate jt) throws IOException {
                return subCmd.execute(jt);
            }

            @Override
            public JobInfo submitJobs(JobTemplate jt, Integer start, Integer end) throws IOException {
                return subCmd.execute(jt, start, end);
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
        this.mgr = new JobManager(createSyncApi(), 1, 1, 0);
        mgr.start();
    }

    @After
    public void destroyManager() {
        this.mgr.stop();
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
        
        Integer jobId = 10000;
        
        when(subCmd.execute(jt, 1, 4))
            .thenReturn(TestUtils.newInfo(jobId, JobStatus.PENDING));
        
        when(jobsCmd.execute())
            .thenReturn(Arrays.asList(TestUtils.newInfo(jobId, JobStatus.PENDING)))
            .thenReturn(Arrays.asList(TestUtils.newInfo(jobId, JobStatus.RUNNING, 0)))
            .thenReturn(Arrays.asList(TestUtils.newInfo(jobId, JobStatus.DONE, 0)));

        JobInfo info1 = TestUtils.newInfo(jobId, JobStatus.RUNNING, null, 1);
        JobInfo info2 = TestUtils.newInfo(jobId, JobStatus.RUNNING, null, 2);
        JobInfo info3 = TestUtils.newInfo(jobId, JobStatus.RUNNING, null, 3);
        JobInfo info4 = TestUtils.newInfo(jobId, JobStatus.RUNNING, null, 4);
        
        JobInfo info1Done = TestUtils.newInfo(jobId, JobStatus.DONE, 0, 1);
        JobInfo info2Done = TestUtils.newInfo(jobId, JobStatus.DONE, 0, 2);
        JobInfo info3Done = TestUtils.newInfo(jobId, JobStatus.DONE, 0, 3);
        JobInfo info4Done = TestUtils.newInfo(jobId, JobStatus.DONE, 0, 4);
        
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
        
        Integer jobId = 10000;
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
