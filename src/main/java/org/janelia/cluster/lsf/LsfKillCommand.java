package org.janelia.cluster.lsf;

import org.janelia.cluster.JobCmdFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Wrapper around the LSF bkill command.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class LsfKillCommand {

    private static final Logger log = LoggerFactory.getLogger(LsfKillCommand.class);

    private static final String BKILL_COMMAND = "bkill";

    public void executeWithJobName(String jobName, JobCmdFlag... flags) throws IOException {
        String[] execArgs = Stream.concat(
                Arrays.stream(flags).flatMap(f -> f.getFlags().stream()),
                Stream.of("-J", jobName)
        ).toArray(String[]::new);
        execute(execArgs);
    }

    public void executeWithJobId(Long jobId, JobCmdFlag... flags) throws IOException {
        String[] execArgs = Stream.concat(
                Arrays.stream(flags).flatMap(f -> f.getFlags().stream()),
                Stream.of(jobId.toString())
        ).toArray(String[]::new);
        execute(execArgs);
    }

    private void execute(String... args) throws IOException {

        List<String> cmd = new ArrayList<>();
        cmd.add(BKILL_COMMAND);
        Collections.addAll(cmd, args);
        log.info("Running: {}", cmd);
        
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);

        Process p = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = input.readLine()) != null) {
                output.append(line).append("\n");
                log.trace(BKILL_COMMAND +" output: {}", line);
            }
        }

        int exitValue;
        try {
            log.trace("Waiting for exit...");
            p.waitFor(30, TimeUnit.SECONDS);
            exitValue = p.exitValue();
            log.trace("exitValue: "+exitValue);
            if (exitValue!=0) {
                log.warn(BKILL_COMMAND +" failed with exit code {}. Output:\n{}", exitValue, output);
                throw new IOException(BKILL_COMMAND +" exited with code "+exitValue);
            }
        }
        catch (InterruptedException e) {
            throw new IOException(BKILL_COMMAND +" did not exit cleanly", e);
        }
    }

}
