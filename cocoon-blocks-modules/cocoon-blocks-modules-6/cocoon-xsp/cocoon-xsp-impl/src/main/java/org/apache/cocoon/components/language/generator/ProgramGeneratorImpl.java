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
package org.apache.cocoon.components.language.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.language.LanguageException;
import org.apache.cocoon.components.language.markup.MarkupLanguage;
import org.apache.cocoon.components.language.programming.CodeFormatter;
import org.apache.cocoon.components.language.programming.Program;
import org.apache.cocoon.components.language.programming.ProgrammingLanguage;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.util.IOUtils;

/**
 * The default implementation of <code>ProgramGenerator</code>
 *
 * @version $Id$
 */
public class ProgramGeneratorImpl extends AbstractLogEnabled
                                  implements ProgramGenerator, Contextualizable, Serviceable,
                                             Parameterizable, Disposable, ThreadSafe {

    /** The auto-reloading option */
    protected boolean autoReload = true;

    /** The pre-loading option */
    protected boolean preload = false;

    /** The check for manual source changes in the repository*/
    protected boolean watchSource = false;

    /**
     * The service selector for programs. Caches Program by program
     * source file.
     */
    protected GeneratorSelector cache;

    /** The component manager */
    protected ServiceManager manager;

    /** The markup language component selector */
    protected ServiceSelector markupSelector;

    /** The programming language component selector */
    protected ServiceSelector languageSelector;

    /** The working directory */
    protected File workDir;

    /** The root package */
    protected String rootPackage;

    /** Servlet Context Directory */
    protected String contextDir;


    /** Contextualize this class */
    public void contextualize(Context context) throws ContextException {
        if (this.contextDir == null) {
            org.apache.cocoon.environment.Context ctx =
                (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);

            // Determine the context directory, preferably as a file
            // FIXME (SW) - this is purposely redundant with some code in CocoonServlet
            //              to have the same rootPath. How to avoid this ?
            try {
                String rootPath = ctx.getRealPath("/");
                if (rootPath != null) {
                    this.contextDir = new File(rootPath).toURL().toExternalForm();
                } else {
                    String webInf = ctx.getResource("/WEB-INF").toExternalForm();
                    this.contextDir = webInf.substring(0, webInf.length() - "WEB-INF".length());
                }
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Context directory is " + this.contextDir);
                }
            } catch (MalformedURLException e) {
                getLogger().warn("Could not get context directory", e);
                this.contextDir = "";
            }
        }
    }

    /**
     * Set the global component manager. This method also sets the
     * <code>ServiceSelector</code> used as language factory for both markup
     * and programming languages.
     * @param manager The global component manager
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.cache = (GeneratorSelector) this.manager.lookup(GeneratorSelector.ROLE + "Selector");
        this.markupSelector = (ServiceSelector)this.manager.lookup(MarkupLanguage.ROLE + "Selector");
        this.languageSelector = (ServiceSelector)this.manager.lookup(ProgrammingLanguage.ROLE + "Selector");
        final Settings settings = (Settings)this.manager.lookup(Settings.ROLE);
        if (this.workDir == null) {
            this.workDir = new File(settings.getWorkDirectory());
        }
    }

    /**
     * Set the sitemap-provided configuration. This method sets the persistent code repository and the auto-reload option
     * @param params The configuration information
     * @exception ParameterException Not thrown here
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.autoReload = params.getParameterAsBoolean("auto-reload", autoReload);
        this.rootPackage = params.getParameter("root-package", "org.apache.cocoon.www");
        this.preload = params.getParameterAsBoolean("preload", preload);
        this.watchSource = params.getParameterAsBoolean("watch-source", watchSource);
    }

    /**
     * Generates program source file name in the working directory
     * from the source SystemID
     */
    private String getNormalizedName(final String systemId) {
        StringBuffer contextFilename = new StringBuffer(this.rootPackage.replace('.', File.separatorChar));
        contextFilename.append(File.separator);
        if(systemId.startsWith(this.contextDir)) {
            // VG: File is located under contextDir; using relative file name ...
            contextFilename.append(systemId.substring(this.contextDir.length()));
        } else {
            // VG: File is located outside of contextDir; using systemId ...
            contextFilename.append(systemId);
        }
        return IOUtils.normalizedFilename(contextFilename.toString());
    }

    /**
     * Load a program built from an XML document written in a <code>MarkupLanguage</code>
     *
     * @param fileName The input document's <code>File</code>
     * @param markupLanguageName The <code>MarkupLanguage</code> in which the input document is written
     * @param programmingLanguageName The <code>ProgrammingLanguage</code> in which the program must be written
     * @return The loaded program instance
     * @exception Exception If an error occurs during generation or loading
     * @deprecated Pass Source object instead of file name.
     */
    public CompiledComponent load(ServiceManager newManager,
                                  String fileName,
                                  String markupLanguageName,
                                  String programmingLanguageName,
                                  SourceResolver resolver)
    throws Exception {

        final Source source = resolver.resolveURI(fileName);
        try {
            return load(newManager, source, markupLanguageName, programmingLanguageName, resolver);
        } finally {
            resolver.release(source);
        }
    }

    /**
     * Load a program built from an XML document written in a <code>MarkupLanguage</code>.
     *
     * This method does not releases passed source object. Caller of the method must release
     * source when needed.
     *
     * @param source The input document's <code>File</code>
     * @param markupLanguageName The <code>MarkupLanguage</code> in which the input document is written
     * @param programmingLanguageName The <code>ProgrammingLanguage</code> in which the program must be written
     * @return The loaded program instance
     * @exception Exception If an error occurs during generation or loading
     */
    public CompiledComponent load(ServiceManager newManager,
                                  Source source,
                                  String markupLanguageName,
                                  String programmingLanguageName,
                                  SourceResolver resolver)
    throws Exception {

        final String id = source.getURI();

        ProgrammingLanguage programmingLanguage = null;
        MarkupLanguage markupLanguage = null;
        try {
            // Create file name for the program generated from the provided source.
            final String normalizedName = getNormalizedName(id);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Loading serverpage systemId=[" + id + "]" +
                    " markupLanguageName=[" + markupLanguageName + "]" +
                    " programmingLanguageName=[" + programmingLanguageName + "]" +
                    " -> normalizedName=[" + normalizedName + "]");
            }

            markupLanguage = (MarkupLanguage) this.markupSelector.select(markupLanguageName);
            programmingLanguage = (ProgrammingLanguage) this.languageSelector.select(programmingLanguageName);
            programmingLanguage.setLanguageName(programmingLanguageName);

            Program program = null;
            CompiledComponent programInstance = null;

            // Attempt to load program object from cache
            try {
                programInstance = (CompiledComponent) this.cache.select(normalizedName);
            } catch (Exception e) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("The serverpage [" + id + "] is not in the cache yet");
                }
            }

            if (programInstance == null && this.preload) {
                // Preloading: Load program if its source/[object file] is available
                try {
                    program = programmingLanguage.preload(normalizedName,
                                                          this.workDir,
                                                          markupLanguage.getEncoding());

                    this.cache.addGenerator(newManager, normalizedName, program);
                    programInstance = (CompiledComponent) this.cache.select(normalizedName);

                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Successfully preloaded serverpage [" + id + "]");
                    }
                } catch (Exception e) {
                    if (getLogger().isInfoEnabled()) {
                        getLogger().info("The serverpage [" + id
                                         + "] could not be preloaded, will be re-created ("
                                         + e + ")");
                    }
                }
            }

            if (programInstance == null) {
                synchronized (this) {
                    // Attempt again to load program object from cache.
                    // This avoids that simultaneous requests recompile
                    // the same XSP over and over again.
                    try {
                        programInstance = (CompiledComponent) this.cache.select(normalizedName);
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("The serverpage [" + id + "] was now in the cache");
                        }
                    } catch (Exception e) {
                        // no instance found
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Creating new serverpage for [" + id + "]");
                        }
                        generateSourcecode(source,
                                           normalizedName,
                                           markupLanguage,
                                           programmingLanguage);

                        programInstance = loadProgram(newManager,
                                                      normalizedName,
                                                      markupLanguage,
                                                      programmingLanguage);
                    }
                }
            } else {
                // found an instance
                if (this.autoReload) {
                    long sourceLastModified = source.getLastModified();
                    // Has XSP changed?
                    // Note : lastModified can be 0 if source is dynamically generated.
                    // In that case, let the program instance decide if it is modified or not.
                    if (programInstance.modifiedSince(sourceLastModified)) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("ReCreating serverpage for [" + id + "]");
                        }
                        synchronized (this) {
                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("Releasing old serverpage program [" + id + "]");
                            }
                            release(programInstance);
                            programmingLanguage.unload(program, normalizedName, this.workDir);
                            this.cache.removeGenerator(normalizedName);
                            programInstance = null;
                            program = null;

                            generateSourcecode(source,
                                               normalizedName,
                                               markupLanguage,
                                               programmingLanguage);

                            programInstance = loadProgram(newManager,
                                                          normalizedName,
                                                          markupLanguage,
                                                          programmingLanguage);
                        }
                    } else {
                        // check the repository for changes at all?
                        if (this.watchSource) {
                            if (getLogger().isDebugEnabled()) {
                                getLogger().debug("Checking sourcecode of [" + id + "] for a change");
                            }
                            File sourcecodeFile = new File(this.workDir,
                                                           normalizedName + "." + programmingLanguage.getSourceExtension());
                            // has sourcecode in repository changed ?
                            if (sourcecodeFile != null && sourcecodeFile.exists()) {
                                long sourcecodeLastModified = sourcecodeFile.lastModified();
                                if (sourcecodeLastModified > sourceLastModified
                                        || sourceLastModified == 0
                                        || sourcecodeLastModified == 0) {
                                    if (getLogger().isDebugEnabled()) {
                                        getLogger().debug("Create new serverpage program for [" + id + "] - repository has changed");
                                    }
                                    synchronized (this) {
                                        if (getLogger().isDebugEnabled()) {
                                            getLogger().debug("Releasing old serverpage program [" + id + "]");
                                        }
                                        release(programInstance);
                                        //programmingLanguage.unload(program, normalizedName, this.workDir);
                                        this.cache.removeGenerator(normalizedName);
                                        programInstance = null;
                                        program = null;

                                        programInstance = loadProgram(newManager,
                                                                      normalizedName,
                                                                      markupLanguage,
                                                                      programmingLanguage);
                                    }
                                } else {
                                    if (getLogger().isDebugEnabled()) {
                                        getLogger().debug("Sourcecode of [" + id + "] has not changed - returning program from cache");
                                    }
                                }
                            } else {
                                if (getLogger().isErrorEnabled()) {
                                    getLogger().error("Could not find sourcecode for [" + id + "]");
                                }
                            }
                        }
                    }
                } else {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Not checking for modifications [autoReload=false] - using current version");
                    }
                }
            }

            return programInstance;
        } finally {
            this.markupSelector.release(markupLanguage);
            this.languageSelector.release(programmingLanguage);
        }
    }

    private CompiledComponent loadProgram(ServiceManager newManager,
                                          String normalizedName,
                                          MarkupLanguage markupLanguage,
                                          ProgrammingLanguage programmingLanguage)
    throws Exception {

        CompiledComponent programInstance;
        try {
            return (CompiledComponent) this.cache.select(normalizedName);
        } catch (Exception e) {
            // Continue if not in cache
        }

        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Loading program [" + normalizedName + "]");
            }
            Program program = programmingLanguage.load(normalizedName, this.workDir, markupLanguage.getEncoding());

            this.cache.addGenerator(newManager, normalizedName, program);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Successfully loaded program [" + normalizedName + "]");
            }
        } catch (LanguageException le) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Got Language Exception", le);
            }
            throw new ProcessingException("Language Exception", le);
        }

        try {
            programInstance = (CompiledComponent) this.cache.select(normalizedName);
        } catch (Exception cme) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Can't load ServerPage: got exception", cme);
            }
            throw new ProcessingException("Can't load ServerPage", cme);
        }

        return (programInstance);
    }


    private void generateSourcecode(Source source,
                                    String normalizedName,
                                    MarkupLanguage markupLanguage,
                                    ProgrammingLanguage programmingLanguage)
            throws Exception {

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Creating sourcecode for [" + source.getURI() + "]");
        }

        // Generate code
        String code = markupLanguage.generateCode(source, normalizedName, programmingLanguage);
        if (code == null || code.length() == 0) {
            // FIXME(VG): Xalan with incremental-processing=true does not propagate exceptions
            // from working thread to main thread. See
            // http://issues.apache.org/bugzilla/show_bug.cgi?id=8033
            throw new ProcessingException("Failed to generate program code (this may happen " +
                    "if you use Xalan in incremental processing mode). " +
                    "Please check log file and/or console for errors.");
        }

        String encoding = markupLanguage.getEncoding();

        // Format source code if applicable
        CodeFormatter codeFormatter = programmingLanguage.getCodeFormatter();
        if (codeFormatter != null) {
            code = codeFormatter.format(code, encoding);
        }

        // Store generated code
        final File sourceFile = new File(this.workDir, normalizedName + "." + programmingLanguage.getSourceExtension());
        final File sourceDir = sourceFile.getParentFile();
        if (sourceDir != null) {
            sourceDir.mkdirs();
        }
        serializeString(sourceFile, code);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Successfully created sourcecode for [" + source.getURI() + "]");
        }
    }

    /**
     * Dump a <code>String</code> to a text file.
     *
     * @param file The output file
     * @param string The string to be dumped
     * @exception IOException IO Error
     */
    private static void serializeString(File file, String string) throws IOException {
        final Writer fw = new FileWriter(file);
        try {
            fw.write(string);
            fw.flush();
        } finally {
            fw.close();
        }
    }

    /**
     * Releases the program instance.
     * @param component program instance to be released
     */
    public void release(CompiledComponent component) {
        this.cache.release(component);
    }

    /**
     * Removes named program from the program generator's cache.
     * Disposes all created instances of the program.
     * @param source of the program to be removed
     */
    public void remove(Source source) {
        final String normalizedName = getNormalizedName(source.getURI());
        this.cache.removeGenerator(normalizedName);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.cache);
            this.cache = null;
            this.manager.release(this.markupSelector);
            this.markupSelector = null;
            this.manager.release(this.languageSelector);
            this.languageSelector = null;

            this.manager = null;
        }
        this.workDir = null;
        this.contextDir = null;
    }
}