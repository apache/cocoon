/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generators;

import org.apache.cocoon.Request;
import org.apache.arch.Changeable;

/**
 * This interface defines the behavior of an automatically generated server
 * pages generator
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-05-23 23:10:11 $
 */
public interface ServerPagesGenerator extends Generator, Changeable {
  /**
   * Returns this generator's creation date
   *
   * @return The date this generator was automatically created
   */
  public long dateCreated();

  /**
   * Determines whether generated content has changed since
   * last invocation. Users may override this method to take
   * advantage of SAX event cacheing
   *
   * @param request The request whose data must be inspected to assert whether
   * dynamically generated content has changed
   * @return Whether content has changes for this request's data
   */
  public boolean hasContentChanged(Request request);
}
