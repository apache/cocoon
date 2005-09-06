/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto.servlet;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.cocoon.portal.pluto.PortletURLProviderImpl;
import org.apache.pluto.om.window.PortletWindow;

/**
 * Our request wrapper
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id$
 */
public class ServletRequestImpl extends HttpServletRequestWrapper {

    /** Cache the parameter map */
    protected Map portletParameterMap;

    final protected PortletURLProviderImpl provider;

    protected PortletWindow window;

    public ServletRequestImpl(HttpServletRequest request,
                              PortletURLProviderImpl provider) {
        super(request);
        this.provider = provider;
    }

    public ServletRequestImpl(HttpServletRequest request,
                              PortletURLProviderImpl provider,
                              PortletWindow window) {
        super(request);
        this.provider = provider;
        this.window = window;
    }

    public ServletRequestImpl getRequest(PortletWindow window) {
        return new ServletRequestImpl((HttpServletRequest)this.getRequest(), provider, window);
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    public String getQueryString() {
        return null;
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        return null;
    }

    /**
     * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding(String arg0)
    throws UnsupportedEncodingException {
        //this.request.setCharacterEncoding(arg0);
    }

    /**
     * @see javax.servlet.ServletRequest#getContentType()
     */
    public String getContentType() {
        String contentType = "text/html";
        if (getCharacterEncoding() != null) {
            contentType += ";" + getCharacterEncoding();
        }
        return contentType;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    public String getParameter(String name) {
        final String[] values = (String[])this.getParameterMap().get(name);
        if ( values != null && values.length > 0 ) {
            return values[0];
        }
        return null;

    }

    /**
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        // TODO - readd caching
            //get control params
            this.portletParameterMap = new HashMap();

            if (this.provider != null
                && this.provider.getPortletWindow().equals(this.window)) {
                // get render parameters
                Iterator i = this.provider.getParameters().entrySet().iterator();
                while (i.hasNext()) {
                    Map.Entry entry = (Map.Entry) i.next();
                    // convert simple values to string arrays
                    if (entry.getValue() instanceof String) {
                        this.portletParameterMap.put(
                            entry.getKey(),
                            new String[] {(String) entry.getValue()});
                    } else {
                        this.portletParameterMap.put(
                            entry.getKey(),
                            entry.getValue());
                    }
                }

                //get request params
            Enumeration parameters = this.getRequest().getParameterNames();
                while (parameters.hasMoreElements()) {
                    String paramName = (String) parameters.nextElement();
                String[] paramValues = this.getRequest().getParameterValues(paramName);
                String[] values = (String[]) this.portletParameterMap.get(paramName);

                if ( !paramName.startsWith("cocoon-") ) {
                    if (values != null) {
                        String[] temp =
                            new String[paramValues.length + values.length];
                        System.arraycopy(
                            paramValues,
                            0,
                            temp,
                            0,
                            paramValues.length);
                        System.arraycopy(
                            values,
                            0,
                            temp,
                            paramValues.length,
                            values.length);
                        paramValues = temp;
                    }
                    this.portletParameterMap.put(paramName, paramValues);
                }
            }
        }

        return this.portletParameterMap;
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return Collections.enumeration(this.getParameterMap().keySet());
    }

    /**
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {
        return (String[]) this.getParameterMap().get(name);
    }
}
