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
package org.apache.cocoon.components.language.programming.javascript;

import org.apache.cocoon.components.language.LanguageException;
import org.apache.cocoon.components.language.programming.java.JavaLanguage;

import org.mozilla.javascript.tools.jsc.Main;

import java.io.File;

/**
 * The compiled Javascript (Rhino) programming language processor
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: CompiledJavascriptLanguage.java,v 1.2 2004/03/08 13:58:32 cziegeler Exp $
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
