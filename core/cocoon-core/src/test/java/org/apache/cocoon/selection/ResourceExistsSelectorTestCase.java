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


public class ResourceExistsSelectorTestCase extends SitemapComponentTestCase {

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
        TestSuite suite = new TestSuite(ResourceExistsSelectorTestCase.class);
        return suite;
    }
    
    /**
     * A resource-exists parameter select test
     */
    public void testResourceExistsSelect() throws Exception {
        
        Parameters parameters = new Parameters();
        boolean result;
        String expression = "";

        // test selection success
        expression = "resource://org/apache/cocoon/selection/ResourceExistsSelectorTestCase.class";
        result = this.select( "resource-exists", expression, parameters );
        System.out.println( result );
        assertTrue( "Test if a exisitng resource is selected", result );
        
        // test selection failure
        expression = "resource://org/apache/cocoon/selection/NonExistingResource.class";
        result = this.select( "resource-exists", expression, parameters );
        System.out.println( result );
        assertTrue( "Test if a non exisitng resource is not selected", !result );
        
    }

    /**
     * A resource-exists parameter select test using the parameter prefix option
     */
    public void testResourceExistsSelectPrefix() throws Exception {
        
        Parameters parameters = new Parameters();
        final String prefix = "resource://org/apache/cocoon/selection/";
        parameters.setParameter( "prefix", prefix );
        boolean result;
        String expression = "";

        // test selection success
        expression = "ResourceExistsSelectorTestCase.class";
        result = this.select( "resource-exists", expression, parameters );
        System.out.println( result );
        assertTrue( "Test if a exisitng resource is selected", result );
        
        // test selection failure
        expression = "NonExistingResource.class";
        result = this.select( "resource-exists", expression, parameters );
        System.out.println( result );
        assertTrue( "Test if a non exisitng resource is not selected", !result );
        
    }
}
