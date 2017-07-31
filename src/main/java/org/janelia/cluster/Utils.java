package org.janelia.cluster;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

/**
 * Utility methods for dealing with jobs.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class Utils {

    public static Multimap<Long, JobInfo> getJobMap(List<JobInfo> jobs) {
        ListMultimap<Long, JobInfo> map = ArrayListMultimap.create();
        for (JobInfo jobInfo : jobs) {
            map.put(jobInfo.getJobId(), jobInfo);
        }
        return map;
    }
     
    public static boolean allDone(Collection<JobInfo> infos) {
        for (JobInfo jobInfo : infos) {
            if (!jobInfo.isComplete()) {
                return false;
            }
        }
        return true;
    }

    // From https://stackoverflow.com/questions/1555262/calculating-the-difference-between-two-java-date-instances
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
}
