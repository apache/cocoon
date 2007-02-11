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
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;


public class RequestAttributeMatcherTestCase extends SitemapComponentTestCase {
    
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
        TestSuite suite = new TestSuite(RequestAttributeMatcherTestCase.class);
        return suite;
    }
    
    /**
     * A simple request-attribute matcher test
     */
    public void testRequestAttributeMatch() throws Exception {
        // create a request attribute
        final String requestAttributeName = "requestAttributeMatchTestCase";
        final String requestAttributeValue = "requestAttributeMatchTestCaseValue";
        getRequest().setAttribute( requestAttributeName, requestAttributeValue );
        
        Parameters parameters = new Parameters();

        Map result = match("request-attribute", requestAttributeName, parameters);
        System.out.println(result);
        assertNotNull("Test if request-attribute entry exists", result);
        assertEquals("Test for request-attribute " + requestAttributeName + " having value " + requestAttributeValue, requestAttributeValue, result.get("1"));
    }
    
    /**
     * A simple request-attribute matcher test
     */
    public void testHeaderMatchFails() throws Exception {
        final String requestAttributeNameDoesNotExist = "requestAttributeDoesNotExist";
        
        // create a request attribute
        final String requestAttributeName = "requestAttributeMatchTestCase";
        final String requestAttributeValue = "requestAttributeMatchTestCaseValue";
        getRequest().setAttribute( requestAttributeName, requestAttributeValue );
        
        
        Parameters parameters = new Parameters();

        Map result = match("request-attribute", requestAttributeNameDoesNotExist, parameters);
        assertNull( "Test if request-attribute entry " + requestAttributeNameDoesNotExist + " does not exist", result );
    }
}
