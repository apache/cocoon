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
package org.apache.cocoon.blocks;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.core.container.spring.avalon.AvalonUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet that dispatch to managed sevlets from the context Spring container.
 * It dispatch to servlets that has the property mountPath, and dispatches to the
 * servlet with the longest prefix of the request pathInfo.
 * 
 * This servlet will also initialize and destroy all the servlets that it finds
 * from the context container. This means that there must only be one dispatcher
 * servlet, otherwise the managed servlets will be initialized several times.
 *
 * @version $Id$
 */
public class DispatcherServlet
    extends HttpServlet {

    private static final String MOUNT_PATH = "mountPath";

    /** All registered mountable servlets. */
    private Map mountableServlets = new HashMap();

    /**
     * The Spring bean factory.
     */
    private ListableBeanFactory beanFactory;
    
    /**
     * The logger.
     */
    private Logger log;    
    
    public void init() throws ServletException {
    	// get the beanFactory from the web application context
        this.beanFactory =
            WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
        
        // setup logger
        this.log = (Logger) this.beanFactory.getBean(AvalonUtils.LOGGER_ROLE);        
        this.log.info("DispatcherServlet is initializing");
        
        // the returned map contains the bean names as key and the beans as values
        final Map servlets = 
            BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, Servlet.class);
        
        // register and initialize the servlets that has a mount path property
        final Iterator i = servlets.values().iterator();
        while ( i.hasNext() ) {
            final Servlet servlet = (Servlet) i.next();
            BeanWrapperImpl wrapper = new BeanWrapperImpl(servlet);
            if (wrapper.isReadableProperty(MOUNT_PATH)) {
                String mountPath = (String) wrapper.getPropertyValue(MOUNT_PATH);
                this.log.debug("DispatcherServlet: initializing servlet " + servlet + " at " + mountPath);
                this.mountableServlets.put(mountPath, servlet);
            }
        }
        this.log("Block dispatcher was initialized successfully.");        
    }

    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String path = req.getPathInfo();
        path = path == null ? "" : path;
        // find the servlet which mount path is the longest prefix of the path info
        int index = path.length();
        Servlet servlet = null;
        while (servlet == null && index != -1) {
            path = path.substring(0, index);
            servlet = (Servlet)this.mountableServlets.get(path);
            index = path.lastIndexOf('/');
        }
        if (servlet == null) {
            throw new ServletException("No block for " + req.getPathInfo());
        }
        // Create a dynamic proxy class that overwrites the getServletPath and
        // getPathInfo methods to privide reasonable values in the called servlet
        // the dynamic proxy implements all interfaces of the original request
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
        		req.getClass().getClassLoader(), 
        		getInterfaces(req.getClass()), 
        		new DynamicProxyRequestHandler(req, path));
        
        if(this.log.isDebugEnabled()) {
	        this.log.debug("DispatcherServlet: service servlet=" + servlet +
	                " mountPath=" + path +
	                " servletPath=" + request.getServletPath() +
	                " pathInfo=" + request.getPathInfo());
        }
        
        servlet.service(request, res);
    }
    
    private void getInterfaces(Set interfaces, Class clazz) {
		Class[] clazzInterfaces = clazz.getInterfaces();
		for (int i = 0; i < clazzInterfaces.length; i++) {
			//add all interfaces extended by this interface or directly
			//implemented by this class
			getInterfaces(interfaces, clazzInterfaces[i]);
		}
		//the superclazz is null if class is instanceof Object, is
		//an interface, a primitive type or void
		Class superclazz = clazz.getSuperclass();
		if (superclazz!=null) {
			//add all interfaces of the superclass to the list
			getInterfaces(interfaces, superclazz);
		}
		interfaces.addAll(Arrays.asList(clazzInterfaces));
	}

	private Class[] getInterfaces(final Class clazz) {
		Set interfaces = new LinkedHashSet();
		getInterfaces(interfaces, clazz);
		return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
	}
}
