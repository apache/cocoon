/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.environment;

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Defines an interface to provide client request information .
 *
 * A client can bind an object attribute into a <code>Request</code> by name.

 * All objects stored in the request using the setAttribute method
 * are available to all sub requests and the main request associatiated
 * Objects stored in the request using the setLocalAttribute method are
 * only available for the current (sub) request.
 * 
 * @version $Id$
 */
public interface Request extends ValueHolder, HttpServletRequest {

    /**
     * This constant defines an request wide scope for the request attribute.
     * @since 2.2
     */
    public static final int GLOBAL_SCOPE = 1;

    /**
     * This constant defines the scope of the request attribute to be
     * private to the current (sub) request.
     * @since 2.2
     */
    public static final int REQUEST_SCOPE = 2;

    /**
     *
     * Returns the value of the named parameter as an <code>Object</code>,
     * or <code>null</code> if no parameter of the given name exists.
     * Basically, this method is similar to {@link #getParameter(String)},
     * but it returns an object instead. 
     * This is useful when special processing has been made on these parameters,
     * for example for file uploads. In this case you get an object 
     * representing the uploaded file.
     * If the parameters have not been processed, you either get a String
     * object if the parameter has one value, or a Collection of Strings
     * if the parameter has more than one value.
     * 
     * @param name        a <code>String</code> specifying the name of
     *                        the parameter
     *
     * @return                an <code>Object</code> containing the value
     *                        of the parameter, or <code>null</code> if
     *                        the parameter does not exist
     *
     */

    Object get(String name);

    /**
     *
     * Returns the value of the named attribute from the <code>GLOBAL_SCOPE</code>
     * as an <code>Object</code>, or <code>null</code> if no attribute 
     * of the given name exists.
     *
     * @param name        a <code>String</code> specifying the name of
     *                        the attribute
     *
     * @return                an <code>Object</code> containing the value
     *                        of the attribute, or <code>null</code> if
     *                        the attribute does not exist
     *
     */
    Object getAttribute(String name);

    /**
     * Returns the value of the named attribute from the <code>LOCAL_SCOPE</code>
     * as an <code>Object</code>, or <code>null</code> if no attribute
     * of the given name exists.
     *
     * @param name        a <code>String</code> specifying the name of
     *                        the attribute
     *
     * @return                an <code>Object</code> containing the value
     *                        of the attribute, or <code>null</code> if
     *                        the attribute does not exist
     *
     * @since 2.2
     */
    Object getLocalAttribute(String name);

    /**
     * Returns an <code>Enumeration</code> containing the
     * names of the attributes available to this request globally.
     * This method returns an empty <code>Enumeration</code>
     * if the request has no attributes available to it.
     *
     *
     * @return                an <code>Enumeration</code> of strings
     *                        containing the names
     *                         of the request's attributes
     *
     */
    Enumeration getAttributeNames();

    /**
     * Returns an <code>Enumeration</code> containing the
     * names of the attributes available to this request locally.
     * This method returns an empty <code>Enumeration</code>
     * if the request has no attributes available to it.
     *
     *
     * @return                an <code>Enumeration</code> of strings
     *                        containing the names
     *                         of the request's attributes
     *
     */
    Enumeration getLocalAttributeNames();

    /**
     *
     * Stores an attribute in this request in the <code>GLOBAL_SCOPE</code>.
     * Attributes are reset between requests.
     *
     * <p>Attribute names should follow the same conventions as
     * package names. Names beginning with <code>java.*</code>,
     * <code>javax.*</code>, and <code>com.sun.*</code>, are
     * reserved for use by Sun Microsystems.
     *
     *
     * @param name                        a <code>String</code> specifying
     *                                        the name of the attribute
     *
     * @param o                                the <code>Object</code> to be stored
     *
     */
    void setAttribute(String name, Object o);

