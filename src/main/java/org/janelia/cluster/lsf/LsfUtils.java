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
        // LSF puts in spaces for text alignment purposes instead of padding with zeros. These unfortunately break the
        // date parser. The following is a relatively naive way to try to fix this, but it should work for all cases
        // that we care about:
        str = str.trim().replaceAll("  ", " 0");
        // Remove the E for "Estimated" and other such characters from the end of dates.
        str = str.replaceAll("( \\w)$", "");
        // Add the current year because LSF is saving valuable space by not sending it. Things will get very interesting on Jan 1st. 
        String dateStr = str + " " + LocalDateTime.now().getYear();
        return LocalDateTime.parse(dateStr, DATE_FORMAT);
    }

    public static Integer parseInt(String str) {
        if (str==null) return null;
        return new Integer(str);
    }

    public static Long parseLong(String str) {
        if (str==null) return null;
        return new Long(str);
    }
}
