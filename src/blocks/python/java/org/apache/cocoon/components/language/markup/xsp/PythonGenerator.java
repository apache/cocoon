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
 * @version CVS $Id: PythonGenerator.java,v 1.4 2004/03/05 13:02:20 bdelacretaz Exp $
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
