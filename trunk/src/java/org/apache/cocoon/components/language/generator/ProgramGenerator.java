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
package org.apache.cocoon.components.language.generator;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.source.Source;
import org.apache.cocoon.environment.SourceResolver;

/**
 * This interface defines a loader for programs automatically built from XML
 * documents written in a <code>MarkupLanguage</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: ProgramGenerator.java,v 1.4 2003/12/29 13:30:11 unico Exp $
 */
public interface ProgramGenerator {

    String ROLE = ProgramGenerator.class.getName();

    /**
     * Load a program built from an XML document written in a
     * <code>MarkupLanguage</code>
     *
     * @param newManager  The ServiceManager that it will be loaded with
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
            ServiceManager newManager,
            String fileName,
            String markupLanguage,
            String programmingLanguage,
            SourceResolver resolver) throws Exception;

    /**
     * Load a program built from an XML document written in a
     * <code>MarkupLanguage</code>
     *
     * @param newManager  The ServiceManager that it will be loaded with
     * @param source The input document's <code>File</code> name
     * @param markupLanguage The <code>MarkupLanguage</code> in which the input
     * document is written
     * @param programmingLanguage The <code>ProgrammingLanguage</code> in which
     * the program must be written
     * @return The loaded object
     * @exception Exception If an error occurs during generation or loading
     */
    CompiledComponent load(
            ServiceManager newManager,
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
