/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.language.programming.java;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import jstyle.JSBeautifier;
import jstyle.JSFormatter;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.components.language.programming.CodeFormatter;

/**
 * This class implements <code>CodeFormatter</code> based on
 * Tal Davidson's (davidsont@bigfoot.com) <i>Jstyle</i> Java
 * beautifier. This implementation is very improvised...
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: JstyleFormatter.java,v 1.4 2004/03/05 13:02:48 bdelacretaz Exp $
 */
public class JstyleFormatter extends AbstractLogEnabled implements CodeFormatter {
  /**
   * The default preferred line length. Should be parametrized!
   */
  protected static final int PREFERRED_LINE_LENGTH = 72;
  /**
   * The default line length deviation. Should be parametrized!
   */
  protected static final int LINE_LENGTH_DEVIATION = 8;

  /**
   * The default space indentation. Should be parametrized!
   */
  protected static final int SPACE_INDENTATION = 2;

  /**
   * Format and beautify a <code>String</code> containing source code.
   * This class has 2 pases: one for beautifying and another one for
   * indentation. This should be performed in a single step!!!
   *
   * @param code The input source code
   * @param encoding The encoding used for constant strings embedded in the
   * source code
   * @return The formatted source code
   */
  public String format(String code, String encoding) {
    try {
      JSFormatter formatter = new JSFormatter();

      formatter.setPreferredLineLength(PREFERRED_LINE_LENGTH);
      formatter.setLineLengthDeviation(LINE_LENGTH_DEVIATION);

      ByteArrayOutputStream out = new ByteArrayOutputStream(code.length());

      formatter.format(
        new BufferedReader(new StringReader(code)), new PrintWriter(out, true)
      );

      JSBeautifier beautifier = new JSBeautifier();

      code = this.getString(out, encoding);

      out = new ByteArrayOutputStream(code.length());

      beautifier.setSpaceIndentation(SPACE_INDENTATION);

      beautifier.beautifyReader(
        new BufferedReader(new StringReader(code)), new PrintWriter(out, true)
      );

      return this.getString(out, encoding);
    } catch (Exception e) {
      getLogger().debug("JstyleFormatter.format()", e);
      return code;
    }
  }

  /**
   * Convert a byte array stream to string according to a given encoding.
   * The encoding can be <code>null</code> for the platform's default
   * encoding
   *
   * @param out
   * @param encoding
   * @return the value
   * @exception UnsupportedEncodingException
   */
  protected String getString(ByteArrayOutputStream out, String encoding)
    throws UnsupportedEncodingException
  {
    if (encoding == null) {
      return out.toString();
    }

    return out.toString(encoding);
  }
}
