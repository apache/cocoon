/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.browser;

import org.apache.avalon.component.Component;
import java.util.HashMap;
import org.w3c.dom.Document;

/**
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-04-20 20:49:48 $
 */
public interface Browser extends Component {

  /**
   * <code>getMedia</code> returns a media type based on the user
   * agent or on the Accept: header. This is used later by the XSLT
   * processor to select the stylesheet to apply on a given XML file.
   *
   * @param userAgent a <code>String</code> value
   * @param acceptHeader a <code>String</code> value
   * @return a <code>String</code> value */
  public HashMap getBrowser(String userAgent, String acceptHeader);

  public String getMedia(HashMap browserInfo);

  public Document getUACapabilities(HashMap browserInfo);
}
