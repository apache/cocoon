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


public class RequestParameterSelectorTestCase extends SitemapComponentTestCase {

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
        TestSuite suite = new TestSuite(RequestParameterSelectorTestCase.class);
        return suite;
    }
    
    /**
     * A request-parameter parameter select test
     */
    public void testRequestParameterSelect() throws Exception {
        final String parameterName = "requestParameterSelector";
        final String parameterValue = "requestParameterSelectorValue";
        getRequest().addParameter( parameterName, parameterValue );        
        Parameters parameters = new Parameters();
        boolean result;
        
        // test selection success
        result = this.select( "request-parameter", parameterValue, parameters );
        System.out.println( result );
        assertTrue( "Test if a requst parameter is selected", result );
        
        // test selection failure
        result = this.select( "request-parameter", "unknownValue", parameters );
        System.out.println( result );
        assertTrue( "Test if a request parameter is not selected", !result );
    }

    /**
     * A request-parameter parameter select test
     */
    public void testRequestParameterSelectOverridden() throws Exception {
        final String parameterName = "requestParameterSelector1";
        final String parameterValue = "requestParameterSelectorValue1";
        getRequest().addParameter( parameterName, parameterValue );
        
        final String parameterNameOverridden = "requestParameterSelector";
        final String parameterValueOverridden = "requestParameterSelectorValue";
        getRequest().addParameter( parameterNameOverridden, parameterValueOverridden );
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "parameter-name", parameterName );
        boolean result;
        
        // test selection success
        result = this.select( "request-parameter", parameterValue, parameters );
        System.out.println( result );
        assertTrue( "Test if a requst attribtue is selected", result );
        
        // test selection failure
        result = this.select( "request-parameter", parameterValueOverridden, parameters );
        System.out.println( result );
        assertTrue( "Test if a request parameter is not selected", !result );
    }
}
