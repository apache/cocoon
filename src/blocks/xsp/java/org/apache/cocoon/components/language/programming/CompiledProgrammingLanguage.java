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

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;

import org.apache.cocoon.Constants;
import org.apache.cocoon.components.language.LanguageException;
import org.apache.cocoon.components.language.programming.java.JavaProgram;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.IOUtils;

import java.io.File;

/**
 * A compiled programming language. This class extends <code>AbstractProgrammingLanguage</code> adding support for compilation
 * and object program files
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: CompiledProgrammingLanguage.java,v 1.1 2004/03/10 12:58:07 stephan Exp $
 */
public abstract class CompiledProgrammingLanguage extends AbstractProgrammingLanguage implements Contextualizable {

    /** The compiler */
    protected Class compilerClass;

    /** The local classpath */
    protected String classpath;

    /** The source deletion option */
    protected boolean deleteSources = false;

    /**
     * Set the configuration parameters. This method instantiates the sitemap-specified language compiler
     * @param params The configuration parameters
     * @exception ParameterException If the language compiler cannot be loaded
     */
    public void parameterize(Parameters params) throws ParameterException {
        super.parameterize(params);

        String compilerClass = params.getParameter("compiler");
        try {
            this.compilerClass = ClassUtils.loadClass(compilerClass);
        } catch (ClassNotFoundException e) {
            throw new ParameterException("Unable to load compiler: " + compilerClass, e);
        }
        this.deleteSources = params.getParameterAsBoolean("delete-sources", false);
    }

    /**
     * Set the context
     * @param context The context
     */
    public void contextualize(Context context) throws ContextException {
        this.classpath = (String) context.get(Constants.CONTEXT_CLASSPATH);
    }

    /**
     * Return the language's canonical object file extension.
     * @return The object file extension
     */
    public abstract String getObjectExtension();

    /**
     * Unload a previously loaded program
     * @param program A previously loaded object program
     * @exception LanguageException If an error occurs during unloading
     */
    public abstract void doUnload(Object program) throws LanguageException;

    /**
     * Unload a previously loaded program given its original filesystem location
     * @param program The previously loaded object program
     * @param filename The base filename of the object program
     * @param baseDirectory The directory contaning the object program file
     * @exception LanguageException If an error occurs
     */
    protected final void doUnload(Object program, String filename, File baseDirectory) throws LanguageException {
        int index = filename.lastIndexOf(File.separator);
        String dir = filename.substring(0, index);
        String file = filename.substring(index + 1);

        File baseDir = new File(baseDirectory, dir);
        File[] files = baseDir.listFiles();

        for (int i = 0;(files != null) && (i < files.length); i++) {
            if (files[i].getName().startsWith(file)) {
                files[i].delete();
            }
        }
        this.doUnload(program);
    }

    /**
     * Actually load an object program from a file.
     * @param filename The object program base file name
     * @param baseDirectory The directory containing the object program file
     * @return The loaded object program
     * @exception LanguageException If an error occurs during loading
     */
    protected abstract Class loadProgram(String filename, File baseDirectory) throws LanguageException;

    /**
     * Compile a source file yielding a loadable object file.
     * @param filename The object program base file name
     * @param baseDirectory The directory containing the object program file
     * @param encoding The encoding expected in the source file or <code>null</code> if it is the platform's default encoding
     * @exception LanguageException If an error occurs during compilation
     */
    protected abstract void compile(String filename, File baseDirectory, String encoding) throws LanguageException;

    /**
     * Preload an object program from a file.
     * This method does not compiles the corresponding source file.
     *
     * @param filename The object program base file name
     * @param baseDirectory The directory containing the object program file
     * @param encoding The encoding expected in the source file or <code>null</code> if it is the platform's default encoding
     * @return The loaded object program
     * @exception LanguageException If an error occurs during compilation
     */
    public Program preload(String filename, File baseDirectory, String encoding) throws LanguageException {
        // Don't need to test for existence of the object code as it might be bundled into the WAR.
        try {
            Class program = this.loadProgram(filename, baseDirectory);
            // Create and discard test instance.
            program.newInstance();
            return new JavaProgram(program);
        } catch (Throwable t) {
            throw new LanguageException("Unable to preload program " + filename, t);
        }
    }

    /**
     * Load an object program from a file.
     * This method compiles the corresponding source file if necessary.
     *
     * @param filename The object program base file name
     * @param baseDirectory The directory containing the object program file
     * @param encoding The encoding expected in the source file or <code>null</code> if it is the platform's default encoding
     * @return The loaded object program
     * @exception LanguageException If an error occurs during compilation
     */
    public Program load(String filename, File baseDirectory, String encoding) throws LanguageException {

        // Does source file exist?
        File sourceFile = new File(baseDirectory, filename + "." + this.getSourceExtension());
        if (!sourceFile.exists()) {
            throw new LanguageException("Can't load program - File doesn't exist: " + IOUtils.getFullFilename(sourceFile));
        }
        if (!sourceFile.isFile()) {
            throw new LanguageException("Can't load program - File is not a normal file: " + IOUtils.getFullFilename(sourceFile));
        }
        if (!sourceFile.canRead()) {
            throw new LanguageException("Can't load program - File cannot be read: " + IOUtils.getFullFilename(sourceFile));
        }
        this.compile(filename, baseDirectory, encoding);
        if (this.deleteSources) {
            sourceFile.delete();
        }
        Class program = this.loadProgram(filename, baseDirectory);

        // Try to instantiate once to ensure there are no exceptions thrown in the constructor
        try {
            // Create and discard test instance
            program.newInstance();
        } catch(IllegalAccessException iae) {
            getLogger().debug("No public constructor for class " + program.getName());
        } catch(Exception e) {
            // Unload class and delete the object file, or it won't be recompiled
            // (leave the source file to allow examination).
            this.doUnload(program);
            new File(baseDirectory, filename + "." + this.getObjectExtension()).delete();

            String message = "Error while instantiating " + filename;
            getLogger().debug(message, e);
            throw new LanguageException(message, e);
        }

        if (program == null) {
            throw new LanguageException("Can't load program : " + baseDirectory.toString() + File.separator + filename);
        }

        return new JavaProgram(program);
    }
}
