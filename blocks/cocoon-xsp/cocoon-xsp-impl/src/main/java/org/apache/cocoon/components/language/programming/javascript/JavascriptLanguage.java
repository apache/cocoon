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
package org.apache.cocoon.components.language.programming.javascript;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.cocoon.components.language.LanguageException;
import org.apache.cocoon.components.language.markup.xsp.XSLTExtension;
import org.apache.cocoon.components.language.programming.AbstractProgrammingLanguage;
import org.apache.cocoon.components.language.programming.Program;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.IOUtils;

/**
 * The interpreted Javascript programming language.
 * Program in Javascript must have comment line as first line of file:
 * <pre>
 * // $Cocoon extends: org.apache.cocoon.components.language.xsp.JSGenerator$
 * </pre>
 * The class specified will be used as a Java wrapper interpreting javascript program.
 *
 * @version $Id$
 */
public class JavascriptLanguage extends AbstractProgrammingLanguage {

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