    /**
     *
     * Stores an attribute in this request locally.
     * Attributes are reset between requests.
     *
     * <p>Attribute names should follow the same conventions as
     * package names. Names beginning with <code>java.*</code>,
     * <code>javax.*</code>, and <code>com.sun.*</code>, are
     * reserved for use by Sun Microsystems.
     *
     *
     * @param name                        a <code>String</code> specifying
     *                                        the name of the attribute
     *
     * @param o                                the <code>Object</code> to be stored
     *
     */
    void setLocalAttribute(String name, Object o);

    /**
     *
     * Removes an attribute from this request globally.
     * This method is not generally needed as attributes only persist as long
     * as the request is being handled.
     *
     * <p>Attribute names should follow the same conventions as
     * package names. Names beginning with <code>java.*</code>,
     * <code>javax.*</code>, and <code>com.sun.*</code>, are
     * reserved for use by Sun Microsystems.
     *
     *
     * @param name                        a <code>String</code> specifying
     *                                        the name of the attribute to remove
     *
     */
    void removeAttribute(String name);

    /**
     *
     * Removes an attribute from this request in the <code>GLOBAL_SCOPE</code>.
     * This method is not
     * generally needed as attributes only persist as long as the request
     * is being handled.
     *
     * <p>Attribute names should follow the same conventions as
     * package names. Names beginning with <code>java.*</code>,
     * <code>javax.*</code>, and <code>com.sun.*</code>, are
     * reserved for use by Sun Microsystems.
     *
     *
     * @param name                        a <code>String</code> specifying
     *                                        the name of the attribute to remove
     *
     */
    void removeLocalAttribute(String name);

    /**
     * Returns the value of the named attribute searching both scopes
     * as an <code>Object</code>, or <code>null</code> if no attribute 
     * of the given name exists. This method first searches in the 
     * request scope and then, if no object is found, in the global scope.
     *
     * @param name        a <code>String</code> specifying the name of
     *                        the attribute
     *
     * @return                an <code>Object</code> containing the value
     *                        of the attribute, or <code>null</code> if
     *                        the attribute does not exist
     *
     * @since 2.2
     */
    Object searchAttribute(String name);

    /**
     * Utility method for getting a <code>Map</code> view of the request attributes.
     * Returns a <code>Map</code> with attributes from the <code>GLOBAL_SCOPE</code>.
     *
     * @return                a <code>Map</code> containing the request attributes.
     *
     * @since 2.2
     */
    Map getAttributes();

    /**
     *
     * Returns the name of the authentication scheme used to protect
     * the servlet, for example, "BASIC" or "SSL," or null if the servlet was
     * not protected
     *
     * @return                The name of the authentication scheme used to
     *                        protect the servlet, or null if the servlet was
     *                        not protected
     */

    String getAuthType();

    /**
     * Returns the name of the character encoding used in the body of this
     * request. This method returns <code>null</code> if the request
     * does not specify a character encoding
     *
     *
     * @return                a <code>String</code> containing the name of
     *                        the chararacter encoding, or <code>null</code>
     *                        if the request does not specify a character encoding
     *
     */

    String getCharacterEncoding();

    /**
     * Overrides the charactor encoding of parameters.
     *
     * @throws java.io.UnsupportedEncodingException if this is not a valid encoding.
     *
     */

    void setCharacterEncoding(String enc) throws java.io.UnsupportedEncodingException;

    /**
     * Returns the length, in bytes, of the request body
     *
     * @return                an integer containing the length of the
     *                         request body or -1 if the length is not known
     *
     */

    int getContentLength();

    /**
     * Returns the MIME type of the body of the request
     *
     * @return                a <code>String</code> containing the name
     *                        of the MIME type of
     *                         the request, or -1 if the type is not known
     *
     */

    String getContentType();

    /**
     * Returns the value of a request parameter as a <code>String</code>,
     *
     * @param name         a <code>String</code> specifying the
     *                        name of the parameter
     *
     * @return                a <code>String</code> representing the
     *                        single value of the parameter
     *
     * @see                 #getParameterValues(String)
     *
     */

