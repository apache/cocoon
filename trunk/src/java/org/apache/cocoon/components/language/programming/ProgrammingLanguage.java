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
package org.apache.cocoon.components.language.programming;

import org.apache.cocoon.components.language.LanguageException;
import org.apache.cocoon.components.language.generator.CompiledComponent;

import java.io.File;

/**
 * This interface states the functionality of a programming language processor
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: ProgrammingLanguage.java,v 1.2 2004/02/06 23:34:33 joerg Exp $
 */
public interface ProgrammingLanguage {

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
