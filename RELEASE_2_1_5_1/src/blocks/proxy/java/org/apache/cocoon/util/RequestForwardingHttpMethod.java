/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.cookie.CookieSpecBase;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;

/**
 * This is a generic and externally configurable method, to forward any Request
 * to a server.
 * 
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @version $Id: RequestForwardingHttpMethod.java,v 1.3 2004/03/05 13:02:20 bdelacretaz Exp $
 */
public class RequestForwardingHttpMethod extends EntityEnclosingMethod {
    
    /** The request to be forwarded */
    HttpServletRequest originalRequest;
    
    /** The HTTPUrl to forward this request to */
    HttpURL destination;
    
    public RequestForwardingHttpMethod(HttpServletRequest req, HttpURL destination)
        throws IOException {
            this.originalRequest = req;
            this.destination = destination;
            this.setFollowRedirects(true);
            this.setPath(req.getRequestURI());
            cloneHeaders();
            cloneCookies();
            setRequestBody(originalRequest.getInputStream());
    }
    

    /**
     * Dinamically get the method.
     * 
     * @see org.apache.commons.httpclient.HttpMethod#getName()
     */
    public String getName() {
        return originalRequest.getMethod();
    }

    /**
     * Clone the original request headers.
     *
     */    
    private void cloneHeaders() {
        Enumeration e = originalRequest.getHeaderNames();
        while (e.hasMoreElements()) {
            String header = (String) e.nextElement();
            String headerValue = originalRequest.getHeader(header);
            this.addRequestHeader(header, headerValue);
        }        
    }
    
    /**
     * Clone cookies, if any.
     *
     */
    private void cloneCookies() {
        ArrayList newCookiesList = new ArrayList();
        javax.servlet.http.Cookie[] cookies = originalRequest.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                String domain = cookies[i].getDomain();
                String name = cookies[i].getName();
                String path = cookies[i].getPath();
                String value = cookies[i].getValue();
                Cookie cookie = new Cookie(domain, path, value);
                cookie.setName(name);
                newCookiesList.add(cookie);
            }
        
            CookieSpecBase cookieFormatter = new CookieSpecBase();
            Header cookieHeader = 
                cookieFormatter.formatCookieHeader((Cookie[])newCookiesList.toArray(new Cookie[0]));
            this.addRequestHeader(cookieHeader);
        }

    }
    
}
