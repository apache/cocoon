/*
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.cocoon.util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This allows the wrapping of a request to Cocoon from other applications.
 *   It is similar to HttpServletReqImpl used in EngineWrapper, but is generic 
 *   enough to work anywhere, not just with ProducerFromFile.
 * It can be used to push files through to Cocoon, or Strings (generated from
 *   some other application.
 *
 * @author <a href="mailto:bmclaugh@algx.net">Brett McLaughlin</a>
 * @version $Revision: 1.2 $ $Date: 2000-02-13 18:29:14 $
 */
public class CocoonServletRequest implements HttpServletRequest {
    
    /** Copy of request to wrap */
    private HttpServletRequest req;
    
    /** Document passed in via String */
    private String document;
    
    /** Path to file to read from */
    private String path;
    
    /** Additional parameters that can be set */
    private Hashtable params;
    
    /**
     * Take in a File to wrap
     *
     * @param path - File to wrap
     */
    public CocoonServletRequest(File path) {
        this(path, null, null);
    }
    
    /**
     * Take in a String based document to wrap
     *
     * @param document - String to use as document
     */
    public CocoonServletRequest(String document) {
        this(null, document, null);
    }
    
    /**
     * Take in an existing HttpServletReq and wrap all calls to that.
     *
     * @param req - HttpServletReq object to wrap
     */
    public CocoonServletRequest(HttpServletRequest req) {
        this(null, null, req);
    }
    
    /**
     * Take in both a file to wrap and a String document - this is the constructor
     *   used to emulate the requests to ProducerFromFile.
     *
     * @param path - File to read from
     * @param document - String to use as document
     */
    public CocoonServletRequest(File path, String document) {
        this(path, document, null);
    }
    
    public CocoonServletRequest(String document, HttpServletRequest req) {
        this(null, document, req);
    }
    
    /**
     * This is the main constructor that takes in all parameters and wraps further calls
     *
     * @param path - File to read from
     * @param document - String to use as document
     * @param req - HttpServletReq object to wrap
     */
    public CocoonServletRequest(File path, String document, HttpServletRequest req) {
        if (path != null)
            this.path = path.toString();
        this.document = document;
        this.req = req;
        this.params = new Hashtable();
    }
    
    /**
     * This emulates the hack needed to allow ProducerFromFile to work
     */
    public String getPathInfo() {
        if (path != null)
            return "";
        if (req != null)
            return req.getPathInfo();
        return "";        
    }    
    
    /**
     * This will return the Reader for the wrapper.  If the document (String)
     *   representation of a file is not null, it overrides any other settings
     *   and returned.  If it is null, then the wrapped object is returned, or
     *   else null.
     */
    public BufferedReader getReader() throws IOException {
        if (document != null)
            return new BufferedReader(new StringReader(document));
        if (req != null)
            return req.getReader();
        return null;
    }
    
    /**
     * This will return all the parameter names in the HttpServletRequest object
     *   and the parameters added to this wrapper.
     */
    public Enumeration getParameterNames() {
        if ((req != null) && (params.size() > 0)) {
            Vector names = new Vector();
            Enumeration reqNames = req.getParameterNames();
            while (reqNames.hasMoreElements())
                names.addElement(reqNames.nextElement());
            reqNames = params.keys();
            while (reqNames.hasMoreElements())
                names.addElement(reqNames.nextElement());
            return params.elements();
        }   
        
        if (req != null)
            return req.getParameterNames();
            
        if (params.size() > 0)
            return params.keys();
            
        return null;        
    }
    
    public void addParameter(String name, String value) {
        if (params.containsKey(name)) {
            String[] old = (String[])params.get(name);
            String[] current = new String[old.length + 1];
            for (int i=0; i<old.length; i++)
                current[i] = old[i];
            current[old.length] = value;
            params.put(name, current);
        } else {
            String[] val = { value };
            params.put(name, val);
        }
    }
    
    public void removeParameter(String name) {
        if (params.containsKey(name))
            params.remove(name);
    }
    
    public String[] getParameterValues(String name) {                    
        if (params.containsKey(name))
            return (String[])params.get(name);        
            
        if (req != null)
            return req.getParameterValues(name);
            
        return null;
    }
    
    /**
     * This will return a parameter value for a given name.  If a custom parameter
     *   has been added, it takes precedence.  Then the wrapped object is checked.
     *   If that value is null, then the parameter is compared against the value
     *   "producer", and a default producer is returned if a match results (the 
     *   default is <code>org.apache.cocoon.producer.ProducerFromRequest</code>).
     *   Else, null is returned.
     */
    public String getParameter(String name) {
        if (params.containsKey(name))
            return ((String[])params.get(name))[0];
        if (req != null) {
            String value = req.getParameter(name);
            if (value != null)
                return value;
        }
        if ((document != null) && (name.equalsIgnoreCase("producer")))
            return "org.apache.cocoon.producer.ProducerFromRequest";
        return null;
    }
    
