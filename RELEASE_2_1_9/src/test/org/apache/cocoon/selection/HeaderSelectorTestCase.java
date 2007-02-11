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


public class HeaderSelectorTestCase extends SitemapComponentTestCase {

    private static final String HEADER_SELECTOR = "header";

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
        TestSuite suite = new TestSuite(HeaderSelectorTestCase.class);
        return suite;
    }
    
    /**
     * A simple header select test
     */
    public void testHeaderSelect() throws Exception {
        final String headerName = "headerSelectorTestCase";
        final String headerValue = "headerValue";
        // create a header
        // setting name := headerName, value := headerValue
        getRequest().setHeader(headerName, headerValue);
        
        Parameters parameters = new Parameters();
        boolean result;

        // test selection success
        result = this.select( HEADER_SELECTOR, headerValue, parameters );
        System.out.println( result );
        assertTrue( "Test if a header is selected", result );
        
        // test selection failure
        result = this.select( HEADER_SELECTOR, "unknownHeaderValue", parameters );
        System.out.println( result );
        assertTrue( "Test if a header is not selected", !result );
    }

    /**
     * A simple header select test
     */
    public void testHeaderSelectUsingParameters() throws Exception {        
        final String headerName = "headerSelectorTestCase1";
        final String headerValue = "headerValue1";
        // create a header
        // setting name := headerName, value := headerValue
        getRequest().setHeader(headerName, headerValue);

        // check the header as defined by this parameter, not as
        // defined in the component configuration
        Parameters parameters = new Parameters();
        parameters.setParameter( "header-name", headerName );
        
        boolean result;
        
        // test selection success
        result = this.select( HEADER_SELECTOR, headerValue, parameters );
        System.out.println( result );
        assertTrue( "Test if a header is selected", result );
        
        // test selection failure
        result = this.select( HEADER_SELECTOR, "unknownHeaderValue", parameters );
        System.out.println( result );
        assertTrue( "Test if a header is not selected", !result );
    }
}
