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

/**
 * Test case for RegexpHeaderSelector.
 * 
 * @version CVS $Id$
 */
public class RegexpHeaderSelectorTestCase extends SitemapComponentTestCase {

    private static final String REGEXP_HEADER_SELECTOR = "regexp-header";

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
        TestSuite suite = new TestSuite(RegexpHeaderSelectorTestCase.class);
        return suite;
    }
    

    /**
     * A simple regexp-header selector test
     */
    public void testRegexpHeaderSelectEmpty() throws Exception {
        // create a header
        final String headerName = "headerSelectorTestCase";
        getRequest().setHeader(headerName, "");
        
        Parameters parameters = new Parameters();
        boolean result;
        
        result = this.select( REGEXP_HEADER_SELECTOR, "empty", parameters );
        System.out.println( result );
        assertTrue( "Test is " + REGEXP_HEADER_SELECTOR + " selects successfully", result );
        
        result = this.select( REGEXP_HEADER_SELECTOR, "number", parameters );
        System.out.println( result );
        assertTrue( "Test is " + REGEXP_HEADER_SELECTOR + " does not select successfully", !result );
        
        result = this.select( REGEXP_HEADER_SELECTOR, "non-defined-name", parameters );
        System.out.println( result );
        assertTrue( "Test is " + REGEXP_HEADER_SELECTOR + " does not select successfully", !result );
    }

    /**
     * A simple regexp-header selector test
     */
    public void testRegexpHeaderSelectNumber() throws Exception {
        // create a header
        final String headerName = "headerSelectorTestCase";
        final String headerName2 = "headerSelectorTestCase1";

        Parameters parameters = new Parameters();
        boolean result;
        
        // test w/o set request parameter
        result = this.select( REGEXP_HEADER_SELECTOR, "empty", parameters );
        System.out.println( result );
        assertTrue( "Test is " + REGEXP_HEADER_SELECTOR + " does not select successfully", !result );

	// this time, set the header
        getRequest().setHeader(headerName, "");

	// create another header
        getRequest().setHeader(headerName2, "123");

        // override configured header name
        parameters.setParameter( "header-name", headerName2 );
        
        result = this.select( REGEXP_HEADER_SELECTOR, "empty", parameters );
        System.out.println( result );
        assertTrue( "Test is " + REGEXP_HEADER_SELECTOR + " does not select successfully", !result );

        result = this.select( REGEXP_HEADER_SELECTOR, "number", parameters );
        System.out.println( result );
        assertTrue( "Test is " + REGEXP_HEADER_SELECTOR + " selects successfully", result );

        result = this.select( REGEXP_HEADER_SELECTOR, "non-defined-name", parameters );
        System.out.println( result );
        assertTrue( "Test is " + REGEXP_HEADER_SELECTOR + " does not select successfully", !result );
    }
}
