/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.logger.AbstractLoggable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.xml.AbstractXMLProducer;
import org.apache.cocoon.xml.XMLProducer;
import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.log.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Allows JSP to be used as a generator.  Builds upon the JSP servlet
 * functionallity - overrides the output method in order to pipe the
 * results into SAX events.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2001-05-01 14:41:34 $
 */
public class JspGenerator extends ServletGenerator implements Poolable {

    public static final String INC_SERVLET_PATH = "javax.servlet.include.servlet_path";

    /**
     * Generate XML data from JSP.
     */
    public void generate() throws IOException, SAXException, ProcessingException {

        // ensure that we are running in a servlet environment
        HttpServletResponse httpResponse =
            (HttpServletResponse)this.objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
        HttpServletRequest httpRequest =
            (HttpServletRequest)this.objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
        if (httpResponse == null || httpRequest == null) {
            throw new ProcessingException("HttpServletRequest or HttpServletResponse object not available");
        }

        Parser parser = null;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MyServletRequest request = new MyServletRequest(httpRequest, this.source);
            MyServletResponse response = new MyServletResponse(httpResponse, output);

            // start JSPServlet.
            Class clazz = Class.forName("org.apache.jasper.servlet.JspServlet");
            HttpServlet jsp = (HttpServlet) clazz.newInstance();
            jsp.init(new config((ServletContext)this.objectModel.get(HttpEnvironment.HTTP_SERVLET_CONTEXT)));
            jsp.service(request, response);
            output.close();

            ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

            // pipe the results into the parser
            parser = (Parser)this.manager.lookup(Roles.PARSER);
            parser.setContentHandler(this.contentHandler);
            parser.setLexicalHandler(this.lexicalHandler);
            parser.parse(new InputSource(input));

            // clean up
            jsp.destroy();
        } catch (SAXException e) {
            getLogger().debug("JspGenerator.generate()", e);
            throw e;
        } catch (IOException e) {
            getLogger().debug("JspGenerator.generate()", e);
            throw e;
        } catch (Exception e) {
            getLogger().debug("JspGenerator.generate()", e);
            throw new IOException(e.toString());
        } finally {
            if (parser != null) this.manager.release((Component)parser);
        }
    }

    /**
     * Stub implementation of Servlet Config
     */
    class config implements ServletConfig {
        ServletContext c;
        public config(ServletContext c) {this.c = c; }

        public String getServletName() { return "JspGenerator"; }
        public Enumeration getInitParameterNames()
               { return c.getInitParameterNames(); }
        public ServletContext getServletContext() { return c; }
        public String getInitParameter(String name) { return null; }
    }

    /**
     * Stub implementation of HttpServletRequest
     */
    class MyServletRequest implements HttpServletRequest {
        HttpServletRequest request;
        String jspFile;

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
        public boolean isRequestedSessionIdFromUrl(){ return request.isRequestedSessionIdFromUrl(); }
        public Object getAttribute(String s){
            if(s != null && s.equals(INC_SERVLET_PATH))
                return jspFile;
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
        public String getRealPath(String s){ return request.getRealPath(s); }
        public java.lang.StringBuffer getRequestURL() { return null; }
        public java.util.Map getParameterMap() { return null; }
        public void setCharacterEncoding(java.lang.String $1) { }
    }

    /**
     * Stub implementation of HttpServletResponse
     */
    class MyServletResponse implements HttpServletResponse {
        ServletOutputStream output;
        HttpServletResponse response;

        public MyServletResponse(HttpServletResponse response, OutputStream output){
            this.response = response;
            this.output = new MyServletOutputStream(output);
        }
        public void flushBuffer() throws IOException { this.output.flush(); }
        public int getBufferSize() { return 1024; }
        public String getCharacterEncoding() { return this.response.getCharacterEncoding();}
        public Locale getLocale(){ return this.response.getLocale();}
        public PrintWriter getWriter() {
            return new PrintWriter(this.output);
        }
        public boolean isCommitted() { return false; }
        public void reset() {}
        public void setBufferSize(int size) {}
        public void setContentLength(int len) {}
        public void setContentType(java.lang.String type) {}
        public void setLocale(java.util.Locale loc) {}
        public ServletOutputStream getOutputStream() {
            return this.output;
        }
        public void addCookie(Cookie cookie){ response.addCookie(cookie); }
        public boolean containsHeader(String s){ return response.containsHeader(s); }
        public String encodeURL(String s){ return response.encodeURL(s); }
        public String encodeRedirectURL(String s){ return response.encodeRedirectURL(s); }
        public String encodeUrl(String s){ return response.encodeUrl(s); }
        public String encodeRedirectUrl(String s){ return response.encodeRedirectUrl(s); }
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
        public void setStatus(int i, String s){response.setStatus(i, s); }
        public void resetBuffer(){}

    }

    /**
     * Stub implementation of ServletOutputStream
     */
    class MyServletOutputStream extends ServletOutputStream {
        OutputStream output;
        public MyServletOutputStream(OutputStream output) {
            this.output = output;
        }
        public void write(byte[] b) throws java.io.IOException {
            output.write(b);
        }
        public void write(byte[] b, int off, int len) throws java.io.IOException  {
            output.write(b,off,len);
        }
        public void write(int b) throws java.io.IOException  {
            output.write(b);
        }
     }
}
