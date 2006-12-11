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
package org.apache.cocoon.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Holder class to expose the servlet context, the current request and the current
 * response in the form of a thread-bound object.
 *
 * @version $Id$
 * @since 2.2
 */
public abstract class ServletRequestHolder  {
	
	private static final ThreadLocal infoContextHolder = new ThreadLocal();

	private static final ThreadLocal inheritableInfoContextHolder = new InheritableThreadLocal();


	/**
	 * Reset the context for the current thread.
	 */
	public static void reset() {
        infoContextHolder.set(null);
        inheritableInfoContextHolder.set(null);
	}

	/**
	 * Bind the given information to the current thread,
	 * <i>not</i> exposing it as inheritable for child threads.
	 * @param context the ServletContext to expose
     * @param req the HttpServletRequest to expose
     * @param res the HttpServletResponse to expose
	 * @see #set(ServletContext, HttpServletRequest, HttpServletResponse, boolean)
	 */
	public static void set(ServletContext context, HttpServletRequest req, HttpServletResponse res) {
        set(context, req, res, false);
	}

	/**
	 * Bind the given information to the current thread.
     * @param context the ServletContext to expose
     * @param req the HttpServletRequest to expose
     * @param res the HttpServletResponse to expose
	 * @param inheritable whether to expose the information as inheritable
	 * for child threads (using an {@link java.lang.InheritableThreadLocal})
	 */
	public static void set(ServletContext context,
                           HttpServletRequest req,
                           HttpServletResponse res,
                           boolean inheritable) {
        final RequestInfo info = new RequestInfo(context, req, res);
		if (inheritable) {
            inheritableInfoContextHolder.set(info);
            infoContextHolder.set(null);
		} else {
            infoContextHolder.set(info);
            inheritableInfoContextHolder.set(null);
		}
	}

	/**
	 * Return the ServletContext currently bound to the thread.
	 * @return the ServletContext currently bound to the thread,
	 * or <code>null</code>
	 */
	public static ServletContext getServletContext() {
        final RequestInfo info = get();
        if ( info != null ) {
            return info.servletContext;
        }
		return null;
	}

    /**
     * Return the HttpServletRequest currently bound to the thread.
     * @return the HttpServletRequest currently bound to the thread,
     * or <code>null</code>
     */
    public static HttpServletRequest getRequest() {
        final RequestInfo info = get();
        if ( info != null ) {
            return info.request;
        }
        return null;
    }

    /**
     * Return the HttpServletResponse currently bound to the thread.
     * @return the HttpServletResponse currently bound to the thread,
     * or <code>null</code>
     */
    public static HttpServletResponse getResponse() {
        final RequestInfo info = get();
        if ( info != null ) {
            return info.respone;
        }
        return null;
    }

    /**
	 * Return the ServletContext currently bound to the thread.
	 * @return the ServletContext currently bound to the thread
	 * @throws IllegalStateException if no ServletContext object
	 * is bound to the current thread
	 */
	public static ServletContext currentServletContext() throws IllegalStateException {
        final RequestInfo info = currentInfo();
        if ( info != null ) {
            return info.servletContext;
        }
        return null;
	}

    /**
     * Return the HttpServletRequest currently bound to the thread.
     * @return the HttpServletRequest currently bound to the thread
     * @throws IllegalStateException if no HttpServletRequest object
     * is bound to the current thread
     */
    public static HttpServletRequest currentRequest() throws IllegalStateException {
        final RequestInfo info = currentInfo();
        if ( info != null ) {
            return info.request;
        }
        return null;
    }

    /**
     * Return the HttpServletResponse currently bound to the thread.
     * @return the HttpServletResponse currently bound to the thread
     * @throws IllegalStateException if no HttpServletResponse object
     * is bound to the current thread
     */
    public static HttpServletResponse currentResponse() throws IllegalStateException {
        final RequestInfo info = currentInfo();
        if ( info != null ) {
            return info.respone;
        }
        return null;
    }

    private static RequestInfo get() {
        RequestInfo info = (RequestInfo)infoContextHolder.get();
        if (info == null) {
            info = (RequestInfo) inheritableInfoContextHolder.get();
        }
        return info;    
    }

    private static RequestInfo currentInfo() {
        RequestInfo info = get();
        if (info == null) {
            throw new IllegalStateException("No thread-bound information found.");
        }
        return info;        
    }

    private static final class RequestInfo {

        public final ServletContext servletContext;
        public final HttpServletRequest request;
        public final HttpServletResponse respone;

        public RequestInfo(ServletContext c, HttpServletRequest req, HttpServletResponse res) {
            this.servletContext = c;
            this.request = req;
            this.respone = res;
        }
    }
}
