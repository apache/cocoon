/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup.xsp;

/**
 * Constants used by XSP classes
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-05-23 23:09:57 $
 */
public interface Constants {
  public static final String XSP_PREFIX = "xsp";
  public static final String XSP_URI = "http://xml.apache.org/xsp";

  public static final String XSP_REQUEST_PREFIX = "xsp-request";
  public static final String XSP_REQUEST_URI = XSP_URI + "/request";

  public static final String XSP_RESPONSE_PREFIX = "xsp-response";
  public static final String XSP_RESPONSE_URI = XSP_URI + "/response";
}
