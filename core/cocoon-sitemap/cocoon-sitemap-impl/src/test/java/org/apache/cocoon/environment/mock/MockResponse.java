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
package org.apache.cocoon.environment.mock;

import java.util.Locale;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.impl.AbstractResponse;

public class MockResponse extends AbstractResponse implements Response {

    private String encoding;
    private Locale locale;
    private Set cookies = new HashSet();
    private Set cocoonCookies = new HashSet();
    private Map header = new HashMap();

    private HttpSession session;
    
    public void setCharacterEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getCharacterEncoding() {
        return encoding;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public javax.servlet.http.Cookie createCookie(String name, String value) {
        javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie(name, value);
        return cookie;
    }

    public void addCookie(javax.servlet.http.Cookie cookie) {
        cookies.add(cookie);
    }

    public Cookie createCocoonCookie(String name, String value) {
        MockCookie cookie = new MockCookie();
        cookie.setName(name);
        cookie.setValue(value);
        return cookie;
    }

    public void addCookie(Cookie cookie) {
        cocoonCookies.add(cookie);
    }

    public Set getCookies() {
        return cookies;
    }

    public boolean containsHeader(String name) {
        return header.containsKey(name);
    }

    public String encodeURL(String url) {
        //throw new AssertionFailedError("Not implemented");
        StringBuffer sb = new StringBuffer();
            sb.append( url );
        if (session != null) {
            sb.append( "?JSESSIONID=");
            sb.append( session.getId() );
        }
        return sb.toString();
    }

    public void setDateHeader(String name, long date) {
        header.put(name, new Long(date));
    }

    public void addDateHeader(String name, long date) {
        header.put(name, new Long(date));
    }

    public void setHeader(String name, String value) {
        header.put(name, value);
    }

    public void addHeader(String name, String value) {
        header.put(name, value);
    }

    public void setIntHeader(String name, int value) {
        header.put(name, new Integer(value));
    }

    public void addIntHeader(String name, int value) {
        header.put(name, new Integer(value));
    }

    public Map getHeader() {
        return header;
    }

    public void reset() {
        encoding = null;
        locale = null;
        cookies.clear();
        header.clear();
    }
    
    public void setSession(HttpSession session) {
        this.session = session;
    }
}
