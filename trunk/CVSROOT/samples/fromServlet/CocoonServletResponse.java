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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.*;
import javax.servlet.http.*;
 
/**
 * This allows the wrapping of a request to Cocoon from other applications.
 *   It is similar to HttpServletReqImpl used in EngineWrapper, but is generic 
 *   enough to work anywhere, not just with ProducerFromFile.
 * It can be used to push files through to Cocoon, or Strings (generated from
 *   some other application.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:bmclaugh@algx.net">Brett McLaughlin</a>
 * @version $Revision: 1.2 $ $Date: 2000-02-13 18:29:14 $
 */ 
public class CocoonServletResponse implements HttpServletResponse {
        
        private PrintWriter out;
        
        public CocoonServletResponse(PrintWriter out) {
            this.out = out;
        }

        public PrintWriter getWriter() throws IOException {
            return this.out;
        }
        
        public void setContentLength(int len) {}
        public void setContentType(String type) {}
        public ServletOutputStream getOutputStream() throws IOException { return null; }
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