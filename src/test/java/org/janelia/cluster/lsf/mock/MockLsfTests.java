package org.janelia.cluster.lsf.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.janelia.cluster.JobInfo;
import org.janelia.cluster.JobStatus;
import org.janelia.cluster.JobTemplate;
import org.janelia.cluster.Utils;
import org.janelia.cluster.lsf.LsfJobsCommand;
import org.janelia.cluster.lsf.LsfSubCommand;
import org.janelia.cluster.lsf.LsfTests;
import org.janelia.cluster.lsf.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

public class MockLsfTests {

    private static final Logger log = LoggerFactory.getLogger(LsfTests.class);
    
    private static LsfSubCommand subCmd;
    private static LsfJobsCommand jobsCmd;
    
    private Path rootPath;
    private Path inputDirPath;
    private Path scriptDirPath;
    private Path outputDirPath;
    
    @BeforeClass
    public static void setup() {
        subCmd = mock(LsfSubCommand.class);
        jobsCmd = mock(LsfJobsCommand.class);
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

    @Test
    public void testSingleSub() throws IOException {

        String scriptPath = scriptDirPath.resolve("test.sh").toString();
        
        JobTemplate jt = new JobTemplate();
        jt.setRemoteCommand("bash");
        jt.setArgs(Arrays.asList(scriptPath));
        jt.setJobName("testApi");
        jt.setInputPath(inputDirPath+"/input.1");
        jt.setOutputPath(outputDirPath+"/out.1");
        jt.setErrorPath(outputDirPath+"/err.1");
        jt.setNativeSpecification(Arrays.asList("-W 1", "-n 2"));

        Long jobId = 1L;
        
        when(subCmd.execute(jt))
            .thenReturn(TestUtils.newInfo(jobId, JobStatus.PENDING));
        
        when(jobsCmd.execute())
            .thenReturn(Arrays.asList(TestUtils.newInfo(jobId, JobStatus.PENDING)))
            .thenReturn(Arrays.asList(TestUtils.newInfo(jobId, JobStatus.RUNNING, 0)))
            .thenReturn(Arrays.asList(TestUtils.newInfo(jobId, JobStatus.DONE, 0)));
        
        JobInfo job = subCmd.execute(jt);

        Assert.assertNotNull(job.getJobId());

        boolean hadErrors = false;
        
        while (true) {

            List<JobInfo> jobs = jobsCmd.execute();
            Assert.assertNotNull(jobs);
            Assert.assertTrue(jobs.size()>=1);
            
            Multimap<Long, JobInfo> jobMap = Utils.getJobMap(jobs);
            Assert.assertTrue(jobMap.containsKey(job.getJobId()));
            
            boolean allDone = true;
            for (JobInfo jobInfo : jobMap.get(job.getJobId())) {
                if (!jobInfo.isComplete()) {
                    allDone = false;
                }
                else {
                    if (jobInfo.getExitCode()!=0) {
                        hadErrors = true;
                    }
                }
            }
            
            if (allDone) break;
            
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                log.error("Sleep interrupted", e);
            }
        }
        
        Assert.assertFalse("Jobs had errors", hadErrors);
    }
    
    @Test
    public void testArraySub() throws IOException {

        String scriptPath = scriptDirPath.resolve("test.sh").toString();
        
        JobTemplate jt = new JobTemplate();
        jt.setRemoteCommand("bash");
        jt.setArgs(Arrays.asList(scriptPath));
        jt.setJobName("testApi");
        jt.setInputPath(inputDirPath+"/input.#");
        jt.setOutputPath(outputDirPath+"/out.#");
        jt.setErrorPath(outputDirPath+"/err.#");
        jt.setNativeSpecification(Arrays.asList("-W 1", "-n 2"));

        Long jobId = 12345L;
        
        when(subCmd.execute(jt, 1, 4))
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
        
        JobInfo job = subCmd.execute(jt, 1, 4);

        Assert.assertNotNull(job);
        Assert.assertNotNull(job.getJobId());

        boolean hadErrors = false;
        
        while (true) {

            List<JobInfo> jobs = jobsCmd.execute();
            Assert.assertNotNull(jobs);
            
            Multimap<Long, JobInfo> jobMap = Utils.getJobMap(jobs);
            Assert.assertTrue(jobMap.containsKey(job.getJobId()));
            
            boolean allDone = true;
            for (JobInfo jobInfo : jobMap.get(job.getJobId())) {
                if (!jobInfo.isComplete()) {
                    allDone = false;
                }
                else {
                    if (jobInfo.getExitCode()!=0) {
                        hadErrors = true;
                    }
                }
            }
            
            if (allDone) break;
            
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                log.error("Sleep interrupted", e);
            }
        }
        
        Assert.assertFalse("Jobs had errors", hadErrors);
    }
}
