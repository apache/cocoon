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


public class RegexpRequestParameterSelectorTestCase extends SitemapComponentTestCase {

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
        TestSuite suite = new TestSuite(RegexpRequestParameterSelectorTestCase.class);
        return suite;
    }
    
    /**
     * A simple regexp-request-parameter selector test
     */
    public void testRegexpRequestParameterSelectEmpty() throws Exception {
        // create a request parameter
        getRequest().addParameter( "parameterRegexpRequestParameterSelector", "" );
        
        Parameters parameters = new Parameters();
        boolean result;
        
        result = this.select( "regexp-request-parameter", "empty", parameters );
        System.out.println( result );
        assertTrue( "Test is regexp-request-parameter selects successfully", result );
        
        result = this.select( "regexp-request-parameter", "number", parameters );
        System.out.println( result );
        assertTrue( "Test is regexp-request-parameter does not select successfully", !result );
        
        result = this.select( "regexp-request-parameter", "non-defined-name", parameters );
        System.out.println( result );
        assertTrue( "Test is regexp-request-parameter does not select successfully", !result );
    }

    /**
     * A simple regexp-request-parameter selector test
     */
    public void testRegexpRequestParameterSelectNumber() throws Exception {
        Parameters parameters = new Parameters();
        boolean result;
        
        // test w/o set request parameter
        result = this.select( "regexp-request-parameter", "number", parameters );
        System.out.println( result );
        assertTrue( "Test is regexp-request-parameter does not select successfully", !result );
        
        // create a request parameter
        getRequest().addParameter( "parameterRegexpRequestParameterSelector1", "123" );

        // override configured parameter name
        parameters.setParameter( "parameter-name", "parameterRegexpRequestParameterSelector1" );
        
        result = this.select( "regexp-request-parameter", "number", parameters );
        System.out.println( result );
        assertTrue( "Test is regexp-request-parameter does not selects successfully", result );

        result = this.select( "regexp-request-parameter", "non-defined-name", parameters );
        System.out.println( result );
        assertTrue( "Test is regexp-request-parameter does not select successfully", !result );
    }
}
