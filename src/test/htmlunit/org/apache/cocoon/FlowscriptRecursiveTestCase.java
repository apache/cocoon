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
 * Check recursive calling a flowscript function.
 *
 * @version $Id: $
 */
public class FlowscriptRecursiveTestCase
    extends HtmlUnitTestCase
{
    final String pageurl = "/samples/flow/test/";

    /**
     * Check dump without XSLT transform.
     */
    public void testFlowscriptRecursive()
        throws Exception
    {
        loadHtmlPage(pageurl+"factorial?n=5");
        assertXPath("html/body/p[1]", "Factorial of 5 is ...");
        assertXPath("html/body/p[2]", "120.0");
    }
}
