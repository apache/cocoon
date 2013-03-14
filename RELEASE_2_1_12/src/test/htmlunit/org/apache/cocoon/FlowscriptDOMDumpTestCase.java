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
 * Check various ways of dumping DOM objects in Flowscript (bugzilla 29381).
 *
 * @version $Id: $
 */
public class FlowscriptDOMDumpTestCase
    extends HtmlUnitTestCase
{
    final String pageurl = "/samples/test/flowscript-dom-dump/";

    /**
     * Check dump without XSLT transform.
     */
    public void testDOMDump()
        throws Exception
    {
        loadXmlPage(pageurl+"dom-dump");
        assertEquals("Content-type", "text/xml", response.getContentType());

        assertXPath("//dump-without-star/root/child", "childText");
        assertXPath("//dump-with-star/root/child", "childText");
    }

    /**
     * Check dump with XSLT transform.
     */
    public void testDOMDumpXSLT()
        throws Exception
    {
        loadXmlPage(pageurl+"dom-dump-xslt");
        assertEquals("Content-type", "text/xml", response.getContentType());

        assertXPath("//dump-without-star/root/@test-transform", "true");
        assertXPath("//dump-without-star/root/child", "childText");
        assertXPath("//dump-with-star/root/@test-transform", "true");
        assertXPath("//dump-with-star/root/child", "childText");
    }
}
