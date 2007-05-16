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


public class RequestMethodSelectorTestCase extends SitemapComponentTestCase {

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
        TestSuite suite = new TestSuite(RequestMethodSelectorTestCase.class);
        return suite;
    }
    
    /**
     * A request-method select test
     */
    public void testRequestMethodSelect() throws Exception {
        final String method = "POST";
        getRequest().setMethod( "POST" );
        
        Parameters parameters = new Parameters();
        boolean result;
        
        // test selection success
        result = this.select( "request-method", method, parameters );
        System.out.println( result );
        assertTrue( "Test if a method is selected", result );
        
        // test selection failure
        result = this.select( "request-method", "post", parameters );
        System.out.println( result );
        assertTrue( "Test if a method is not selected", !result );
        
        // test selection failure
        result = this.select( "request-method", "GET", parameters );
        System.out.println( result );
        assertTrue( "Test if a method is not selected", !result );
    }

}
