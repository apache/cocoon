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
package org.apache.cocoon.components.language.programming;

/**
 * This interface defines source beautifying formatting
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: CodeFormatter.java,v 1.1 2004/03/10 12:58:07 stephan Exp $
 */
public interface CodeFormatter {
  /**
   * Format and beautify a <code>String</code> containing source code
   *
   * @param code The input source code
   * @param encoding The encoding used for constant strings embedded in the
   * source code
   * @return The formatted source code
   */
  String format(String code, String encoding);
}