    public String getAuthType() {
        if (req != null)
            return req.getAuthType();
        return null;
    }
    
    public String getRemoteAddr() {
        if (req != null)
            return req.getRemoteAddr();
        return null;
    }
    
    public Object getAttribute(String name) {
        if (req != null)
            return req.getAttribute(name);
        return null;
    }
    
    public Enumeration getAttributeNames() {
        if (req != null)
            return req.getAttributeNames();
        return null;
    }
    
    public void setAttribute(String name, Object value) {
        if (req != null)
            req.setAttribute(name, value);
    }
    
    public void removeAttribute(String name) {
        if (req != null)
            req.removeAttribute(name);
    }
    
    public String getContextPath() {
        if (req != null)
            return req.getContextPath();
        return null;
    }
    
    public Cookie[] getCookies() {
        if (req != null)
            return req.getCookies();
        return null;
    }
    
    public long getDateHeader(String name) {
        if (req != null)
            return req.getDateHeader(name);
        return -1;
    }
    
    public String getHeader(String name) {
        if (req != null)
            return req.getHeader(name);
        return null;
    }
    
    public Enumeration getHeaders(String name) {
        if (req != null)
            return req.getHeaders(name);
        return null;
    }
    
    public Enumeration getHeaderNames() {
        if (req != null)
            return req.getHeaderNames();
        return null;
    }
    
    public int getIntHeader(String name) {
        if (req != null)
            return req.getIntHeader(name);
        return -1;
    }
    
    public String getMethod() {
        if (req != null)
            return req.getMethod();
        return null;
    }
    
    public String getPathTranslated() {
        if (path != null)
            return path;
        if (req != null)
            if (req.getPathTranslated() != null)
                return req.getPathTranslated();
        return File.separator;
    }
    
    public String getQueryString() {
        if (req != null)
            return req.getQueryString();
        return null;
    }
    
    public String getRemoteUser() {
        if (req != null)
            return req.getRemoteUser();
        return null;
    }
    
    public String getRequestedSessionId() {
        if (req != null)
            return req.getRequestedSessionId();
        return null;
    }
    
    public String getRequestURI() {
        if (req != null)
            return req.getRequestURI();
        return null;
    }
    
    public String getServletPath() {
        if (req != null)
            return req.getServletPath();
        return null;
    }
    
    public HttpSession getSession() {
        if (req != null)
            return req.getSession();
        return null;
    }
    
    public HttpSession getSession(boolean create) {
        if (req != null)
            return req.getSession(create);
        return null;
    }
    
    public boolean isRequestedSessionIdFromCookie() {
        if (req != null)
            return req.isRequestedSessionIdFromCookie();
        return false;
    }
    
    public boolean isRequestedSessionIdFromURL() {
        if (req != null)
            return req.isRequestedSessionIdFromURL();
        return false;
    }
    
    public boolean isRequestedSessionIdValid() {
        if (req != null)
            return req.isRequestedSessionIdValid();
        return false;
    }    
    
    /** @deprecated */
    public boolean isRequestedSessionIdFromUrl() {
        if (req != null)
            return req.isRequestedSessionIdFromUrl();
        return false;
    }
    
    public int getServerPort() {
        if (req != null)
            return req.getServerPort();
        return -1;
    }
    
    public String getServerName() {
        if (req != null)
            return req.getServerName();
        return null;
    }
    
    public String getContentType() {
        if (req != null)
            return req.getContentType();
        return null;
    }
    
    public ServletInputStream getInputStream() throws IOException {
        if (req != null)
            return req.getInputStream();
        return null;
    }
    
    public String getScheme() {
        if (req != null)
            return req.getScheme();
        return null;
    }
    
    /** @deprecated */
    public String getRealPath(String path) {
        if (req != null)
            return req.getRealPath(path);
        return null;
    }
    
    public boolean isSecure() {
        if (req != null)
            return req.isSecure();
        return false;
    }
    
    public Locale getLocale() {
        if (req != null)
            return req.getLocale();
        return null;
    }
    
    public Enumeration getLocales() {
        if (req != null)
            return req.getLocales();
        return null;
    }
    
    public boolean isUserInRole(String role) {
        if (req != null)
            return req.isUserInRole(role);
        return false;
    }
    
    public RequestDispatcher getRequestDispatcher(String path) {
        if (req != null)
            return req.getRequestDispatcher(path);
        return null;
    }
    
    public String getCharacterEncoding() {
        if (req != null)
            return req.getCharacterEncoding();
        return null;
    }
    
    public String getProtocol() {
        if (req != null)
            return req.getProtocol();
        return null;
    }
    
    public Principal getUserPrincipal() {
        if (req != null)
            return req.getUserPrincipal();
        return null;
    }
    
    public String getRemoteHost() {
        if (req != null)
            return req.getRemoteHost();
        return null;
    }
    
    public int getContentLength() {
        if (req != null)
            return req.getContentLength();
        return -1;
    }
    
}