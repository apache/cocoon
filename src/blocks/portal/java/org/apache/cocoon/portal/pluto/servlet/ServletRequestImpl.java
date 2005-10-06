/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

    /** Cache the parameter map. */
    protected Map portletParameterMap;

    /** Request object used for {@link #portletParameterMap}. */
    protected HttpServletRequest cachedRequest;

    /** Original Request. */
    final protected HttpServletRequest originalRequest;

    final protected PortletURLProviderImpl provider;

    protected PortletWindow window;

    public ServletRequestImpl(HttpServletRequest request,
                              PortletURLProviderImpl provider) {
        super(request);
        this.provider = provider;
        this.originalRequest = request;
    }

    public ServletRequestImpl(HttpServletRequest request,
                              PortletURLProviderImpl provider,
                              PortletWindow window) {
        super(request);
        this.provider = provider;
        this.window = window;
        this.originalRequest = request;
    }

    public ServletRequestImpl getRequest(PortletWindow window) {
        return new ServletRequestImpl((HttpServletRequest)this.getRequest(), provider, window);
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
        HttpServletRequest currentRequest = (HttpServletRequest)this.getRequest();
        if ( this.portletParameterMap == null
             || currentRequest != this.cachedRequest ) {
            this.cachedRequest = currentRequest;

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

                // get request params if the wrapped request is not the Cocoon request
                if ( currentRequest == this.originalRequest ) {
                    Enumeration parameters = currentRequest.getParameterNames();
                    while (parameters.hasMoreElements()) {
                        String paramName = (String) parameters.nextElement();
                        String[] paramValues = this.getRequest().getParameterValues(paramName);
                        String[] values = (String[]) this.portletParameterMap.get(paramName);
    
                        if ( !paramName.startsWith("cocoon-") ) {
                            if (values != null) {
                                String[] temp = new String[paramValues.length + values.length];
                                System.arraycopy(paramValues, 0, temp, 0, paramValues.length);
                                System.arraycopy(values, 0, temp, paramValues.length, values.length);
                                paramValues = temp;
                            }
                            this.portletParameterMap.put(paramName, paramValues);
                        }
                    }
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
    /**
     * JST-168 PLT.16.3.3 cxxix
     * @see javax.servlet.ServletRequest#getProtocol()
     */
    public String getProtocol() {
        return null;
    }

    /**
     * JST-168 PLT.16.3.3 cxxix
     * @see javax.servlet.ServletRequest#getRemoteAddr()
     */
    public String getRemoteAddr() {
        return null;
    }

    /**
     * JST-168 PLT.16.3.3 cxxix
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    public String getRemoteHost() {
        return null;
    }

    /**
     * JST-168 PLT.16.3.3 cxxix
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        return null;
    }

    /**
     * JST-168 PLT.16.3.3 cxxx
     * @see javax.servlet.http.HttpServletRequest#getPathInfo()
     */
    public String getPathInfo() {
        String attr = (String)super.getAttribute("javax.servlet.include.path_info");
        return (attr != null) ? attr : super.getPathInfo();
    }

    /**
     * JST-168 PLT.16.3.3 cxxx
     * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
     */
    public String getPathTranslated() {
        // TODO: Don't know yet how to implement this. 
        //       A null value is a valid value. 
        return null;
    }

    /**
     * JST-168 PLT.16.3.3 cxxx
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    public String getQueryString() {
        String attr = (String)super.getAttribute("javax.servlet.include.query_string");
        return (attr != null) ? attr : super.getQueryString();
    }

    /**
     * JST-168 PLT.16.3.3 cxxx
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    public String getRequestURI() {
        String attr = (String)super.getAttribute("javax.servlet.include.request_uri");
        return (attr != null) ? attr : super.getRequestURI();
    }

    /**
     * JST-168 PLT.16.3.3 cxxx
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    public String getServletPath() {
        String attr = (String)super.getAttribute("javax.servlet.include.servlet_path");
        return (attr != null) ? attr : super.getServletPath();
    }

    /**
     * JST-168 PLT.16.3.3 cxxxi
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    public String getContextPath() {
        String attr = (String)super.getAttribute("javax.servlet.include.context_path");
        return (attr != null) ? attr : super.getContextPath();
    }
}
