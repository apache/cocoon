/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.arch.named;

import org.apache.arch.Component;

/**
 * A component which is part of a collection and is to be
 * identified by name.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-05-23 23:09:51 $
 */
public interface NamedComponent extends Component {
  /**
   * Returns the name used to identify this component instance.
   * This name must be unique among all components in the same
   * collection type.
   * @return The component's name
   */
  public String getName();
}

