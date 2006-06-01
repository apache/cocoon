/*
* Copyright 1999-2006 The Apache Software Foundation
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
package org.apache.cocoon.matching.helpers;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Testcase for the WildcardHelper class.
 * @version $Id$
 */
public class WildcardHelperTestCase extends TestCase {

    public void testWildcardURIMatch() throws Exception {
        final Map resultMap = new HashMap();
        final String uri = "test/foo/bla/end";
        final String pattern = "**";
        int[] expr = WildcardHelper.compilePattern(pattern);
        boolean result = WildcardHelper.match(resultMap, uri, expr);
        assertTrue("Test if url matches: " + uri + " - " + pattern, result);
        assertEquals("Test if result matches for {0}", uri, resultMap.get("0"));
        assertEquals("Test if result matches for {1}", uri, resultMap.get("1"));

        resultMap.clear();
        final String pattern2 = "**/bla/*";
        int[] expr2 = WildcardHelper.compilePattern(pattern2);
        boolean result2 = WildcardHelper.match(resultMap, uri, expr2);
        assertTrue("Test if url matches: " + uri + " - " + pattern2, result2);
        assertEquals("Test if result matches for {0}", uri, resultMap.get("0"));
        assertEquals("Test if result matches for {1}", "test/foo", resultMap.get("1"));
        assertEquals("Test if result matches for {2}", "end", resultMap.get("2"));
    }

    public void testWildcardURIMatchSimplePattern() throws Exception {
        final Map resultMap = new HashMap();
        final String uri = "test";
        final String pattern = "*";
        int[] expr = WildcardHelper.compilePattern(pattern);
        boolean result = WildcardHelper.match(resultMap, uri, expr);
        assertTrue("Test if url matches: " + uri + " - " + pattern, result);
        assertEquals("Test if result matches for {0}", uri, resultMap.get("0"));
        assertEquals("Test if result matches for {1}", uri, resultMap.get("1"));
    }

    public void testWildcardURIMatchDoublePattern() throws Exception {
        final Map resultMap = new HashMap();
        final String uri = "test/something.xmlbla.xml";
        final String pattern = "*/*.xml";
        int[] expr = WildcardHelper.compilePattern(pattern);
        boolean result = WildcardHelper.match(resultMap, uri, expr);
        assertTrue("Test if url matches: " + uri + " - " + pattern, result);
        assertEquals("Test if result matches for {0}", uri, resultMap.get("0"));
        assertEquals("Test if result matches for {1}", "test", resultMap.get("1"));
        assertEquals("Test if result matches for {2}", "something.xmlbla", resultMap.get("2"));
    }
}
