/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup;

import org.w3c.dom.Document;
import org.apache.avalon.NamedComponent;

import org.apache.cocoon.components.language.programming.ProgrammingLanguage;

/**
 * This interface defines a markup language whose instance documents are to be
 * translated into an executable program capable or rebuilding the original
 * document augmenting it with dynamic content
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-07-11 03:09:38 $
 */
public interface MarkupLanguage extends NamedComponent {
  /**
   * Return the input document's encoding or <code>null</code> if it is the
   * platform's default encoding
   *
   * @return The document's encoding
   */
  public String getEncoding(Document document);

  /**
   * Generate source code from the input document for the target
   * <code>ProgrammingLanguage</code>.
   *
   * @param document The input document
   * @param filename The input document's original filename
   * @param programmingLanguage The target programming language
   * @return The generated source code
   * @exception Exception If an error occurs during code generation
   */
  public String generateCode(
    Document document, String filename, ProgrammingLanguage programmingLanguage
  ) throws Exception;
}
