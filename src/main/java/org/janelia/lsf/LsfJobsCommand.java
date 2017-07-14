package org.janelia.lsf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd HH:mm yyyy");
    private static final String BJOBS_COMMAND = "bjobs";
    private static final Character BJOBS_DELIMITER = ',';

    public List<JobInfo> run() throws IOException {
        return run(null);
    }
    
    public List<JobInfo> run(String user) throws IOException {

        String formatSpec = "jobid name from_host exec_host stat queue project "
                + "max_req_proc nalloc_slot submit_time start_time finish_time "
                + "max_mem exit_code delimiter='"+BJOBS_DELIMITER+"'";
        
        List<String> args = new ArrayList<>();
        
        if (user!=null) {
            args.add("-u");
            args.add(user);
        }
        args.add("-a"); // bring back recent history, so we know what happened to jobs that recently completed
        args.add("-X"); // bring back expanded hostnames
        args.add("-o"); // format the output
        args.add(formatSpec);
        
        return runJobsCommand(args, new Function<String, JobInfo>() {
            @Override
            public JobInfo apply(String line) {

                // Skip blank lines, and the header
                if (StringUtils.isBlank(line) || line.startsWith("JOBID")) {
                    return null;
                }
                
                if ("No job found".equals(line)) {
                    return null;
                }
                
                try {
                    String[] split = line.split(BJOBS_DELIMITER.toString());
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
                              
                    if (execHost!=null) {
                        int s1 = execHost.indexOf('*');
                        if (s1>0) {
                            execHost = execHost.substring(s1+1);
                        }
                    }
                    
                    JobInfo info = new JobInfo();
                    info.setJobId(parseInt(jobIdStr));
                    info.setName(name);
                    info.setFromHost(fromHost);
                    info.setExecHost(execHost);
                    info.setStatus(JobStatus.parse(stat));
                    info.setQueue(queue);
                    info.setProject(project);
                    info.setReqSlot(parseInt(reqSlot));
                    info.setAllocSlot(parseInt(allocSlot));
                    info.setSubmitTime(parseDate(submitTime));
                    info.setStartTime(parseDate(startTime));
                    info.setFinishTime(parseDate(finishTime));
                    info.setMaxMem(maxMem);
                    
                    // LSF does not give an exit code unless it is non-zero
                    Integer exitCode = parseInt(exitCodeStr);
                    if (exitCode==null && info.getStatus().isDone()) exitCode = 0;
                    info.setExitCode(exitCode);
    
                    int b1 = name.indexOf('[');
                    int b2 = name.indexOf(']');
                    if (b1>0 && b2>0) {
                        info.setArrayIndex(parseInt(name.substring(b1+1, b2)));
                    }

                    return info;
                }
                catch (Exception e) {
                    log.error("Error parsing line: "+line, e);
                    return null;
                }
            } 
        });
    }
    
    private String getValue(String[] values, int index) {
        try {
            String s = values[index];
            if ("-".equals(s)) return null;
            return s;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            log.error("Error parsing index {} from line",index);
            return null;
        }
    }
    
    private Integer parseInt(String str) {
        if (str==null) return null;
        return new Integer(str);
    }
    
    private LocalDateTime parseDate(String str) throws ParseException {
        if (str==null) return null;
        // Remove the E for Estimated and other such characters from the end of dates.
        // Add the year because LSF is saving valuable space by not sending it. Things will get interesting on Jan 1st. 
        String dateStr = str.replaceAll("( \\w)$", "") + " " + LocalDateTime.now().getYear();
        return LocalDateTime.parse(dateStr, DATE_FORMAT);
    }

    private List<JobInfo> runJobsCommand(List<String> args, Function<String,JobInfo> parser) throws IOException {

        List<String> cmd = new ArrayList<>();
        cmd.add(BJOBS_COMMAND);
        cmd.addAll(args);
        
        log.debug("Running: {}", cmd);
        
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        Process p = processBuilder.start();
        
        List<JobInfo> statusList = new ArrayList<>();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = input.readLine()) != null) {
                JobInfo info = parser.apply(line);
                if (info!=null) {
                    statusList.add(info);
                }
            }
        }
        
        return statusList;
    }
    
    public static void main(String[] args) {
        
        LsfJobsCommand commands = new LsfJobsCommand();
        
        try {
            List<JobInfo> jobs = commands.run("jacs");
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
