/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.arch;

/**
 * This interface is implemented by classes whose changes in
 * internal state trigger reinstantiation.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-05-23 23:09:49 $
 */
public interface Changeable {
  /**
   * Determine whether this instance has changed.
   */
  public boolean hasChanged();
}
