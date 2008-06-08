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
package org.apache.cocoon.servletservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.servletservice.util.ServletContextWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Id$
 * @since 1.0.0
 */
public class ServletServiceContext extends ServletContextWrapper implements Absolutizable {

    public static final String SUPER = "super";

    private final Log logger = LogFactory.getLog(ServletServiceContext.class);

    private Map attributes = new Hashtable();
    private Servlet servlet;
    private String mountPath;
    private String contextPath;
    private URL contextPathURL;
    private Map properties;
    private Map connections;
    private Map connectionServiceNames;
    private String serviceName;

    /*
     * TODO inheritance of attributes from the parent context is only partly implemented: removeAttribute and
     * getAttributeNames doesn't respect inheritance yet.
     */
    public Object getAttribute(String name) {
        Object value = this.attributes.get(name);
        return value != null ? value : super.getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public Enumeration getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    /**
     * @param map the attributes to set
     */
    public void setAttributes(Map map) {
        if (map != null) {
            this.attributes = map;
        }
    }

    public URL getResource(String path) throws MalformedURLException {
        if (path == null || !path.startsWith("/")) {
            throw new MalformedURLException("The path must begin with a '/' and is interpreted "
                    + "as relative to the current context root.");
        }

        // lazy initialization of the base URL
        synchronized (this) {
            if (this.contextPathURL == null) {
                this.contextPathURL = new URL(this.contextPath);
            }
        }

        return new URL(this.contextPathURL, path.substring(1));
    }

    public String getRealPath(String path) {
        // We better don't assume that blocks are unpacked
        return null;
    }

    // FIXME, this should be defined in the config instead
    public String getInitParameter(String name) {
        if (this.properties == null) {
            return null;
        }

        String value = (String) this.properties.get(name);
        // Ask the super servlet for the property
        if (value == null) {
            ServletContext superContext = this.getNamedContext(SUPER);
            if (superContext != null) {
                value = superContext.getInitParameter(name);
            }
        }

        // Ask the parent context
        if (value == null) {
            value = super.getInitParameter(name);
        }

        return value;
    }

    public Enumeration getInitParameterNames() {
        Vector names = new Vector();

        // add all names of the parent servlet context
        Enumeration enumeration = super.getInitParameterNames();
        while (enumeration.hasMoreElements()) {
            names.add(enumeration.nextElement());
        }

        // add names of the super servlet
        ServletContext superContext = this.getNamedContext(SUPER);
        if (superContext != null) {
            enumeration = superContext.getInitParameterNames();
            while (enumeration.hasMoreElements()) {
                names.add(enumeration.nextElement());
            }
        }

        // add property names of this servlet
        if (this.properties != null) {
            names.addAll(this.properties.keySet());
        }

        return names.elements();
    }

    public InputStream getResourceAsStream(String path) {
        try {
            return this.getResource(path).openStream();
        } catch (IOException e) {
            this.logger.error("Can't open stream on " + path, e);
            return null;
        }
    }

    public ServletContext getContext(String uripath) {
        return null;
    }

    public int getMajorVersion() {
        return 2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getMinorVersion()
     */
    public int getMinorVersion() {
        return 3;
    }

    private Collection getDirectoryList(File file, String pathPrefix) {
        ArrayList filenames = new ArrayList();

        if (!file.isDirectory()) {
            filenames.add("/" + file.toString().substring(pathPrefix.length() - 1));
            return filenames;
        }

        File[] files = file.listFiles();

        for (int i = 0; i < files.length; i++) {
            File subfile = files[i];
            filenames.addAll(this.getDirectoryList(subfile, pathPrefix));
        }

        return filenames;
    }

    public Set getResourcePaths(String path) {
        if (path == null) {
            return Collections.EMPTY_SET;
        }

        String pathPrefix;
        if (this.contextPath.startsWith("file:")) {
            pathPrefix = this.contextPath.substring("file:".length());
        } else {
            pathPrefix = this.contextPath;
        }

        path = pathPrefix + path;

        File file = new File(path);

        if (!file.exists()) {
            return Collections.EMPTY_SET;
        }

        HashSet set = new HashSet();
        set.addAll(this.getDirectoryList(file, pathPrefix));

        return set;
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        PathDispatcher dispatcher = new PathDispatcher(path);
        return dispatcher.exists() ? dispatcher : null;
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        NamedDispatcher dispatcher = new NamedDispatcher(name);
        return dispatcher.exists() ? dispatcher : null;
    }

    public String getServerInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getServletContextName() {
        // TODO Auto-generated method stub
        return null;
    }

    // Servlet service specific methods

    /**
     * Set the servlet of the context
     * 
     * @param servlet
     */
    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }

    /**
     * Takes the scheme specific part of a servlet service URI (the scheme is the responsibilty of the ServletSource)
     * and resolve it with respect to the servlets mount point.
     */
    public URI absolutizeURI(URI uri) throws URISyntaxException {
        String servletServiceName = uri.getScheme();
        ServletServiceContext servletServiceContext;
        if (servletServiceName == null) {
            // this servlet service
            servletServiceContext = this;
        } else {
            // another servlet service
            servletServiceContext = (ServletServiceContext) this.getNamedContext(servletServiceName);
            if (servletServiceContext == null) {
                throw new URISyntaxException(uri.toString(), "Unknown servlet service name");
            }
        }

        String mountPath = servletServiceContext.getMountPath();
        if (mountPath == null) {
            throw new URISyntaxException(uri.toString(), "No mount point for this URI");
        }
        if (mountPath.endsWith("/")) {
            mountPath = mountPath.substring(0, mountPath.length() - 1);
        }

        String absoluteURI = mountPath + uri.getSchemeSpecificPart();
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Resolving " + uri.toString() + " to " + absoluteURI);
        }

        return new URI(absoluteURI);
    }

