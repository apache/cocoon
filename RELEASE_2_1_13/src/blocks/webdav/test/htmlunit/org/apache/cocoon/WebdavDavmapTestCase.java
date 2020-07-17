/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * Check davmap samples.
 *
 * @version $Id: $
 */
public class WebdavDavmapTestCase
    extends HtmlUnitTestCase
{
    static final String pageurl = "/samples/blocks/webdav/davmap/";
    static final String davmapTitle = "repo";

    static final String contentB =
        "<page>\n"+
        "<title>Page 2</title>\n"+
        "<content>\n"+
        "  <para>Paragraph 1</para>\n"+
        "  <para>Paragraph 2</para>\n"+
        "</content>\n"+
        "</page>\n";

    public void testDavmapTitle()
        throws Exception
    {
        loadHtmlPage(pageurl+"repo/");
        assertXPath("/html/head/title", davmapTitle);
    }

    public void testPutReadDelete()
        throws Exception
    {
        loadPutResponse(pageurl+"repo/contentB.xml", contentB);
        assertEquals("Status code", 201, response.getStatusCode());

        loadXmlPage(pageurl+"repo/contentB.xml");
        assertXPath("/page/title", "Page 2");

        loadDeleteResponse(pageurl+"repo/contentB.xml");
        assertEquals("Status code", 200, response.getStatusCode());
    }
}
