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
package org.apache.cocoon.components.deli;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.xml.dom.DOMParser;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.util.AbstractLogEnabled;

import com.hp.hpl.deli.Profile;
import com.hp.hpl.deli.ProfileAttribute;
import com.hp.hpl.deli.Workspace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Allows the use of <a href="http://www-uk.hpl.hp.com/people/marbut/">DELI</a>
 * to provide <a href="http://www.w3.org/Mobile/CCPP/">CC/PP</a> or
 * <a href="http://www1.wapforum.org/tech/terms.asp?doc=WAP-248-UAProf-20010530-p.pdf">UAProf</a>
 * support. For more details of DELI see the Technical Report
 * <a href="http://www-uk.hpl.hp.com/people/marbut/DeliUserGuideWEB.htm">DELI:
 * A Delivery Context Library for CC/PP and UAProf</a>.
 *
 * @version $Id$
 */
public final class DeliImpl extends AbstractLogEnabled
                            implements Parameterizable, Deli, Serviceable, Disposable,
                                       Initializable, ThreadSafe, Contextualizable {

    /** The name of the main DELI configuration file */
    private String deliConfig = "deli/config/deliConfig.xml";

    /** The service manager */
    protected ServiceManager manager = null;

    /** Parser used to construct the DOM tree to import the profile to a stylesheet */
    protected DOMParser parser;

    /** A context, used to retrieve the path to the configuration file */
    protected CocoonServletContext servletContext;

    /** Contextualize this class */
    public void contextualize(Context context) throws ContextException {
        org.apache.cocoon.environment.Context ctx =
            (org.apache.cocoon.environment.Context)context.get(
                Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        this.servletContext = new CocoonServletContext(ctx);
    }

    /** Service this class */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        try {
            this.parser = (DOMParser)this.manager.lookup(DOMParser.ROLE);
        } catch (ServiceException e) {
            getLogger().error("DELI Exception while creating parser: ", e);
            throw e;
        }
    }

    /** Configure this class */
    public void parameterize(Parameters params) {
        this.deliConfig = params.getParameter("deli-config-file", this.deliConfig);
    }

    /**
     * Initialize
     */
    public void initialize() throws Exception {
        try {
            Workspace.getInstance().configure(this.servletContext, this.deliConfig);
        } catch (Exception e) {
            getLogger().error("DELI Exception while creating workspace: ", e);
            throw e;
        }
    }

    /** Dispose of this class */
    public void dispose() {
        if (parser != null) {
            this.manager.release(parser);
        }
        this.parser = null;
    }

    /** Process a HttpServletRequest and either extract
     *  CC/PP or UAProf information from it and use this information
     *  to resolve a profile or examine the user agent string, match
     *  this using the DELI legacy device database, and use this
     *  information to retrieve the appropriate CC/PP profile.
     *
     * @param	theRequest	The Request.
     * @return	The profile as a vector of profile attributes.
     * @throws	IOException
     * @throws	ServletException
     * @throws	Exception
     */
    public Profile getProfile(Request theRequest) throws IOException, ServletException, Exception {
        try {
            CocoonServletRequest servletRequest = new CocoonServletRequest(theRequest);
            return new Profile(servletRequest);
        } catch (Exception e) {
            getLogger().error("DELI Exception while retrieving profile: ", e);
            throw e;
        }
    }

    /** Convert a profile stored as a vector of profile attributes
     *  to a DOM tree.
     *
     * @param	theProfile	The profile as a vector of profile attributes.
     * @return	The DOM tree.
     */
    public Document getUACapabilities(Profile theProfile) throws Exception {
        Document document;
        try {
            Element rootElement;
            Element attributeNode;
            Element complexAttributeNode;
            Text text;

            document = parser.createDocument();
            rootElement = document.createElementNS(null, "browser");
            document.appendChild(rootElement);

            Iterator i = theProfile.iterator();
            while (i.hasNext()) {
                ProfileAttribute p = (ProfileAttribute) i.next();
                attributeNode = document.createElementNS(null, p.getAttribute());
                rootElement.appendChild(attributeNode);
                Vector attributeValue = p.get();
                if (attributeValue != null) {
                    Iterator complexValueIter = attributeValue.iterator();
                    if (p.getCollectionType().equals("Simple")) {
                        // Simple attribute
                        String value = (String)complexValueIter.next();
                        text = document.createTextNode(value);
                        attributeNode.appendChild(text);
                    } else {
                        // Complex attribute e.g. Seq or Bag
                        while (complexValueIter.hasNext()) {
                            String value = (String)complexValueIter.next();
                            complexAttributeNode = document.createElementNS(null, "li");
                            attributeNode.appendChild(complexAttributeNode);
                            text = document.createTextNode(value);
                            complexAttributeNode.appendChild(text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            getLogger().error("DELI Exception while converting profile to DOM fragment: ", e);
            throw e;
        }
        return document;
    }

    public Document getUACapabilities(Request theRequest)
    throws IOException, Exception {
        return this.getUACapabilities(this.getProfile(theRequest));
    }

    /**
     * Stub implementation of Servlet Context
     */
    public class CocoonServletContext implements ServletContext {

        org.apache.cocoon.environment.Context envContext;

        public CocoonServletContext(org.apache.cocoon.environment.Context context) {
            this.envContext = context;
        }

        public Object getAttribute(String name) { return envContext.getAttribute(name); }
        public void setAttribute(String name, Object value) { envContext.setAttribute(name, value); }
        public Enumeration getAttributeNames() { return envContext.getAttributeNames(); }
        public java.net.URL getResource(String path) throws MalformedURLException { return envContext.getResource(path); }
        public String getRealPath(String path) { return envContext.getRealPath(path); }
        public String getMimeType(String file) { return envContext.getMimeType(file); }
        public String getInitParameter(String name) { return envContext.getInitParameter(name); }
        public java.io.InputStream getResourceAsStream(String path) { return envContext.getResourceAsStream(path); }

        public ServletContext getContext(String uripath) { return (null); }
        public Enumeration getInitParameterNames() { return (null); }
        public int getMajorVersion() { return (2); }
        public int getMinorVersion() { return (3); }
        public RequestDispatcher getNamedDispatcher(String name) { return (null); }
        public RequestDispatcher getRequestDispatcher(String path) { return (null); }
        public Set getResourcePaths(String path) { return null; }
        public String getServerInfo() { return (null); }
        /**
         * @deprecated The method DeliImpl.CocoonServletContext.getServlet(String)
         *             overrides a deprecated method from ServletContext.
         * @see <a href="http://java.sun.com/j2ee/sdk_1.3/techdocs/api/javax/servlet/ServletContext.html#getServlet(java.lang.String)">ServletContext#getServlet(java.lang.String)</a>
         */
        public Servlet getServlet(String name) throws ServletException { return (null); }
        public String getServletContextName() { return (null); }
        /**
         * @deprecated The method DeliImpl.CocoonServletContext.getServletNames()
         *             overrides a deprecated method from ServletContext.
         * @see <a href="http://java.sun.com/j2ee/sdk_1.3/techdocs/api/javax/servlet/ServletContext.html#getServletNames()">ServletContext#getServletNames()</a>
         */
        public Enumeration getServletNames() { return (null); }
        /**
         * @deprecated The method DeliImpl.CocoonServletContext.getServlets()
         *             overrides a deprecated method from ServletContext.
         * @see <a href="http://java.sun.com/j2ee/sdk_1.3/techdocs/api/javax/servlet/ServletContext.html#getServlets()">ServletContext#getServlets()</a>
         */
        public Enumeration getServlets() { return (null); }
        public void log(String message) {}
        /** @deprecated use {@link #log(String message, Throwable throwable)} instead. */
        public void log(Exception exception, String message) {}
        public void log(String message, Throwable throwable) {}
        public void removeAttribute(String name) {}
    }

    /**
     * Stub implementation of HttpServletRequest
     */
    public class CocoonServletRequest implements HttpServletRequest {
        Request request;

        public CocoonServletRequest(Request request) {
            this.request = request;
        }

        public String getAuthType() { return request.getAuthType(); }
        public long getDateHeader(String s) { return request.getDateHeader(s); }
        public String getHeader(String s) { return request.getHeader(s); }
        public Enumeration getHeaders(String s) { return request.getHeaders(s); }
        public Enumeration getHeaderNames() { return request.getHeaderNames(); }
        public String getMethod() { return request.getMethod(); }
        public String getPathInfo() { return request.getPathInfo(); }
        public String getPathTranslated() { return request.getPathTranslated(); }
        public String getContextPath() { return request.getContextPath(); }
        public String getQueryString() { return request.getQueryString(); }
        public String getRemoteUser() { return request.getRemoteUser(); }
        public boolean isUserInRole(String s) { return request.isUserInRole(s); }
        public String getRequestedSessionId() { return request.getRequestedSessionId(); }
        public String getRequestURI() { return request.getRequestURI(); }
        public String getServletPath() { return request.getServletPath(); }
        public boolean isRequestedSessionIdValid() { return request.isRequestedSessionIdValid(); }
        public boolean isRequestedSessionIdFromCookie() { return request.isRequestedSessionIdFromCookie(); }
        public Object getAttribute(String s) { return request.getAttribute(s); }
        public Enumeration getAttributeNames() { return request.getAttributeNames(); }
        public String getCharacterEncoding() { return request.getCharacterEncoding(); }
        public int getContentLength() { return request.getContentLength(); }
        public String getContentType() { return request.getContentType(); }
        public String getParameter(String s) { return request.getParameter(s); }
        public Enumeration getParameterNames() { return request.getParameterNames(); }
        public String[] getParameterValues(String s) { return request.getParameterValues(s); }
        public String getProtocol() { return request.getProtocol(); }
        public String getScheme() { return request.getScheme(); }
        public String getServerName() { return request.getServerName(); }
        public int getServerPort() { return request.getServerPort(); }
        public String getRemoteAddr() { return request.getRemoteAddr(); }
        public String getRemoteHost() { return request.getRemoteHost(); }
        public void setAttribute(String s, Object obj) { request.setAttribute(s, obj); }
        public void removeAttribute(String s) { request.removeAttribute(s); }
        public boolean isSecure() { return request.isSecure(); }
        public StringBuffer getRequestURL() { return null; }
        public Map getParameterMap() { return null; }
        public void setCharacterEncoding(String s) {}
        public Principal getUserPrincipal() { return request.getUserPrincipal(); }
        public Locale getLocale() { return request.getLocale(); }
        public Enumeration getLocales() { return request.getLocales(); }

        /** @deprecated use {@link org.apache.cocoon.components.deli.DeliImpl.CocoonServletContext#getRealPath(java.lang.String)} instead. */
        public String getRealPath(String s) { return null; }
        public Cookie[] getCookies() { return null; }
        public RequestDispatcher getRequestDispatcher(String s) { return null; }
        public BufferedReader getReader() throws IOException { return null; }
        public ServletInputStream getInputStream() throws IOException { return null; }
        public HttpSession getSession(boolean flag) { return null; }
        public HttpSession getSession() { return null; }
        public boolean isRequestedSessionIdFromURL() { return false; }
        /** @deprecated use {@link #isRequestedSessionIdFromURL()} instead */
        public boolean isRequestedSessionIdFromUrl() { return false; }
        public int getIntHeader(String s) { return 0; }

        public String getLocalAddr() {
            // TODO Auto-generated method stub
            return null;
        }

        public String getLocalName() {
            // TODO Auto-generated method stub
            return null;
        }

        public int getLocalPort() {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getRemotePort() {
            // TODO Auto-generated method stub
            return 0;
        }
    }

}

