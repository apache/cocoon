/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.XMLParser;
import org.apache.excalibur.source.Source;
import org.xml.sax.SAXException;

/**
 * The JellyGenerator executes jelly scripts using the Jakarta Jelly engine
 * as a Cocoon Generator. 
 *
 * @author <a href="mailto:amal.sirvisetti@sirvisetti.com">Amal Sirvisetti</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 */
public class JellyGenerator 
    extends AbstractGenerator {

    /** The Jelly Context */
    protected JellyContext jellyContext;  

    /** The Jelly Parser */
    protected XMLParser jellyParser = new XMLParser();
    
    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        super.recycle();
        this.jellyContext = null;
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(SourceResolver, Map, String, Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters params) 
    throws ProcessingException,SAXException,IOException {

        super.setup(resolver, objectModel, src, params);

        // Initialize the Jelly context
        this.jellyContext = new JellyContext();
        
        // Update JellyContext with sitemap parameters
        this.updateContext(params);
    }

    /**
     * Generate XML data from Jelly script.
     */
    public void generate() 
    throws IOException, SAXException, ProcessingException {
        Source scriptSource = null;
        try {
            
             // Update JellyContext with request parameters. Variables set earlier
             // (from sitemap parameters) will be overriden, if same variables are
             // supplied through the request object.
            this.updateContext();

            // Execute Jelly script
            XMLOutput xmlOutput = new XMLOutput(this.contentHandler, this.lexicalHandler);
            
            // TODO - Compile the script and cache the compiled version
            
            scriptSource = this.resolver.resolveURI(this.source);
            
            Script script = this.jellyParser.parse(SourceUtil.getInputSource(scriptSource));
            script = script.compile();
            
            // the script does not output startDocument/endDocument events
            this.contentHandler.startDocument();
            script.run(this.jellyContext, xmlOutput);
            xmlOutput.flush();
            this.contentHandler.endDocument();
            
        } catch (IOException e) {
            getLogger().error("JellyGenerator.generate()", e);
            throw new ResourceNotFoundException("JellyGenerator could not find resource", e);
        } catch (Exception e) {
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in JellyGenerator.generate()", e);
        } finally {
            this.resolver.release( scriptSource );
        }
    }

    /**
     * Update JellyContext with variables from sitemap
     */
    protected void updateContext(Parameters params) throws ProcessingException {
        try {
            String pArray[] = params.getNames();
            for(int i=0; i<pArray.length; i++) {
                String var = pArray[i];
                String val = params.getParameter(var);
                this.jellyContext.setVariable( var, val );
            }
        } catch (Exception e) {
            getLogger().error("Error in JellyGenerator.updateContext(Parameters params)", e);
            throw new ProcessingException("Exception in JellyGenerator.updateContext(Parameters params)", e);
        }
    }
    
    /**
     * Update JellyContext with variables from request
     */
    protected void updateContext() throws ProcessingException {
        try {
            Request request = ObjectModelHelper.getRequest( this.objectModel );
            Enumeration enum = request.getParameterNames();
            while (enum.hasMoreElements()) {
                String var = (String) enum.nextElement();
                String val = request.getParameter(var);
                this.jellyContext.setVariable( var, val );
            }
        } catch (Exception e) {
            getLogger().error("Error in JellyGenerator.updateContext()", e);
            throw new ProcessingException("Exception in JellyGenerator.updateContext()", e);
        }
    }
}


