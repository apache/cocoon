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
package org.apache.cocoon.environment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.io.InputStream;

import javax.servlet.ServletContext;

/**
 * Defines an interface to provide context information.
 *
 * Since 2.2 this interface extends the {@link ServletContext} interface.
 *
 * @version $Id$
 */
public interface Context extends ServletContext {

    Object getAttribute(String name);

    void setAttribute(String name, Object value);

    void removeAttribute(String name);

    Enumeration getAttributeNames();

    /**
     * Utility method for getting a <code>Map</code> view of the context attributes.
     * Returns a <code>Map</code> with context attributes.
     *
     * @return                a <code>Map</code> containing the context attributes.
     * @since 2.2
     */
    Map getAttributes();

    URL getResource(String path) throws MalformedURLException;

    String getRealPath(String path);

    String getMimeType(String file);

    String getInitParameter(String name);

    InputStream getResourceAsStream(String path);
}
