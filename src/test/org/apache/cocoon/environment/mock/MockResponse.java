/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.environment.mock;

import java.util.Locale;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.AssertionFailedError;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Response;

public class MockResponse implements Response {

    private String encoding;
    private Locale locale;
    private HashSet cookies = new HashSet();
    private HashMap header = new HashMap();

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

    public Cookie createCookie(String name, String value) {
        MockCookie cookie = new MockCookie();
        cookie.setName(name);
        cookie.setValue(value);
        return cookie;
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public Set getCookies() {
        return cookies;
    }

    public boolean containsHeader(String name) {
        return header.containsKey(name);
    }

    public String encodeURL(String url) {
        throw new AssertionFailedError("Not implemented");
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
}
