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
package org.apache.cocoon.components.language.markup;

import org.apache.excalibur.source.Source;
import org.apache.cocoon.xml.AbstractXMLPipe;

/**
 * This interfaces defines the functionality of a source code generator
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: MarkupCodeGenerator.java,v 1.1 2004/03/10 12:58:04 stephan Exp $
 */
public interface MarkupCodeGenerator {

    /**
     * Generate source code from the given markup source.
     * Start and end specify SAX pre processing pipeline.
     *
     * @param source The source of the markup program
     * @param filter Pre-processing SAX filter
     * @return The generated source code
     * @exception Exception If an error occurs during code generation
     */
    String generateCode(Source source, AbstractXMLPipe filter)
        throws Exception;
}
