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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


/**
 * Stub implementation of HttpServletResponse.
 */
public class JSPEngineServletResponse implements HttpServletResponse {

    private final HttpServletResponse servletResponse;
    private final JSPEngineServletOutputStream output;
    
    private boolean hasWriter = false;
    private boolean hasOutputStream = false;

    public JSPEngineServletResponse(HttpServletResponse servletResponse, JSPEngineServletOutputStream output) {
        this.servletResponse = servletResponse;
        this.output = output;
    }
    public String getCharacterEncoding() {
        return this.servletResponse.getCharacterEncoding();
    }
    public Locale getLocale(){
        return this.servletResponse.getLocale();
    }
    public PrintWriter getWriter() {
        if (this.hasOutputStream) {
            throw new IllegalStateException("getOutputStream was already called.");
        }
        this.hasWriter = true;
        return this.output.getWriter();
    }
    public void setContentLength(int len) {
        // this value can be overriden by cocoon
        servletResponse.setContentLength(len);
    }
    public void setContentType(java.lang.String type) {
        servletResponse.setContentType(type);
    }
    public void setLocale(java.util.Locale loc) {
        servletResponse.setLocale(loc);
    }
    public ServletOutputStream getOutputStream() {
        if (this.hasWriter) {
            throw new IllegalStateException("getWriter was already called.");
        }
        this.hasOutputStream = true;
        return this.output;
    }
    public void addCookie(Cookie cookie){
        servletResponse.addCookie(cookie);
    }
    public boolean containsHeader(String s){
        return servletResponse.containsHeader(s);
    }
    /** @deprecated use encodeURL(String url) instead. */
    public String encodeUrl(String s){
        return servletResponse.encodeUrl(s);
    }
    public String encodeURL(String s){
        return servletResponse.encodeURL(s);
    }
    /** @deprecated use encodeRedirectURL(String url) instead. */
    public String encodeRedirectUrl(String s){
        return servletResponse.encodeRedirectUrl(s);
    }
    public String encodeRedirectURL(String s){
        return servletResponse.encodeRedirectURL(s);
    }
    public void sendError(int i, String s) throws IOException{
        servletResponse.sendError(i,s); 
    }
    public void sendError(int i) throws IOException{
        servletResponse.sendError(i);
    }
    public void sendRedirect(String s) throws IOException{
        servletResponse.sendRedirect(s);
    }
    public void setDateHeader(String s, long l) {
        servletResponse.setDateHeader(s, l);
    }
    public void addDateHeader(String s, long l) {
        servletResponse.addDateHeader(s, l);
    }
    public void setHeader(String s, String s1) {
        servletResponse.setHeader(s, s1);
    }
    public void addHeader(String s, String s1) {
        servletResponse.addHeader(s, s1);
    }
    public void setIntHeader(String s, int i) {
        servletResponse.setIntHeader(s, i);
    }
    public void addIntHeader(String s, int i) {
        servletResponse.addIntHeader(s, i);
    }
    public void setStatus(int i){
        servletResponse.setStatus(i);
    }
    /** @deprecated use sendError(int, String) instead */
    public void setStatus(int i, String s){
        servletResponse.setStatus(i, s);
    }
    public void resetBuffer() {}
    public void reset() {}
    public int getBufferSize() { return 1024; }
    public void setBufferSize(int size) {}
    public void flushBuffer() throws IOException {}
    public boolean isCommitted() { return false; }

}
