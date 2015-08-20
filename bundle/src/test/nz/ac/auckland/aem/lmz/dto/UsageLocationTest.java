package nz.ac.auckland.aem.lmz.dto;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author: Marnix Cook <m.cook@auckland.ac.nz>
 */
public class UsageLocationTest {

    @Test
    public void testGetPath() throws Exception {
        UsageLocation l = new UsageLocation("/app/components/blaat", "/content/abi/test.html", "Test page");
        Assert.assertEquals("/content/abi/test", l.getPath());
    }
}