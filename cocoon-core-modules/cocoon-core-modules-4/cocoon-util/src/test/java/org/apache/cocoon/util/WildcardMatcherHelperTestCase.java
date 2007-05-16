/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
        assertNull(result.get("1"));
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

    public void test21WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("*/**", "samples/blocks/");
        assertNotNull(result);
        assertEquals("samples", result.get("1"));
        assertEquals("blocks/", result.get("2"));
    }

    public void test22WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("*/**", "samples/");
        assertNotNull(result);
        assertEquals("samples", result.get("1"));
        assertEquals("", result.get("2"));
    }

    public void test23WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("**favicon.ico", "samples/");
        assertNull(result);
    }

    public void test24WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("**favicon.ico", "samples1234/");
        assertNull(result);
    }

    public void test25WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("**favicon.ico", "samples123/");
        assertNull(result);
    }

    public void test26WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("**/*/**", "foo/bar/baz/bug");
        assertNotNull(result);
        assertEquals("foo/bar", result.get("1"));
        assertEquals("baz", result.get("2"));
        assertEquals("bug", result.get("3"));
    }

    public void test27WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("end*end*end*end", "endXXendYendend");
        assertNotNull(result);
        assertEquals("XX", result.get("1"));
        assertEquals("Y", result.get("2"));
        assertEquals("", result.get("3"));
    }

    public void test28WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("end*end*end*end", "endendendend");
        assertNotNull(result);
        assertEquals("", result.get("1"));
        assertEquals("", result.get("2"));
        assertEquals("", result.get("3"));
    }

    public void test29WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("end**end**end**end", "endXXendYendend");
        assertNotNull(result);
        assertEquals("XX", result.get("1"));
        assertEquals("Y", result.get("2"));
        assertEquals("", result.get("3"));
    }

    public void test30WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("end**end**end**end", "endendendend");
        assertNotNull(result);
        assertEquals("", result.get("1"));
        assertEquals("", result.get("2"));
        assertEquals("", result.get("3"));
    }

    public void test31WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("*/", "test/foo/bar");
        assertNull(result);
    }

    public void test32WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("**/*.html", "foo/bar/baz.html");
        assertNotNull(result);
        assertEquals("baz", result.get("2"));
        assertEquals("foo/bar", result.get("1"));
    }

    public void test33WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("*.html", "menu/baz.html");
        assertNull(result);
    }

    public void test34WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("*.html", "baz.html");
        assertNotNull(result);
        assertEquals("baz", result.get("1"));
    }

    public void test35WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("menu/**/foo_*_bar.*", "menu//foo_baz_bar.html");
        assertNotNull(result);
        assertEquals("", result.get("1"));
        assertEquals("baz", result.get("2"));
        assertEquals("html", result.get("3"));
    }

    public void test36WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("menu/**/foo/*", "menu/bar/baz.xml");
        assertNull(result);
    }

    public void test37WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("menu/*.xml", "menu/foo/bar.xml");
        assertNull(result);
    }

    public void test38WildcardURIMatch()
    throws Exception {
        Map result = WildcardMatcherHelper.match("\\\\foo\\*\\n\\0\\", "\\foo*\\n\\0\\");
        assertNotNull(result);
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
