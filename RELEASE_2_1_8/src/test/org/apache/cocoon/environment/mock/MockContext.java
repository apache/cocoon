/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.environment.mock;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.InputStream;

import org.apache.cocoon.environment.Context;

public class MockContext implements Context {

    private Hashtable attributes = new Hashtable();
    private Hashtable resources = new Hashtable();
    private Hashtable mappings = new Hashtable();
    private Hashtable initparameters = new Hashtable();

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public Enumeration getAttributeNames() {
        return attributes.keys();
    }

    public void setResource(String path, URL url) {
        resources.put(path, url);
    }

    public URL getResource(String path) throws MalformedURLException {
        return (URL)resources.get(path);
    }

    public String getRealPath(String path) {
      return path;
    }

    public String getMimeType(String file) {
        return (String)mappings.get(file.substring(file.lastIndexOf(".")+1)); 
    }

    public void setInitParameter(String name, String value) {
        initparameters.put(name, value);
    }

    public String getInitParameter(String name) {
        return (String)initparameters.get(name);
    }

    public InputStream getResourceAsStream(String path) {
        return null;
    }

    public void reset() {
        attributes.clear();
        resources.clear();
        mappings.clear();
        initparameters.clear();
    }
}
