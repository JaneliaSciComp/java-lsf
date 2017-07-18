package org.janelia.cluster.lsf;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilities for dealing with LSF command line utilities.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class LsfUtils {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd HH:mm yyyy");

    public static LocalDateTime parseDate(String str) throws ParseException {
        if (str==null) return null;
        // Remove the E for Estimated and other such characters from the end of dates.
        // Add the current year because LSF is saving valuable space by not sending it. Things will get very interesting on Jan 1st. 
        String dateStr = str.replaceAll("( \\w)$", "") + " " + LocalDateTime.now().getYear();
        return LocalDateTime.parse(dateStr, DATE_FORMAT);
    }

    public static Integer parseInt(String str) {
        if (str==null) return null;
        return new Integer(str);
    }
}
