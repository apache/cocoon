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
package org.apache.cocoon.environment;

import java.util.Locale;

/**
 * Defines an interface to provide client response information .
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: Response.java,v 1.2 2003/12/23 15:28:32 joerg Exp $
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
     * @return                a <code>String</code> specifying the
     *                        name of the charset, for
     *                        example, <code>ISO-8859-1</code>
     *
     */

    String getCharacterEncoding();

    /**
     * Sets the locale of the response, setting the headers (including the
     * Content-Type's charset) as appropriate.  By default, the response locale
     * is the default locale for the server.
     *
     * @param loc  the locale of the response
     *
     * @see                 #getLocale()
     *
     */

    void setLocale(Locale loc);

    /**
     * Returns the locale assigned to the response.
     *
     *
     * @see                 #setLocale(Locale)
     *
     */

    Locale getLocale();

    /**
     * Constructs a cookie with a specified name and value.
     *
     * <p>The name must conform to RFC 2109. That means it can contain
     * only ASCII alphanumeric characters and cannot contain commas,
     * semicolons, or white space or begin with a $ character. The cookie's
     * name cannot be changed after creation.
     *
     * <p>The value can be anything the server chooses to send. Its
     * value is probably of interest only to the server. The cookie's
     * value can be changed after creation with the
     * <code>setValue</code> method.
     *
     * <p>By default, cookies are created according to the Netscape
     * cookie specification. The version can be changed with the
     * <code>setVersion</code> method.
     *
     *
     * @param name                         a <code>String</code> specifying the name of the cookie
     *
     * @param value                        a <code>String</code> specifying the value of the cookie
     *
     * @throws IllegalArgumentException        if the cookie name contains illegal characters
     *                                        (for example, a comma, space, or semicolon)
     *                                        or it is one of the tokens reserved for use
     *                                        by the cookie protocol
     *
     */
    Cookie createCookie(String name, String value);

    /**
     * Adds the specified cookie to the response.  This method can be called
     * multiple times to set more than one cookie.
     *
     * @param cookie the Cookie to return to the client
     *
     */

    void addCookie(Cookie cookie);

    /**
     * Returns a boolean indicating whether the named response header
     * has already been set.
     *
     * @param        name        the header name
     * @return                <code>true</code> if the named response header
     *                        has already been set;
     *                         <code>false</code> otherwise
     */

    boolean containsHeader(String name);

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
     * @param        url        the url to be encoded.
     * @return                the encoded URL if encoding is needed;
     *                         the unchanged URL otherwise.
     */

    String encodeURL(String url);

    /**
     *
     * Sets a response header with the given name and
     * date-value.  The date is specified in terms of
     * milliseconds since the epoch.  If the header had already
     * been set, the new value overwrites the previous one.  The
     * <code>containsHeader</code> method can be used to test for the
     * presence of a header before setting its value.
     *
     * @param        name        the name of the header to set
     * @param        date        the assigned date value
     *
     * @see #containsHeader(String)
     * @see #addDateHeader(String, long)
     */

    void setDateHeader(String name, long date);

    /**
     *
     * Adds a response header with the given name and
     * date-value.  The date is specified in terms of
     * milliseconds since the epoch.  This method allows response headers
     * to have multiple values.
     *
     * @param        name        the name of the header to set
     * @param        date        the additional date value
     *
     * @see #setDateHeader(String, long)
     */

    void addDateHeader(String name, long date);

    /**
     *
     * Sets a response header with the given name and value.
     * If the header had already been set, the new value overwrites the
     * previous one.  The <code>containsHeader</code> method can be
     * used to test for the presence of a header before setting its
     * value.
     *
     * @param        name        the name of the header
     * @param        value        the header value
     *
     * @see #containsHeader(String)
     * @see #addHeader(String, String)
     */

    void setHeader(String name, String value);

    /**
     * Adds a response header with the given name and value.
     * This method allows response headers to have multiple values.
     *
     * @param        name        the name of the header
     * @param        value        the additional header value
     *
     * @see #setHeader(String, String)
     */

    void addHeader(String name, String value);

    /**
     * Sets a response header with the given name and
     * int value. If the header had already
     * been set, the new value overwrites the previous one.  The
     * <code>containsHeader</code> method can be used to test for the
     * presence of a header before setting its value.
     *
     * @param        name        the name of the header to set
     * @param        value       the assigned int value
     *
     * @see #containsHeader(String)
     * @see #addIntHeader(String, int)
     */

    void setIntHeader(String name, int value);

    /**
     * Adds a response header with the given name and
     * int value. This method allows response headers
     * to have multiple values.
     *
     * @param        name        the name of the header to set
     * @param        value       the additional int value
     *
     * @see #setIntHeader(String, int)
     */

    void addIntHeader(String name, int value);
}
