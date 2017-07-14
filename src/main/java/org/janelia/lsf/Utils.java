package org.janelia.lsf;

import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

public class Utils {

    public static Multimap<Integer, JobInfo> getJobMap(List<JobInfo> jobs) {
        ListMultimap<Integer, JobInfo> map = ArrayListMultimap.create();
        for (JobInfo jobInfo : jobs) {
            map.put(jobInfo.getJobId(), jobInfo);
        }
        return map;
    }
    
    
}