    String getParameter(String name);

    /**
     *
     * Returns an <code>Enumeration</code> of <code>String</code>
     * objects containing the names of the parameters contained
     * in this request. If the request has
     * no parameters, the method returns an
     * empty <code>Enumeration</code>.
     *
     * @return                an <code>Enumeration</code> of <code>String</code>
     *                        objects, each <code>String</code> containing
     *                         the name of a request parameter; or an
     *                        empty <code>Enumeration</code> if the
     *                        request has no parameters
     *
     */

    Enumeration getParameterNames();

    /**
     * Returns an array of <code>String</code> objects containing
     * all of the values the given request parameter has, or
     * <code>null</code> if the parameter does not exist.
     *
     * <p>If the parameter has a single value, the array has a length
     * of 1.
     *
     * @param name        a <code>String</code> containing the name of
     *                        the parameter whose value is requested
     *
     * @return                an array of <code>String</code> objects
     *                        containing the parameter's values
     *
     * @see                #getParameter(String)
     *
     */

    String[] getParameterValues(String name);


    /**
     * Utility method for getting a <code>Map</code> view of the request parameters.
     * Returns a <code>Map</code> with request parameters.
     *
     * @return                a <code>Map</code> containing the request parameters.
     *
     * @since 2.2
     */
    Map getParameters();

    /**
     * Returns the name and version of the protocol the request uses
     * in the form <i>protocol/majorVersion.minorVersion</i>, for
     * example, HTTP/1.1. For HTTP servlets, the value
     * returned is the same as the value of the CGI variable
     * <code>SERVER_PROTOCOL</code>.
     *
     * @return                a <code>String</code> containing the protocol
     *                        name and version number
     *
     */

    String getProtocol();

    /**
     * Returns the name of the scheme used to make this request,
     * for example,
     * <code>http</code>, <code>https</code>, or <code>ftp</code>.
     * Different schemes have different rules for constructing URLs,
     * as noted in RFC 1738.
     *
     * @return                a <code>String</code> containing the name
     *                        of the scheme used to make this request
     *
     */

    String getScheme();

    /**
     * Returns the host name of the server that received the request.
     * For HTTP servlets, same as the value of the CGI variable
     * <code>SERVER_NAME</code>.
     *
     * @return                a <code>String</code> containing the name
     *                        of the server to which the request was sent
     */

    String getServerName();

    /**
     * Returns the port number on which this request was received.
     * For HTTP servlets, same as the value of the CGI variable
     * <code>SERVER_PORT</code>.
     *
     * @return                an integer specifying the port number
     *
     */

    int getServerPort();

    /**
     * Returns the Internet Protocol (IP) address of the client
     * that sent the request.  For HTTP servlets, same as the value of the
     * CGI variable <code>REMOTE_ADDR</code>.
     *
     * @return                a <code>String</code> containing the
     *                        IP address of the client that sent the request
     *
     */

    String getRemoteAddr();

    /**
     * Returns the fully qualified name of the client that sent the
     * request, or the IP address of the client if the name cannot be
     * determined. For HTTP servlets, same as the value of the CGI variable
     * <code>REMOTE_HOST</code>.
     *
     * @return                a <code>String</code> containing the fully qualified name
     *                        of the client
     *
     */

    String getRemoteHost();

    /**
     *
     * Returns the preferred <code>Locale</code> that the client will
     * accept content in, based on the Accept-Language header.
     * If the client request doesn't provide an Accept-Language header,
     * this method returns the default locale for the server.
     *
     *
     * @return                the preferred <code>Locale</code> for the client
     *
     */

    Locale getLocale();

    /**
     *
     * Returns an <code>Enumeration</code> of <code>Locale</code> objects
     * indicating, in decreasing order starting with the preferred locale, the
     * locales that are acceptable to the client based on the Accept-Language
     * header.
     * If the client request doesn't provide an Accept-Language header,
     * this method returns an <code>Enumeration</code> containing one
     * <code>Locale</code>, the default locale for the server.
     *
     *
     * @return                an <code>Enumeration</code> of preferred
     *                  <code>Locale</code> objects for the client
     *
     */

