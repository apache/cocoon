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
 * @version CVS $Id: ServletResponseImpl.java,v 1.4 2004/03/05 13:01:58 bdelacretaz Exp $
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
