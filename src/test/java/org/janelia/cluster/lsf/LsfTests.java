package org.janelia.cluster.lsf;

import com.google.common.collect.Multimap;
import org.apache.commons.io.FileUtils;
import org.janelia.cluster.JobInfo;
import org.janelia.cluster.JobTemplate;
import org.janelia.cluster.Utils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class LsfTests {
    
    private static final Logger log = LoggerFactory.getLogger(LsfTests.class);
    
    private LsfSubCommand subCmd = new LsfSubCommand();
    private LsfJobsCommand jobsCmd = new LsfJobsCommand();
    
    private Path rootPath;
    private Path inputDirPath;
    private Path scriptDirPath;
    private Path outputDirPath;
    
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
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                log.error("Sleep interrupted", e);
            }
        }
        
        Assert.assertFalse("Jobs had errors", hadErrors);

        String output = FileUtils.readFileToString(outputDirPath.resolve("out.1").toFile(), "UTF-8");
        Assert.assertTrue(output.contains("Got input INPUT1"));
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
        
        JobInfo job = subCmd.execute(jt, 1, 4);

        Assert.assertNotNull(job.getJobId());

        boolean hadErrors = false;
        
        while (true) {

            List<JobInfo> jobs = jobsCmd.execute();
            Assert.assertNotNull(jobs);
            Assert.assertTrue(jobs.size()>1);
            
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
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                log.error("Sleep interrupted", e);
            }
        }
        
        Assert.assertFalse("Jobs had errors", hadErrors);
        
        for(int i=1; i<=4; i++) {
            String output = FileUtils.readFileToString(outputDirPath.resolve("out."+i).toFile(), "UTF-8");
            Assert.assertTrue(output.contains("Got input INPUT"+i));
        }
    }
}
