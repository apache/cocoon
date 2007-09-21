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
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.cocoon.servletservice.util.ServletContextWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @version $Id$
 */
public class ServletServiceContext extends ServletContextWrapper implements Absolutizable {

    public static final String SUPER = "super";

    private final Log logger = LogFactory.getLog(ServletServiceContext.class);

    private Map attributes = new Hashtable();
    private Servlet servlet;
    private String mountPath;
    private String contextPath;
    private Map properties;
    private Map connections;


    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
     */
    /*
     *  TODO ineritance of attributes from the parent context is only
     *  partly implemented: removeAttribute and getAttributeNames
     *  doesn't respect inheritance yet.  
     */
    public Object getAttribute(String name) {
        Object value = this.attributes.get(name);
        return value != null ? value : super.getAttribute(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#setAttribute(java.lang.String,
     *      java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    /**
     * @param map the attributes to set
     */
    public void setAttributes(Map map) {
        if (map != null)
            this.attributes = map;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getResource(java.lang.String)
     */
    public URL getResource(String path) throws MalformedURLException {
        // hack for getting a file protocol or other protocols that can be used as context
        // path in the getResource method in the servlet context
        if (!(contextPath.startsWith("file:") || contextPath.startsWith("/")
              || contextPath.indexOf(':') == -1)) {
            SourceResolver resolver = null;
            Source source = null;
            try {
                BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(this);
                resolver = (SourceResolver) factory.getBean(SourceResolver.ROLE);
                source = resolver.resolveURI(contextPath);
                contextPath = source.getURI();
            } catch (IOException e) {
                throw new MalformedURLException("Could not resolve " + contextPath);
            } finally {
                if (resolver != null)
                    resolver.release(source);
            }
        }

        // HACK: allow file:/ URLs for reloading of sitemaps during development
        if (this.contextPath.startsWith("file:")) {
            return new URL("file", null, this.contextPath.substring("file:".length()) + path);
        }

        if (this.contextPath.length() != 0 && this.contextPath.charAt(0) != '/') {
            throw new MalformedURLException("The contextPath must be empty or start with '/' " +
                                            this.contextPath);
        }

        // prefix the path with the servlet context resolve and resolve in the embeding
        // servlet context
        return super.getResource(this.contextPath + path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
     */
    public String getRealPath(String path) {
        // We better don't assume that blocks are unpacked
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getInitParameterNames()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
     */
    public InputStream getResourceAsStream(String path) {
        try {
            return this.getResource(path).openStream();
        } catch (IOException e) {
            // FIXME Error handling
            e.printStackTrace();
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getContext(java.lang.String)
     */
    public ServletContext getContext(String uripath) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getMajorVersion()
     */
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
            filenames.add("/" + file.toString().substring(pathPrefix.length()-1));
            return filenames;
        }

        File[] files = file.listFiles();

        for (int i = 0; i < files.length; i++) {
            File subfile = files[i];
            filenames.addAll(getDirectoryList(subfile, pathPrefix));
        }

        return filenames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
     */
    public Set getResourcePaths(String path) {
        String pathPrefix;
        if (this.contextPath.startsWith("file:")) {
            pathPrefix = this.contextPath.substring("file:".length());
        } else {
            pathPrefix = this.contextPath;
        }

        path = pathPrefix + path;

        if (path == null) {
            return Collections.EMPTY_SET;
        }

        File file = new File(path);

        if (!file.exists()) {
            return Collections.EMPTY_SET;
        }

        HashSet set = new HashSet();
        set.addAll(getDirectoryList(file, pathPrefix));

        return set;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        PathDispatcher dispatcher = new PathDispatcher(path);
        return dispatcher.exists() ? dispatcher : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
     */
    public RequestDispatcher getNamedDispatcher(String name) {
        NamedDispatcher dispatcher = new NamedDispatcher(name);
        return dispatcher.exists() ? dispatcher : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getServerInfo()
     */
    public String getServerInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getServletContextName()
     */
    public String getServletContextName() {
        // TODO Auto-generated method stub
        return null;
    }

    // Servlet service specific methods

    /**
     * Set the servlet of the context
     * @param servlet
     */
    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }

    /**
     * Takes the scheme specific part of a servlet service URI (the scheme is the
     * responsibilty of the ServletSource) and resolve it with respect to the
     * servlets mount point.
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
            throw new URISyntaxException(uri.toString(),
                                         "No mount point for this URI");
        }
        if (mountPath.endsWith("/")) {
            mountPath = mountPath.substring(0, mountPath.length() - 1);
        }

        String absoluteURI = mountPath + uri.getSchemeSpecificPart();
        if (logger.isInfoEnabled()) {
            logger.info("Resolving " + uri.toString() + " to " + absoluteURI);
        }

        return new URI(absoluteURI);
    }

    /**
     * Get the context of a servlet service with a given name.
     */
    // FIXME implement NPE handling
    public ServletContext getNamedContext(String name) {
        if (this.connections == null) {
            return null;
        }
        
        Servlet servlet =
            (Servlet) this.connections.get(name);
        if (servlet == null && !name.equals(SUPER)) {
        	Servlet _super = ((Servlet)this.connections.get(SUPER));
        	if (_super != null) {
        		ServletContext c = _super.getServletConfig().getServletContext();
        		if (c instanceof ServletServiceContext)
        			return ((ServletServiceContext)c).getNamedContext(name);
        		
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
        this.contextPath = contextPath;
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

    protected class NamedDispatcher implements RequestDispatcher {

        private String servletServiceName;
        private boolean superCall;
        private ServletContext context;


        public NamedDispatcher(String servletServiceName) {
            this.servletServiceName = servletServiceName;
            this.superCall = SUPER.equals(this.servletServiceName);

            // Call to a named servlet service that exists in the current context
            this.context = ServletServiceContext.this.getNamedContext(this.servletServiceName);
        }

        protected boolean exists() {
            return this.context != null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest,
         *      javax.servlet.ServletResponse)
         */
        public void forward(ServletRequest request, ServletResponse response)
        throws ServletException, IOException {
            // Call to named servlet service

            if (logger.isInfoEnabled()) {
                logger.info("Enter processing in servlet service " + this.servletServiceName);
            }
            RequestDispatcher dispatcher =
                    this.context.getRequestDispatcher(((HttpServletRequest)request).getPathInfo());
            if (dispatcher != null && dispatcher instanceof PathDispatcher) {
                ((PathDispatcher)dispatcher).forward(request, response, this.superCall);
            } else {
                // Cannot happen
                throw new IllegalStateException();
            }
            if (logger.isInfoEnabled()) {
                logger.info("Leaving processing in servlet service " + this.servletServiceName);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.servlet.RequestDispatcher#include(javax.servlet.ServletRequest,
         *      javax.servlet.ServletResponse)
         */
        public void include(ServletRequest request, ServletResponse response)
        throws ServletException, IOException {
            throw new UnsupportedOperationException();
        }
    }

    /**
     *  Limited functionality, assumes that there is at most one servlet in the context
     */
    private class PathDispatcher implements RequestDispatcher {

        // Ignores path, as the assumed only servlet within the context is
        // implicitly mounted on '/*'
        private PathDispatcher(String path) {
        }

        private boolean exists() {
            return ServletServiceContext.this.servlet != null;
        }

        /* (non-Javadoc)
         * @see javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
         */
        public void forward(ServletRequest request, ServletResponse response)
        throws ServletException, IOException {
            this.forward(request, response, false);
        }

        protected void forward(ServletRequest request, ServletResponse response, boolean superCall)
        throws ServletException, IOException {
            try {
                StatusRetrievableWrappedResponse wrappedResponse = new StatusRetrievableWrappedResponse((HttpServletResponse)response);
                //FIXME: I think that Cocoon should always set status code on its own
                wrappedResponse.setStatus(HttpServletResponse.SC_OK);
                if (!superCall) {
                    // It is important to set the current context each time
                    // a new context is entered, this is used for the servlet
                    // protocol
                    CallStackHelper.enterServlet(ServletServiceContext.this, (HttpServletRequest)request, (HttpServletResponse)wrappedResponse);
                } else {
                    // A super servlet service should be called in the context of
                    // the called servlet service to get polymorphic calls resolved
                    // in the right way. We still need to register the
                    // current context for resolving super calls relative it.
                    CallStackHelper.enterSuperServlet(ServletServiceContext.this, (HttpServletRequest)request, (HttpServletResponse)wrappedResponse);
                }
                ServletException se = null;
                try {
                	ServletServiceContext.this.servlet.service(request, wrappedResponse);
                }
                catch (ServletException e) {
                	se = e;
                }
               	int status = wrappedResponse.getStatus();
               	if (se != null || (status < 200 || status >= 400)) {
               		wrappedResponse.reset();
               		NamedDispatcher _super = (NamedDispatcher) ServletServiceContext.this.getNamedDispatcher(SUPER);
               		if (_super != null) {
               			_super.forward(request, wrappedResponse);
               		}
               		else
               			throw se;
               	}

            } finally {
                CallStackHelper.leaveServlet();
            }
        }

        /* (non-Javadoc)
         * @see javax.servlet.RequestDispatcher#include(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
         */
        public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            throw new UnsupportedOperationException();
        }
    }

    private static class StatusRetrievableWrappedResponse extends HttpServletResponseWrapper {
    	
       	private int status;
    
       	public StatusRetrievableWrappedResponse(HttpServletResponse wrapped) {
       		super(wrapped);
       	}
       	
    	public void setStatus(int sc, String sm) {
    		this.status = sc;
    		super.setStatus(sc, sm);
    	}
    
    	public void setStatus(int sc) {
    		this.status = sc;
    		super.setStatus(sc);
    	}
    	
    	public int getStatus() {
    		return this.status;
    	}
    	
    	public void sendError(int errorCode) throws IOException {
    		this.status = errorCode;
    		super.sendError(errorCode);	
    	}
    	
    	public void sendError(int errorCode, String errorMessage) throws IOException {
    		this.status = errorCode;
    		super.sendError(errorCode, errorMessage);	
    	}		
    }
    
}
