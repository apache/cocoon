/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.cocoon.sitemap.Response;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-11 13:14:47 $
 */
public class CocoonServletResponse implements Response, HttpServletResponse {
    /** Deny public construction */
    private CocoonServletResponse() {}
    
    /** This response <code>HttpServletResponse</code> */
    private HttpServletResponse response=null;
    
    /**
     * Create a new instance of this <code>CocoonServletResponse</code>
     */
    protected CocoonServletResponse(HttpServletResponse res) {
        super();
        this.response=res;
    }

    /**
     * Adds a field to the response header with the given name and value.
     */
    public void setHeader(String name, String value) {
        this.response.setHeader(name,value);
    }

    /**
     * Adds a field to the response header with the given name and
     * integer value.
     */
    public void setIntHeader(String name, int value) {
        this.response.setIntHeader(name,value);
    }

    /**
     * Adds a field to the response header with the given name and
     * date-valued field.
     */
    public void setDateHeader(String name, long date) {
        this.response.setDateHeader(name,date);
    }

    /**
     * Sets the content type for this response.
     */
    public void setContentType(String type) {
        this.response.setContentType(type);
    }

    /**
     * Sets the content length for this response.
     * <br>
     * This method always throw an IllegalStateException since components are
     * not allowed to set content lengths, as they don't use the output stream
     * to generate output.
     *
     * @exception IllegalStateException This exception is always thrown.
     */
    public void setContentLength(int len) {
        throw new IllegalStateException("Cannot set content length now");
    }

    /**
     * Returns an output stream for writing binary response data.
     * <br>
     * NOTE: (PF) This method always throw an IOException since components
     * cannot have access to the servlet output stream. They have to rely on
     * serializers.
     *
     * @exception IOException This exception is always thrown.
     */
    public ServletOutputStream getOutputStream()
    throws IOException {
        throw new IOException("Cannot access servlet response output stream");
    }

    /**
     * Returns a print writer for writing formatted text responses.
     * <br>
     * NOTE: (PF) This method always throw an IOException since components
     * cannot have access to the servlet output stream. They have to rely on
     * serializers.
     *
     * @exception IOException This exception is always thrown.
     */
    public PrintWriter getWriter()
    throws IOException {
        throw new IOException("Cannot access servlet response writer");
    }

    /**
     * Returns the character set encoding used for this MIME body.
     */
    public String getCharacterEncoding() {
        return(this.response.getCharacterEncoding());
    }

    /**
     * Adds the specified cookie to the response.
     */
    public void addCookie(Cookie cookie) {
        this.response.addCookie(cookie);
    }

    /**
     * Checks whether the response message header has a field with
     * the specified name.
     */
    public boolean containsHeader(String name) {
        return(this.response.containsHeader(name));
    }

    /**
     * Sets the status code and message for this response.
     */
    public void setStatus(int statusCode, String message) {
        this.response.setStatus(statusCode, message);
    }

    /**
     * Sets the status code for this response.
     */
    public void setStatus(int statusCode) {
        this.response.setStatus(statusCode);
    }

    /**
     * Sends an error response to the client using the specified status
     * code and descriptive message.
     * <br>
     * NOTE: (PF) This method always throws a <code>CocoonException</code>
     * that will be handled by the sitemap exception handler.
     */
    public void sendError(int statusCode, String message)
    throws IOException {
        //throw new CocoonException(statusCode,message);
    }        

    /**
     * Sends an error response to the client using the specified
     * status code and a default message.
     * <br>
     * NOTE: (PF) This method always throws a <code>CocoonException</code>
     * that will be handled by the sitemap exception handler.
     */
    public void sendError(int statusCode)
    throws IOException {
        //throw new CocoonException(statusCode);
    }

    /**
     * Sends a temporary redirect response to the client using the
     * specified redirect location URL.
     * <br>
     * NOTE: (PF) This method always throws a
     * <code>CocoonRedirectionException</code> that will be handled by the
     * sitemap exception handler.
     */
    public void sendRedirect(String location)
    throws IOException {
        //throw new CocoonRedirectionException(location);
    }

    /**
     * Encodes the specified URL by including the session ID in it,
     * or, if encoding is not needed, returns the URL unchanged.
     */
    public String encodeUrl(String url) {
        return(this.response.encodeUrl(url));
    }

    /**
     * Encodes the specified URL for use in the
     * <code>sendRedirect</code> method or, if encoding is not needed,
     * returns the URL unchanged.
     * <br>
     * NOTE: (PF) This method calls simply <code>encodeUrl(url)</code> as the
     * complete URL mapping for rediriection is handled by the sitemap exception
     * handler.
     */
    public String encodeRedirectUrl(String url) {
        return(this.response.encodeUrl(url));
    }
}
