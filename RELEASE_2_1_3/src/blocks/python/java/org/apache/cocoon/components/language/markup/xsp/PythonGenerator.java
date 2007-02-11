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
package org.apache.cocoon.components.language.markup.xsp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;

import org.python.core.Py;
import org.python.core.PyCode;
import org.python.util.PythonInterpreter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Class representing interpreted XSP-generated
 * <code>ServerPagesGenerator</code> programs
 * written in Python language
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: PythonGenerator.java,v 1.3 2003/09/07 06:05:05 vgritsenko Exp $
 */
public class PythonGenerator extends XSPGenerator
        implements Configurable, Initializable {

    /**
     * Python source file
     */
    private File file;

    private PythonInterpreter python;

    private PyCode code;
    private Exception compileError;


    public void configure(Configuration configuration) throws ConfigurationException {
        this.file = new File(configuration.getChild("file").getValue());

        Configuration[] dependencies = configuration.getChildren("dependency");
        this.dependencies = new File[dependencies.length];
        for (int i = 0; i < dependencies.length; i ++) {
            this.dependencies[i] = new File(dependencies[i].getValue());
        }
    }

    /**
     * Determines whether this generator's source files have changed
     *
     * @return Whether any of the files this generator depends on has changed
     * since it was created
     */
    public boolean modifiedSince(long date) {
        if (this.file.lastModified() < date) {
            return true;
        }

        for (int i = 0; i < dependencies.length; i++) {
            if (this.file.lastModified() < dependencies[i].lastModified()) {
                return true;
            }
        }

        return false;
    }

    public void initialize() throws Exception {
        try {
            Properties properties = new Properties();
            File workDir = (File)avalonContext.get(Constants.CONTEXT_WORK_DIR);
            properties.setProperty("python.home", workDir.toString());
            properties.setProperty("python.packages.fakepath",
                    (String)avalonContext.get(Constants.CONTEXT_CLASSPATH));
            PythonInterpreter.initialize(System.getProperties(), properties, new String[]{});

            python = new PythonInterpreter();
            python.set("page", this);
            python.set("logger", getLogger());
            python.set("xspAttr", new AttributesImpl());
        
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Compiling script " + file);
            }
            
            this.code = Py.compile(new FileInputStream(this.file),
                                   this.file.toString(),
                                   "exec");
        } catch (Exception e) {
            this.compileError = e;
        }
    }


    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
            throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        if (this.compileError != null) {
            throw new ProcessingException("Failed to compile script", compileError);
        }

        python.set("objectModel", this.objectModel);
        python.set("request", this.request);
        python.set("response", this.response);
        python.set("context", this.context);
        python.set("resolver", this.resolver);
        python.set("parameters", this.parameters);
    }

    public void generate() throws IOException, ProcessingException {
        try {
            python.set("contentHandler", this.contentHandler);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Executing script " + file);
            }
            python.exec(code);
        } catch (Exception e) {
            throw new ProcessingException("generate: Got Python exception", e);
        }
    }

    public void recycle() {
        python.set("contentHandler", null);

        python.set("objectModel", null);
        python.set("request", null);
        python.set("response", null);
        python.set("context", null);
        python.set("resolver", null);
        python.set("parameters", null);

        super.recycle();
    }

    public void dispose() {
        python.set("page", null);
        python.set("logger", null);
        python.set("xspAttr", null);

        this.python = null;
        this.compileError = null;
        this.code = null;

        super.dispose();
    }
}
