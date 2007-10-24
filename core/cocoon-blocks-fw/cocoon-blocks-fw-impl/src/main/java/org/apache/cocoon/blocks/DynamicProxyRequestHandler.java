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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
/**
 * A dynamic wrapper for servlet requests that overwrites the
 * getServletPath and getPathInfo methods to relect the mount
 * path of the block servlet.
 * 
 * @version $Id$
 */
public class DynamicProxyRequestHandler implements InvocationHandler {
	private final HttpServletRequest wrapped;
	private final String mountPath;
	
	private static final Method getServletPathMethod;
	private static final Method getPathInfoMethod;
	
	static {
		getPathInfoMethod = getHttpServletRequestMethod("getPathInfo");
		getServletPathMethod = getHttpServletRequestMethod("getServletPath");
	}
	/**
	 * Helper method for getting methods of the HttpServletRequest interface
	 * @param name name of the method
	 * @return the method object
	 */
	static private Method getHttpServletRequestMethod(
            String name) {
        Class[] paramTypes = new Class[] {};
        try {
            return HttpServletRequest.class.getMethod(name, paramTypes);
        } catch (SecurityException e) {
            throw new RuntimeException( "could not get method: " + 
                    name + " from class: " + HttpServletRequest.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException( "could not get method: " + 
                    name + " from class: " + HttpServletRequest.class);
        }
    }
	/**
	 * Creates a new request wrapper from a specified proxied request and
	 * the mount path of the block servlet
	 * @param req the request to proxy
	 * @param mountPath the mount path of the servlet
	 */
	public DynamicProxyRequestHandler(HttpServletRequest req, String mountPath) {
		this.wrapped = req;
		this.mountPath = mountPath;
	}
	/**
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] arguments)
			throws Throwable {
		if (method.equals(getPathInfoMethod)) {
			String pathInfo = wrapped.getPathInfo().substring(mountPath.length()); 
            return pathInfo.length() == 0 ? null : pathInfo;
		} else if (method.equals(getServletPathMethod)) {
			return wrapped.getServletPath() + mountPath;
		} else {
			return method.invoke(wrapped, arguments);
		}
	}
}
