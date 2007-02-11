/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
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


public class HostSelectorTestCase extends SitemapComponentTestCase {

    private static final String HOST_SELECTOR = "host";

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
        TestSuite suite = new TestSuite(HostSelectorTestCase.class);
        return suite;
    }
    
    /**
     * A simple host selector test
     */
    public void testHostSelectEurope() throws Exception {
        final String host = "myhost-dns-name-in-a-europe-country";
        String expectedHostName;
        
        getRequest().setHeader("Host", host );
        Parameters parameters = new Parameters();
        boolean result;
 
        // test selecting succeeds
        expectedHostName = "myhost-eu";
        result = this.select( HOST_SELECTOR, expectedHostName, parameters );
        System.out.println(result);
        assertTrue( "Test if host is " + expectedHostName, result );
        
        // test selecting fails
        expectedHostName = "myhost-us";
        result = this.select( HOST_SELECTOR, expectedHostName, parameters );
        System.out.println(result);
        assertTrue( "Test if host is not " + expectedHostName, !result );
    }
    
    /**
     * A simple host selector test
     */
    public void testHostSelectUnknownHost() throws Exception {
        final String host = "myhost-dns-name-in-a-asia-country";
        String expectedHostName;
        
        getRequest().setHeader("Host", host );
        Parameters parameters = new Parameters();
        boolean result;
        
        // test selecting succeeds
        expectedHostName = "myhost-eu";
        result = this.select( HOST_SELECTOR, expectedHostName, parameters );
        System.out.println(result);
        assertTrue( "Test if host is not " + expectedHostName, !result );
        
        // test selecting fails
        expectedHostName = "myhost-us";
        result = this.select( HOST_SELECTOR, expectedHostName, parameters );
        System.out.println(result);
        assertTrue( "Test if host is not " + expectedHostName, !result );
    }
}
