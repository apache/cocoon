package org.apache.cocoon.matching;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;

public class WildcardURIMatcherTestCase extends SitemapComponentTestCase {

    public WildcardURIMatcherTestCase(String name) {
        super(name);
    }

    public void testWildcardURIMatch() {
        getRequest().setRequestURI("/test/foo/bla/end");

        Parameters parameters = new Parameters();

        Map result = match("wildcard-uri", "**", parameters);
        System.out.println(result);
        assertNotNull("Test if resource exists", result);
        assertEquals("Test for **", "test/foo/bla/end", result.get("1"));
        
        result = match("wildcard-uri", "**/bla/*", parameters);
        System.out.println(result);
        assertNotNull("Test if resource exists", result);
        assertEquals("Test for **/bla/* {1}", "test/foo", result.get("1"));
        assertEquals("Test for **/bla/* {2}", "end", result.get("2"));
    }
}
