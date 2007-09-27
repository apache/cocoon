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
package org.apache.cocoon.servletservice.util;

import junit.framework.TestCase;

/**
 * @version $Id$
 */
public class RequestParametersTestCase extends TestCase {

    public void testParseMultiple() {
        RequestParameters p = new RequestParameters("a=1&a=2&a=3&b=11&b=22");
        String[] a = p.getParameterValues("a");
        assertEquals(3, a.length);
        assertEquals("1", a[0]);
        assertEquals("2", a[1]);
        assertEquals("3", a[2]);

        String[] b = p.getParameterValues("b");
        assertEquals(2, b.length);
        assertEquals("11", b[0]);
        assertEquals("22", b[1]);
    }

    public void testParseEncoded() {
        RequestParameters p = new RequestParameters("a=one+two&b=one%3Ftwo&c=");
        assertEquals("one two", p.getParameter("a"));
        assertEquals("one?two", p.getParameter("b"));
    }

    public void testParseEncodedWrong() {
        try {
            new RequestParameters("a=one%");
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            new RequestParameters("a=one%3");
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            new RequestParameters("a=one%u");
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            new RequestParameters("a=one%u0");
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            new RequestParameters("a=one%u00");
            fail();
        } catch (IllegalArgumentException e) { }
        try {
            new RequestParameters("a=one%u003");
            fail();
        } catch (IllegalArgumentException e) { }
    }

    public void testParseUnicode() {
        final char u = '\u11F3'; // Hangul character

        RequestParameters p = new RequestParameters("a=a%E1%87%B3b&b=a%u11f3b");
        assertEquals("a" + u + "b", p.getParameter("a"));
        assertEquals("a" + u + "b", p.getParameter("b"));
    }

    public void testParseWSDL() {
        assertEquals("", new RequestParameters("foo").getParameter("foo"));
    }
}
