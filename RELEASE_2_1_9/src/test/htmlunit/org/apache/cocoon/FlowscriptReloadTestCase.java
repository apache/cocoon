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
 * Check the reloading of flowscript source files.
 *
 * @version $Id: $
 */
public class FlowscriptReloadTestCase
    extends HtmlUnitTestCase
{
    final String pageurl = "/samples/flow/test/";
    final String flowscriptPath = "samples/flow/test/sendpage.js";
    final String paramToken = "@REPLACEME@";
    final String resultXPath = "html/body//p[1]";

    public void testFlowscriptReload()
        throws Exception
    {
        // Copy the flowscript from its source directory to the destination
        // area, and replace the parameter value with 'abc' 

        final String expected1 = "replaceme-abc";
        copyWebappFile(flowscriptPath, paramToken, expected1);
        loadHtmlPage(pageurl+"showString");
        String result1 = evalXPath(resultXPath);
        assertEquals("Original request", expected1, result1);

        // Copy the flowscript from its source directory to the destination
        // area, and replace the parameter value with '123' 

        final String expected2 = "replaceme-123";
        copyWebappFile(flowscriptPath, paramToken, expected2);
        loadHtmlPage(pageurl+"showString");
        String result2 = evalXPath(resultXPath);
        assertEquals("After flowscript was modified", expected2, result2);
    }
}
