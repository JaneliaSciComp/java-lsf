package org.janelia.lsf;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Multimap;

public class LsfTestsMocking {
    
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
        
        JobInfo info = new JobInfo();
        info.setJobId(10000);
        when(subCmd.runJob(jt)).thenReturn(info);
        
        JobInfo info1 = new JobInfo();
        info1.setJobId(10000);
        info1.setStatus(JobStatus.DONE);
        info1.setExitCode(0);
        when(jobsCmd.run()).thenReturn(Arrays.asList(info)).thenReturn(Arrays.asList(info1));
        
        JobInfo job = subCmd.runJob(jt);

        Assert.assertNotNull(job.getJobId());

        boolean hadErrors = false;
        
        while (true) {

            List<JobInfo> jobs = jobsCmd.run();
            Assert.assertNotNull(jobs);
            Assert.assertTrue(jobs.size()>=1);
            
            Multimap<Integer, JobInfo> jobMap = Utils.getJobMap(jobs);
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
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        Assert.assertFalse("Jobs had errors", hadErrors);
    }
    
    @Test
    public void testBatchSub() throws IOException {

        String scriptPath = scriptDirPath.resolve("test.sh").toString();
        
        JobTemplate jt = new JobTemplate();
        jt.setRemoteCommand("bash");
        jt.setArgs(Arrays.asList(scriptPath));
        jt.setJobName("testApi");
        jt.setInputPath(inputDirPath+"/input.#");
        jt.setOutputPath(outputDirPath+"/out.#");
        jt.setErrorPath(outputDirPath+"/err.#");
        jt.setNativeSpecification(Arrays.asList("-W 1", "-n 2"));
        
        JobInfo info = new JobInfo();
        info.setJobId(10000);
        when(subCmd.runJobs(jt, 1, 4)).thenReturn(info);
        
        JobInfo info1 = new JobInfo();
        info1.setJobId(10000);
        info1.setArrayIndex(1);
        info1.setStatus(JobStatus.DONE);
        info1.setExitCode(0);
        JobInfo info2 = new JobInfo();
        info2.setJobId(10000);
        info2.setArrayIndex(2);
        info2.setStatus(JobStatus.DONE);
        info2.setExitCode(0);
        JobInfo info3 = new JobInfo();
        info3.setJobId(10000);
        info3.setArrayIndex(3);
        info3.setStatus(JobStatus.DONE);
        info3.setExitCode(0);
        JobInfo info4 = new JobInfo();
        info4.setJobId(10000);
        info4.setArrayIndex(4);
        info4.setStatus(JobStatus.DONE);
        info4.setExitCode(0);
        when(jobsCmd.run()).thenReturn(Arrays.asList(info)).thenReturn(Arrays.asList(info1, info2, info3, info4));
        
        JobInfo job = subCmd.runJobs(jt, 1, 4);

        Assert.assertNotNull(job.getJobId());

        boolean hadErrors = false;
        
        while (true) {

            List<JobInfo> jobs = jobsCmd.run();
            Assert.assertNotNull(jobs);
            
            Multimap<Integer, JobInfo> jobMap = Utils.getJobMap(jobs);
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
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        Assert.assertFalse("Jobs had errors", hadErrors);
    }
}
