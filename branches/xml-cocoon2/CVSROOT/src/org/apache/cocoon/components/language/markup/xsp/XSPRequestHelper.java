/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup.xsp;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;

import org.apache.cocoon.Cocoon;

/**
 * The <code>HttpServletRequest</code> object helper
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2000-12-14 13:36:49 $
 */
public class XSPRequestHelper extends XSPObjectHelper {
  /**
   * Assign values to the object's namespace uri and prefix
   */
  static {
    URI = Cocoon.XSP_REQUEST_URI;
    PREFIX = Cocoon.XSP_REQUEST_PREFIX;
  }

  /**
   * Output the uri associated with the given <code>HttpServletRequest</code>
   *
   * @param request The HttpServletRequest request
   * @param contentHandler The SAX content handler
   * @exception SAXException If a SAX error occurs
   */
  public static void getUri(
    HttpServletRequest request,
    ContentHandler contentHandler
  )
    throws SAXException
  {
    elementData(contentHandler, "uri", request.getRequestURI());
  }

  /**
   * Return the given request parameter value or a user-provided default if
   * none was specified.
   *
   * @param request The HttpServletRequest request
   * @param name The parameter name
   * @param defaultValue Value to substitute in absence of a parameter value
   */
  public static String getParameter(
    HttpServletRequest request,
    String name,
    String defaultValue
  ) {
    String value = request.getParameter(name);

    if (value == null) {
      value = defaultValue;
    }

    return value;
  }

  /**
   * Output the given request parameter value or a user-provided default if
   * none was specified.
   *
   * @param request The HttpServletRequest request
   * @param contentHandler The SAX content handler
   * @param name The parameter name
   * @param defaultValue Value to substitute in absence of a parameter value
   * @exception SAXException If a SAX error occurs
   */
  public static void getParameter(
    HttpServletRequest request,
    ContentHandler contentHandler,
    String name,
    String defaultValue
  )
    throws SAXException
  {
    AttributesImpl attr = new AttributesImpl();
    addAttribute(attr, "name", name);

    elementData(
      contentHandler,
      "parameter",
      getParameter(request, name, defaultValue),
      attr
    );
  }

  /**
   * Output the request parameter values for a given name
   *
   * @param request The HttpServletRequest request
   * @param contentHandler The SAX content handler
   * @exception SAXException If a SAX error occurs
   */
  public static void getParameterValues(
    HttpServletRequest request,
    ContentHandler contentHandler,
    String name
  )
    throws SAXException
  {
    AttributesImpl attr = new AttributesImpl();
    addAttribute(attr, "name", name);

    start(contentHandler, "parameter-values", attr);

    String[] values = request.getParameterValues(name);

    if (values != null) {
      for (int i = 0; i < values.length; i++) {
        elementData(contentHandler, "value", values[i]);
      }
    }

    end(contentHandler, "parameter-values");
  }

  /**
   * Output parameter names for a given request
   *
   * @param request The HttpServletRequest request
   * @param contentHandler The SAX content handler
   * @exception SAXException If a SAX error occurs
   */
  public static void getParameterNames(
    HttpServletRequest request,
    ContentHandler contentHandler
  )
    throws SAXException
  {
    start(contentHandler, "parameter-names");

    Enumeration e = request.getParameterNames();
    while (e.hasMoreElements()) {
      String name = (String) e.nextElement();
      elementData(contentHandler, "name", name);
    }

    end(contentHandler, "parameter-names");
  }

  /**
   * Return the request header value for a given name
   *
   * @param request The HttpServletRequest request
   * @param name The parameter name
   */
  public static String getHeader(
    HttpServletRequest request,
    String name
  ) {
    return request.getHeader(name);
  }

  /**
   * Output the request header value for a given name
   *
   * @param request The HttpServletRequest request
   * @param contentHandler The SAX content handler
   * @param name The parameter name
   * @exception SAXException If a SAX error occurs
   */
  public static void getHeader(
    HttpServletRequest request,
    ContentHandler contentHandler,
    String name
  )
    throws SAXException
  {
    AttributesImpl attr = new AttributesImpl();
    addAttribute(attr, "name", name);

    String value = getHeader(request, name);
    if (value == null) {
      value = "";
    }

    elementData(
      contentHandler,
      "header",
      value,
      attr
    );
  }

  /**
   * Output the header names for a given request
   *
   * @param request The HttpServletRequest request
   * @param contentHandler The SAX content handler
   * @exception SAXException If a SAX error occurs
   */
  public static void getHeaderNames(
    HttpServletRequest request,
    ContentHandler contentHandler
  )
    throws SAXException
  {
    start(contentHandler, "header-names");

    Enumeration e = request.getHeaderNames();
    while (e.hasMoreElements()) {
      String name = (String) e.nextElement();
      elementData(contentHandler, "name", name);
    }

    end(contentHandler, "header-names");
  }

  /**
   * Return the given session attribute value or a user-provided default if
   * none was specified.
   *
   * @param request The HttpServletRequest request
   * @param name The parameter name
   * @param defaultValue Value to substitute in absence of a parameter value
   */
  public static String getSessionAttribute(
	HttpServletRequest request, 
	String name, 
	String defaultValue) {
 
	HttpSession session = request.getSession(false);
	String value = null;

	if (session != null) {
		value = (String) session.getAttribute(name);
	}

	if (value == null) {
		value = defaultValue;
	}

	return value;
  }  

  /**
   * Output the given session attribute value or a user-provided default if
   * none was specified.
   *
   * @param request The HttpServletRequest request
   * @param contentHandler The SAX content handler
   * @param name The parameter name
   * @param defaultValue Value to substitute in absence of a parameter value
   * @exception SAXException If a SAX error occurs
   */
  public static void getSessionAttribute(
	HttpServletRequest request,
	ContentHandler contentHandler,
	String name,
	String defaultValue
  )
	throws SAXException
  {
	AttributesImpl attr = new AttributesImpl();
	addAttribute(attr, "name", name);

	elementData(
	  contentHandler,
	  "attribute",
	  getSessionAttribute(request, name, defaultValue),
	  attr
	);
  }   
}
