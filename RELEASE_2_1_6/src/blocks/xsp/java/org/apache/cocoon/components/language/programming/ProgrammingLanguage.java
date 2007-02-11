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

import org.apache.avalon.framework.component.Component;

import org.apache.cocoon.components.language.LanguageException;
import org.apache.cocoon.components.language.generator.CompiledComponent;

import java.io.File;

/**
 * This interface states the functionality of a programming language processor
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: ProgrammingLanguage.java,v 1.1 2004/03/10 12:58:07 stephan Exp $
 */
public interface ProgrammingLanguage extends Component {

    String ROLE = ProgrammingLanguage.class.getName();

    /**
     * Return the programming language's source file extension
     *
     * @return The canonical source file extension
     */
    String getSourceExtension();

    /**
     * Preload a program from a file
     *
     * @param filename The program base file name
     * @param baseDirectory The directory containing the program file
     * @param encoding The encoding expected in the source file or
     * <code>null</code> if it is the platform's default encoding
     * @return The loaded program
     * @exception LanguageException If an error occurs during loading
     */
    Program preload(String filename, File baseDirectory, String encoding)
            throws LanguageException;

    /**
     * Load a program from a file
     *
     * @param filename The program base file name
     * @param baseDirectory The directory containing the program file
     * @param encoding The encoding expected in the source file or
     * <code>null</code> if it is the platform's default encoding
     * @return The loaded program
     * @exception LanguageException If an error occurs during loading
     */
    Program load(String filename, File baseDirectory, String encoding)
            throws LanguageException;

    /**
     * Create a new instance for the given program type
     *
     * @param program The program type
     * @return A new program type instance
     * @exception LanguageException If an instantiation error occurs
     */
    // FIXME(VG): Not used
    CompiledComponent instantiate(Program program) throws LanguageException;

    /**
     * Unload from memory and invalidate a given program
     *
     * @param program The program
     * @param filename The name of the file this program was loaded from
     * @param baseDirectory The directory containing the program file
     * @exception LanguageException If an error occurs
     */
    void unload(Object program, String filename, File baseDirectory) // unload(Program ?
            throws LanguageException;

    /**
     * Return the <code>CodeFormatter</code> associated with this programming
     * language
     *
     * @return The code formatter object or <code>null</code> if none is
     * available
     */
    CodeFormatter getCodeFormatter();

    /**
     * Escape a <code>String</code> according to the programming language's
     * string constant encoding rules.
     *
     * @param constant The string to be escaped
     * @return The escaped string
     */
    String quoteString(String constant);

    /**
     * Set Language Name
     *
     * @param name The name of the language
     */
    void setLanguageName(String name);

    /**
     * Get Language Name
     *
     * @return The name of the language
     */
    String getLanguageName();
}
