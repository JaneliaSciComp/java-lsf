package org.janelia.cluster.lsf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for dealing with LSF command line utilities.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class LsfUtils {

    private static final Logger log = LoggerFactory.getLogger(LsfUtils.class);

    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;
    private static final long TB = GB * 1024;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd HH:mm yyyy");
    private static final DateTimeFormatter DATE_FORMAT_SECS = DateTimeFormatter.ofPattern("MMM dd HH:mm:ss yyyy");

    public static LocalDateTime parseDate(String str) {
        if (str==null) return null;
        // LSF puts in spaces for text alignment purposes instead of padding with zeros. These unfortunately break the
        // date parser. The following is a relatively naive way to try to fix this, but it should work for all cases
        // that we care about:
        str = str.trim().replaceAll("  ", " 0");
        // Remove the E for "Estimated" and other such characters from the end of dates.
        str = str.replaceAll("( \\w)$", "");
        // If necessary, add the current year (necessary before the 10.1.0.3 service pack)
        String dateStr = str;
        if (!dateStr.matches(".*\\d{4}")) {
            dateStr = dateStr + " " + LocalDateTime.now().getYear();
        }
        if (dateStr.matches(".*\\d{2}:\\d{2}:\\d{2}.*")) {
            return LocalDateTime.parse(dateStr, DATE_FORMAT_SECS);
        }
        return LocalDateTime.parse(dateStr, DATE_FORMAT);
    }

    public static Integer parseInt(String str) {
        if (str==null) return null;
        return Integer.valueOf(str);
    }

    public static Long parseLong(String str) {
        if (str==null) return null;
        return Long.valueOf(str);
    }

    /**
     * Convert the given LocalDateTime instance to an old-school Date.
     * @param ldt
     * @return date in the system time zone
     */
    public static Date convertLocalDateTime(LocalDateTime ldt) {
        if (ldt == null) return null;
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Calculate the difference in seconds between two local date times.
     * @param startTime
     * @param finishTime
     * @return number of seconds
     */
    public static Long getDiffSecs(LocalDateTime startTime, LocalDateTime finishTime) {
        if (startTime==null || finishTime==null) return null;
        return ChronoUnit.SECONDS.between(startTime, finishTime);
    }

    /**
     * Parse LSF's max_mem field into a normalized number of bytes.
     * @param memLsf
     * @return number of bytes
     */
    public static Long parseMemToBytes(String memLsf) {
        if (memLsf == null) return null;
        Double bytes;

        Pattern p = Pattern.compile("([\\d.]+)\\s+(\\w+)");
        Matcher m = p.matcher(memLsf.trim());
        if (m.matches()) {
            double amount = Double.parseDouble(m.group(1));
            String units = m.group(2).toLowerCase();

            if (units.startsWith("k")) {
                bytes = KB * amount;
            }
            else if (units.startsWith("m")) {
                bytes = MB * amount;
            }
            else if (units.startsWith("g")) {
                bytes = GB * amount;
            }
            else if (units.startsWith("t")) {
                // Future-proof!
                bytes = TB * amount;
            }
            else {
                log.warn("Could not parse units in max mem: '{}'", units);
                return null;
            }
        }
        else {
            log.warn("Could not parse max mem: '{}'", memLsf);
            return null;
        }

        return bytes.longValue();
    }
}
