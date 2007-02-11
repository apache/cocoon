/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon;

/**
 * Check redirects.
 *
 * @version $Id: $
 */
public class RedirectTestCase
    extends HtmlUnitTestCase
{
    final String pageurl = "/samples/test/redirect/";

    public void testRedirectToFromSitemap()
        throws Exception
    {
        loadResponse(pageurl+"redirect-to-from-sitemap");
        assertEquals("Status code", 302, response.getStatusCode());
    }

    public void testRedirectToInternalFromSitemap()
        throws Exception
    {
        loadResponse(pageurl+"redirect-to-internal-from-sitemap");
        assertEquals("Status code", 200, response.getStatusCode());
    }

    public void testRedirectToFromFlow()
        throws Exception
    {
        loadResponse(pageurl+"redirect-to-from-flow");
        assertEquals("Status code", 302, response.getStatusCode());
    }

    public void testSendStatus()
        throws Exception
    {
        loadResponse(pageurl+"send-status");
        assertEquals("Status code", 204, response.getStatusCode());
    }

    public void testSendPage()
        throws Exception
    {
        loadResponse(pageurl+"send-page");
        assertEquals("Status code", 200, response.getStatusCode());
    }

    public void testDoNothingFromSitemap()
        throws Exception
    {
        loadResponse(pageurl+"donothing-from-sitemap");
        assertEquals("Status code", 404, response.getStatusCode());
    }

    public void testDoNothingFromFlow()
        throws Exception
    {
        loadResponse(pageurl+"donothing-from-flow");
        assertEquals("Status code", 500, response.getStatusCode());
    }
}
