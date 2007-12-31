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
 * Testcase to simulate the behavior of a user that opens a browser, starts
 * the calculator example, and goes back in the processing several times.
 *
 * @version $Id: $
 */
public class CalcTestCase
    extends HtmlUnitTestCase
{
    final String pageurl = "/samples/flow/jxcalc/";
    final String submitXPath = "html/body//form/@action";
    final String resultXPath = "html/body//form/p[contains(text(),'Result')]/strong";

    public void testCalc()
        throws Exception
    {
        loadHtmlPage(pageurl);
        final String cont1 = evalXPath(submitXPath);
        assertNotNull("cont1", cont1);

        loadHtmlPage(pageurl+cont1+"?a=1");
        final String cont2 = evalXPath(submitXPath);
        assertNotNull("cont2", cont2);

        loadHtmlPage(pageurl+cont2+"?b=2");
        final String cont3 = evalXPath(submitXPath);
        assertNotNull("cont3", cont3);

        loadHtmlPage(pageurl+cont3+"?operator=plus");
        final String result1 = evalXPath(resultXPath);
        assertEquals("result1", "3.0", result1);

        // Simulate going back in the browser

        loadHtmlPage(pageurl+cont2+"?b=4");
        final String cont4 = evalXPath(submitXPath);
        assertNotNull("cont4", cont4);

        loadHtmlPage(pageurl+cont4+"?operator=minus");
        final String result2 = evalXPath(resultXPath);
        assertEquals("result2", "-3.0", result2);

        // Simulate going back again in the browser

        loadHtmlPage(pageurl+cont4+"?operator=divide");
        final String result3 = evalXPath(resultXPath);
        assertEquals("result3", "0.25", result3);
    }
}