    Enumeration getLocales();

    /**
     *
     * Returns a boolean indicating whether this request was made using a
     * secure channel, such as HTTPS.
     *
     *
     * @return                a boolean indicating if the request was made using a
     *                  secure channel
     *
     */

    boolean isSecure();

    /**
     *
     * Returns an array containing all of the {@link javax.servlet.http.Cookie}
     * objects the client sent with this request.
     * This method returns <code>null</code> if no cookies were sent.
     *
     * @return                an array of all the {@link javax.servlet.http.Cookie}
     *                        included with this request, or <code>null</code>
     *                        if the request has no cookies
     *
     *
     */

    javax.servlet.http.Cookie[] getCookies();

    /**
     * Returns a map of the {@link javax.servlet.http.Cookie} objects the client sent
     * with this request, indexed by name. This method returns an empty
     * map if no cookies were sent.
     *
     * @return a Map of {@link javax.servlet.http.Cookie} objects
     */
    Map getCookieMap();

    /**
    *
    * Returns an array containing all of the <code>Cookie</code>
    * objects the client sent with this request.
    * This method returns <code>null</code> if no cookies were sent.
    *
    * @return                an array of all the <code>Cookies</code>
    *                        included with this request, or <code>null</code>
    *                        if the request has no cookies
    *
    * @deprecated use {@link #getCookies()} instead.
    */

   Cookie[] getCocoonCookies();

   /**
    * Returns a map of the <code>Cookie</code> objects the client sent
    * with this request, indexed by name. This method returns an empty
    * map if no cookies were sent.
    *
    * @return a Map of <code>Cookie</code> objects
    * 
    * @deprecated use {@link #getCookieMap()} instead.
    */
   Map getCocoonCookieMap();

    /**
     *
     * Returns the value of the specified request header
     * as a <code>long</code> value that represents a
     * <code>Date</code> object. Use this method with
     * headers that contain dates, such as
     * <code>If-Modified-Since</code>.
     *
     * <p>The date is returned as
     * the number of milliseconds since January 1, 1970 GMT.
     * The header name is case insensitive.
     *
     * <p>If the request did not have a header of the
     * specified name, this method returns -1. If the header
     * can't be converted to a date, the method throws
     * an <code>IllegalArgumentException</code>.
     *
     * @param name                a <code>String</code> specifying the
     *                                name of the header
     *
     * @return                        a <code>long</code> value
     *                                representing the date specified
     *                                in the header expressed as
     *                                the number of milliseconds
     *                                since January 1, 1970 GMT,
     *                                or -1 if the named header
     *                                was not included with the
     *                                reqest
     *
     * @exception        IllegalArgumentException        If the header value
     *                                                        can't be converted
     *                                                        to a date
     *
     */

    long getDateHeader(String name);

    /**
     *
     * Returns the value of the specified request header
     * as a <code>String</code>. If the request did not include a header
     * of the specified name, this method returns <code>null</code>.
     * The header name is case insensitive. You can use
     * this method with any request header.
     *
     * @param name                a <code>String</code> specifying the
     *                                header name
     *
     * @return                        a <code>String</code> containing the
     *                                value of the requested
     *                                header, or <code>null</code>
     *                                if the request does not
     *                                have a header of that name
     *
     */

    String getHeader(String name);

    /**
     *
     * Returns all the values of the specified request header
     * as an <code>Enumeration</code> of <code>String</code> objects.
     *
     * <p>Some headers, such as <code>Accept-Language</code> can be sent
     * by clients as several headers each with a different value rather than
     * sending the header as a comma separated list.
     *
     * <p>If the request did not include any headers
     * of the specified name, this method returns an empty
     * <code>Enumeration</code>.
     * The header name is case insensitive. You can use
     * this method with any request header.
     *
     * @param name                a <code>String</code> specifying the
     *                                header name
     *
     * @return                        a <code>Enumeration</code> containing the
     *                                values of the requested
     *                                header, or <code>null</code>
     *                                if the request does not
     *                                have any headers of that name
     *
     */

