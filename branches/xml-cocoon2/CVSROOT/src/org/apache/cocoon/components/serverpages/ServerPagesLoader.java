/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.serverpages;

import java.io.File;
import org.apache.arch.Component;
import org.apache.cocoon.generators.ServerPagesGenerator;

/**
 * This interface defines a <code>Generator</code> loader for Cocoon generators
 * automatically built from XML documents writeen in a
 * <code>MarkupLanguage</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-05-23 23:10:09 $
 */
public interface ServerPagesLoader extends Component {
  /**
   * Load a <code>Generator</code> built from an XML document written in a
   * <code>MarkupLanguage</code>
   *
   * @param file The input document's <code>File</code>
   * @param markupLanguage The <code>MarkupLanguage</code> in which the input
   * document is written
   * @param programmingLanguage The <code>ProgrammingLanguage</code> in which
   * the generator must be written
   * @return The loaded generator
   * @exception Exception If an error occurs during generation or loading
   */
  public ServerPagesGenerator load(
    File file, String markupLanguage, String programmingLanguage
  ) throws Exception;
}
