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
package org.apache.cocoon.acting;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.fortress.impl.handler.ComponentFactory;
import org.apache.avalon.fortress.impl.handler.ComponentHandler;
import org.apache.avalon.fortress.impl.handler.PoolableComponentHandler;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.components.sax.XMLByteStreamFragment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServerPagesGenerator;
import org.apache.cocoon.xml.AbstractXMLConsumer;
import org.apache.excalibur.mpool.ObjectFactory;

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
 * @version CVS $Id: ServerPagesAction.java,v 1.9 2004/03/08 13:57:35 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type="Action"
 * @x-avalon.lifestyle type="singleton"
 * @x-avalon.info name="serverpages"
 * 
 */
public class ServerPagesAction
        extends ConfigurableServiceableAction
        implements Disposable,Contextualizable, Initializable, ThreadSafe {

    public static final String REDIRECTOR_OBJECT = "xsp-action:redirector";
    public static final String ACTION_RESULT_OBJECT = "xsp-action:result";
    public static final String ACTION_SUCCESS_OBJECT = "xsp-action:success";

    ComponentHandler m_generatorHandler;
    Configuration m_conf;
    private Context m_context;

    public void configure(Configuration conf)
      throws ConfigurationException {
        m_conf = conf;
    }

    public void dispose() {
        ContainerUtil.dispose(m_generatorHandler);
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
        ServerPagesGenerator generator = (ServerPagesGenerator)this.m_generatorHandler.get();

        // Generator ouptut, if output-attribute was given
        XMLByteStreamCompiler compiler = null;

        try {
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
            m_generatorHandler.put(generator);

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

    public void initialize() throws Exception
    {
        ComponentFactory factory = new ComponentFactory(
                ServerPagesGenerator.class,
                m_conf,
                manager, // ServiceManager
                m_context, // Context
                null, // LoggerManager
                null ); // Extension Manager
        m_generatorHandler = new PoolableComponentHandler();

        DefaultServiceManager newManager = new DefaultServiceManager(manager);
        newManager.put(ObjectFactory.class.getName(), factory);

        ContainerUtil.enableLogging( m_generatorHandler, getLogger() );
        ContainerUtil.contextualize( m_generatorHandler, m_context );
        ContainerUtil.service(m_generatorHandler, newManager);
        ContainerUtil.configure(m_generatorHandler, m_conf);
        ContainerUtil.initialize(m_generatorHandler);
    }

    public void contextualize( Context context ) throws ContextException
    {
        m_context = context;
    }
}
