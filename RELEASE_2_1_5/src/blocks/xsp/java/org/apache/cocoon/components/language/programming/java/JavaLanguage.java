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
package org.apache.cocoon.components.language.programming.java;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.classloader.ClassLoaderManager;
import org.apache.cocoon.components.language.LanguageException;
import org.apache.cocoon.components.language.markup.xsp.XSLTExtension;
import org.apache.cocoon.components.language.programming.CompiledProgrammingLanguage;
import org.apache.cocoon.components.language.programming.CompilerError;
import org.apache.cocoon.components.language.programming.LanguageCompiler;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.JavaArchiveFilter;

/**
 * The Java programming language processor
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: JavaLanguage.java,v 1.1 2004/03/10 12:58:07 stephan Exp $
 */
public class JavaLanguage extends CompiledProgrammingLanguage
        implements Initializable, ThreadSafe, Serviceable, Disposable {

    /** The class loader */
    private ClassLoaderManager classLoaderManager;

    /** The service manager */
    protected ServiceManager manager = null;

    /** Classpath */
    private String classpath;

    private String classLoaderClass;
    
    /**
     * Return the language's canonical source file extension.
     *
     * @return The source file extension
     */
    public String getSourceExtension() {
        return "java";
    }

    /**
     * Return the language's canonical object file extension.
     *
     * @return The object file extension
     */
    public String getObjectExtension() {
        return "class";
    }

    /**
     * Set the configuration parameters. This method instantiates the
     * sitemap-specified <code>ClassLoaderManager</code>
     *
     * @param params The configuration parameters
     * @throws ParameterException If the class loader manager cannot be
     *                            instantiated or looked up.
     */
    public void parameterize(Parameters params) throws ParameterException {
        super.parameterize(params);

        this.classLoaderClass = params.getParameter("class-loader", null);
        if (this.classLoaderClass != null) {
            try {
                this.classLoaderManager = (ClassLoaderManager)
                        ClassUtils.newInstance(this.classLoaderClass);
            } catch (Exception e) {
                throw new ParameterException("Unable to load class loader: "
                                             + this.classLoaderClass, e);
            }
        } else {
            try {
                getLogger().debug("Looking up " + ClassLoaderManager.ROLE);
                this.classLoaderManager = (ClassLoaderManager)
                        manager.lookup(ClassLoaderManager.ROLE);
            } catch (ServiceException e) {
                throw new ParameterException("Lookup of ClassLoaderManager failed", e);
            }
        }
    }

    /**
     * Set the global service manager.
     *
     * @param manager The global service manager
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public void initialize() throws Exception {

        // Initialize the classpath
        String systemBootClasspath = System.getProperty("sun.boot.class.path");
        String systemClasspath = System.getProperty("java.class.path");
        String systemExtDirs = System.getProperty("java.ext.dirs");
        String systemExtClasspath = null;

        try {
            systemExtClasspath = expandDirs(systemExtDirs);
        } catch (Exception e) {
            getLogger().warn("Could not expand Directory:" + systemExtDirs, e);
        }

        this.classpath =
            ((super.classpath != null) ? File.pathSeparator + super.classpath : "") +
            ((systemBootClasspath != null) ? File.pathSeparator + systemBootClasspath : "") +
            ((systemClasspath != null) ? File.pathSeparator + systemClasspath : "") +
            ((systemExtClasspath != null) ? File.pathSeparator + systemExtClasspath : "");
    }

    /**
     * Actually load an object program from a class file.
     *
     * @param name The object program base file name
     * @param baseDirectory The directory containing the object program file
     * @return The loaded object program
     * @exception LanguageException If an error occurs during loading
     */
    protected Class loadProgram(String name, File baseDirectory)
            throws LanguageException {
        try {
            this.classLoaderManager.addDirectory(baseDirectory);
            return this.classLoaderManager.loadClass(name.replace(File.separatorChar, '.'));
        } catch (Exception e) {
            throw new LanguageException("Could not load class for program '" + name + "' due to a " + e.getClass().getName() + ": " + e.getMessage());
        }
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
    protected void compile(String name, File baseDirectory, String encoding)
            throws LanguageException {

        try {
            LanguageCompiler compiler = (LanguageCompiler) this.compilerClass.newInstance();
            // AbstractJavaCompiler is LogEnabled
            if (compiler instanceof LogEnabled) {
                ((LogEnabled)compiler).enableLogging(getLogger());
            }

            int pos = name.lastIndexOf(File.separatorChar);
            String filename = name.substring(pos + 1);

            final String basePath = baseDirectory.getCanonicalPath();
            String filepath = basePath + File.separator
                    + name + "." + getSourceExtension();

            compiler.setFile(filepath);
            compiler.setSource(basePath);
            compiler.setDestination(basePath);
            compiler.setClasspath(basePath + this.classpath);

            if (encoding != null) {
                compiler.setEncoding(encoding);
            }

            getLogger().debug("Compiling " + filepath);
            if (!compiler.compile()) {
                StringBuffer message = new StringBuffer("Error compiling ");
                message.append(filename);
                message.append(":\n");

                List errors = compiler.getErrors();
                CompilerError[] compilerErrors = new CompilerError[errors.size()];
                errors.toArray(compilerErrors);

                throw new LanguageException(message.toString(), filepath, compilerErrors);
            }

        } catch (InstantiationException e) {
            getLogger().warn("Could not instantiate the compiler", e);
            throw new LanguageException("Could not instantiate the compiler: " + e.getMessage());
        } catch (IllegalAccessException e) {
            getLogger().warn("Could not access the compiler class", e);
            throw new LanguageException("Could not access the compiler class: " + e.getMessage());
        } catch (IOException e) {
            getLogger().warn("Error during compilation", e);
            throw new LanguageException("Error during compilation: " + e.getMessage());
        }
    }

    /**
     * Unload a previously loaded class. This method simply reinstantiates the
     * class loader to ensure that a new version of the same class will be
     * correctly loaded in a future loading operation
     *
     * @param program A previously loaded class
     * @exception LanguageException If an error occurs during unloading
     */
    public void doUnload(Object program) throws LanguageException {
        this.classLoaderManager.reinstantiate();
    }

    /**
     * Escape a <code>String</code> according to the Java string constant
     * encoding rules.
     *
     * @param constant The string to be escaped
     * @return The escaped string
     */
    public String quoteString(String constant) {
        return XSLTExtension.escapeJavaString(constant);
    }

    /**
     * Expand a directory path or list of directory paths (File.pathSeparator
     * delimited) into a list of file paths of all the jar files in those
     * directories.
     *
     * @param dirPaths The string containing the directory path or list of
     * 		directory paths.
     * @return The file paths of the jar files in the directories. This is an
     *		empty string if no files were found, and is terminated by an
     *		additional pathSeparator in all other cases.
     */
    private String expandDirs(String dirPaths) {
        StringTokenizer st = new StringTokenizer(dirPaths, File.pathSeparator);
        StringBuffer buffer = new StringBuffer();
        while (st.hasMoreTokens()) {
            String d = st.nextToken();
            File dir = new File(d);
            if (!dir.isDirectory()) {
                // The absence of a listed directory may not be an error.
                if (getLogger().isWarnEnabled()) getLogger().warn("Attempted to retrieve directory listing of non-directory " + dir.toString());
            } else {
                File[] files = dir.listFiles(new JavaArchiveFilter());
                for (int i = 0; i < files.length; i++) {
                    buffer.append(files[i]).append(File.pathSeparator);
                }
            }
        }

        return buffer.toString();
    }

    /**
     *  dispose
     */
    public void dispose() {
        if (this.classLoaderClass == null && this.classLoaderManager != null) {
            manager.release(this.classLoaderManager);
            this.classLoaderManager = null;
        }
    }
}
