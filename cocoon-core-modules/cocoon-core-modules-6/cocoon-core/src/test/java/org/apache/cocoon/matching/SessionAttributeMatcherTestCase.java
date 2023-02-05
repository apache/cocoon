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
package org.apache.cocoon.matching;

import java.util.Map;

import javax.servlet.http.HttpSession;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;

public class SessionAttributeMatcherTestCase extends SitemapComponentTestCase {
    
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
        TestSuite suite = new TestSuite(SessionAttributeMatcherTestCase.class);
        return suite;
    }
    
    /**
     * A simple session-attribute matcher test
     */
    public void testSessionAttributeMatch() throws Exception {
        // create a session attribute
        final String sessionAttributeName = "sessionAttributeMatchTestCase";
        final String sessionAttributeValue = "sessionAttributeMatchTestCaseValue";
        HttpSession session = getRequest().getSession(true);
        session.setAttribute(sessionAttributeName, sessionAttributeValue );
        
        Parameters parameters = new Parameters();

        Map result = match("session-attribute", sessionAttributeName, parameters);
        System.out.println(result);
        assertNotNull("Test if session-attribute entry exists", result);
        assertEquals("Test for session-attribute " + sessionAttributeName + " having value " + sessionAttributeValue, sessionAttributeValue, result.get("1"));
    }
    
    /**
     * A simple request-attribute matcher test
     */
    public void testSessionMatchFails() throws Exception {
        final String sessionAttributeNameDoesNotExist = "sessionAttributeDoesNotExist";
        Parameters parameters = new Parameters();

        // test w/o an existing session
        getRequest().clearSession();

        Map result;
        result = match("session-attribute", sessionAttributeNameDoesNotExist, parameters);
        assertNull( "Test if session-attribute entry " + sessionAttributeNameDoesNotExist + " does not exist", result );
        
        // create a session attribute
        final String sessionAttributeName = "sessionAttributeMatchTestCase";
        final String sessionAttributeValue = "sessionAttributeMatchTestCaseValue";
        HttpSession session = getRequest().getSession(true);
        session.setAttribute(sessionAttributeName, sessionAttributeValue );        

        // test w an existing session
        result = match("session-attribute", sessionAttributeNameDoesNotExist, parameters);
        assertNull( "Test if session-attribute entry " + sessionAttributeNameDoesNotExist + " does not exist", result );
    }
}
