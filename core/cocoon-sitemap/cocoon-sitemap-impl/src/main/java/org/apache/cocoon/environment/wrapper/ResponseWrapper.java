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
package org.apache.cocoon.environment.wrapper;

import java.util.Locale;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.impl.AbstractResponse;

/**
 * This is a wrapper class for the Response object.
 * It contains the same properties as the wrapped instance
 * but swallows calls that would modify response headers.
 */
public class ResponseWrapper extends AbstractResponse implements Response {

    private Response res;
    
    public ResponseWrapper(Response response) {
        this.res = response;
    }

    public String getCharacterEncoding() {
        return res.getCharacterEncoding();
    }

    public void setLocale(Locale loc) {
        res.setLocale(loc);
    }

    public Locale getLocale() {
        return res.getLocale();
    }

    public javax.servlet.http.Cookie createCookie(String name, String value) {
        return res.createCookie(name, value);
    }

    public void addCookie(javax.servlet.http.Cookie cookie) {
        res.addCookie(cookie);
    }

    public Cookie createCocoonCookie(String name, String value) {
        return res.createCocoonCookie(name, value);
    }

    public void addCookie(Cookie cookie) {
        res.addCookie(cookie);
    }

    public String encodeURL(String url) {
        return res.encodeURL(url);
    }

    public boolean containsHeader(String name) {
        return res.containsHeader(name);
    }

    public void setDateHeader(String name, long date) {
    }

    public void addDateHeader(String name, long date) {
    }

    public void setHeader(String name, String value) {
    }

    public void addHeader(String name, String value) {
    }

    public void setIntHeader(String name, int value) {
    }

    public void addIntHeader(String name, int value) {
    }

}
