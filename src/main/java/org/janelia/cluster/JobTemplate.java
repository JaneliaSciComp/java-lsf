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

    public void setRemoteCommand(String remoteCommand) {
        this.remoteCommand = remoteCommand;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getErrorPath() {
        return errorPath;
    }

    public void setErrorPath(String errorPath) {
        this.errorPath = errorPath;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public List<String> getNativeSpecification() {
        return nativeSpecification;
    }

    public void setNativeSpecification(List<String> nativeSpecification) {
        this.nativeSpecification = nativeSpecification;
    }

}
