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
package weblogic.servlet.internal;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * **********************************************************************
 * *                            W A R N I N G                           *
 * **********************************************************************
 *
 *  This is a mock object of the class, not the actual class.
 *  It's used to compile the code in absence of the actual class.
 *
 *  This class is created by hand, not automatically.
 *
 * **********************************************************************
 * 
 * @version CVS $Id: ServletResponseImpl.java,v 1.3 2004/03/01 03:50:59 antonio Exp $
 */
 
public class ServletResponseImpl implements HttpServletResponse {

    public ServletResponseImpl() {
        super();
    }
    
    public ServletResponseImpl(ServletContextImpl sci) {
        super();
    }

    public void setOutputStream(ServletOutputStreamImpl sosi) {
    }
    
    public void addCookie(javax.servlet.http.Cookie arg1) {
    }

    public void addDateHeader(java.lang.String arg1, long arg2) {
    }

    public void addHeader(java.lang.String arg1, java.lang.String arg2) {
    }

    public void addIntHeader(java.lang.String arg1, int arg2) {
    }

    public boolean containsHeader(String arg1) {
        return false;
    }

    /** @deprecated The method ServletResponseImpl.encodeRedirectUrl(String)
     *              overrides a deprecated method from HttpServletResponse */
    public String encodeRedirectUrl(String arg1) {
        return null;
    }

    public String encodeRedirectURL(String arg1) {
        return null;
    }

    /** @deprecated The method ServletResponseImpl.encodeUrl(String) overrides
     *              a deprecated method from HttpServletResponse */
    public String encodeUrl(String arg1) {
        return null;
    }

    public String encodeURL(String arg1) {
        return null;
    }

    public void flushBuffer() throws java.io.IOException {
    }

    public int getBufferSize() {
        return 0;
    }

    public String getCharacterEncoding() {
        return null;
    }

    public java.util.Locale getLocale() {
        return null;
    }

    public javax.servlet.ServletOutputStream getOutputStream()
        throws java.io.IOException {
        return null;
    }

    public String getOutputStreamContents() {
        return "";
    }

    public java.io.PrintWriter getWriter() throws java.io.IOException {
        return null;
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {
    }

    public void resetBuffer(){
    }

    public void setExpectedError(int anErrorCode){
    }

    public void setExpectedError(int anErrorCode, String anErrorMessage){
    }

    public void setExpectedErrorNothing(){
    }

    public void sendError(int anErrorCode) throws java.io.IOException {
    }

    public void sendError(int anErrorCode, String anErrorMessage)
    throws IOException {
    }

    public void sendRedirect(String aURL) throws java.io.IOException {
    }

    public void setBufferSize(int arg1) {
    }

    public void setContentLength(int arg1) {
    }

    public void setContentType(String contentType) {
    }

    public void setDateHeader(String arg1, long arg2) {
    }

    public void setExpectedContentType(String contentType) {
    }

    public void setExpectedHeader(String key, String value) {
    }

    public void setExpectedRedirect(String aURL) throws IOException {
    }

    public void setExpectedSetStatusCalls(int callCount) {
    }

    public void setHeader(String key, String value) {
    }

    public void setIntHeader(String arg1, int arg2) {
    }

    public void setLocale(java.util.Locale arg1) {
    }

    public void setStatus(int status) {
    }

    /** @deprecated The method ServletResponseImpl.setStatus(int, String)
     *              overrides a deprecated method from HttpServletResponse */
    public void setStatus(int arg1, String arg2) {
    }

    public void setupOutputStream(ServletOutputStream anOutputStream) {
    }
}
