/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.cocoon.Cocoon;
import org.apache.avalon.ComponentManager;
import org.apache.cocoon.components.parser.Parser;

import org.apache.cocoon.generation.AbstractServerPage;

/**
 * Base class for XSP-generated <code>ServerPagesGenerator</code> classes
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2000-08-31 16:41:58 $
 */
public abstract class XSPGenerator extends AbstractServerPage {

  protected Parser parser;

  /**
   * Set the current <code>ComponentManager</code> instance used by this
   * <code>Generator</code> and initialize relevant instance variables.
   *
   * @param manager The global component manager
   */
  public void setComponentManager(ComponentManager manager) {
    super.setComponentManager(manager);

    this.parser = (Parser) this.manager.getComponent("parser");
  }
}
