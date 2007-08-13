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

import javax.servlet.http.HttpSession;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;

public class SessionAttributeSelectorTestCase extends SitemapComponentTestCase {

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
        TestSuite suite = new TestSuite(SessionAttributeSelectorTestCase.class);
        return suite;
    }
    
    /**
     * A session-attribute select test
     */
    public void testSessionAttributeSelect() throws Exception {
        final String attributeName = "sessionAttributeSelector";
        final String attributeValue = "sessionAttributeSelectorValue";
        
        HttpSession session = getRequest().getSession(true);
        session.setAttribute( attributeName, attributeValue );        
        Parameters parameters = new Parameters();
        boolean result;
        
        // test selection success
        result = this.select( "session-attribute", attributeValue, parameters );
        System.out.println( result );
        assertTrue( "Test if a session attribtue is selected", result );
        
        // test selection failure
        result = this.select( "session-attribute", "unknownValue", parameters );
        System.out.println( result );
        assertTrue( "Test if a session attribute is not selected", !result );
    }

    /**
     * A session-attribute select test
     */
    public void testSessionAttributeSelectOverridden() throws Exception {
        final String attributeName = "sessionAttributeSelector1";
        final String attributeValue = "sessionAttributeSelectorValue1";
        HttpSession session = getRequest().getSession(true);
        session.setAttribute( attributeName, attributeValue );        
        
        final String attributeNameOverridden = "sessionAttributeSelector";
        final String attributeValueOverridden = "sessionAttributeSelectorValue";
        session.setAttribute( attributeNameOverridden, attributeValueOverridden );
        
        Parameters parameters = new Parameters();
        parameters.setParameter( "attribute-name", attributeName );
        boolean result;
        
        // test selection success
        result = this.select( "session-attribute", attributeValue, parameters );
        System.out.println( result );
        assertTrue( "Test if a requst attribtue is selected", result );
        
        // test selection failure
        result = this.select( "session-attribute", attributeValueOverridden, parameters );
        System.out.println( result );
        assertTrue( "Test if a session attribute is not selected", !result );
    }
    
    /**
     * A session-attribute select test
     */
    public void testSessionAttributeSelectMissingSession() throws Exception {
        final String attributeValue = "sessionAttributeSelectorValue";

        // test w/o session
        getRequest().clearSession();
        
        Parameters parameters = new Parameters();
        boolean result;
        
        // test selection fails
        result = this.select( "session-attribute", attributeValue, parameters );
        System.out.println( result );
        assertTrue( "Test if a session attribtue is not selected", !result );
    }
}
