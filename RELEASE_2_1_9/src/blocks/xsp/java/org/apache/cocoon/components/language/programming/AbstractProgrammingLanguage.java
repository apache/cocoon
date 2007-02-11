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

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;

import org.apache.cocoon.components.language.LanguageException;
import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.util.ClassUtils;

import java.io.File;

/**
 * Base implementation of <code>ProgrammingLanguage</code>. This class sets the
 * <code>CodeFormatter</code> instance and deletes source program files after
 * unloading.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: AbstractProgrammingLanguage.java,v 1.1 2004/03/10 12:58:07 stephan Exp $
 */
public abstract class AbstractProgrammingLanguage extends AbstractLogEnabled
        implements ProgrammingLanguage, Parameterizable {

    /** The source code formatter */
    protected Class codeFormatter;

    protected String languageName;

    /**
     * Set the configuration parameters. This method instantiates the
     * sitemap-specified source code formatter
     *
     * @param params The configuration parameters
     * @exception ParameterException If the language compiler cannot be loaded
     */
    public void parameterize(Parameters params) throws ParameterException {
        String className = params.getParameter("code-formatter", null);

        try {
            if (className != null) {
                this.codeFormatter = ClassUtils.loadClass(className);
            }
        } catch (Exception e) {
            getLogger().error("Error with \"code-formatter\" parameter", e);
            throw new ParameterException("Unable to load code formatter: " + className, e);
        }
    }

    /**
     * Return this language's source code formatter. A new formatter instance is
     * created on each invocation.
     *
     * @return The language source code formatter
     */
    public CodeFormatter getCodeFormatter() {
        if (this.codeFormatter != null) {
            try {
                CodeFormatter formatter = (CodeFormatter) this.codeFormatter.newInstance();
                if (formatter instanceof LogEnabled) {
                    ((LogEnabled) formatter).enableLogging(this.getLogger());
                }
                return formatter;
            } catch (Exception e) {
                getLogger().error("Error instantiating CodeFormatter", e);
            }
        }

        return null;
    }

    /**
     * Unload a previously loaded program
     *
     * @param program A previously loaded object program
     * @exception LanguageException If an error occurs during unloading
     */
    protected abstract void doUnload(Object program, String filename,
                                     File baseDirectory)
            throws LanguageException;

    public final void unload(Object program, String filename, File baseDirectory)
            throws LanguageException {

        File file = new File(baseDirectory,
                             filename + "." + getSourceExtension());
        file.delete();
        this.doUnload(program, filename, baseDirectory);
    }

    public final void setLanguageName(String name) {
        this.languageName = name;
    }

    public final String getLanguageName() {
        return this.languageName;
    }

    /**
     * Create a new instance for the given class
     *
     * @param program The Java class
     * @return A new class instance
     * @exception LanguageException If an instantiation error occurs
     */
    public CompiledComponent instantiate(Program program) throws LanguageException {
        try {
            return program.newInstance();
        } catch (Exception e) {
            getLogger().warn("Could not instantiate program instance", e);
            throw new LanguageException("Could not instantiate program instance due to a " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