    Enumeration getHeaders(String name);

    /**
     *
     * Returns an enumeration of all the header names
     * this request contains. If the request has no
     * headers, this method returns an empty enumeration.
     *
     * <p>Some servlet containers do not allow do not allow
     * servlets to access headers using this method, in
     * which case this method returns <code>null</code>
     *
     * @return                        an enumeration of all the
     *                                header names sent with this
     *                                request; if the request has
     *                                no headers, an empty enumeration;
     *                                if the servlet container does not
     *                                allow servlets to use this method,
     *                                <code>null</code>
     *
     */

    Enumeration getHeaderNames();
    
    /**
     * Utility method for getting a <code>Map</code> view of the request headers.
     * Returns a <code>Map</code> with request headers.
     *
     * @return                a <code>Map</code> containing the request headers.
     *
     * @since 2.2
     */
    Map getHeaders();

    /**
     * Retrieves the body of the request as binary data using 
     * an <code>InputStream</code>.
     * 
     * @return                        an <code>InputStream</code>
     *                                containing the body of the request.
     * 
     * @throws IOException            if an input or output exception occurred
     * @throws UnsupportedOperationException
     */
    ServletInputStream getInputStream() throws IOException, UnsupportedOperationException;

    /**
     *
     * Returns the name of the HTTP method with which this
     * request was made, for example, GET, POST, or PUT.
     * Same as the value of the CGI variable REQUEST_METHOD.
     *
     * @return                        a <code>String</code>
     *                                specifying the name
     *                                of the method with which
     *                                this request was made
     *
     */

    String getMethod();

    /**
     *
     * Returns any extra path information associated with
     * the URL the client sent when it made this request.
     * The extra path information follows the servlet path
     * but precedes the query string.
     * This method returns <code>null</code> if there
     * was no extra path information.
     *
     * <p>Same as the value of the CGI variable PATH_INFO.
     *
     *
     * @return                a <code>String</code> specifying
     *                        extra path information that comes
     *                        after the servlet path but before
     *                        the query string in the request URL;
     *                        or <code>null</code> if the URL does not have
     *                        any extra path information
     *
     */

    String getPathInfo();

    /**
     *
     * Returns any extra path information after the servlet name
     * but before the query string, and translates it to a real
     * path. Same as the value of the CGI variable PATH_TRANSLATED.
     *
     * <p>If the URL does not have any extra path information,
     * this method returns <code>null</code>.
     *
     *
     * @return                a <code>String</code> specifying the
     *                        real path, or <code>null</code> if
     *                        the URL does not have any extra path
     *                        information
     *
     *
     */

    String getPathTranslated();

    /**
     *
     * Returns the portion of the request URI that indicates the context
     * of the request.  The context path always comes first in a request
     * URI.  The path starts with a "/" character but does not end with a "/"
     * character.  For servlets in the default (root) context, this method
     * returns "".
     *
     *
     * @return                a <code>String</code> specifying the
     *                        portion of the request URI that indicates the context
     *                        of the request
     *
     *
     */

    String getContextPath();

    /**
     *
     * Returns the query string that is contained in the request
     * URL after the path. This method returns <code>null</code>
     * if the URL does not have a query string. Same as the value
     * of the CGI variable QUERY_STRING.
     *
     * @return                a <code>String</code> containing the query
     *                        string or <code>null</code> if the URL
     *                        contains no query string
     *
     */

    String getQueryString();

