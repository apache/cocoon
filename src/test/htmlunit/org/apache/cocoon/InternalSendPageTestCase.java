package org.apache.cocoon;

/**
 * Check internal sendpage.
 *
 * @version $Id: $
 */
public class InternalSendPageTestCase
    extends HtmlUnitTestCase
{
    final String pageurl = "/samples/test/sendpage/";

    public void testExternal()
        throws Exception
    {
        loadResponse(pageurl+"testExternal");
        assertEquals("Status code", 200, response.getStatusCode());
    }

    public void testInternal()
        throws Exception
    {
        loadResponse(pageurl+"testInternal");
        assertEquals("Status code", 200, response.getStatusCode());
    }
}
