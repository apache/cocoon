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
package org.apache.cocoon.components.language.generator;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.excalibur.source.Source;

import org.apache.cocoon.environment.SourceResolver;

/**
 * This interface defines a loader for programs automatically built from XML
 * documents written in a <code>MarkupLanguage</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: ProgramGenerator.java,v 1.2 2004/03/05 13:02:47 bdelacretaz Exp $
 */
public interface ProgramGenerator extends Component {

    String ROLE = ProgramGenerator.class.getName();

    /**
     * Load a program built from an XML document written in a
     * <code>MarkupLanguage</code>
     *
     * @param newManager  The ComponentManager that it will be loaded with
     * @param fileName The input document's <code>File</code> name
     * @param markupLanguage The <code>MarkupLanguage</code> in which the input
     * document is written
     * @param programmingLanguage The <code>ProgrammingLanguage</code> in which
     * the program must be written
     * @return The loaded object
     * @exception Exception If an error occurs during generation or loading
     * @deprecated Pass Source object instead of file name.
     */
    CompiledComponent load(
            ComponentManager newManager,
            String fileName,
            String markupLanguage,
            String programmingLanguage,
            SourceResolver resolver) throws Exception;

    /**
     * Load a program built from an XML document written in a
     * <code>MarkupLanguage</code>
     *
     * @param newManager  The ComponentManager that it will be loaded with
     * @param source The input document's <code>File</code> name
     * @param markupLanguage The <code>MarkupLanguage</code> in which the input
     * document is written
     * @param programmingLanguage The <code>ProgrammingLanguage</code> in which
     * the program must be written
     * @return The loaded object
     * @exception Exception If an error occurs during generation or loading
     */
    CompiledComponent load(
            ComponentManager newManager,
            Source source,
            String markupLanguage,
            String programmingLanguage,
            SourceResolver resolver) throws Exception;

    /**
     * Release a program instance built from an XML document written in a
     * <code>MarkupLanguage</code>.
     *
     * @param component to be released.
     */
    void release(CompiledComponent component);

    /**
     * Remove a program from the generator's cache and dipose all
     * instances of this program.
     *
     * @param source of the program to be removed.
     */
    void remove(Source source);
}