    /**
     *
     * Returns the login of the user making this request, if the
     * user has been authenticated, or <code>null</code> if the user
     * has not been authenticated.
     * Whether the user name is sent with each subsequent request
     * depends on the browser and type of authentication. Same as the
     * value of the CGI variable REMOTE_USER.
     *
     * @return                a <code>String</code> specifying the login
     *                        of the user making this request, or <code>null</code
     *                        if the user login is not known
     *
     */

    String getRemoteUser();

    /**
     *
     * Returns the login of the user making this request, if the
     * user has been authenticated, or <code>null</code> if the user
     * has not been authenticated.
     * Whether the user name is sent with each subsequent request
     * depends on the browser and type of authentication. Same as the
     * value of the CGI variable REMOTE_USER.
     *
     * @return                a <code>String</code> specifying the login
     *                        of the user making this request, or <code>null</code
     *                        if the user login is not known
     *
     */

    Principal getUserPrincipal();

    /**
     *
     * Checks whether the currently logged in user is in a specified role.
     *
     * @return                        <code>true</code> if the user is
     *                                authenticated and in the role;
     *                                otherwise, <code>false</code>
     *
     *
     * @see                        #getRemoteUser()
     *
     */

    boolean isUserInRole(String role);

    /**
     *
     * Returns the session ID specified by the client. This may
     * not be the same as the ID of the actual session in use.
     * For example, if the request specified an old (expired)
     * session ID and the server has started a new session, this
     * method gets a new session with a new ID. If the request
     * did not specify a session ID, this method returns
     * <code>null</code>.
     *
     *
     * @return                a <code>String</code> specifying the session
     *                        ID, or <code>null</code> if the request did
     *                        not specify a session ID
     *
     * @see                #isRequestedSessionIdValid()
     *
     */

    String getRequestedSessionId();

    /**
     *
     * Returns the part of this request's URL from the protocol
     * name up to the query string in the first line of the HTTP request.
     * For example:
     *
     * <blockquote>
     * <table>
     * <tr align=left><th>First line of HTTP request<th>
     * <th>Returned Value
     * <tr><td>POST /some/path.html HTTP/1.1<td><td>/some/path.html
     * <tr><td>GET http://foo.bar/a.html HTTP/1.0
     * <td><td>http://foo.bar/a.html
     * <tr><td>HEAD /xyz?a=b HTTP/1.1<td><td>/xyz
     * </table>
     * </blockquote>
     * 
     * For internal requests, this method returns
     * the information for the original/external request!
     *
     * @return                a <code>String</code> containing
     *                        the part of the URL from the
     *                        protocol name up to the query string
     */
    String getRequestURI();

    /**
     * <p>
     * Returns the URI of the requested resource as interpreted by the sitemap.
     * For example, if your webapp is mounted at "/webapp" and the HTTP request
     * is for "/webapp/foo", this method returns "foo". Consequently, if the
     * request is for "/webapp", this method returns an empty string.
     * </p>
     * <p>
     * Note that if the request is mapped to a pipeline that contains
     * aggregated content, and if this method is called in the context of
     * one of the aggregated parts (e.g. a server page), this method will
     * return the URI of the aggregated part, not the original requested URI.
     * </p>
     *
     * @return a <code>String</code> containing the URL as mangled by the
     *         sitemap
     */
    String getSitemapURI();

    /**
     * <p>
     * Returns the URI Prefix of the requested resource where the sitemap is mounted.
     * For example, if your webapp is mounted at "/webapp" and the HTTP request
     * is for "/webapp/foo", this method returns "webapp/".
     * </p>
     *
     * @return a <code>String</code> containing the URI prefix as mangled by the
     *         sitemap
     */

    String getSitemapURIPrefix();

    /**
     * <p>
     * Returns the path to the sitemap of the requested resource as interpreted 
     * by the sitemap.
     * For example, if your webapp is mounted at "webapp" and the HTTP request
     * is for "webapp/foo", this method returns "webapp/". Consequently, if the
     * request is for "foo", this method returns the empty string.
     * </p>
     *
     * @return a <code>String</code> containing the path to the sitemap
     * @since 2.2
     */

