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

import javax.servlet.http.Cookie;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;

public class CookieMatcherTestCase extends SitemapComponentTestCase {
    
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
        TestSuite suite = new TestSuite(CookieMatcherTestCase.class);
        return suite;
    }
    
    /**
     * A simple cookie matcher test
     */
    public void testCookieMatch() throws Exception {
        // create a cookie
        // setting name := cookieName, value := cookieValue
        Map cookies = getRequest().getCookieMap();
        Cookie mockCookie = new Cookie("cookieName", "cookieValue");
        cookies.put( "cookieName", mockCookie );
        
        Parameters parameters = new Parameters();

        Map result = match("cookie", "cookieName", parameters);
        System.out.println(result);
        assertNotNull("Test if cookie exists", result);
        assertEquals("Test for cookie cookieName having value cookieValue", "cookieValue", result.get("1"));
    }
    
    /**
     * A simple cookie matcher test
     */
    public void testCookieMatchFails() throws Exception {
        // create a cookie
        // setting name := cookieName, value := cookieValue
        Map cookies = getRequest().getCookieMap();
        Cookie mockCookie = new Cookie("cookieName", "cookieValue");
        cookies.put( "cookieName", mockCookie );
        
        Parameters parameters = new Parameters();
        
        Map result = match( "cookie", "cookieNameDoesNotExists", parameters );
        System.out.println(result);
        assertNull( "Test if cookie does not exist", result );
    }

    protected boolean addSourceFactories() {
        return true;
    }
    
    protected boolean addSourceResolver() {
        return true;
    }

}
