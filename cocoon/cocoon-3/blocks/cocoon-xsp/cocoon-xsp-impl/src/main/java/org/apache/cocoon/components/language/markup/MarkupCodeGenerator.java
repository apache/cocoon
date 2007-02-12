/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * @version $Id$
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
