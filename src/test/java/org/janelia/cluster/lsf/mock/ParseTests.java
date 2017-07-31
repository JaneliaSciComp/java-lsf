package org.janelia.cluster.lsf.mock;

import org.janelia.cluster.lsf.LsfUtils;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.Month;

public class ParseTests {

    @Test
    public void testDateParser() throws ParseException {
        LocalDateTime date = LsfUtils.parseDate("Mar 10 12:30");
        Assert.assertEquals(Month.MARCH, date.getMonth());
        Assert.assertEquals(10, date.getDayOfMonth());
        Assert.assertEquals(12, date.getHour());
        Assert.assertEquals(30, date.getMinute());
        Assert.assertEquals(LocalDateTime.now().getYear(), date.getYear());
    }

    @Test
    public void testSingleDigitDay() throws ParseException {
        LocalDateTime date = LsfUtils.parseDate("Aug  1 18:59 E");
        Assert.assertEquals(Month.AUGUST, date.getMonth());
        Assert.assertEquals(1, date.getDayOfMonth());
        Assert.assertEquals(18, date.getHour());
        Assert.assertEquals(59, date.getMinute());
        Assert.assertEquals(LocalDateTime.now().getYear(), date.getYear());
    }

    @Test
    public void testNullIntParse() throws ParseException {
        Assert.assertNull(LsfUtils.parseInt(null));
    }

    @Test
    public void testIntParse() throws ParseException {
        Assert.assertEquals(new Integer(1234), LsfUtils.parseInt("1234"));
    }

    @Test
    public void testLongParse() throws ParseException {
        Assert.assertEquals(new Long(9999999999L), LsfUtils.parseLong("9999999999"));
    }

}
