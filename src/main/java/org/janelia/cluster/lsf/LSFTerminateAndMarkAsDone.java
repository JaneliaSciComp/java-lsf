package org.janelia.cluster.lsf;

import java.util.Collections;
import java.util.List;

import org.janelia.cluster.JobCmdFlag;

public class LSFTerminateAndMarkAsDone implements JobCmdFlag {
    @Override
    public List<String> getFlags() {
        return Collections.singletonList("-d");
    }
}
