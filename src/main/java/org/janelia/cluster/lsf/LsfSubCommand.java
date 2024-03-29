package org.janelia.cluster.lsf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.janelia.cluster.JobInfo;
import org.janelia.cluster.JobStatus;
import org.janelia.cluster.JobTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around the LSF bsub command.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class LsfSubCommand {

    private static final Logger log = LoggerFactory.getLogger(LsfSubCommand.class);

    private static final String BSUB_COMMAND = "bsub";
    private static final String BSUB_ENV_REPORT_MAIL = "LSB_JOB_REPORT_MAIL";
    private static final Pattern SUCCESS_PATTERN = Pattern.compile("Job <(\\d+)> is submitted to (?:\\S+ )?queue <(.+)>.");

    private boolean isJobReportMail = false;

    public JobInfo execute(JobTemplate jt) throws IOException {
        return execute(jt, null, null);
    }
    
    public JobInfo execute(JobTemplate jt, Long start, Long end) throws IOException {

        List<String> cmd = new ArrayList<>();
        cmd.add(BSUB_COMMAND);
        
        if (jt.getWorkingDir()!=null) {
            cmd.add("-cwd");
            cmd.add(jt.getWorkingDir());
        }
        
        if (jt.getInputPath()!=null) {
            cmd.add("-i");
            cmd.add(jt.getInputPath().replace("#", "%I"));
        }
        
        if (jt.getOutputPath()!=null) {
            cmd.add("-o");
            cmd.add(jt.getOutputPath().replace("#", "%I"));
        }

        if (jt.getErrorPath()!=null) {
            cmd.add("-e");
            cmd.add(jt.getErrorPath().replace("#", "%I"));
        }
        
        if (jt.getNativeSpecification()!=null) {
            cmd.addAll(jt.getNativeSpecification());
        }

        cmd.add("-J");
        String name = jt.getJobName();
        if (start!=null && end!=null) {
            cmd.add(String.format("%s[%d-%d]", name, start, end));
        }
        else {
            cmd.add(name);
        }
        
        cmd.add(jt.getRemoteCommand());
        cmd.addAll(jt.getArgs());

        log.debug("Running: {}", cmd);
        
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);

        Map<String, String> env = processBuilder.environment();
        env.put(BSUB_ENV_REPORT_MAIL, isJobReportMail ? "y":"n");
        if (jt.getJobEnvironment() != null) {
            env.putAll(jt.getJobEnvironment());
        }

        Process p = processBuilder.start();

        StringBuilder output = new StringBuilder();
        JobInfo info = null;
        try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = input.readLine()) != null) {
                output.append(line).append("\n");
                log.debug("{} output: {}", BSUB_COMMAND, line);
                Matcher m = SUCCESS_PATTERN.matcher(line);
                if (m.matches()) {
                    Long jobId = LsfUtils.parseLong(m.group(1));
                    String queue = m.group(2);
                    info = new JobInfo();
                    info.setJobId(jobId);
                    info.setQueue(queue);
                    info.setStatus(JobStatus.PENDING);
                    break;
                }
            }
        }

        if (info==null) {
            log.warn("{} failed to return job id. Output:\n{}", cmd, output);
            throw new IOException(BSUB_COMMAND+" failed to return job id");
        }

        int exitValue = waitWhileAlive(p);
        log.trace("exitValue: {}", exitValue);
        if (exitValue != 0) {
            log.warn("{} failed with exit code {}. Output:\n{}", cmd, exitValue, output);
            throw new IOException(BSUB_COMMAND + " exited with code " + exitValue);
        }
        return info;
    }

    private int waitWhileAlive(Process p) {
        try {
            log.trace("Waiting for exit...");
            p.waitFor(100, TimeUnit.SECONDS);
            return p.exitValue();
        } catch (InterruptedException e) {
            log.warn("Interrupt while waiting for process to end", e);
            return 1; // some non 0 value
        }
    }

    /**
     * If this is set to true, bsub will be called with the LSB_JOB_REPORT_MAIL=y environment variable,
     * otherwise it will be called with LSB_JOB_REPORT_MAIL=n.
     */
    boolean isJobReportMail() {
        return isJobReportMail;
    }

    /**
     * If this is set to true, bsub will be called with the LSB_JOB_REPORT_MAIL=y environment variable,
     * otherwise it will be called with LSB_JOB_REPORT_MAIL=n.
     */
    void setJobReportMail(boolean jobReportMail) {
        this.isJobReportMail = jobReportMail;
    }

    public static void main(String[] args) throws IOException {
        
        LsfSubCommand commands = new LsfSubCommand();

        Path rootPath = Paths.get(".").toAbsolutePath().normalize();
        Path scriptDirPath = rootPath.resolve("src/test/bash");
        Path inputDirPath = rootPath.resolve("src/test/resources");
        Path outputDirPath = rootPath.resolve("target/test");
        String scriptPath = scriptDirPath.resolve("test.sh").toString();
        
        // Recreate the output directory
        FileUtils.deleteDirectory(outputDirPath.toFile());
        outputDirPath.toFile().mkdirs();
        
        try {
            JobTemplate jt = new JobTemplate();
            jt.setRemoteCommand("bash");
            jt.setArgs(Collections.singletonList(scriptPath));
            jt.setJobName("testApi");
            jt.setInputPath(inputDirPath+"/input.#");
            jt.setOutputPath(outputDirPath+"/out.#");
            jt.setErrorPath(outputDirPath+"/err.#");
            jt.setNativeSpecification(Arrays.asList("-W 1", "-n 2"));
            
            JobInfo job = commands.execute(jt, 1L, 4L);
            log.info("Submitted job as {}", job.getJobId());
        }
        catch (IOException e) {
            log.error("Error running bsub", e);
        }        
    }
}