    String getSitemapPath();
    
    /**
     *
     * Returns the part of this request's URL that calls
     * the servlet. This includes either the servlet name or
     * a path to the servlet, but does not include any extra
     * path information or a query string. Same as the value
     * of the CGI variable SCRIPT_NAME.
     *
     *
     * @return                a <code>String</code> containing
     *                        the name or path of the servlet being
     *                        called, as specified in the request URL
     *
     *
     */

    String getServletPath();

    /**
     *
     * Returns the current {@link HttpSession}
     * associated with this request or, if if there is no
     * current session and <code>create</code> is true, returns
     * a new session.
     *
     * <p>If <code>create</code> is <code>false</code>
     * and the request has no valid <code>Session</code>,
     * this method returns <code>null</code>.
     *
     * <p>To make sure the session is properly maintained,
     * you must call this method before
     * the response is committed.
     *
     *
     *
     *
     * @param create          <code>true</code> to create
     *                        a new session for this request if necessary;
     *                        <code>false</code> to return <code>null</code>
     *                        if there's no current session
     *
     *
     * @return                 the {@link HttpSession} associated
     *                        with this request or <code>null</code> if
     *                         <code>create</code> is <code>false</code>
     *                        and the request has no valid session
     *
     * @see        #getSession()
     *
     *
     */

    HttpSession getSession(boolean create);

    /**
     *
     * Returns the current session associated with this request,
     * or if the request does not have a session, creates one.
     *
     * @return                the {@link HttpSession} associated
     *                        with this request
     *
     * @see        #getSession(boolean)
     *
     */

     HttpSession getSession();
     /**
     *
     * Returns the current <code>Session</code>
     * associated with this request or, if if there is no
     * current session and <code>create</code> is true, returns
     * a new session.
     *
     * <p>If <code>create</code> is <code>false</code>
     * and the request has no valid <code>Session</code>,
     * this method returns <code>null</code>.
     *
     * <p>To make sure the session is properly maintained,
     * you must call this method before
     * the response is committed.
     *
     *
     *
     *
     * @param create          <code>true</code> to create
     *                        a new session for this request if necessary;
     *                        <code>false</code> to return <code>null</code>
     *                        if there's no current session
     *
     *
     * @return                 the <code>Session</code> associated
     *                        with this request or <code>null</code> if
     *                         <code>create</code> is <code>false</code>
     *                        and the request has no valid session
     *
     * @see        #getSession()
     *
     * @deprecated use {@link #getSession(boolean)} instead.
     */

    Session getCocoonSession(boolean create);

    /**
     *
     * Returns the current session associated with this request,
     * or if the request does not have a session, creates one.
     *
     * @return                the <code>Session</code> associated
     *                        with this request
     *
     * @see        #getSession(boolean)
     *
     * @deprecated use {@link #getSession()} instead.
     */

     Session getCocoonSession();

    /**
     *
     * Checks whether the requested session ID is still valid.
     *
     * @return                        <code>true</code> if this
     *                                request has an id for a valid session
     *                                in the current session context;
     *                                <code>false</code> otherwise
     *
     * @see                        #getRequestedSessionId()
     * @see                        #getSession()
     *
     */

    boolean isRequestedSessionIdValid();

    /**
     *
     * Checks whether the requested session ID came in as a cookie.
     *
     * @return                        <code>true</code> if the session ID
     *                                came in as a
     *                                cookie; otherwise, <code>false</code>
     *
     *
     * @see                        #getSession()
     *
     */

    boolean isRequestedSessionIdFromCookie();

    /**
     *
     * Checks whether the requested session ID came in as part of the
     * request URL.
     *
     * @return                        <code>true</code> if the session ID
     *                                came in as part of a URL; otherwise,
     *                                <code>false</code>
     *
     *
     * @see                        #getSession()
     *
     */

    boolean isRequestedSessionIdFromURL();
}
