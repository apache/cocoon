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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Some utility methods for request handling etc.
 *
 * @version $Id$
 * @since 2.2
 */
public class RequestUtil {

    public static String getCompleteUri(HttpServletRequest request,
                                        HttpServletResponse response)
    throws IOException {
        // We got it... Process the request
        String uri = request.getServletPath();
        // uri should never be null, but we check it anyway
        if (uri == null) {
            uri = "";
        }
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            // VG: WebLogic fix: Both uri and pathInfo starts with '/'
            // This problem exists only in WL6.1sp2, not in WL6.0sp2 or WL7.0b.
            // Comment: The servletPath always starts with '/', so it seems
            //          that the above mentioned bug is only occuring if the servlet path
            //          is just a "/".
            if (uri.length() > 0 && uri.charAt(0) == '/') {
                uri = uri.substring(1);
            }
            uri += pathInfo;
        }

        if (uri.length() == 0) {
            /* empty relative URI
                 -> HTTP-redirect from /cocoon to /cocoon/ to avoid
                    StringIndexOutOfBoundsException when calling
                    "".charAt(0)
               else process URI normally
            */
            String serverAbsoluteUri = request.getRequestURI();
            if (serverAbsoluteUri == null) {
                serverAbsoluteUri = "/";
            } else {
                serverAbsoluteUri += "/";
            }

            response.sendRedirect(response.encodeRedirectURL(serverAbsoluteUri));
            return null;
        }

        if (uri.charAt(0) == '/') {
            uri = uri.substring(1);
        }
        return uri;
    }

    public static HttpServletRequest createRequestForUri(HttpServletRequest request, String servletPath, String pathInfo) {
        return new UriHttpServletRequestWrapper(request, servletPath, pathInfo);
    }

    public static HttpServletRequest createRequestByRemovingPrefixFromUri(HttpServletRequest request, String prefix) {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String newPathInfo = pathInfo.substring(prefix.length()+1);
        String newServletPath;
        if ( servletPath == null ) {
            newServletPath = pathInfo.substring(0, prefix.length()+1);
        } else {
            newServletPath = servletPath + pathInfo.substring(0, prefix.length()+1);
        }
        return new UriHttpServletRequestWrapper(request, newServletPath, newPathInfo);
    }
}
