/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup;

import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.IOException;
import org.xml.sax.SAXException;

/**
 * A logicsheet-based implementation of <code>MarkupGenerator</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-05-23 23:09:56 $
 */
public class LogicsheetCodeGenerator implements MarkupCodeGenerator {
  /**
   * Ordered list of logicsheet to be used in code generation
   */
  protected Vector logicsheets;

  /**
   * The default constructor
   */
  public LogicsheetCodeGenerator() {
    this.logicsheets = new Vector();
  }

  /**
   * Add a logicsheet to the logicsheet list
   *
   * @param logicsheet The logicsheet to be added
   */
  public void addLogicsheet(Logicsheet logicsheet) {
    this.logicsheets.addElement(logicsheet);
  }

  /**
   * Generate source code from the input document. Filename information is
   * ignored in the logicsheet-based code generation approach
   *
   * @param input The input document
   * @param filename The input source original filename
   * @return The generated source code
   * @exception Exception If an error occurs during code generation
   */
  public String generateCode(Document input, String filename) throws Exception {
    int count = this.logicsheets.size();
    for (int i = 0; i < count; i++) {
      Logicsheet logicsheet = (Logicsheet) this.logicsheets.elementAt(i);
      input = logicsheet.apply(input);
    }

    Element result = input.getDocumentElement();
    result.normalize();

    return result.getFirstChild().getNodeValue();
  }
}
