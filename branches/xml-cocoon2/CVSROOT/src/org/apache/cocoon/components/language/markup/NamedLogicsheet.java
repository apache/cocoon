/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup;

import org.xml.sax.InputSource;

import java.io.IOException;
import org.xml.sax.SAXException;

/**
 * An extension to <code>Logicsheet</code> that is associated with a namespace.
 * Named logicsheets are implicitly declared (and automagically applied) when
 * the markup language document's root element declares the same logichseet's
 * namespace 
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-07-29 18:30:28 $
 */
public class NamedLogicsheet extends Logicsheet {
  /**
   * The namespace uri
   */
  protected String uri;

  /**
   * The namespace prefix
   */
  protected String prefix;

  /**
   * Set the logichseet's namespace prefix
   *
   * @param prefix The namespace prefix
   */
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  /**
   * Return the logicsheet's namespace prefix
   *
   * @return The logicsheet's namespace prefix
   */
  public String getPrefix() {
    return this.prefix;
  }

  /**
   * Set the logichseet's namespace uri
   *
   * @param prefix The namespace uri
   */
  public void setUri(String uri) {
    this.uri = uri;
  }

  /**
   * Return the logicsheet's namespace uri
   *
   * @return The logicsheet's namespace uri
   */
  public String getUri() {
    return this.uri;
  }
}
