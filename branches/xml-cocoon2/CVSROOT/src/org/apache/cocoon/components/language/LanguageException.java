/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language;

/**
 * The language exception.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-07-29 18:30:27 $
 */
public class LanguageException extends Exception {
  /**
   * The default constructor.
   */
  public LanguageException(String message) {
    super(message);
  }
}
