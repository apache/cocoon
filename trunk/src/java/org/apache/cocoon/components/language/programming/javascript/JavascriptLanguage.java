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
import org.apache.cocoon.components.language.markup.xsp.XSLTExtension;
import org.apache.cocoon.components.language.programming.AbstractProgrammingLanguage;
import org.apache.cocoon.components.language.programming.Program;
import org.apache.cocoon.components.language.programming.ProgrammingLanguage;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The interpreted Javascript programming language.
 * Program in Javascript must have comment line as first line of file:
 * <pre>
 * // $Cocoon extends: org.apache.cocoon.components.language.xsp.JSGenerator$
 * </pre>
 * The class specified will be used as a Java wrapper interpreting javascript program.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: JavascriptLanguage.java,v 1.3 2003/12/29 13:31:33 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProgrammingLanguage
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=javascript-language
 */
public class JavascriptLanguage extends AbstractProgrammingLanguage implements ProgrammingLanguage {

    public Program preload(String filename, File baseDirectory, String encoding) throws LanguageException {
        return load(filename, baseDirectory, encoding);
    }

    public Program load(String filename, File baseDirectory, String encoding) throws LanguageException {
        // Does source file exist?
        File sourceFile = new File(baseDirectory,
                filename + "." + this.getSourceExtension());
        if (!sourceFile.exists()) {
            throw new LanguageException("Can't load program - File doesn't exist: "
                    + IOUtils.getFullFilename(sourceFile));
        }
        if (!sourceFile.isFile()) {
            throw new LanguageException("Can't load program - File is not a normal file: "
                    + IOUtils.getFullFilename(sourceFile));
        }
        if (!sourceFile.canRead()) {
            throw new LanguageException("Can't load program - File cannot be read: "
                    + IOUtils.getFullFilename(sourceFile));
        }

        Class clazz = null;
        ArrayList dependecies = new ArrayList();

        String className = null;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(sourceFile));
            className = getMeta(r.readLine(), "extends");
            if (className == null) {
                throw new LanguageException("Can't load program - Signature is not found: "
                        + IOUtils.getFullFilename(sourceFile));
            }

            clazz = ClassUtils.loadClass(className);

            String line;
            while((line = getMeta(r.readLine(), "depends")) != null) {
                dependecies.add(line);
            }
        } catch (IOException e) {
            throw new LanguageException("Can't load program - Signature is not found: "
                    + IOUtils.getFullFilename(sourceFile));
        } catch (ClassNotFoundException e) {
            throw new LanguageException("Can't load program - Base class " + className + " is not found: "
                    + IOUtils.getFullFilename(sourceFile));
        } finally {
            if (r != null) try {
                r.close();
            } catch (IOException ignored) {
            }
        }

        return new JavascriptProgram(sourceFile, clazz, dependecies);
    }

    private String getMeta(String line, String meta) {
        if (line == null) {
            return null;
        }

        meta = "$Cocoon " + meta + ": ";
        int i = line.indexOf(meta);
        if (i != -1) {
            int j = line.indexOf("$", i + 1);
            if (j != -1) {
                line = line.substring(i + meta.length(), j);
            } else {
                line = null;
            }
        } else {
            line = null;
        }
        return line;
    }

    protected void doUnload(Object program, String filename, File baseDir)
            throws LanguageException {
        // Do nothing. Source is already deleted by the AbstractProgrammingLanguage.
    }

    public String quoteString(String constant) {
        return XSLTExtension.escapeJavaString(constant);
    }

    /**
     * Return the language's canonical source file extension.
     *
     * @return The source file extension
     */
    public String getSourceExtension() {
        return "js";
    }
}
