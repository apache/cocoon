/*
 * Copyright 1999-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.util;

import java.util.Map;

import junit.framework.TestCase;


/**
 * @version $Id$
 */
public class WildcardMatcherHelperTestCase
    extends TestCase {

    //~ Methods ------------------------------------------------------------------------------------

    public void test01WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("test", "test");
        assertNotNull(result);
        assertEquals("test", result.get("0"));
    }

    public void test02WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("end", "enp");
        assertNull(result);
    }

    public void test03WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("/t\\*d", "/t*d");
        assertNotNull(result);
        assertEquals("/t*d", result.get("0"));
    }

    public void test04WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("\\*d", "*d");
        assertNotNull(result);
        assertEquals("*d", result.get("0"));
    }

    public void test05WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("**", "*d");
        assertNotNull(result);
        assertEquals("*d", result.get("0"));
        assertEquals("*d", result.get("1"));
    }

    public void test06WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("foo**", "foo*d");
        assertNotNull(result);
        assertEquals("foo*d", result.get("0"));
        assertEquals("*d", result.get("1"));
    }

    public void test07WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("end", "en");
        assertNull(result);
    }

    public void test08WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("en", "end");
        assertNull(result);
    }

    public void test09WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("end**", "end");
        assertNotNull(result);
        assertEquals("", result.get("1"));
    }

    public void test10WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("end**end", "endendend");
        assertNotNull(result);
        assertEquals("end", result.get("1"));
    }

    public void test11WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("end**end", "endxxend");
        assertNotNull(result);
        assertEquals("xx", result.get("1"));
    }

    public void test12WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("*/end", "xx/end");
        assertNotNull(result);
        assertEquals("xx", result.get("1"));
    }

    public void test13WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("ab/cd*/end", "ab/cdxx/end");
        assertNotNull(result);
        assertEquals("xx", result.get("1"));
    }

    public void test14WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("a*/cd*/end", "ab/cdxx/end");
        assertNotNull(result);
        assertEquals("b", result.get("1"));
        assertEquals("xx", result.get("2"));
    }

    public void test15WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("a**/cd*/end", "ab/yy/cdxx/end");
        assertNotNull(result);
        assertEquals("b/yy", result.get("1"));
        assertEquals("xx", result.get("2"));
    }

    public void test16WildcardURIMatch()
        throws Exception { 
        Map result = WildcardMatcherHelper.match("a**/cd*/end/*", "ab/yy/cdxx/end/foobar/ii");
        assertNull(result);
    }

    public void test17WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("a**/cd*/end/**", "ab/yy/cdxx/end/foobar/ii");
        assertNotNull(result);
        assertEquals("b/yy", result.get("1"));
        assertEquals("xx", result.get("2"));
        assertEquals("foobar/ii", result.get("3"));
    }

    public void test18WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("a**cd*/end/**", "ab/yy/cdxx/end/foobar/ii");
        assertNotNull(result);
        assertEquals("b/yy/", result.get("1"));
        assertEquals("xx", result.get("2"));
        assertEquals("foobar/ii", result.get("3"));
    }

    public void test19WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("*/*.xml", "test/something.xmlbla.xml");
        assertNotNull(result);
        assertEquals("test", result.get("1"));
        assertEquals("something.xmlbla", result.get("2"));
    }

    public void test20WildcardURIMatch()
        throws Exception {
        Map result = WildcardMatcherHelper.match("ab/cd*/end", "ab/cd/end");
        assertNotNull(result);
        assertEquals("", result.get("1"));
    }

    public void testEmptyPattern() throws Exception {
        assertNotNull(WildcardMatcherHelper.match("", ""));
        assertNull(WildcardMatcherHelper.match("", "foo"));
        assertNull(WildcardMatcherHelper.match("", "foo/bar"));
    }

    public void testEndPattern() throws Exception {
        assertNotNull(WildcardMatcherHelper.match("*/", "foo/"));
        assertNull(WildcardMatcherHelper.match("*/", "foo/bar/"));
        assertNull(WildcardMatcherHelper.match("*/", "test/foo/bar/"));
    }
}
