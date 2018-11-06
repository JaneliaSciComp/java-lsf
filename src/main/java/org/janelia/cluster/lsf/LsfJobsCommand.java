package org.janelia.cluster.lsf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.janelia.cluster.JobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * Wrapper around LSF bjobs command.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class LsfJobsCommand {

    private static final Logger log = LoggerFactory.getLogger(LsfJobsCommand.class);
    
    private static final String BJOBS_COMMAND = "bjobs";

    private static final Character BJOBS_DELIMITER = '^';

    private static final String FORMAT_SPEC =
            "jobid name from_host exec_host stat queue project "
            + "max_req_proc nalloc_slot submit_time start_time finish_time "
            + "max_mem exit_code exit_reason delimiter='"+BJOBS_DELIMITER+"'";

    public List<JobInfo> execute() throws IOException {
        return execute(null, null);
    }
    
    public List<JobInfo> execute(String user, Long jobId) throws IOException {

        List<String> args = new ArrayList<>();

        if (user != null) {
            args.add("-u");
            args.add(user);
        }
        args.add("-a"); // bring back recent history, so we know what happened to jobs that recently completed
        args.add("-X"); // bring back expanded hostnames
        args.add("-o"); // format the output
        args.add(FORMAT_SPEC);
        if (jobId != null) {
            args.add(jobId.toString());
        }
        
        return runJobsCommand(args, (line) -> {

            // Skip blank lines, and the header
            if (StringUtils.isBlank(line) || line.startsWith("JOBID")) {
                return null;
            }

            if (line.matches("No .* found")) {
                return null;
            }

            try {
                // We have to escape the delimiter, because split takes a regex
                String[] split = line.split("\\"+BJOBS_DELIMITER);
                log.trace("Parsed '{}' into {} values", line, split.length);

                int c = 0;
                String jobIdStr = getValue(split, c++);
                String name = getValue(split, c++);
                String fromHost = getValue(split, c++);
                String execHost = getValue(split, c++);
                String stat = getValue(split, c++);
                String queue = getValue(split, c++);
                String project = getValue(split, c++);
                String reqSlot = getValue(split, c++);
                String allocSlot = getValue(split, c++);
                String submitTime = getValue(split, c++);
                String startTime = getValue(split, c++);
                String finishTime = getValue(split, c++);
                String maxMem = getValue(split, c++);
                String exitCodeStr = getValue(split, c++);
                String exitReason = getValue(split, c++);

                LsfJobInfo info = new LsfJobInfo();
                info.setJobId(LsfUtils.parseLong(jobIdStr));
                info.setLsfJobName(name);
                info.setFromHost(fromHost);
                info.setExecHost(execHost);
                info.setLsfJobStatus(stat);
                info.setQueue(queue);
                info.setProject(project);
                info.setReqSlot(LsfUtils.parseInt(reqSlot));
                info.setAllocSlot(LsfUtils.parseInt(allocSlot));
                info.setMaxMem(maxMem);

                try {
                    info.setSubmitTime(LsfUtils.parseDate(submitTime));
                }
                catch (DateTimeParseException e) {
                    log.error("Error parsing date: "+submitTime);
                }

                try {
                    info.setStartTime(LsfUtils.parseDate(startTime));
                }
                catch (DateTimeParseException e) {
                    log.error("Error parsing date: "+startTime);
                }

                try {
                    info.setFinishTime(LsfUtils.parseDate(finishTime));
                }
                catch (DateTimeParseException e) {
                    log.error("Error parsing date: "+finishTime);
                }

                // LSF does not give an exit code unless it is non-zero
                Integer exitCode = LsfUtils.parseInt(exitCodeStr);
                if (exitCode==null && info.getStatus().isDone()) exitCode = 0;
                info.setExitCode(exitCode);
                info.setExitReason(exitReason);

                return info;
            } catch (Exception e) {
                log.error("Error parsing line: "+line, e);
                return null;
            }
        });
    }
    
    private String getValue(String[] values, int index) {
        String s = values[index];
        // Interpret missing values as nulls
        if ("-".equals(s)) return null;
        // Remove any extra whitespace
        return s.trim();
    }

    private List<JobInfo> runJobsCommand(List<String> args, Function<String,JobInfo> parser) throws IOException {

        List<String> cmd = new ArrayList<>();
        cmd.add(BJOBS_COMMAND);
        cmd.addAll(args);
        
        log.trace("Running: {}", cmd);
        
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        Process p = processBuilder.start();

        StringBuilder output = new StringBuilder();
        List<JobInfo> statusList = new ArrayList<>();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = input.readLine()) != null) {
                output.append(line).append("\n");
                log.trace(BJOBS_COMMAND+" output: {}", line);
                JobInfo info = parser.apply(line);
                if (info!=null) {
                    statusList.add(info);
                }
            }
        }

        int exitValue = waitUntilDone(p);
        log.trace("exitValue: {}", exitValue);
        if (exitValue != 0) {
            log.warn(BJOBS_COMMAND + " failed with exit code {}. Output:\n{}", exitValue, output);
            throw new IOException(BJOBS_COMMAND + " exited with code " + exitValue);
        }
        return statusList;
    }

    private int waitUntilDone(Process p) {
        try {
            log.trace("Waiting for exit...");
            p.waitFor(100, TimeUnit.SECONDS);
            return p.exitValue();
        } catch (InterruptedException e) {
            log.warn("Interrupt while waiting for process to end", e);
            throw new IllegalStateException(BJOBS_COMMAND+" did not exit cleanly", e);
        }

    }

    public static void main(String[] args) {
        
        LsfJobsCommand commands = new LsfJobsCommand();
        
        try {
            List<JobInfo> jobs = commands.execute();
            log.info("Found {} jobs", jobs.size());
            for (JobInfo jobInfo : jobs) {
                log.info("{}", jobInfo);
            }
        }
        catch (IOException e) {
            log.error("Error parsing bjobs", e);
        }        
    }

}
