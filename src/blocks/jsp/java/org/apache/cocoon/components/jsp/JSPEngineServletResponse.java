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
