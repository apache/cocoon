/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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
package org.apache.cocoon.components.jsp;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;

/**
 * Allows JSP to be used as a generator.  Builds upon the JSP servlet
 * functionality - overrides the output method and returns the byte(s).
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: JSPEngineImpl.java,v 1.8 2004/01/05 14:54:27 unico Exp $
 */
public class JSPEngineImpl extends AbstractLogEnabled
    implements JSPEngine, Parameterizable, ThreadSafe {

    /** The servlet include path. */
    public static final String INC_SERVLET_PATH = "javax.servlet.include.servlet_path";
    /** The servlet request uri, needed for Resin. */
    public static final String INC_REQUEST_URI = "javax.servlet.include.request_uri";

    /** The Default Servlet Class Name for Tomcat 3.X and 4.X*/
    public static final String DEFAULT_SERVLET_CLASS = "org.apache.jasper.servlet.JspServlet";

    /** Servlet Class Name */
    public String jspServletClass = DEFAULT_SERVLET_CLASS;

    /**
     * Set the sitemap-provided configuration.
     * @param params The configuration parameters
     */
    public void parameterize(Parameters params)  {
        this.jspServletClass = params.getParameter("servlet-class", DEFAULT_SERVLET_CLASS);
    }

    /**
     * execute the JSP and return the output in UTF8 encoding
     *
     * @param context The Servlet Context
     * @exception IOException
     * @exception ServletException
     * @exception Exception
     */
    public byte[] executeJSP(String url, HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServletContext context)
        throws IOException, ServletException, Exception {

        byte[] bytes = null;

        MyServletRequest request = new MyServletRequest(httpRequest, url);
        MyServletResponse response = new MyServletResponse(httpResponse);

        // start JSPServlet.
        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(this.jspServletClass);
        Servlet jsp = (Servlet) clazz.newInstance();
        jsp.init(new config(context));
        jsp.service(request, response);

        bytes = response.toByteArray();

        // clean up
        jsp.destroy();

        return bytes;
    }


    /**
     * Stub implementation of Servlet Config
     */
    class config implements ServletConfig {
        ServletContext c = null;
        public config(ServletContext c) {this.c = c; }

        public String getServletName() { return "JSPEngineImpl"; }
        public Enumeration getInitParameterNames()
               { return c.getInitParameterNames(); }
        public ServletContext getServletContext() { return c; }
        public String getInitParameter(String name) { return c.getInitParameter(name); }
    }

    /**
     * Stub implementation of HttpServletRequest
     */
    class MyServletRequest implements HttpServletRequest {
        HttpServletRequest request = null;
        String jspFile = null;

        public MyServletRequest(HttpServletRequest request, String jspFile) {
            this.request = request;
            this.jspFile = jspFile;
        }
        public String getAuthType(){ return request.getAuthType(); }
        public Cookie[] getCookies(){ return request.getCookies(); }
        public long getDateHeader(String s){ return request.getDateHeader(s); }
        public String getHeader(String s){ return request.getHeader(s); }
        public Enumeration getHeaders(String s){ return request.getHeaders(s); }
        public Enumeration getHeaderNames(){ return request.getHeaderNames(); }
        public int getIntHeader(String s){ return request.getIntHeader(s); }
        public String getMethod(){ return request.getMethod(); }
        public String getPathInfo(){ return request.getPathInfo(); }
        public String getPathTranslated(){ return request.getPathTranslated(); }
        public String getContextPath(){ return request.getContextPath(); }
        public String getQueryString(){ return request.getQueryString(); }
        public String getRemoteUser(){ return request.getRemoteUser(); }
        public boolean isUserInRole(String s){ return request.isUserInRole(s); }
        public Principal getUserPrincipal(){ return request.getUserPrincipal(); }
        public String getRequestedSessionId(){ return request.getRequestedSessionId(); }
        public String getRequestURI(){ return request.getRequestURI(); }
        public String getServletPath(){ return request.getServletPath(); }
        public HttpSession getSession(boolean flag){ return request.getSession(flag); }
        public HttpSession getSession(){ return request.getSession(); }
        public boolean isRequestedSessionIdValid(){ return request.isRequestedSessionIdValid(); }
        public boolean isRequestedSessionIdFromCookie(){ return request.isRequestedSessionIdFromCookie(); }
        public boolean isRequestedSessionIdFromURL(){ return request.isRequestedSessionIdFromURL(); }
        /** @deprecated use isRequestedSessionIdFromURL instead. */
        public boolean isRequestedSessionIdFromUrl(){ return request.isRequestedSessionIdFromUrl(); }
        public Object getAttribute(String s){
            if (s != null && (s.equals(INC_SERVLET_PATH) || s.equals(INC_REQUEST_URI))) {
                return jspFile;
            }
            return request.getAttribute(s);
        }
        public Enumeration getAttributeNames(){ return request.getAttributeNames(); }
        public String getCharacterEncoding(){ return request.getCharacterEncoding(); }
        public int getContentLength(){ return request.getContentLength(); }
        public String getContentType(){ return request.getContentType(); }
        public ServletInputStream getInputStream() throws IOException{ return request.getInputStream(); }
        public String getParameter(String s){ return request.getParameter(s); }
        public Enumeration getParameterNames(){ return request.getParameterNames(); }
        public String[] getParameterValues(String s){ return request.getParameterValues(s); }
        public String getProtocol(){ return request.getProtocol(); }
        public String getScheme(){ return request.getScheme(); }
        public String getServerName(){ return request.getServerName(); }
        public int getServerPort(){ return request.getServerPort(); }
        public BufferedReader getReader()
            throws IOException{ return request.getReader(); }
        public String getRemoteAddr(){ return request.getRemoteAddr(); }
        public String getRemoteHost(){ return request.getRemoteHost(); }
        public void setAttribute(String s, Object obj){ request.setAttribute(s,obj); }
        public void removeAttribute(String s){ request.removeAttribute(s); }
        public Locale getLocale(){ return request.getLocale(); }
        public Enumeration getLocales(){ return request.getLocales(); }
        public boolean isSecure(){ return request.isSecure(); }
        public RequestDispatcher getRequestDispatcher(String s){ return request.getRequestDispatcher(s); }
        /** @deprecated use ServletContext.getRealPath(java.lang.String) instead. */
        public String getRealPath(String s){ return request.getRealPath(s); }
        public java.lang.StringBuffer getRequestURL() { return null; }
        public java.util.Map getParameterMap() { return null; }
        public void setCharacterEncoding(java.lang.String s) { }
    }

    /**
     * Stub implementation of HttpServletResponse
     */
    class MyServletResponse implements HttpServletResponse {
        HttpServletResponse response = null;
        MyServletOutputStream output = null;
        boolean hasWriter = false;
        boolean hasOutputStream = false;

        public MyServletResponse(HttpServletResponse response){
            this.response = response;
            this.output = new MyServletOutputStream();
        }
        public void flushBuffer() throws IOException { }
        public int getBufferSize() { return 1024; }
        public String getCharacterEncoding() { return this.response.getCharacterEncoding();}
        public Locale getLocale(){ return this.response.getLocale();}
        public PrintWriter getWriter() {
            if (this.hasOutputStream) {
                throw new IllegalStateException("getOutputStream was already called.");
            }
            this.hasWriter = true;
            return this.output.getWriter();
        }
        public boolean isCommitted() { return false; }
        public void reset() {}
        public void setBufferSize(int size) {}
        public void setContentLength(int len) {}
        public void setContentType(java.lang.String type) {}
        public void setLocale(java.util.Locale loc) {}
        public ServletOutputStream getOutputStream() {
            if (this.hasWriter) {
                throw new IllegalStateException("getWriter was already called.");
            }
            this.hasOutputStream = true;
            return this.output;
        }
        public void addCookie(Cookie cookie){ response.addCookie(cookie); }
        public boolean containsHeader(String s){ return response.containsHeader(s); }
        /** @deprecated use encodeURL(String url) instead. */
        public String encodeUrl(String s){ return response.encodeUrl(s); }
        public String encodeURL(String s){ return response.encodeURL(s); }
        /** @deprecated use encodeRedirectURL(String url) instead. */
        public String encodeRedirectUrl(String s){ return response.encodeRedirectUrl(s); }
        public String encodeRedirectURL(String s){ return response.encodeRedirectURL(s); }
        public void sendError(int i, String s)
            throws IOException{response.sendError(i,s); }
        public void sendError(int i)
            throws IOException{response.sendError(i); }
        public void sendRedirect(String s)
            throws IOException{response.sendRedirect(s); }
        public void setDateHeader(String s, long l){response.setDateHeader(s, l); }
        public void addDateHeader(String s, long l){response.addDateHeader(s, l); }
        public void setHeader(String s, String s1){response.setHeader(s, s1); }
        public void addHeader(String s, String s1){response.addHeader(s, s1); }
        public void setIntHeader(String s, int i){response.setIntHeader(s, i); }
        public void addIntHeader(String s, int i){response.addIntHeader(s, i); }
        public void setStatus(int i){response.setStatus(i); }
        /** @deprecated use sendError(int, String) instead */
        public void setStatus(int i, String s){response.setStatus(i, s); }
        public void resetBuffer(){}

        public byte[] toByteArray() {
            return output.toByteArray();
        }
    }

    /**
     * Stub implementation of ServletOutputStream
     */
    class MyServletOutputStream extends ServletOutputStream {
        ByteArrayOutputStream output = null;
        PrintWriter writer = null;

        public MyServletOutputStream() {
            this.output = new ByteArrayOutputStream();
            try {
                this.writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                getLogger().error("Your JVM seems not to support UTF-8,"
                                  + " using platform default instead."
                                  + " This can cause problems as you can imagine.");
                this.writer = new PrintWriter(new OutputStreamWriter(output));
            }
        }
        public PrintWriter getWriter() {
            return this.writer;
        }
        public void write(int b) throws IOException  {
            this.output.write(b);
        }
        public byte[] toByteArray() {
            this.writer.flush();
            byte[] bytes = output.toByteArray();
            return bytes;
        }
    }
}
