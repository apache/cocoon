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

import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.cocoon.environment.mock.MockCookie;

public class CookieSelectorTestCase extends SitemapComponentTestCase {

    private static final String COOKIE_SELECTOR = "cookie";
    
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
        TestSuite suite = new TestSuite(CookieSelectorTestCase.class);
        return suite;
    }
    
    /**
     * A simple cookie select test
     */
    public void testCookieSelect() throws Exception {
        final String cookieName = "cookieSelectorTestCase";
        final String cookieValue = "cookieValue";
        // create a cookie
        // setting name := cookieName, value := cookieValue
        Map cookies = getRequest().getCookieMap();
        MockCookie mockCookie = new MockCookie();
        mockCookie.setName( cookieName);
        mockCookie.setValue( cookieValue );
        cookies.put( cookieName, mockCookie );
        
        Parameters parameters = new Parameters();
        boolean result;
        
        // test selection success
        result = this.select( COOKIE_SELECTOR, cookieValue, parameters );
        System.out.println( result );
        assertTrue( "Test if a cookie is selected", result );
        
        // test selection failure
        result = this.select( COOKIE_SELECTOR, "unknownCookieValue", parameters );
        System.out.println( result );
        assertTrue( "Test if a cookie is not selected", !result );
    }

    /**
     * A simple cookie select test
     */
    public void testCookieSelectUsingParameters() throws Exception {
        final String cookieName = "cookieSelectorTestCase1";
        final String cookieValue = "cookieValue";
        
        // create a cookie
        // setting name := cookieName, value := cookieValue
        Map cookies = getRequest().getCookieMap();
        MockCookie mockCookie = new MockCookie();
        // this cookie shall get selected
        mockCookie.setName( cookieName);
        mockCookie.setValue( cookieValue );
        cookies.put( cookieName, mockCookie );
        
        // this cookie shall be ignored, as its name differs
        // from the parameterized cookie name
        mockCookie = new MockCookie();
        mockCookie.setName( "cookieSelectorTestCase" );
        mockCookie.setValue( "unknownCookieValue" );
        cookies.put( "cookieSelectorTestCase", mockCookie );

        // check the cookie as defined by this parameter, not as
        // defined in the component configuration
        Parameters parameters = new Parameters();
        parameters.setParameter( "cookie-name", cookieName );
        
        boolean result;
        
        // test selection success
        result = this.select( COOKIE_SELECTOR, cookieValue, parameters );
        System.out.println( result );
        assertTrue( "Test if a cookie is selected", result );
        
        // test selection failure
        result = this.select( COOKIE_SELECTOR, "unknownCookieValue", parameters );
        System.out.println( result );
        assertTrue( "Test if a cookie is not selected", !result );
    }
}
