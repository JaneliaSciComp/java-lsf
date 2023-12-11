package org.janelia.cluster.lsf;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.janelia.cluster.JobCmdFlag;

public class LSFTerminateWithReason implements JobCmdFlag {

    private final String reason;

    public LSFTerminateWithReason(String reason) {
        this.reason = reason;
    }

    @Override
    public List<String> getFlags() {
        if (StringUtils.isNotBlank(reason)) {
            return Arrays.asList("-C", reason);
        } else {
            return Collections.emptyList();
        }
    }
}
