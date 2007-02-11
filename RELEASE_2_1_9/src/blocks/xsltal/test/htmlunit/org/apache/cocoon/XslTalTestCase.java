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
 * Check the xsltal testdoc.html document
 * @version $Id: $
 */
public class XslTalTestCase
    extends HtmlUnitTestCase
{
    static final String pageurl = "/samples/blocks/xsltal/plain/testdoc.html";

    public void testXslTalTestDoc()
        throws Exception
    {
        loadHtmlPage(pageurl);

        final String pageTitle = "This is the XSLTAL test document";
        assertXPath("/html/head/title", pageTitle);
        assertXPath("/html/body/div[1]/@class", "content");
        assertXPath("/html/body/h1[1]", pageTitle);
        assertXPath("/html/body/p[1]", "First paragraph of the test document.");
        assertXPath("/html/body/p[2]", "Second paragraph of the test document.");
    }
}
