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
        // Eliminate extra spaces which LSF likes to put in places for text alignment purposes. These unfortunately break the date parser.
        str = str.replaceAll("\\s+", " ").trim();
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
}
