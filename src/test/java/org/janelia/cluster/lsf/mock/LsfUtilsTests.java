package org.janelia.cluster.lsf.mock;

import java.time.LocalDateTime;

import org.janelia.cluster.lsf.LsfUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class LsfUtilsTests {

    @Test
    public void testParseDate() throws Exception {
        Assert.assertEquals(LocalDateTime.of(2019, 1, 2, 14, 05, 43),
                LsfUtils.parseDate("Jan  2 14:05:43 2019 X"));
    }

    @Test
    public void testParseDate2() throws Exception {
        Assert.assertEquals(LocalDateTime.of(2018, 9, 12, 18, 15),
                LsfUtils.parseDate("Sep 12 18:15 2018 E"));
    }

    @Test
    public void testParseDate3() throws Exception {
        Assert.assertEquals(LocalDateTime.of(2016, 10, 1, 18, 15),
                LsfUtils.parseDate("Oct  1 18:15 2016"));
    }

    @Test
    public void testParseInt() throws Exception {
        Assert.assertEquals(new Integer(15), LsfUtils.parseInt("15"));
        Assert.assertNull(null, LsfUtils.parseInt(null));
    }

    @Test
    public void testParseLong() throws Exception {
        Assert.assertEquals(new Long(Long.MAX_VALUE), LsfUtils.parseLong(Long.MAX_VALUE+""));
        Assert.assertNull(null, LsfUtils.parseLong(null));
    }

    @Test
    public void testDiffSecs() throws Exception {
        LocalDateTime d1 = LocalDateTime.of(2016, 10, 1, 18, 15);
        LocalDateTime d2 = LocalDateTime.of(2016, 10, 1, 18, 16);
        Assert.assertEquals(60, Math.abs(LsfUtils.getDiffSecs(d1, d2)));
        Assert.assertEquals(60, Math.abs(LsfUtils.getDiffSecs(d2, d1)));
    }

    @Test
    public void testParseMemGb() throws Exception {
        Long expected = 1825361100L;
        Assert.assertEquals(expected, LsfUtils.parseMemToBytes("1.7 Gbytes"));
        Assert.assertEquals(expected, LsfUtils.parseMemToBytes("1.7 Gbytes "));
        Assert.assertEquals(expected, LsfUtils.parseMemToBytes(" 1.7    Gbytes "));
    }

    @Test
    public void testParseMemMb() throws Exception {
        Long expected = 8283750L;
        Assert.assertEquals(expected, LsfUtils.parseMemToBytes("7.9 Mbytes"));
    }

}
