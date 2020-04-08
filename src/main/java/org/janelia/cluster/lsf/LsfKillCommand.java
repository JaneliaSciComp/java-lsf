package org.janelia.cluster.lsf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around the LSF bkill command.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class LsfKillCommand {

    private static final Logger log = LoggerFactory.getLogger(LsfKillCommand.class);

    private static final String BKILL_COMMAND = "bkill";

    public void executeWithJobName(String jobName) throws IOException {
        execute("-J", jobName);
    }

    public void executeWithJobId(Long jobId) throws IOException {
        execute(jobId.toString());
    }

    private void execute(String... args) throws IOException {

        List<String> cmd = new ArrayList<>();
        cmd.add(BKILL_COMMAND);
        for (String arg : args) {
            cmd.add(arg);
        }
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
