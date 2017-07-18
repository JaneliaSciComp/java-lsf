package org.janelia.cluster;

import java.util.List;

/**
 * A generic template for a job to execute on the cluster.
 * 
 * When the job is intended to run as job array with multiple inputs, the inputPath, outputPath, and errorPaths 
 * should contain a pound sign (#) to indicate the array index which will change with each individual job.  
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class JobTemplate {

    private String remoteCommand;
    private List<String> args;
    private String workingDir;
    private String inputPath;
    private String outputPath;
    private String errorPath;
    private String jobName;
    private List<String> nativeSpecification;

    public String getRemoteCommand() {
        return remoteCommand;
    }

    /**
     * Sets the command to be executed on the cluster. Frequently this is bash, and the argument is a script name.
     */
    public void setRemoteCommand(String remoteCommand) {
        this.remoteCommand = remoteCommand;
    }

    public List<String> getArgs() {
        return args;
    }

    /**
     * Sets the arguments to pass to the remote command.
     */
    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    /**
     * Sets the working directory (CWD) for the job to run inside.
     * @param workingDir
     */
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getInputPath() {
        return inputPath;
    }

    /**
     * Sets the input path for a file which will be directed into the remote command via STDIN. 
     * In the case of job arrays, this path should contain a pound sign (#) which will be replaced 
     * with array indexes at run time.
     */
    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    /**
     * Sets the output path for a file where the job will append its STDOUT stream. 
     * In the case of job arrays, this path should contain a pound sign (#) which will be replaced 
     * with array indexes at run time.
     */
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getErrorPath() {
        return errorPath;
    }

    /**
     * Sets the output path for a file where the job will append its STDERR stream. 
     * In the case of job arrays, this path should contain a pound sign (#) which will be replaced 
     * with array indexes at run time.
     */
    public void setErrorPath(String errorPath) {
        this.errorPath = errorPath;
    }

    public String getJobName() {
        return jobName;
    }

    /**
     * Sets a name for the job or job array.
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public List<String> getNativeSpecification() {
        return nativeSpecification;
    }

    /**
     * Sets any native specification parameters to pass onto the scheduler.
     */
    public void setNativeSpecification(List<String> nativeSpecification) {
        this.nativeSpecification = nativeSpecification;
    }

}
