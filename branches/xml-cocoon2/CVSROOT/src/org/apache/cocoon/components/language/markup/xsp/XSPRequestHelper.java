/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup.xsp;

import java.util.Enumeration;

import org.apache.cocoon.environment.http.HttpRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Vector;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;

import org.apache.cocoon.Constants;

/**
 * The <code>HttpServletRequest</code> object helper
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2001-03-23 19:38:06 $
 */
public class XSPRequestHelper extends XSPObjectHelper {
  /**
   * Assign values to the object's namespace uri and prefix
   */
  static {
    URI = Constants.XSP_REQUEST_URI;
    PREFIX = Constants.XSP_REQUEST_PREFIX;
  }

  /**
   * Output the uri associated with the given <code>HttpServletRequest</code>
   *
   * @param objectModel The Map objectModel
   * @param contentHandler The SAX content handler
   * @exception SAXException If a SAX error occurs
   */
  public static void getUri(
    Map objectModel,
    ContentHandler contentHandler
  )
    throws SAXException
  {
    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
    elementData(contentHandler, "uri", request.getRequestURI());
  }

  /**
   * Output the uri associated with the given objectModel
   *
   * @param objectModel The Map objectModel
   */
  public static String getUri(
    Map objectModel
  )
  {
    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
    return request.getRequestURI();
  }

