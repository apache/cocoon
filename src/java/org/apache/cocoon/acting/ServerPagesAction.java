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
package org.apache.cocoon.acting;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.avalon.excalibur.component.ComponentHandler;

import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.components.sax.XMLByteStreamFragment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServerPagesGenerator;
import org.apache.cocoon.xml.AbstractXMLConsumer;

/**
 * Allows actions to be written in XSP. This allows to use XSP to produce
 * XML fragments that are later reused in generators.<br/>
 *
 * This action works in concert with the "action" logicheet, that offers
 * actions-related services such as redirect or result map access, and the
 * "capture" logicsheet that allows to capture parts of XSP-generated XML
 * either as an <code>XMLizable</code> containing serialized SAX events,
 * or as a DOM <code>Node</code>.<br/>
 *
 * As for generators, the XSP file name is set using the "src" attribute.<br/>
 *
 * This action accepts a single parameter, "output-attribute", which names
 * the request attribute where the XSP-generated document will be stored
 * (as an <code>XMLizable</code>). If this parameter is omitted, the
 * XSP result is discarded (often the case when inner fragments are captured
 * with the "capture" logicsheet").<br/>
 *
 * When "output-attribute" is set, the action status defaults to "success",
 * meaning child sitemap statements are executed. This allows to use an
 * existing XSP without modification with this action.<br/>
 *
 * When "output-attribute" isn't set, the action status defaults to "failure".
 * The XSP must then use the "action" logicsheet to set its status.<br/>
 *
 * Example :
 * <pre>
 *   &lt;action type="serverpages" src="myAction.xsp"&gt;
 *     &lt;map:param name="output-attribute" value="xsp-action-result"/&gt;
 *     ...
 *   &lt;/action&gt;
 * </pre>
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ServerPagesAction.java,v 1.4 2004/01/21 10:46:43 antonio Exp $
 */
public class ServerPagesAction
        extends ConfigurableComposerAction
        implements Disposable, ThreadSafe {

    public static final String REDIRECTOR_OBJECT = "xsp-action:redirector";
    public static final String ACTION_RESULT_OBJECT = "xsp-action:result";
    public static final String ACTION_SUCCESS_OBJECT = "xsp-action:success";

    ComponentHandler generatorHandler;

    public void configure(Configuration conf)
      throws ConfigurationException {
        try {
            this.generatorHandler = ComponentHandler.getComponentHandler(
                ServerPagesGenerator.class,
                conf,
                this.manager,
                null, // Context
                null,  // RoleManager
                null,  // LogkitLoggerManager
                null, // InstrumentManager
                "N/A" // instrumentableName
            );

            this.generatorHandler.enableLogging(getLogger());
            this.generatorHandler.initialize();

        } catch(Exception e) {
            throw new ConfigurationException("Cannot set up component handler", e);
        }
    }

    public void dispose() {
        if (this.generatorHandler != null) {
            this.generatorHandler.dispose();
            this.generatorHandler = null;
        }
    }

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel,
        String source, Parameters parameters)
      throws Exception {

        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("serverpage source: " + source);
        }

        String outputKey = parameters.getParameter("output-attribute", null);
        Map resultMap = new HashMap();
        Object success = null;

        // Get a ServerPagesGenerator
        ServerPagesGenerator generator = (ServerPagesGenerator)this.generatorHandler.get();

        // Generator output, if output-attribute was given
        XMLByteStreamCompiler compiler = null;

        try {
            generator.enableLogging(getLogger());
            generator.compose(this.manager);
            generator.setup(resolver, objectModel, source, parameters);

            // Setup generator output
            if (outputKey == null) {
                // discard output to a "black hole"
                generator.setConsumer(new AbstractXMLConsumer() { } ); // Make the abstract class instanciable
            } else {
                // store output in a byte stream
                compiler = new XMLByteStreamCompiler();
                generator.setConsumer(compiler);
            }

            // Augment the object model for the "action" logicsheet
            objectModel.put(REDIRECTOR_OBJECT, redirector);
            objectModel.put(ACTION_RESULT_OBJECT, resultMap);

            // Let the XSP do it's stuff
            generator.generate();
            success = objectModel.get(ACTION_SUCCESS_OBJECT);

        } finally {
            // Release generator
            generatorHandler.put(generator);

            // Clean up object model
            objectModel.remove(REDIRECTOR_OBJECT);
            objectModel.remove(ACTION_RESULT_OBJECT);
            objectModel.remove(ACTION_SUCCESS_OBJECT);
        }

        if (outputKey != null) {
            // Success defaults to true when the whole output is captured
            if (success == null) {
                success = Boolean.TRUE;
            }

            if (success == Boolean.TRUE) {
                // Store the XSP output in the request
                Request req = ObjectModelHelper.getRequest(objectModel);
                req.setAttribute(outputKey, new XMLByteStreamFragment(compiler.getSAXFragment()));
            }
        }

        return (success == Boolean.TRUE) ? resultMap : null;
    }
}
