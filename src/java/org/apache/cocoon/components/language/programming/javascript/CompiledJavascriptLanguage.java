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
package org.apache.cocoon.components.language.programming.javascript;

import org.apache.cocoon.components.language.LanguageException;
import org.apache.cocoon.components.language.programming.java.JavaLanguage;

import org.mozilla.javascript.tools.jsc.Main;

import java.io.File;

/**
 * The compiled Javascript (Rhino) programming language processor
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: CompiledJavascriptLanguage.java,v 1.1 2003/03/09 00:09:01 pier Exp $
 */
public class CompiledJavascriptLanguage extends JavaLanguage {

    /**
     * Return the language's canonical source file extension.
     *
     * @return The source file extension
     */
    public String getSourceExtension() {
        return "js";
    }

    /**
     * Compile a source file yielding a loadable class file.
     *
     * @param name The object program base file name
     * @param baseDirectory The directory containing the object program file
     * @param encoding The encoding expected in the source file or
     * <code>null</code> if it is the platform's default encoding
     * @exception LanguageException If an error occurs during compilation
     */
    protected void compile(
            String name, File baseDirectory, String encoding
            ) throws LanguageException {
        try {
            int pos = name.lastIndexOf(File.separatorChar);
            String filename = name.substring(pos + 1);
            String pathname =
                    baseDirectory.getCanonicalPath() + File.separator +
                    name.substring(0, pos).replace(File.separatorChar, '/');
            String packageName =
                    name.substring(0, pos).replace(File.separatorChar, '.');

            String[] args = {
                "-extends",
                "org.apache.cocoon.components.language.markup.xsp.JSGenerator",
                "-nosource",
                "-O", "9",
                "-package", packageName,
                "-o", filename + ".class",
                pathname + File.separator + filename + "." + this.getSourceExtension()
            };

            Main.main(args);
        } catch (Exception e) {
            getLogger().warn("JavascriptLanguage.compile", e);
            throw new LanguageException(e.getMessage());
        }
    }
}
