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

import org.apache.cocoon.components.language.programming.ProgrammingLanguage;
import org.apache.excalibur.source.Source;

/**
 * This interface defines a markup language whose SAX producer's instance are to
 * be translated into an executable program capable or transforming the original
 * document augmenting it with dynamic content
 *
 * @version $Id$
 */
public interface MarkupLanguage {

    String ROLE = MarkupLanguage.class.getName();

    /**
     * Return the input document's encoding or <code>null</code> if it is the
     * platform's default encoding.
     * This method should be called after <code>generateCode<code> method.
     *
     * @return The input document's encoding
     */
    String getEncoding();

    /**
     * Generate source code from the input source for the target
     * <code>ProgrammingLanguage</code>.
     *
     * @param source The source document
     * @param filename The input document's original filename
     * @param programmingLanguage The target programming language
     * @return The generated source code
     * @exception Exception If an error occurs during code generation
     */
    String generateCode(Source source,
                        String filename,
                        ProgrammingLanguage programmingLanguage)
            throws Exception;
}
