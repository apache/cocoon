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
package org.apache.cocoon.components.language.programming.java;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.components.language.programming.LanguageCompiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * This class implements the functionality common to all Java compilers.
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: AbstractJavaCompiler.java,v 1.3 2003/12/06 21:22:10 cziegeler Exp $
 * @since 2.0
 */
public abstract class AbstractJavaCompiler extends AbstractLogEnabled implements LanguageCompiler, Recyclable {

    /**
     * The source program filename
     */
    protected String file;

    /**
     * The name of the directory containing the source program file
     */
    protected String srcDir;

    /**
     * The name of the directory to contain the resulting object program file
     */
    protected String destDir;

    /**
     * The classpath to be used for compilation
     */
    protected String classpath;

    /**
     * The encoding of the source program or <code>null</code> to use the
     * platform's default encoding
     */
    protected String encoding = null;

    /**
     * The input stream to output compilation errors
     */
    protected InputStream errors;

    /**
     * Set the name of the file containing the source program
     *
     * @param file The name of the file containing the source program
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Set the name of the directory containing the source program file
     *
     * @param srcDir The name of the directory containing the source program file
     */
    public void setSource(String srcDir) {
        this.srcDir = srcDir;
    }

    /**
     * Set the name of the directory to contain the resulting object program file
     *
     * @param destDir The name of the directory to contain the resulting object
     * program file
     */
    public void setDestination(String destDir) {
        this.destDir = destDir;
    }

    /**
     * Set the classpath to be used for this compilation
     *
     * @param classpath The classpath to be used for this compilation
     */
    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    /**
     * Set the encoding of the input source file or <code>null</code> to use the
     * platform's default encoding
     *
     * @param encoding The encoding of the input source file or <code>null</code>
     * to use the platform's default encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Return the list of errors generated by this compilation
     *
     * @return The list of errors generated by this compilation
     * @exception IOException If an error occurs during message collection
     */
    public List getErrors() throws IOException {
        return parseStream(new BufferedReader(new InputStreamReader(errors)));
    }

    /**
     * Parse the compiler error stream to produce a list of
     * <code>CompilerError</code>s
     *
     * @param errors The error stream
     * @return The list of compiler error messages
     * @exception IOException If an error occurs during message collection
     */
    protected abstract List parseStream(BufferedReader errors)
            throws IOException;

    /**
     * Fill the arguments taken by the Java compiler
     *
     * @param arguments The list of compilation arguments
     * @return The prepared list of compilation arguments
     */
    protected List fillArguments(List arguments) {
        // destination directory
        arguments.add("-d");
        arguments.add(destDir);

        // classpath
        arguments.add("-classpath");
        arguments.add(classpath);

        // sourcepath
        arguments.add("-sourcepath");
        arguments.add(srcDir);

        // add optimization (for what is worth)
        arguments.add("-O");

        // add encoding if set
        if (encoding != null) {
            arguments.add("-encoding");
            arguments.add(encoding);
        }

        return arguments;
    }

    /**
     * Copy arguments to a string array
     *
     * @param arguments The compiler arguments
     * @return A string array containing compilation arguments
     */
    protected String[] toStringArray(List arguments) {
        int i;
        String[] args = new String[arguments.size() + 1];

        for (i = 0; i < arguments.size(); i++) {
            args[i] = (String) arguments.get(i);
        }

        args[i] = file;

        return args;
    }

    /** Reset all internal state.
     * This method is called by the component manager before this
     * component is return to its pool.
     */
    public void recycle() {
        file = null;
        srcDir = null;
        destDir = null;
        classpath = null;
        encoding = null;
        errors = null;
    }
}
