package nz.ac.auckland.aem.lmz.helper;

import nz.ac.auckland.aem.lmz.helper.UrlPruner;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author: Marnix Cook <m.cook@auckland.ac.nz>
 */
public class UrlPrunerTest {

    @Test
    public void testGetPrunedUrl() throws Exception {

        UrlPruner location = new UrlPruner();
        location.setMaxUrlWidth(6);

        assertEquals(null, location.getPrunedUrl(null));

        // exact length
        assertEquals("123456", location.getPrunedUrl("123456"));

        // one than desirable
        assertEquals("12345", location.getPrunedUrl("12345"));

        // cut out the middle two
        assertEquals("123 ... 678", location.getPrunedUrl("12345678"));
    }

}