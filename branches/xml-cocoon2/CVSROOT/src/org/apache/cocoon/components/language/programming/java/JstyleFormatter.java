/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.programming.java;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import jstyle.JSFormatter;
import jstyle.JSBeautifier;

import org.apache.cocoon.components.language.programming.*;

import org.apache.log.Logger;
import org.apache.avalon.logger.Loggable;

/**
 * This class implements <code>CodeFormatter</code> based on
 * Tal Davidson's (davidsont@bigfoot.com) <i>Jstyle</i> Java
 * beautifier. This implementation is very improvised...
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-04-20 20:49:59 $
 */
public class JstyleFormatter implements CodeFormatter, Loggable {
  protected Logger log;
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

    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }

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
      log.debug("JstyleFormatter.format()", e);
      return code;
    }
  }

  /**
   * Convert a byte array stream to string according to a given encoding.
   * The encoding can be <code>null</code> for the platform's default
   * encoding
   *
   * @param PARAM_NAME Param description
   * @return the value
   * @exception EXCEPTION_NAME If an error occurs
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
