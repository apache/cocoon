/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup;

import org.w3c.dom.Document;

/**
 * This interfaces defines the functionality of a source code generator
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-05-23 23:09:56 $
 */
public interface MarkupCodeGenerator {
  /**
   * Generate source code from the input document. Filename information may be
   * needed by certain code-generation approaches and programming languages
   *
   * @param input The input document
   * @param filename The input source original filename
   * @return The generated source code
   * @exception Exception If an error occurs during code generation
   */
  public String generateCode(Document document, String filename)
    throws Exception;
}
