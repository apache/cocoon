/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup.xsp;

import java.util.Enumeration;

import org.apache.cocoon.Response;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

import org.xml.sax.SAXException;

/**
 * The XSP <code>Response</code> object helper
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-05-23 23:09:59 $
 */
public class XSPResponseHelper extends XSPObjectHelper {
  /**
   * Assign values to the object's namespace uri and prefix
   */
  static {
    URI = XSP_RESPONSE_URI;
    PREFIX = XSP_RESPONSE_PREFIX;
  }

  /**
   * Set the content header for a given response
   *
   * @param response The Cocoon <code>Response</code>
   * @param name The header name
   * @param value The header value
   */
  public static void setHeader(Response response, String name, String value) {
    response.setHeader(name, value);
  }

  /**
   * Set the content type for a given response
   *
   * @param response The Cocoon <code>Response</code>
   * @param type The content type
   */
  public static void setContentType(Response response, String type) {
    response.setContentType(type);
  }
}
