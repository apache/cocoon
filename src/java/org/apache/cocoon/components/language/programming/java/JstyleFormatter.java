/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: JstyleFormatter.java,v 1.3 2004/03/01 03:50:57 antonio Exp $
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
