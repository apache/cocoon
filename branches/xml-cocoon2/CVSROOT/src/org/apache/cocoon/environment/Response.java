/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.environment;

import java.util.Locale;
import java.io.IOException;

import javax.servlet.http.Cookie;


/**
 * Defines an interface to provide client response information .  
 * 
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-04-09 11:15:47 $
 *
 */

public interface Response {

    /**
     * Returns the name of the charset used for
     * the MIME body sent in this response.
     *
     * <p>If no charset has been assigned, it is implicitly
     * set to <code>ISO-8859-1</code> (<code>Latin-1</code>).
     *
     * <p>See RFC 2047 (http://ds.internic.net/rfc/rfc2045.txt)
     * for more information about character encoding and MIME.
     *
     * @return		a <code>String</code> specifying the
     *			name of the charset, for
     *			example, <code>ISO-8859-1</code>
     *
     */
  
    public String getCharacterEncoding();
    
    /**
     * Sets the length of the content body in the response
     * In HTTP servlets, this method sets the HTTP Content-Length header.
     *
     *
     * @param len 	an integer specifying the length of the 
     * 			content being returned to the client; sets
     *			the Content-Length header
     *
     */

    public void setContentLength(int len);
    
    /**
     * Sets the content type of the response being sent to
     * the client. The content type may include the type of character
     * encoding used, for example, <code>text/html; charset=ISO-8859-4</code>.
     *
     *
     * @param type 	a <code>String</code> specifying the MIME 
     *			type of the content
     *
     */

    public void setContentType(String type);
    
    /**
     * Sets the locale of the response, setting the headers (including the
     * Content-Type's charset) as appropriate.  By default, the response locale
     * is the default locale for the server.
     * 
     * @param loc  the locale of the response
     *
     * @see 		#getLocale
     *
     */

    public void setLocale(Locale loc);
    
    /**
     * Returns the locale assigned to the response.
     * 
     * 
     * @see 		#setLocale
     *
     */

    public Locale getLocale();

    /**
     * Adds the specified cookie to the response.  This method can be called
     * multiple times to set more than one cookie.
     *
     * @param cookie the Cookie to return to the client
     *
     */

    public void addCookie(Cookie cookie);

    /**
     * Returns a boolean indicating whether the named response header 
     * has already been set.
     * 
     * @param	name	the header name
     * @return		<code>true</code> if the named response header 
     *			has already been set; 
     * 			<code>false</code> otherwise
     */

    public boolean containsHeader(String name);

    /**
     * Encodes the specified URL by including the session ID in it,
     * or, if encoding is not needed, returns the URL unchanged.
     * The implementation of this method includes the logic to
     * determine whether the session ID needs to be encoded in the URL.
     * For example, if the browser supports cookies, or session
     * tracking is turned off, URL encoding is unnecessary.
     * 
     * <p>For robust session tracking, all URLs emitted by a servlet 
     * should be run through this
     * method.  Otherwise, URL rewriting cannot be used with browsers
     * which do not support cookies.
     *
     * @param	url	the url to be encoded.
     * @return		the encoded URL if encoding is needed;
     * 			the unchanged URL otherwise.
     */

    public String encodeURL(String url);

    /**
     * Encodes the specified URL for use in the
     * <code>sendRedirect</code> method or, if encoding is not needed,
     * returns the URL unchanged.  The implementation of this method
     * includes the logic to determine whether the session ID
     * needs to be encoded in the URL.  Because the rules for making
     * this determination can differ from those used to decide whether to
     * encode a normal link, this method is seperate from the
     * <code>encodeURL</code> method.
     * 
     * <p>All URLs sent to the <code>HttpServletResponse.sendRedirect</code>
     * method should be run through this method.  Otherwise, URL
     * rewriting cannot be used with browsers which do not support
     * cookies.
     *
     * @param	url	the url to be encoded.
     * @return		the encoded URL if encoding is needed;
     * 			the unchanged URL otherwise.
     *
     * @see #sendRedirect
     */

    public String encodeRedirectURL(String url);

    /**
     * Sends a temporary redirect response to the client using the
     * specified redirect location URL.  This method can accept relative URLs;
     * the servlet container will convert the relative URL to an absolute URL
     * before sending the response to the client.
     *
     * <p>If the response has already been committed, this method throws 
     * an IllegalStateException.
     * After using this method, the response should be considered
     * to be committed and should not be written to.
     *
     * @param		location	the redirect location URL
     * @exception	IOException	If an input or output exception occurs
     * @exception	IllegalStateException	If the response was committed
     */

    public void sendRedirect(String location) throws IOException;
    
    /**
     * 
     * Sets a response header with the given name and
     * date-value.  The date is specified in terms of
     * milliseconds since the epoch.  If the header had already
     * been set, the new value overwrites the previous one.  The
     * <code>containsHeader</code> method can be used to test for the
     * presence of a header before setting its value.
     * 
     * @param	name	the name of the header to set
     * @param	value	the assigned date value
     * 
     * @see #containsHeader
     * @see #addDateHeader
     */

    public void setDateHeader(String name, long date);
    
    /**
     * 
     * Adds a response header with the given name and
     * date-value.  The date is specified in terms of
     * milliseconds since the epoch.  This method allows response headers 
     * to have multiple values.
     * 
     * @param	name	the name of the header to set
     * @param	value	the additional date value
     * 
     * @see #setDateHeader
     */

    public void addDateHeader(String name, long date);
    
    /**
     *
     * Sets a response header with the given name and value.
     * If the header had already been set, the new value overwrites the
     * previous one.  The <code>containsHeader</code> method can be
     * used to test for the presence of a header before setting its
     * value.
     * 
     * @param	name	the name of the header
     * @param	value	the header value
     *
     * @see #containsHeader
     * @see #addHeader
     */

    public void setHeader(String name, String value);
    
    /**
     * Adds a response header with the given name and value.
     * This method allows response headers to have multiple values.
     * 
     * @param	name	the name of the header
     * @param	value	the additional header value
     *
     * @see #setHeader
     */

    public void addHeader(String name, String value);

    /**
     * Sets the status code for this response.  This method is used to
     * set the return status code when there is no error (for example,
     * for the status codes SC_OK or SC_MOVED_TEMPORARILY).  
     *
     * @param	sc	the status code
     *
     */

    public void setStatus(int sc);
  
}



