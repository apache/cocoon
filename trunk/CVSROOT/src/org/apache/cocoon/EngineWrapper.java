/*-- $Id: EngineWrapper.java,v 1.14 2001-02-24 18:20:42 greenrd Exp $ -- 

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
package org.apache.cocoon;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.cocoon.framework.*;

/**
 * This class implements wrapping methods that allow the engine to be
 * called even from other APIs or standalone applications.
 *
 * NOTE: this is a dirty hack and I know it. The problem is that Cocoon is
 * a servlet and the servlet API are not that easy to deal with when you
 * enter other modes of operation (like command line or RMI).
 * 
 * We will need to clean this up and remove the need of direct 
 * HttpServletRequest/Response emulation when we integrate with Stylebook.
 * But I have more important stuff to do right now.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.14 $ $Date: 2001-02-24 18:20:42 $
 */

public class EngineWrapper {

    private Engine engine;
    private String userAgent;
    
    protected EngineWrapper(Configurations confs) throws Exception {
        this.engine = Engine.getInstance(confs, new Object());
        this.userAgent = (String)confs.get("user-agent");
    }

    public void handle(OutputStream out, File pathToDocument) throws Exception {
        this.engine.handle(new HttpServletRequestImpl(pathToDocument), new HttpServletResponseImpl(out));
    }

    public void handle(OutputStream out, File documentPath, String document) throws Exception {
        this.engine.handle(new HttpServletRequestImpl(documentPath, document), new HttpServletResponseImpl(out));
    }

    /**
     * Dummy implementation of the HttpServletRequest class to create a 
     * fake but functional request for the main engine.
     * NOTE: this works only with the default file producer.
     */
    public class HttpServletRequestImpl implements HttpServletRequest {
        
        private String path = "/";
        private String document = null;
        
        public HttpServletRequestImpl(File path) {
            this(path, null);
        }

        public HttpServletRequestImpl(File path, String document) {
            if (path != null) {
                this.path = path.toString();
            }
            
            if (document != null) {
                this.document = document;
            }
        }

        public String getPathTranslated() {
            return this.path;
        }

        // FIXME: this is a quick hack to make command line operation work
        // with FileProducer. Check this when Servlet 2.2 are in place.
        public String getPathInfo() { return ""; }

        public String getParameter(String name) { 
            if (name.equalsIgnoreCase("user-agent"))
              return userAgent;
            else
                return null;
        }

        public BufferedReader getReader () throws IOException { 
            return (document == null) ? null : new BufferedReader(new StringReader(document)); 
        }
        
        public Enumeration getParameterNames() { return null; }
        public String[] getParameterValues(String name) { return null; }
        public int getContentLength() { return -1; }
        public String getContentType() { return null; }
        public String getProtocol()  { return "none"; }
        public String getScheme() { return "none"; }
        public String getServerName() { return Cocoon.version(); }
        public int getServerPort() { return -1; }
        public String getRemoteAddr() { return null; }
        public String getRemoteHost() { return null; }
        public ServletInputStream getInputStream() throws IOException { return null; }
        public Object getAttribute(String name) { return null; }
        public String getCharacterEncoding () { return null; }
        public Cookie[] getCookies() { return null; }
        public String getMethod() { return null; }
        public String getRequestURI() { return null; }
        public String getServletPath() { return null; }
        public String getQueryString() { return null; }
        public String getRemoteUser() { return null; }
        public String getAuthType() { return null; }
        public String getHeader(String name) { return null; }
        public int getIntHeader(String name) { return -1; }
        public long getDateHeader(String name) { return -1; }
        public Enumeration getHeaderNames() { return null; }
        public HttpSession getSession(boolean create) { return null; }
        public String getRequestedSessionId() { return null; }
        public boolean isRequestedSessionIdValid() { return false; }
        public boolean isRequestedSessionIdFromCookie() { return false; }
        public boolean isRequestedSessionIdFromURL() { return false; }
        public Enumeration getAttributeNames() { return null; }
        public void setAttribute(String name, Object value) {}
        public void removeAttribute(String name) {}
        public Locale getLocale() { return null; }
        public Enumeration getLocales() { return null; }
        public HttpSession getSession() { return null; }
        public boolean isSecure() { return false; }
        public RequestDispatcher getRequestDispatcher(String path) { return null; }
        public Enumeration getHeaders(String name) { return null; }
        public String getContextPath() { return null; }
        public boolean isUserInRole(String role) { return false; }
        public java.security.Principal getUserPrincipal() { return null; }

        /** @deprecated */
        public String getRealPath(String path) { return null; }
        /** @deprecated */
        public boolean isRequestedSessionIdFromUrl() { return false; }
    }

    /**
     * Dummy implementation of the HttpServletResponse class to create a 
     * fake but funtional response for the main engine.
     */
    public class HttpServletResponseImpl implements HttpServletResponse {
        
        private OutputStream out;
        
        public HttpServletResponseImpl(OutputStream out) {
            this.out = out;
        }

        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(new OutputStreamWriter(this.out));
        }

        public ServletOutputStream getOutputStream() throws IOException {
            return new ServletOutputStream()
            {
                public void write(int c) throws IOException
                {
                    out.write(c);
                }
                public void write(byte[] b, int off, int len)
                    throws IOException
               {
                    out.write(b,off,len);
                }
            };
        }
        
        public void setContentLength(int len) {}
        public void setContentType(String type) {}
        public String getCharacterEncoding() { return null; }
        public void addCookie(Cookie cookie) {}
        public boolean containsHeader(String name) { return false; }
        public void setStatus(int sc) {}
        public void setHeader(String name, String value) {}
        public void setIntHeader(String name, int value) {}
        public void setDateHeader(String name, long date) {}
        public void sendError(int sc, String msg) throws IOException {}
        public void sendError(int sc) throws IOException {}
        public void sendRedirect(String location) throws IOException {}
        public String encodeURL (String url) { return url; }
        public String encodeRedirectURL (String url) { return url; }
        public void setBufferSize(int size) { }
        public int getBufferSize() { return 0; }
        public void flushBuffer() { }
        public boolean isCommitted() { return false; }
        public void reset() { }
        public void setLocale(Locale locale) { }
        public Locale getLocale() { return null; }
        public void addDateHeader(String name, long date) { }
        public void addHeader(String name, String value) { }
        public void addIntHeader(String name, int value) { }
        
        /** @deprecated */
        public void setStatus(int sc, String sm) {}
        /** @deprecated */
        public String encodeUrl (String url) { return url; }
        /** @deprecated */
        public String encodeRedirectUrl (String url) { return url; }
    }
}
