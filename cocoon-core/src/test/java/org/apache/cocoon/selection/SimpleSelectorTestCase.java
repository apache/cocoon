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


public class SimpleSelectorTestCase extends SitemapComponentTestCase {

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
        TestSuite suite = new TestSuite(SimpleSelectorTestCase.class);
        return suite;
    }
    
    /**
     * A simple parameter select test
     */
    public void testSimpleSelect() throws Exception {
        final String value = "simpleSelectorTestCase";
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "value", value );
        boolean result;
        
        // test selection success
        result = this.select( "simple", value, parameters );
        System.out.println( result );
        assertTrue( "Test if a parameter is selected", result );
        
        // test selection failure
        result = this.select( "simple", "unknownValue", parameters );
        System.out.println( result );
        assertTrue( "Test if a parameter is not selected", !result );
    }

    /**
     * A simple parameter select test
     */
    public void testParameterSelectUndefined() throws Exception {
        final String value = "valueSelectorTestCase";
        
        Parameters parameters = new Parameters();
        boolean result;
        
        // test selection fails
        result = this.select( "simple", value, parameters );
        System.out.println( result );
        assertTrue( "Test if a parameter is not selected", !result );

        parameters.setParameter( "value", "some-value" );
        // test selection fails
        result = this.select( "simple", value, parameters );
        System.out.println( result );
        assertTrue( "Test if a parameter is not selected", !result );
    }
}
