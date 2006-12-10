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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * A servlet filter making some per request information available.
 * This class is similar to Spring's RequestContextListener with some additional
 * logic.
 *
 * @version $Id
 * @since 2.2
 */
public class RequestListener implements Filter {

	/** Logger available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

    /** The servlet context. */
    protected ServletContext servletContext;

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        this.servletContext = config.getServletContext();
    }

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
        this.servletContext = null;
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
    throws IOException, ServletException {
        if (!(req instanceof HttpServletRequest)) {
            throw new IllegalArgumentException("Request is not an HttpServletRequest: " + req);
        }
        if (!(res instanceof HttpServletResponse)) {
            throw new IllegalArgumentException("Response is not an HttpServletResponse: " + res);
        }
        final HttpServletRequest request = (HttpServletRequest)req;
        final HttpServletResponse resposne = (HttpServletResponse)res;
        try {
            LocaleContextHolder.setLocale(request.getLocale());
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
            ServletRequestHolder.set(this.servletContext, request, resposne);
            if (logger.isDebugEnabled()) {
                logger.debug("Bound request information to thread: " + request);
            }
        } finally {
            final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if ( requestAttributes != null ) {
                requestAttributes.requestCompleted();
            }
            RequestContextHolder.resetRequestAttributes();
            LocaleContextHolder.resetLocaleContext();
            ServletRequestHolder.reset();
            if (logger.isDebugEnabled()) {
                logger.debug("Cleared thread-bound request information: " + request);
            }
        }
    }
}
