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
package org.apache.cocoon.environment.commandline;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Response;

import java.util.Locale;

/**
 * Creates a specific servlet response simulation from command line usage.
 *
 * @version $Id$
 */
public class CommandLineResponse implements Response {

    public String getCharacterEncoding() { return null; }
    public Cookie createCookie(String name, String value) { return null; }
    public void addCookie(Cookie cookie) {}
    public boolean containsHeader(String name) { return false; }
    public void setHeader(String name, String value) {}
    public void setIntHeader(String name, int value) {}
    public void setDateHeader(String name, long date) {}
    public String encodeURL (String url) { return url; }
    public void setLocale(Locale locale) { }
    public Locale getLocale() { return null; }
    public void addDateHeader(String name, long date) { }
    public void addHeader(String name, String value) { }
    public void addIntHeader(String name, int value) { }
}
