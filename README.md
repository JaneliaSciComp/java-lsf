# Java API to LSF

This is a simple wrapper library for using [IBM Platform LSF](https://en.wikipedia.org/wiki/Platform_LSF) from Java via command-line utilities such as bsub and bjobs. It is intended to be as simple an interface as possible, without the added complexity of DRMAA or the LSF APIs.

The job management piece is abstracted in such a way that it could be extended for use with other cluster management engines, but currently LSF is the only implementation.

## Building

This project builds with Maven. For example:
```
$ mvn test
$ mvn package
```

## Usage

Simply instantiate ```org.janelia.cluster.JobManager``` and maintain a reference to it. It will take care of running a separate thread to poll bjobs for job status information. For usage example, see the unit tests, in particular ```org.janelia.cluster.lsf.LsfTests```. 

## License 

MIT License, see the LICENSE file for details.

