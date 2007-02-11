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
package org.apache.cocoon.generation;

import com.ibm.bsf.BSFException;
import com.ibm.bsf.BSFManager;
import com.ibm.bsf.util.IOUtils;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * The Scriptgenerator executes arbitraty scripts using the BSF framework
 * and additional interpreter (Rhino, Jython, etc.) as a Cocoon Generator.
 *
 * Additional language support can be added during configuration, eg
 * using:
 *
 * <pre>
 *   &lt;add-languages&gt;
 *     &lt;language name="potatoscript" src="edu.purdue.cs.bsf.engines.Potatoscript"&gt;
 *       &lt;extension&gt;pos&lt;/extension&gt;
 *       &lt;extension&gt;psc&lt;/extension&gt;
 *     &lt;language&gt;
 *     &lt;language name="kawa-scheme" src="org.gnu.kawa.bsf.engines.KawaEngine"&gt;
 *       &lt;extension&gt;scm&lt;/extension&gt;
 *     &lt;language&gt;
 *   &lt;/add-languages&gt;
 * </pre>
 *
 * @author <a href="mailto:jafoster@engmail.uwaterloo.ca">Jason Foster</a>
 * @version CVS $Id: ScriptGenerator.java,v 1.4 2004/03/05 13:01:47 bdelacretaz Exp $
 */
public class ScriptGenerator extends ServiceableGenerator implements Configurable {

    protected class BSFLanguage
    {
        public String name;
        public String engineSrc;
        public String[] extensions;
    }

    protected BSFLanguage[] additionalLanguages;

    /** The source */
    private Source inputSource;

    public void configure(Configuration conf) throws ConfigurationException
    {
        if (conf != null)
        {
            //add optional support for additional languages
            Configuration languagesToAdd = conf.getChild("add-languages");

            Configuration[] languages = languagesToAdd.getChildren("language");
            this.additionalLanguages = new BSFLanguage[languages.length];


            for (int i = 0; i < languages.length; ++i)
            {
                Configuration language = languages[i];
                BSFLanguage bsfLanguage = new BSFLanguage();

                bsfLanguage.name = language.getAttribute("name");
                bsfLanguage.engineSrc = language.getAttribute("src");

                getLogger().debug("Configuring ScriptGenerator with additional BSF language " + bsfLanguage.name);
                getLogger().debug("Configuring ScriptGenerator with BSF engine " + bsfLanguage.engineSrc);


                Configuration[] extensions = language.getChildren("extension");
                bsfLanguage.extensions = new String[extensions.length];

                for (int j = 0; j < extensions.length; ++j)
                {
                    bsfLanguage.extensions[i] = extensions[i].getValue();
                    getLogger().debug("Configuring ScriptGenerator with lang extension " + bsfLanguage.extensions[i]);
                }

                this.additionalLanguages[i] = bsfLanguage;
            }
        }
    }

    public void recycle() {
        if (this.inputSource != null) {
            this.resolver.release(this.inputSource);
            this.inputSource = null;
        }
        super.recycle();
    }

    public void generate() throws ProcessingException {
        SAXParser parser = null;
        try {
            // Figure out what file to open and do so
            getLogger().debug("processing file [" + super.source + "]");
            this.inputSource = this.resolver.resolveURI(super.source);

            getLogger().debug("file resolved to [" + this.inputSource.getURI() + "]");

            Reader in = new InputStreamReader(this.inputSource.getInputStream());

            // Set up the BSF manager and register relevant helper "beans"
            BSFManager mgr = new BSFManager();

            // add support for additional languages

            if (this.additionalLanguages != null)
            {
                for (int i = 0; i < this.additionalLanguages.length; ++i)
                {
                    getLogger().debug("adding BSF language " + this.additionalLanguages[i].name + " with engine " + this.additionalLanguages[i].engineSrc);

                    BSFManager.registerScriptingEngine(this.additionalLanguages[i].name,
                                                this.additionalLanguages[i].engineSrc,
                                                this.additionalLanguages[i].extensions);
                }
            }

            StringBuffer output = new StringBuffer();

            mgr.registerBean("resolver", this.resolver);
            mgr.registerBean("source", super.source);
            mgr.registerBean("objectModel", this.objectModel);
            mgr.registerBean("parameters", this.parameters);
            mgr.registerBean("output", output);
            mgr.registerBean("logger", getLogger());

            getLogger().debug("BSFManager execution begining");

            // Execute the script

            mgr.exec(BSFManager.getLangFromFilename(this.inputSource.getURI()),
                     this.inputSource.getURI(), 0, 0, IOUtils.getStringFromReader(in));

            getLogger().debug("BSFManager execution complete");
            getLogger().debug("output = [" + output.toString() + "]");

            // Extract the XML string from the BSFManager and parse it

            InputSource xmlInput =
                    new InputSource(new StringReader(output.toString()));
            parser = (SAXParser)(this.manager.lookup(SAXParser.ROLE));
            parser.parse(xmlInput, this.xmlConsumer);
        } catch (SourceException se) {
            throw SourceUtil.handle(se);
        } catch (FileNotFoundException e) {
            throw new ResourceNotFoundException(
                "Could not load script " + this.inputSource.getURI(), e);
        } catch (BSFException e) {
            throw new ProcessingException(
                    "Exception in ScriptGenerator.generate()", e);
        } catch (Exception e) {
            throw new ProcessingException(
                    "Exception in ScriptGenerator.generate()", e);
        } finally {
            this.manager.release(parser);
        }
    }
}
