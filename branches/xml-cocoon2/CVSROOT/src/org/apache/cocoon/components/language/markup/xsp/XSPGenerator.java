/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.cocoon.Roles;
import org.apache.avalon.ComponentManager;
import org.apache.cocoon.components.parser.Parser;

import org.apache.cocoon.components.language.generator.CompiledComponent;

import org.apache.log.Logger;
import org.apache.avalon.Loggable;
import org.apache.avalon.Poolable;

import org.apache.cocoon.generation.AbstractServerPage;

/**
 * Base class for XSP-generated <code>ServerPagesGenerator</code> classes
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.15 $ $Date: 2001-02-22 19:07:53 $
 */
public abstract class XSPGenerator extends AbstractServerPage implements Loggable, CompiledComponent {
  protected Logger log;

    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }

  /**
   * Set the current <code>ComponentManager</code> instance used by this
   * <code>Generator</code> and initialize relevant instance variables.
   *
   * @param manager The global component manager
   */
  public void compose(ComponentManager manager) {
    super.compose(manager);
  }
}
