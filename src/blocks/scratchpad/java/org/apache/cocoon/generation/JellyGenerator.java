/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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