  /**
   * Return the given request parameter value or a user-provided default if
   * none was specified.
   *
   * @param objectModel The Map objectModel
   * @param name The parameter name
   * @param defaultValue Value to substitute in absence of a parameter value
   */
  public static String getParameter(
    Map objectModel,
    String name,
    String defaultValue
  ) {
    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
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
   * @param objectModel The Map objectModel
   * @param contentHandler The SAX content handler
   * @param name The parameter name
   * @param defaultValue Value to substitute in absence of a parameter value
   * @exception SAXException If a SAX error occurs
   */
  public static void getParameter(
    Map objectModel,
    ContentHandler contentHandler,
    String name,
    String defaultValue
  )
    throws SAXException
  {
    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
    AttributesImpl attr = new AttributesImpl();
    addAttribute(attr, "name", name);

    elementData(
      contentHandler,
      "parameter",
      getParameter(objectModel, name, defaultValue),
      attr
    );
  }

  /**
   * Output the request parameter values for a given name
   *
   * @param objectModel The Map objectModel
   * @param contentHandler The SAX content handler
   * @exception SAXException If a SAX error occurs
   */
  public static void getParameterValues(
    Map objectModel,
    ContentHandler contentHandler,
    String name
  )
    throws SAXException
  {
    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
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
   * @param objectModel The Map objectModel
   * @param contentHandler The SAX content handler
   * @exception SAXException If a SAX error occurs
   */
  public static void getParameterNames(
    Map objectModel,
    ContentHandler contentHandler
  )
    throws SAXException
  {
    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
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
   * @param objectModel The Map objectModel
   * @param name The parameter name
   */
  public static String getHeader(
    Map objectModel,
    String name
  ) {
    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
    return request.getHeader(name);
  }

  /**
   * Output the request header value for a given name
   *
   * @param objectModel The Map objectModel
   * @param contentHandler The SAX content handler
   * @param name The parameter name
   * @exception SAXException If a SAX error occurs
   */
  public static void getHeader(
    Map objectModel,
    ContentHandler contentHandler,
    String name
  )
    throws SAXException
  {
    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
    AttributesImpl attr = new AttributesImpl();
    addAttribute(attr, "name", name);

    String value = getHeader(objectModel, name);
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
   * @param objectModel The Map objectModel
   * @param contentHandler The SAX content handler
   * @exception SAXException If a SAX error occurs
   */
  public static void getHeaderNames(
    Map objectModel,
    ContentHandler contentHandler
  )
    throws SAXException
  {
    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
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
   * @param objectModel The Map objectModel
   * @param name The parameter name
   * @param defaultValue Value to substitute in absence of a parameter value
   */
  public static String getSessionAttribute(
    Map objectModel,
    String name) {

    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
    HttpSession session = request.getSession(false);
    return (String) session.getAttribute(name);
  }

  /**
   * Return the given session attribute value or a user-provided default if
   * none was specified.
   *
   * @param objectModel The Map objectModel
   * @param name The parameter name
   * @param defaultValue Value to substitute in absence of a parameter value
   */
  public static String getSessionAttribute(
    Map objectModel,
    String name,
    String defaultValue) {

    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
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
   * Remove the specified attribute
   *
   * @param objectModel The Map objectModel
   * @param name The parameter name
   */
  public static void removeAttribute(
    Map objectModel,
    String name) {

    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
    request.removeAttribute(name);
  }

  /**
   * Output the given session attribute value or a user-provided default if
   * none was specified.
   *
   * @param objectModel The Map objectModel
   * @param contentHandler The SAX content handler
   * @param name The parameter name
   * @param defaultValue Value to substitute in absence of a parameter value
   * @exception SAXException If a SAX error occurs
   */
  public static void getSessionAttribute(
    Map objectModel,
    ContentHandler contentHandler,
    String name,
    String defaultValue
  )
    throws SAXException
  {
    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
    AttributesImpl attr = new AttributesImpl();
    addAttribute(attr, "name", name);

    elementData(
      contentHandler,
      "attribute",
      getSessionAttribute(objectModel, name, defaultValue),
      attr
    );
  }

  /**
   * Checks the secure flag
   *
   * @param objectModel The Map objectModel
   */
  public static boolean isSecure(
    Map objectModel) {
    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
    return request.isSecure();
  }


  /**
   * Remove the specified attribute
   *
   * @param objectModel The Map objectModel
   */
  public static String getServerName(
    Map objectModel) {

    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
    return request.getServerName();
  }

  /**
   * Remove the specified attribute
   *
   * @param objectModel The Map objectModel
   */
  public static int getServerPort(
    Map objectModel) {

    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
    return request.getServerPort();
  }
  

  /**
   * Get the session attribute names.
   *
   * @param objectModel The Map objectModel
   */
  public static Vector getSessionAttributeNames (
    Map objectModel) {
      HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
      Vector v = new Vector();
      Enumeration e = request.getSession().getAttributeNames();

      while (e.hasMoreElements()) {
          v.addElement(request.getSession().getAttribute((String) e.nextElement()));
      }

      String[] attributeNames = new String[v.size()];
      v.copyInto(attributeNames);
      return v;
  }


  /**
   * Get the session creation time
   *
   * @param objectModel The Map objectModel
   */
  public static long getSessionCreationTime (
    Map objectModel) {
      HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
      return request.getSession().getCreationTime();
  }

  /**
   * Get the session id
   *
   * @param objectModel The Map objectModel
   */
  public static String getSessionId (
    Map objectModel) {
      HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
      return request.getSession().getId();
  }

  /**
   * Get the session last accessed time
   *
   * @param objectModel The Map objectModel
   */
  public static long getSessionLastAccessedTime (
    Map objectModel) {
      HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
      return request.getSession().getLastAccessedTime();
  }

  /**
   * Get the session max inactive interval
   *
   * @param objectModel The Map objectModel
   */
  public static long getSessionMaxInactiveInterval (
    Map objectModel) {
      HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
      return request.getSession().getMaxInactiveInterval();
  }

  /**
   * Set the session max inactive interval
   *
   * @param objectModel The Map objectModel
   * @param interval max inactive interval 
   */
  public static void setSessionMaxInactiveInterval (
    Map objectModel,
    int interval) {
      HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
      request.getSession().setMaxInactiveInterval(interval);
  }

  /**
   * Invalidate the session
   *
   * @param objectModel The Map objectModel
   */
  public static void invalidateSession (
    Map objectModel) {
      HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
      request.getSession().invalidate();
  }


  /**
   * Checks the isNew flag
   *
   * @param objectModel The Map objectModel
   */
  public static boolean isSessionNew(
    Map objectModel) {
    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
    return request.getSession().isNew();
  }

  /**
   * Remove the specified attribute
   *
   * @param objectModel The Map objectModel
   * @param name The parameter name
   */
  public static void removeSessionAttribute(
    Map objectModel,
    String name) {

    HttpRequest request = (HttpRequest)objectModel.get(Constants.REQUEST_OBJECT);
    request.getSession().removeAttribute(name);
  }
}
