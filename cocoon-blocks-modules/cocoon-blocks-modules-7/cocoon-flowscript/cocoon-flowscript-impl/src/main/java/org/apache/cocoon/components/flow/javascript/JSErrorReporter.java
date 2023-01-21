/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.components.flow.javascript;

import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.LocationImpl;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.tools.ToolErrorReporter;

/**
 * Implements a Rhino JavaScript {@link
 * org.mozilla.javascript.ErrorReporter}. 
 * Like ToolErrorReporter but logs to supplied logger instead of stdout
 *
 * @version $Id$
 */
public class JSErrorReporter extends AbstractLogEnabled
                             implements ErrorReporter {

  private Location location;
  private StringBuffer message;


  public JSErrorReporter() {
  }
  
  private void appendMessage(String text, String sourceName, int line, int column) {
      if (location == null) {
          location = new LocationImpl(null, sourceName, line, column);
          message = new StringBuffer();
      } else {
          // Append a linefeed
          message.append("\n");
      }
      
      message.append(text);
  }

  public void error(String message,
                    String sourceName, int line,
                    String lineSrc, int column)
  {
      String errMsg = getErrorMessage("msg.error", message, 
                                      sourceName, line, lineSrc, column);
      appendMessage(errMsg, sourceName, line, column);
      System.err.println(errMsg);
      getLogger().error(errMsg);
  }

  public void warning(String message, String sourceName, int line,
                      String lineSrc, int column)
  {
      String errMsg = getErrorMessage("msg.warning", message, 
                                    sourceName, line, lineSrc, column);
      appendMessage(errMsg, sourceName, line, column);
      System.err.println(errMsg);
      getLogger().warn(errMsg);
  }
    
  public EvaluatorException runtimeError(String message, String sourceName,
                                         int line, String lineSrc,
                                         int column)
  {
      String errMsg = getErrorMessage("msg.error", message,
                                      sourceName, line,
                                      lineSrc, column);
      appendMessage(errMsg, sourceName, line, column);
      System.err.println(errMsg);
      // FIXME(SW): need to build a locatable extension to EvaluatorException
      return new EvaluatorException(this.message.toString());
  }

  /**
   * Formats error message
   *
   * @param type a <code>String</code> value, indicating the error
   * type (error or warning)
   * @param message a <code>String</code> value, the error or warning
   * message
   * @param line an <code>int</code> value, the original cummulative
   * line number
   * @param lineSource a <code>String</code> value, the text of the
   * line in the file
   * @param column an <code>int</code> value, the column in
   * <code>lineSource</code> where the error appeared
   * @return a <code>String</code> value, the aggregated error
   * message, with the source file and line number adjusted to the
   * real values
   */
    String getErrorMessage(String type,
                           String message,
                           String sourceName, int line,
                           String lineSource, int column)
    {
        if (line > 0) {
            if (sourceName != null) {
                Object[] errArgs = { sourceName, new Integer(line), message };
                return ToolErrorReporter.getMessage("msg.format3", errArgs);
          } else {
              Object[] errArgs = { new Integer(line), message };
              return ToolErrorReporter.getMessage("msg.format2", errArgs);
            }
        } else {
            Object[] errArgs = { message };
            return ToolErrorReporter.getMessage("msg.format1", errArgs);
        }
    }
}
