# Java API to LSF

![CI Build](https://github.com/JaneliaSciComp/java-lsf/actions/workflows/maven.yml/badge.svg)

This is a simple wrapper library for using [IBM Platform LSF](https://en.wikipedia.org/wiki/Platform_LSF) from Java via command-line utilities such as bsub and bjobs. It is intended to be as simple an interface as possible, without the added complexity of DRMAA or the LSF APIs.

The job management piece is abstracted in such a way that it could be extended for use with other job schedulers, or other job scheduler APIs, but currently LSF command line is the only implementation.

## Building

This project builds easily with Maven. For example:
```
$ mvn package
```

## Deploying to Janelia repo
To deploy to Janelia nexus repository create a settings xml file like the one 
below:
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">

   <servers>
      <server>
         <id>janelia-releases</id>
         <username>yourusername</username>
         <password>yourpassword</password>
      </server>
   </servers>

</settings>
```
and then you can run. 
```
mvn -s <path_to_my_deploy_settings_xml> deploy
```
You can also add the server to your default settings.xml.

## Concepts

The API contains of a generic job monitoring application (`org.janelia.cluster`) and an LSF implementation (`org.janelia.cluster.lsf`). Most of the concepts are taken directly from DRMAA:

* Job - job running on a remote cluster (sometimes this term can also encompass job arrays)
* Job Array - a job template that's been parameterized to run one or more times with different inputs
* Job Template - the specification for a job or job array. This is what gets submitted to the cluster scheduler.
* Job Status - status of the job on the cluster
* Job Info - a snapshot of a job's status and other metadata at a certain point in time.

## Usage

Simply instantiate `org.janelia.cluster.JobManager` and call `start()`. It will keep track of all jobs submitted to the cluster, and complete futures when they are ready. For example:

```java
JobSyncApi api = new LsfSyncApi();
JobManager mgr = new JobManager(api);
JobMonitor monitor = new JobMonitor(mgr);
monitor.start();

JobTemplate jt = new JobTemplate();
jt.setRemoteCommand("bash");
jt.setArgs(Arrays.asList(scriptPath));
jt.setJobName("test");
jt.setInputPath(inputDirPath+"/input.#");
jt.setOutputPath(outputDirPath+"/out.#");
jt.setErrorPath(outputDirPath+"/err.#");
jt.setNativeSpecification(Arrays.asList("-W 1", "-n 2"));

final JobFuture future = mgr.submitJob(jt);

future.whenCompleteAsync(new BiConsumer<Collection<JobInfo>, Throwable>() {
    @Override
    public void accept(Collection<JobInfo> infos, Throwable t) {
        if (t!=null) {
            log.error("Error running job", t);
        }
        log.info("Job has completed: "+infos);
    }
});
```

Alternatively, you can omit the JobMonitor and periodically call `checkJobs()` manually. This is useful when running in managed environments such as an application server, which have their own internal periodic job scheduling.

If you'd like to check the status of a specific job without spinning up a background thread, you can call `JobSyncApi::getJobInfo` like this:
```java
JobSyncApi api = new LsfSyncApi();

List<JobInfo> jobs = api.getJobInfo();
Multimap<Long, JobInfo> jobMap = Utils.getJobMap(jobs);
// infos will contain one object for every job running on the grid with the given jobId
List<JobInfo> infos = jobMap.get(jobId);
```

For more usage example, see the unit tests, in particular `org.janelia.cluster.lsf.LsfTests`.

