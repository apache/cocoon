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
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.SystemUtils;

import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * Servlet filter for printing request debug information into log file.
 * 
 * @version $Id$
 */
public class DebugFilter extends AbstractLogEnabled
                         implements Filter {

    final static protected String lineSeparator = SystemUtils.LINE_SEPARATOR;

    /** Active request count. */
    private volatile int activeRequestCount;


    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // nothing to do here
    }

    /**
     * Log debug information about the current environment.
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
    throws IOException, ServletException {
        // we don't do debug msgs if this is not a http servlet request
        if ( ! (req instanceof HttpServletRequest) ) {
            filterChain.doFilter(req, res);
            return;
        }
        try {
            ++activeRequestCount;
            final HttpServletRequest request = (HttpServletRequest) req;

            if (getLogger().isDebugEnabled()) {
                final StringBuffer msg = new StringBuffer();

                msg.append("DEBUGGING INFORMATION:").append(lineSeparator);
                msg.append("REQUEST: ").append(request.getRequestURI()).append(lineSeparator).append(
                        lineSeparator);
                msg.append("CONTEXT PATH: ").append(request.getContextPath()).append(lineSeparator);
                msg.append("SERVLET PATH: ").append(request.getServletPath()).append(lineSeparator);
                msg.append("PATH INFO: ").append(request.getPathInfo()).append(lineSeparator).append(
                        lineSeparator);

                msg.append("REMOTE HOST: ").append(request.getRemoteHost()).append(lineSeparator);
                msg.append("REMOTE ADDRESS: ").append(request.getRemoteAddr()).append(lineSeparator);
                msg.append("REMOTE USER: ").append(request.getRemoteUser()).append(lineSeparator);
                msg.append("REQUEST SESSION ID: ").append(request.getRequestedSessionId()).append(
                        lineSeparator);
                msg.append("REQUEST PREFERRED LOCALE: ").append(request.getLocale().toString()).append(
                        lineSeparator);
                msg.append("SERVER HOST: ").append(request.getServerName()).append(lineSeparator);
                msg.append("SERVER PORT: ").append(request.getServerPort()).append(lineSeparator).append(
                        lineSeparator);

                msg.append("METHOD: ").append(request.getMethod()).append(lineSeparator);
                msg.append("CONTENT LENGTH: ").append(request.getContentLength()).append(lineSeparator);
                msg.append("PROTOCOL: ").append(request.getProtocol()).append(lineSeparator);
                msg.append("SCHEME: ").append(request.getScheme()).append(lineSeparator);
                msg.append("AUTH TYPE: ").append(request.getAuthType()).append(lineSeparator).append(
                        lineSeparator);
                msg.append("CURRENT ACTIVE REQUESTS: ").append(activeRequestCount).append(lineSeparator);

                // log all of the request parameters
                final Enumeration e = request.getParameterNames();

                msg.append("REQUEST PARAMETERS:").append(lineSeparator).append(lineSeparator);

                while (e.hasMoreElements()) {
                    String p = (String) e.nextElement();

                    msg.append("PARAM: '").append(p).append("' ").append("VALUES: '");
                    String[] params = request.getParameterValues(p);
                    for (int i = 0; i < params.length; i++) {
                        msg.append("[" + params[i] + "]");
                        if (i != (params.length - 1)) {
                            msg.append(", ");
                        }
                    }

                    msg.append("'").append(lineSeparator);
                }

                // log all of the header parameters
                final Enumeration e2 = request.getHeaderNames();

                msg.append("HEADER PARAMETERS:").append(lineSeparator).append(lineSeparator);

                while (e2.hasMoreElements()) {
                    String p = (String) e2.nextElement();

                    msg.append("PARAM: '").append(p).append("' ").append("VALUES: '");
                    Enumeration e3 = request.getHeaders(p);
                    while (e3.hasMoreElements()) {
                        msg.append("[" + e3.nextElement() + "]");
                        if (e3.hasMoreElements()) {
                            msg.append(", ");
                        }
                    }

                    msg.append("'").append(lineSeparator);
                }

                msg.append(lineSeparator).append("SESSION ATTRIBUTES:").append(lineSeparator).append(
                        lineSeparator);

                // log all of the session attributes
                final HttpSession session = ((HttpServletRequest) req).getSession(false);
                if (session != null) {
                    // Fix bug #12139: Session can be modified while still
                    // being enumerated here
                    synchronized (session) {
                        final Enumeration se = session.getAttributeNames();
                        while (se.hasMoreElements()) {
                            String p = (String) se.nextElement();
                            msg.append("PARAM: '").append(p).append("' ").append("VALUE: '").append(
                                    session.getAttribute(p)).append("'").append(lineSeparator);
                        }
                    }
                }
                getLogger().debug(msg.toString());
            }

            // Delegate
            filterChain.doFilter(request, res);
        } finally {
            --activeRequestCount;
        }
    }
}
