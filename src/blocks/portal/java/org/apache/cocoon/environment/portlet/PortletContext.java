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
package org.apache.cocoon.environment.portlet;

import org.apache.cocoon.environment.Context;

import org.apache.avalon.framework.CascadingRuntimeException;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Implements the {@link org.apache.cocoon.environment.Context} interface
 * for the JSR-168 (Portlet) environment.
 *
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: PortletContext.java,v 1.2 2004/03/05 13:02:08 bdelacretaz Exp $
 */
public final class PortletContext implements Context {

    /**
     * The PortletContext
     */
    private final javax.portlet.PortletContext context;

    /**
     * Constructs a PortletContext object from a PortletContext object
     */
    public PortletContext(javax.portlet.PortletContext context) {
        this.context = context;
    }

    public Object getAttribute(String name) {
        return context.getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        context.setAttribute(name, value);
    }

    public void removeAttribute(String name) {
        context.removeAttribute(name);
    }

    public Enumeration getAttributeNames() {
        return context.getAttributeNames();
    }

    public URL getResource(String path) throws MalformedURLException {
        return context.getResource(path);
    }

    public InputStream getResourceAsStream(String path) {
        return context.getResourceAsStream(path);
    }

    public String getRealPath(String path) {
        if (path.equals("/")) {
            String value = context.getRealPath(path);
            if (value == null) {
                // Try to figure out the path of the root from that of WEB-INF
                try {
                    value = this.context.getResource("/WEB-INF").toString();
                } catch (MalformedURLException mue) {
                    throw new CascadingRuntimeException("Cannot determine the base URL for " + path, mue);
                }
                value = value.substring(0, value.length() - "WEB-INF".length());
            }
            return value;
        }
        return context.getRealPath(path);
    }

    public String getMimeType(String file) {
        return context.getMimeType(file);
    }

    public String getInitParameter(String name) {
        return context.getInitParameter(name);
    }


    // PortletContext methods

    public Enumeration getInitParameterNames() {
        return context.getInitParameterNames();
    }

    public int getMajorVersion() {
        return context.getMajorVersion();
    }

    public int getMinorVersion() {
        return context.getMinorVersion();
    }

    public String getPortletContextName() {
        return context.getPortletContextName();
    }

    public String getServerInfo() {
        return context.getServerInfo();
    }
}
