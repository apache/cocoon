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
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * The <code>Request</code> object helper
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: XSPRequestHelper.java,v 1.1 2003/03/09 00:08:55 pier Exp $
 */
public class XSPRequestHelper {
    /**
     * Assign values to the object's namespace uri and prefix
     */
    private static final String URI = Constants.XSP_REQUEST_URI;
    private static final String PREFIX = Constants.XSP_REQUEST_PREFIX;

    private static void getLocale(Locale locale, ContentHandler handler)
        throws SAXException
    {
        XSPObjectHelper.start(URI, PREFIX, handler, "locale");

        XSPObjectHelper.elementData(URI, PREFIX, handler, "language",
            locale.getLanguage());
        XSPObjectHelper.elementData(URI, PREFIX, handler, "country",
            locale.getCountry());
        XSPObjectHelper.elementData(URI, PREFIX, handler, "variant",
            locale.getVariant());

        XSPObjectHelper.end(URI, PREFIX, handler, "locale");
    }

    public static void getLocale(Map objectModel, ContentHandler handler)
        throws SAXException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        getLocale(request.getLocale(), handler);
    }

    /**
     * Return the request locales as array
     * @return Array containing request locales.
     */
    public static Locale[] getLocales(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);

        ArrayList a = new ArrayList(10);
        Enumeration e = request.getLocales();
        while (e.hasMoreElements()) {
            a.add(e.nextElement());
        }

        return (Locale[])a.toArray(new Locale[a.size()]);
    }

    /**
     * Output request locales
     * @param objectModel The Map objectModel
     * @param contentHandler The SAX content handler
     * @exception SAXException If a SAX error occurs
     */
    public static void getLocales(Map objectModel, ContentHandler contentHandler)
        throws SAXException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        XSPObjectHelper.start(URI, PREFIX, contentHandler, "locales");
        Enumeration e = request.getLocales();
        while (e.hasMoreElements()) {
            getLocale((Locale)e.nextElement(), contentHandler);
        }
        XSPObjectHelper.end(URI, PREFIX, contentHandler, "locales");
    }

    /**
     * Return the given request parameter value or a user-provided default if
     * none was specified.
     *
     * @param objectModel The Map objectModel
     * @param name The parameter name
     * @param defaultValue Value to substitute in absence of a parameter value
     */
    public static String getParameter(Map objectModel, String name,
                                      String defaultValue) {
        return getParameter(objectModel, name, defaultValue, null, null);
    }

    /**
     * Return the given request parameter value or a user-provided default if
     * none was specified.
     *
     * @param objectModel The Map objectModel
     * @param name The parameter name
     * @param defaultValue Value to substitute in absence of a parameter value
     * @param form_encoding The supposed encoding of the request parameter.
     * @param container_encoding The encoding used by container.
     */
    public static String getParameter(Map objectModel, String name,
                                      String defaultValue, String form_encoding,
                                      String container_encoding) {
        if(container_encoding == null)
            container_encoding = "ISO-8859-1"; // default per Servlet spec

        Request request = ObjectModelHelper.getRequest(objectModel);
        String value = request.getParameter(name);
        if(form_encoding != null && value != null && value.length() > 0) {
            try {
                value = new String(value.getBytes(container_encoding), form_encoding);
            } catch(java.io.UnsupportedEncodingException uee) {
                throw new CascadingRuntimeException("Unsupported Encoding Exception", uee);
           }
        }

        if (value == null) {
            value = defaultValue;
        }

        return value;
    }

    /**
     * Output the given request parameter value or a user-provided default if
     * none was specified.
     *
     * @param objectModel The Map objectModel
     * @param contentHandler The SAX content handler
     * @param name The parameter name
     * @param defaultValue Value to substitute in absence of a parameter value
     * @exception SAXException If a SAX error occurs
     */
    public static void getParameter(Map objectModel, ContentHandler contentHandler,
                                    String name, String defaultValue)
        throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        XSPObjectHelper.addAttribute(attr, "name", name);

        XSPObjectHelper.elementData(URI, PREFIX, contentHandler, "parameter",
            getParameter(objectModel, name, defaultValue, null, null), attr);
    }

    /**
     * Output the given request parameter value or a user-provided default if
     * none was specified.
     *
     * @param objectModel The Map objectModel
     * @param contentHandler The SAX content handler
     * @param name The parameter name
     * @param defaultValue Value to substitute in absence of a parameter value
     * @param form_encoding The supposed encoding of the request parameter.
     * @param container_encoding The encoding used by container.
     * @exception SAXException If a SAX error occurs
     */
    public static void getParameter(Map objectModel, ContentHandler contentHandler,
                                    String name, String defaultValue,
                                    String form_encoding,
                                    String container_encoding)
        throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        XSPObjectHelper.addAttribute(attr, "name", name);
        XSPObjectHelper.elementData(URI, PREFIX, contentHandler, "parameter",
            String.valueOf(getParameter(objectModel, name, defaultValue,
                form_encoding, container_encoding)), attr);
    }

    /**
     * Output the request parameter values for a given name
     *
     * @param objectModel The Map objectModel
     * @param contentHandler The SAX content handler
     * @exception SAXException If a SAX error occurs
     */
    public static void getParameterValues(Map objectModel,
                                          ContentHandler contentHandler,
                                          String name)
        throws SAXException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        AttributesImpl attr = new AttributesImpl();
        XSPObjectHelper.addAttribute(attr, "name", name);

        XSPObjectHelper.start(URI, PREFIX, contentHandler,
            "parameter-values", attr);

        String[] values = request.getParameterValues(name);

        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                XSPObjectHelper.elementData(URI, PREFIX, contentHandler,
                    "value", values[i]);
            }
        }

        XSPObjectHelper.end(URI, PREFIX, contentHandler, "parameter-values");
    }

    /**
     * Output the request parameter values for a given name
     *
     * @param objectModel The Map objectModel
     * @param contentHandler The SAX content handler
     * @param form_encoding The supposed encoding of the request parameter.
     * @param container_encoding The encoding used by container.
     * @exception SAXException If a SAX error occurs
     */
    public static void getParameterValues(Map objectModel,
                                          ContentHandler contentHandler,
                                          String name, String form_encoding,
                                          String container_encoding)
        throws SAXException {
        Request request = ObjectModelHelper.getRequest(objectModel);

        AttributesImpl attr = new AttributesImpl();
        XSPObjectHelper.addAttribute(attr, "name", name);
        XSPObjectHelper.start(URI, PREFIX, contentHandler,
            "parameter-values", attr);

        String[] values = request.getParameterValues(name);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if(form_encoding != null && values[i] != null &&
                    values[i].length() > 0) {
                    try {
                        values[i] = new String(values[i].getBytes(container_encoding),
                            form_encoding);
                    } catch(java.io.UnsupportedEncodingException uee) {
                        throw new CascadingRuntimeException("Unsupported Encoding Exception", uee);
                    }
                }
                XSPObjectHelper.elementData(URI, PREFIX, contentHandler,
                    "value", values[i]);
            }
        }

        XSPObjectHelper.end(URI, PREFIX, contentHandler, "parameter-values");
    }

    /**
     * return the request parameter values for a given name as array
     *
     * @param objectModel The Map objectModel
     * @param form_encoding The supposed encoding of the request parameter.
     * @param container_encoding The encoding used by container.
     * @return Array containing requested values.
     */
    public static String[] getParameterValues(Map objectModel, String name,
                                              String form_encoding,
                                              String container_encoding) {
        Request request = ObjectModelHelper.getRequest(objectModel);

        String[] values = request.getParameterValues(name);

        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if(form_encoding != null && values[i] != null &&
                    values[i].length() > 0) {
                    try {
                        values[i] = new String(values[i].getBytes(container_encoding),
                            form_encoding);
                    } catch(java.io.UnsupportedEncodingException uee) {
                        throw new CascadingRuntimeException("Unsupported Encoding Exception", uee);
                    }
                }
            }
        }
        return values;
    }

    /**
     * return the request parameter names as array
     *
     * @return Array containing parameter names.
     */
    public static String[] getParameterNames(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);

        ArrayList a = new ArrayList(10);
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            a.add(e.nextElement());
        }

        return (String[])a.toArray(new String[a.size()]);
    }

    /**
     * Output parameter names for a given request
     *
     * @param objectModel The Map objectModel
     * @param contentHandler The SAX content handler
     * @exception SAXException If a SAX error occurs
     */
    public static void getParameterNames(Map objectModel,
                                         ContentHandler contentHandler)
        throws SAXException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        XSPObjectHelper.start(URI, PREFIX, contentHandler, "parameter-names");

        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            XSPObjectHelper.elementData(URI, PREFIX, contentHandler, "name", name);
        }

        XSPObjectHelper.end(URI, PREFIX, contentHandler, "parameter-names");
    }

    /**
     * Output the header names for a given request
     *
     * @param objectModel The Map objectModel
     * @param contentHandler The SAX content handler
     * @exception SAXException If a SAX error occurs
     */
    public static void getHeaderNames(Map objectModel,
                                      ContentHandler contentHandler)
        throws SAXException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        XSPObjectHelper.start(URI, PREFIX, contentHandler, "header-names");

        Enumeration e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            XSPObjectHelper.elementData(URI, PREFIX, contentHandler, "name", name);
        }

        XSPObjectHelper.end(URI, PREFIX, contentHandler, "header-names");
    }

    /**
     * Returns the header names for a given request
     *
     * @param objectModel The Map objectModel
     */
    public static String[] getHeaderNames(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);

        ArrayList a = new ArrayList(10);
        Enumeration e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            a.add(e.nextElement());
        }

        return (String[])a.toArray(new String[a.size()]);
    }

    public static String[] getHeaders(Map objectModel, String name) {
        Request request = ObjectModelHelper.getRequest(objectModel);

        ArrayList a = new ArrayList(10);
        Enumeration e = request.getHeaders(name);
        while (e.hasMoreElements()) {
            a.add(e.nextElement());
        }

        return (String[])a.toArray(new String[a.size()]);
    }

    public static void getHeaders(Map objectModel, String name,
                                  ContentHandler contentHandler)
        throws SAXException {
        Request request = ObjectModelHelper.getRequest(objectModel);

        AttributesImpl attr = new AttributesImpl();
        XSPObjectHelper.addAttribute(attr, "name", name);
        XSPObjectHelper.start(URI, PREFIX, contentHandler, "header-values");

        Enumeration e = request.getHeaders(name);
        while (e.hasMoreElements()) {
            String value = (String) e.nextElement();
            XSPObjectHelper.elementData(URI, PREFIX, contentHandler, "value", value);
        }

        XSPObjectHelper.end(URI, PREFIX, contentHandler, "header-values");
    }

    public static Date getDateHeader(Map objectModel, String name) {
        Request request = ObjectModelHelper.getRequest(objectModel);

        if (name == null || name.length() == 0) {
            return null;
        }
        long dateHeader = request.getDateHeader(name);
        if (dateHeader == -1) {
            return null;
        }
        return new Date(dateHeader);
    }

    public static String getDateHeader(Map objectModel, String name, String format) {
        Request request = ObjectModelHelper.getRequest(objectModel);

        if (name == null || name.length() == 0) {
            return null;
        }
        long dateHeader = request.getDateHeader(name);
        if (dateHeader == -1) {
            return null;
        }
        if (format != null) format = format.trim();
        return XSPUtil.formatDate(new Date(dateHeader), format);
    }

    /**
     * Output attribute names for a given request
     *
     * @param objectModel The Map objectModel
     * @param contentHandler The SAX content handler
     * @exception SAXException If a SAX error occurs
     */
    public static void getAttributeNames(Map objectModel,
                                         ContentHandler contentHandler)
        throws SAXException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        XSPObjectHelper.start(URI, PREFIX, contentHandler, "attribute-names");

        Enumeration e = request.getAttributeNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            XSPObjectHelper.elementData(URI, PREFIX, contentHandler, "name", name);
        }

        XSPObjectHelper.end(URI, PREFIX, contentHandler, "attribute-names");
    }

    /**
     * Returns the attribute names
     *
     * @param objectModel The Map objectModel
     */
    public static String[] getAttributeNames(Map objectModel)
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        ArrayList a = new ArrayList(10);
        Enumeration e = request.getAttributeNames();
        while (e.hasMoreElements()) {
            a.add(e.nextElement());
        }

        return (String[])a.toArray(new String[a.size()]);
    }

    public static String getRequestedURL(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        StringBuffer uribuf = null;
        boolean isSecure = request.isSecure();
        int port = request.getServerPort();

        if (isSecure) {
            uribuf = new StringBuffer("https://");
        } else {
            uribuf = new StringBuffer("http://");
        }

        uribuf.append(request.getServerName());
        if (isSecure) {
            if (port != 443) {
                uribuf.append(":").append(port);
            }
        } else {
            if (port != 80) {
                uribuf.append(":").append(port);
            }
        }

        uribuf.append(request.getRequestURI());
        return uribuf.toString();
    }

    /**
     * Return the given session attribute value or a user-provided default if
     * none was specified.
     *
     * @param objectModel The Map objectModel
     * @param name The attribute name
     * @param defaultValue Value to substitute in absence of a attribute value
     */
    public static Object getSessionAttribute(Map objectModel, String name,
                                             Object defaultValue ) {

        Session session = ObjectModelHelper.getRequest(objectModel).getSession();
        Object obj = session.getAttribute(name);
        return (obj != null ? obj : defaultValue );
    }

    /**
     * Output the given session attribute value or a user-provided default if
     * none was specified.
     *
     * @param objectModel The Map objectModel
     * @param contentHandler The SAX content handler
     * @param name The attribute name
     * @param defaultValue Value to substitute in absence of an attribute value
     * @exception SAXException If a SAX error occurs
     */
    public static void getSessionAttribute(Map objectModel, ContentHandler contentHandler,
                                           String name, Object defaultValue)
        throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        XSPObjectHelper.addAttribute(attr, "name", name);

        XSPObjectHelper.elementData(URI, PREFIX, contentHandler, "parameter",
            getSessionAttribute(objectModel, name, defaultValue).toString(), attr);
    }
}
