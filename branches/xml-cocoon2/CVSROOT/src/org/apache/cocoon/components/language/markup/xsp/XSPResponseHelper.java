/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup.xsp;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletResponse;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;

import org.apache.cocoon.Cocoon;

import org.apache.log.LogKit;

/**
 * The XSP <code>HttpResponse</code> object helper
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2000-12-08 20:39:05 $
 */
public class XSPResponseHelper extends XSPObjectHelper {
  /**
   * Assign values to the object's namespace uri and prefix
   */
  static {
    URI = Cocoon.XSP_RESPONSE_URI;
    PREFIX = Cocoon.XSP_RESPONSE_PREFIX;
  }

  /**
   * Set the content header for a given response
   *
   * @param response The <code>HttpServletResponse</code>
   * @param name The header name
   * @param value The header value
   */
  public static void setHeader(HttpServletResponse response, String name, String value) {
    response.setHeader(name, value);
  }

  /**
   * Set the content type for a given response
   *
   * @param response The <code>HttpServletResponse</code>
   * @param type The content type
   */
  public static void setContentType(HttpServletResponse response, String type) {
    response.setContentType(type);
  }

  /**
   * Send an HTTP redirect
   *
   * @param response The <code>HttpServletResponse</code>
   * @param location The location URL
   */
  public static void sendRedirect(HttpServletResponse response, String location) {
    try {
      response.sendRedirect(location);
    }
    catch (IOException e) {LogKit.getLoggerFor("cocoon").warn("XSPResponseHelper.sendRedirect", e);}
  }
}
