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
 *  Verify that sitemap annotations (bugzilla 25352) are ignored.
 *  In the (2.2) trunk this test is done using anteater,
 *  as the HtmlUnit-based tests were not ported to 2.2 yet
 *  when this feature was implemented.
 *
 * @version $Id: $
 */
public class SitemapAnnotationsTestCase
    extends HtmlUnitTestCase
{
    public void testSitemapAnnotations()
        throws Exception
    {
        final String pageurl = "samples/test/sitemap-annotations/annotations";

        // Just check that the annotated sitemap works
        loadXmlPage(pageurl);
        final String author = evalXPath("/sitemap-annotations/info/author");
        assertEquals("The Cocoon team",author);
    }
}