    public String getServiceName(String connectionName) {
        return (String) this.connectionServiceNames.get(connectionName);
    }

    public String getServiceName() {
        return this.serviceName;
    }

    /**
     * Get the context of a servlet service with a given name.
     */
    // FIXME implement NPE handling
    public ServletContext getNamedContext(String name) {
        if (this.connections == null) {
            return null;
        }

        Servlet servlet = (Servlet) this.connections.get(name);
        if (servlet == null && !name.equals(SUPER)) {
            Servlet _super = (Servlet) this.connections.get(SUPER);
            if (_super != null) {
                ServletContext c = _super.getServletConfig().getServletContext();
                if (c instanceof ServletServiceContext) {
                    return ((ServletServiceContext) c).getNamedContext(name);
                }

                return null;
            }
        }
        return servlet != null ? servlet.getServletConfig().getServletContext() : null;
    }

    /**
     * @param mountPath The mountPath to set.
     */
    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }

    /**
     * Get the mount path of the servlet service context
     */
    public String getMountPath() {
        return this.mountPath;
    }

    /**
     * @param contextPath
     */
    public void setContextPath(String contextPath) {
        if (contextPath == null) {
            throw new IllegalArgumentException("Context path must not be null.");
        }

        if (contextPath.endsWith("/")) {
            this.contextPath = contextPath;
        } else {
            this.contextPath = contextPath + "/";
        }
    }

    /**
     * @param properties The properties to set.
     */
    public void setInitParams(Map properties) {
        this.properties = properties;
    }

    /**
     * @param connections the connections to set
     */
    public void setConnections(Map connections) {
        this.connections = connections;
    }

    /**
     * @param connections the service names of the connections
     */
    public void setConnectionServiceNames(Map connectionServletServiceNames) {
        this.connectionServiceNames = connectionServletServiceNames;
    }

    /**
     * @param serviceName the name of the
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    protected class NamedDispatcher implements RequestDispatcher {

        private final String servletServiceName;

        private final boolean superCall;

        private final ServletContext context;

        public NamedDispatcher(String servletServiceName) {
            this.servletServiceName = servletServiceName;
            this.superCall = SUPER.equals(this.servletServiceName);

            // Call to a named servlet service that exists in the current
            // context
            this.context = ServletServiceContext.this.getNamedContext(this.servletServiceName);
        }

        protected boolean exists() {
            return this.context != null;
        }

        public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            // Call to named servlet service

            if (ServletServiceContext.this.logger.isInfoEnabled()) {
                ServletServiceContext.this.logger
                        .info("Enter processing in servlet service " + this.servletServiceName);
            }
            RequestDispatcher dispatcher = this.context.getRequestDispatcher(((HttpServletRequest) request)
                    .getPathInfo());
            if (dispatcher != null && dispatcher instanceof PathDispatcher) {
                ((PathDispatcher) dispatcher).forward(request, response, this.superCall);
            } else {
                // Cannot happen
                throw new IllegalStateException();
            }
            if (ServletServiceContext.this.logger.isInfoEnabled()) {
                ServletServiceContext.this.logger.info("Leaving processing in servlet service "
                        + this.servletServiceName);
            }
        }

        public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Limited functionality, assumes that there is at most one servlet in the context
     */
    private class PathDispatcher implements RequestDispatcher {

        // Ignores path, as the assumed only servlet within the context is
        // implicitly mounted on '/*'
        private PathDispatcher(String path) {
        }

        private boolean exists() {
            return ServletServiceContext.this.servlet != null;
        }

        public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            this.forward(request, response, false);
        }

        protected void forward(ServletRequest request, ServletResponse response, boolean superCall)
                throws ServletException, IOException {
            try {
                HttpServletResponseBufferingWrapper wrappedResponse = new HttpServletResponseBufferingWrapper(
                        (HttpServletResponse) response);
                // FIXME: I think that Cocoon should always set status code on
                // its own
                wrappedResponse.setStatus(HttpServletResponse.SC_OK);
                if (!superCall) {
                    // It is important to set the current context each time
                    // a new context is entered, this is used for the servlet
                    // protocol
                    CallStackHelper.enterServlet(ServletServiceContext.this, (HttpServletRequest) request,
                            wrappedResponse);
                } else {
                    // A super servlet service should be called in the context
                    // of the called servlet service to get polymorphic calls
                    // resolved in the right way. We still need to register the
                    // current context for resolving super calls relative it.
                    CallStackHelper.enterSuperServlet(ServletServiceContext.this, (HttpServletRequest) request,
                            wrappedResponse);
                }

                ServletServiceContext.this.servlet.service(request, wrappedResponse);

                int status = wrappedResponse.getStatusCode();
                NamedDispatcher _super = (NamedDispatcher) ServletServiceContext.this.getNamedDispatcher(SUPER);
                if (status == HttpServletResponse.SC_NOT_FOUND && _super != null) {
                    // if servlet returned NOT_FOUND (404) and has super servlet declared let's reset everything and ask
                    // the super servlet

                    // wrapping object resets underlying response as well
                    wrappedResponse.resetBufferedResponse();
                    // here we don't need to pass wrappedResponse object because it's not our concern to buffer response
                    // anymore
                    // this avoids many overlapping buffers
                    _super.forward(request, response);
                } else {
                    wrappedResponse.flushBufferedResponse();
                }
            } finally {
                CallStackHelper.leaveServlet();
            }
        }

        public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            throw new UnsupportedOperationException();
        }
    }

}
