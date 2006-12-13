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


public class RequestAttributeSelectorTestCase extends SitemapComponentTestCase {

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
        TestSuite suite = new TestSuite(RequestAttributeSelectorTestCase.class);
        return suite;
    }
    
    /**
     * A request-attribute parameter select test
     */
    public void testRequestAttributeSelect() throws Exception {
        final String attributeName = "requestAttributeSelector";
        final String attributeValue = "requestAttributeSelectorValue";
        getRequest().setAttribute( attributeName, attributeValue );        
        Parameters parameters = new Parameters();
        boolean result;
        
        // test selection success
        result = this.select( "request-attribute", attributeValue, parameters );
        System.out.println( result );
        assertTrue( "Test if a request attribtue is selected", result );
        
        // test selection failure
        result = this.select( "request-attribute", "unknownValue", parameters );
        System.out.println( result );
        assertTrue( "Test if a request attribute is not selected", !result );
    }

    /**
     * A request-attribute parameter select test
     */
    public void testRequestAttributeSelectOverridden() throws Exception {
        final String attributeName = "requestAttributeSelector1";
        final String attributeValue = "requestAttributeSelectorValue1";
        getRequest().setAttribute( attributeName, attributeValue );
        
        final String attributeNameOverridden = "requestAttributeSelector";
        final String attributeValueOverridden = "requestAttributeSelectorValue";
        getRequest().setAttribute( attributeNameOverridden, attributeValueOverridden );
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "attribute-name", attributeName );
        boolean result;
        
        // test selection success
        result = this.select( "request-attribute", attributeValue, parameters );
        System.out.println( result );
        assertTrue( "Test if a requst attribtue is selected", result );
        
        // test selection failure
        result = this.select( "request-attribute", attributeValueOverridden, parameters );
        System.out.println( result );
        assertTrue( "Test if a request attribute is not selected", !result );
    }
}
