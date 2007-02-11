/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.selection;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;


public class BrowserSelectorTestCase extends SitemapComponentTestCase {

    /**
     * Run this test suite from commandline
     *
     * @param args commandline arguments (ignored)
     */
    public static void main( String[] args ) {
        TestRunner.run(suite());
    }
    
    /** Create a test suite.
     * This test suite contains all test cases of this class.
     * @return the Test object containing all test cases.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(BrowserSelectorTestCase.class);
        return suite;
    }
    
    /**
     * A simple non-configured browser name test
     */
    public void testBrowserSelectMisconfigured() throws Exception {
        final String userAgent = "Mozilla";
        
        getRequest().setHeader("User-Agent", userAgent );
        Parameters parameters = new Parameters();
        boolean result;
        
        result = this.select( "browser", "non-configured-browser-name", parameters );
        System.out.println( result );
        assertTrue( "Test is browser is a non-configured-browser-name", !result );
    }

    /**
     * A simple netscape browser test
     */
    public void testBrowserSelectNetscape() throws Exception {
        final String userAgent = "Mozilla";
        String expectedBrowserName;
        
        getRequest().setHeader("User-Agent", userAgent );
        Parameters parameters = new Parameters();
        boolean result;
        
        expectedBrowserName = "netscape";
        result = this.select( "browser", expectedBrowserName, parameters );
        System.out.println(result);
        assertTrue( "Test if browser is " + expectedBrowserName, result );
        
        expectedBrowserName = "explorer";
        result = this.select( "browser", expectedBrowserName, parameters );
        System.out.println( result );
        assertTrue( "Test if browser is NOT " + expectedBrowserName, !result );
    }
}
