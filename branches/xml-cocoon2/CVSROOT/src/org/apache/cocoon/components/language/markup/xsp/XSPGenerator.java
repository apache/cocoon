/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.cocoon.Roles;
import org.apache.avalon.component.ComponentManager;
import org.apache.cocoon.components.parser.Parser;

import org.apache.cocoon.components.language.generator.CompiledComponent;

import org.apache.excalibur.pool.Poolable;
import org.apache.avalon.context.Contextualizable;
import org.apache.avalon.context.Context;

import org.apache.cocoon.generation.AbstractServerPage;

/**
 * Base class for XSP-generated <code>ServerPagesGenerator</code> classes
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.18 $ $Date: 2001-04-20 20:49:54 $
 */
public abstract class XSPGenerator extends AbstractServerPage implements CompiledComponent, Contextualizable, Poolable {

  /** Contextualize this class */
  public void contextualize(Context context) {
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
