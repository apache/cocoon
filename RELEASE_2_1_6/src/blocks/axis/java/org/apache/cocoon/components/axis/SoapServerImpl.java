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
package org.apache.cocoon.components.axis;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.axis.AxisEngine;
import org.apache.axis.Constants;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.MessageContext;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.deployment.wsdd.WSDDDeployment;
import org.apache.axis.deployment.wsdd.WSDDDocument;
import org.apache.axis.deployment.wsdd.WSDDService;
import org.apache.axis.security.servlet.ServletSecurityProvider;
import org.apache.axis.server.AxisServer;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.transport.http.HTTPTransport;
import org.apache.axis.transport.http.ServletEndpointContextImpl;
import org.apache.axis.utils.XMLUtils;
import org.apache.cocoon.components.axis.providers.AvalonProvider;
import org.apache.cocoon.util.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.dom.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * SOAP Server Implementation
 *
 * <p>
 *  This server accepts a SOAP Request, and generates the resultant
 *  response as output. Essentially, this reader allows you to serve SOAP
 *  requests from your Cocoon application.
 * </p>
 *
 * <p>
 *  Code originates from the Apache
 *  <a href="http://xml.apache.org/axis">AXIS</a> project,
 *  <code>org.apache.axis.http.transport.AxisServlet</code>.
 * </p>
 *
 * Ported to Cocoon by:
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 *
 * Original <code>AxisServlet</code> authors:
 *
 * @author <a href="mailto:">Steve Loughran</a>
 * @author <a href="mailto:dug@us.ibm.com">Doug Davis</a>
 * 
 * @version CVS $Id: SoapServerImpl.java,v 1.4 2004/03/28 20:51:24 antonio Exp $
 */
public class SoapServerImpl extends AbstractLogEnabled
    implements SoapServer, Composable, Configurable, Contextualizable, Initializable,
               Startable, ThreadSafe
{
    /**
     * Constant describing the default location of the server configuration file
     */
    public static final String DEFAULT_SERVER_CONFIG
        = "resource://org/apache/axis/server/server-config.wsdd";

    // transport name
    private String m_transportName;

    // security provider reference
    private ServletSecurityProvider m_securityProvider;

    // JWS output directory
    private String m_jwsClassDir;

    // per-instance cache of the axis server
    private AxisServer m_axisServer;

    // axis server configuration
    private FileProvider m_engineConfig;

    // location of attachments
    private String m_attachmentDir;

    // server configuration
    private Source m_serverWSDD;

    // array containing locations to descriptors this reader should manage
    private WSDDDocument[] m_descriptors;

    // context reference
    private Context m_context;

    // component manager reference
    private ComponentManager m_manager;

    /**
     * Contextualize this Reader.
     *
     * @param context a <code>Context</code> instance
     * @exception ContextException if an error occurs
     */
    public void contextualize(final Context context)
        throws ContextException
    {
        m_context = context;
    }

    /**
     * Compose this server
     *
     * @param manager a <code>ComponentManager</code> value
     * @exception ComponentException if an error occurs
     */
    public void compose(ComponentManager manager)
        throws ComponentException
    {
        m_manager = manager;
    }

    /**
     * Configures this reader.
     *
     * <p>
     *  Sets the following optional configuration settings:
     *
     *  <ul>
     *   <li>Server WSDD configuration
     *   <li>Attachment directory
     *   <li>JWS directory
     *   <li>Security provider
     *   <li>Transport name
     *   <li>Mananged services
     *  </ul>
     * </p>
     *
     * <p>
     *  The following format is used:
     *  <pre>
     *   &lt;soap-server&gt;
     *    &lt;server-wsdd src="..."/&gt;
     *    &lt;attachment-dir src="..."/&gt;
     *    &lt;jws-dir src="..."/&gt;
     *    &lt;security-provider enabled="..."/&gt;
     *    &lt;transport name="..."/&gt;
     *    &lt;managed-services&gt;
     *     &lt;descriptor src="..."/&gt;
     *     &lt;descriptor src="..."/&gt;
     *    &lt;/managed-services&gt;
     *   &lt;/soap-server&gt;
     *  </pre>
     * </p>
     *
     * @param config a <code>Configuration</code> instance
     * @exception ConfigurationException if an error occurs
     */
    public void configure(final Configuration config)
        throws ConfigurationException
    {
        try {
            setServerConfig(config);
            setAttachmentDir(config);
            setJWSDir(config);
            setSecurityProvider(config);
            setTransportName(config);
            setManagedServices(config);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("SoapServerImpl.configure() complete");
            }
        } catch (final Exception e) {
            throw new ConfigurationException("Error during configuration", e);
        }
    }

    /**
     * Helper method to set the axis server configuration.
     *
     * @param config a <code>Configuration</code> instance
     * @exception Exception if an error occurs
     */
    public void setServerConfig(final Configuration config)
        throws Exception
    {
        final Configuration wsdd = config.getChild("server-wsdd");
        SourceResolver resolver = null;

        try
        {
            resolver = (SourceResolver) m_manager.lookup(SourceResolver.ROLE);
            m_serverWSDD =
                resolver.resolveURI(
                    wsdd.getAttribute("src", DEFAULT_SERVER_CONFIG)
                );
        }
        finally
        {
            if (resolver != null) m_manager.release(resolver);
        }
    }

    /**
     * Helper method to set the attachment dir. If no attachment directory has
     * been specified, then its set up to operate out of the Cocoon workarea.
     *
     * @param config a <code>Configuration</code> instance
     * @exception ConfigurationException if a configuration error occurs
     * @exception ContextException if a context error occurs
     */
    private void setAttachmentDir(final Configuration config)
        throws ConfigurationException, ContextException
    {
        final Configuration dir = config.getChild("attachment-dir");
        m_attachmentDir = dir.getAttribute("src", null);

        if (m_attachmentDir == null)
        {
            File workDir =
                (File) m_context.get(org.apache.cocoon.Constants.CONTEXT_WORK_DIR);
            File attachmentDir =
                IOUtils.createFile(workDir, "attachments" + File.separator);
            m_attachmentDir = IOUtils.getFullFilename(attachmentDir);
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("attachment directory = " + m_attachmentDir);
        }
    }

    /**
     * Helper method to set the JWS class dir. If no directory is specified then
     * the directory <i>axis-jws</i> is used, under the Cocoon workarea.
     *
     * @param config a <code>Configuration</code> instance
     * @exception ConfigurationException if a configuration error occurs
     * @exception ContextException if a context error occurs
     */
    private void setJWSDir(final Configuration config)
        throws ConfigurationException, ContextException
    {
        final Configuration dir = config.getChild("jws-dir");
        m_jwsClassDir = dir.getAttribute("src", null);

        if (m_jwsClassDir == null)
        {
            File workDir =
                (File) m_context.get(org.apache.cocoon.Constants.CONTEXT_WORK_DIR);
            File jwsClassDir =
                IOUtils.createFile(workDir, "axis-jws" + File.separator);
            m_jwsClassDir = IOUtils.getFullFilename(jwsClassDir);
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("jws class directory = " + m_jwsClassDir);
        }
    }

    /**
     * Helper method to set the security provider.
     *
     * @param config a <code>Configuration</code> instance
     * @exception ConfigurationException if an error occurs
     */
    private void setSecurityProvider(final Configuration config)
        throws ConfigurationException
    {
        final Configuration secProvider =
            config.getChild("security-provider", false);

        if (secProvider != null)
        {
            final String attr = secProvider.getAttribute("enabled");
            final boolean providerIsEnabled = BooleanUtils.toBoolean(attr);

            if (providerIsEnabled)
                m_securityProvider = new ServletSecurityProvider();
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("security provider = " + m_securityProvider);
        }
    }

    /**
     * Helper method to set the transport name
     *
     * @param config a <code>Configuration</code> instance
     * @exception ConfigurationException if an error occurs
     */
    private void setTransportName(final Configuration config)
        throws ConfigurationException
    {
        final Configuration name = config.getChild("transport");
        m_transportName =
            name.getAttribute("name", HTTPTransport.DEFAULT_TRANSPORT_NAME);
    }

    /**
     * Helper method to obtain a list of managed services from the given
     * configuration (ie. locations of deployement descriptors to be 
     * deployed).
     *
     * @param config a <code>Configuration</code> value
     * @exception Exception if an error occurs
     */
    private void setManagedServices(final Configuration config)
        throws Exception
    {
        final Configuration m = config.getChild("managed-services", false);
        final List descriptors = new ArrayList();

        if (m != null)
        {
            SourceResolver resolver = null;
            DOMParser parser = null;

            try
            {
                final Configuration[] services = m.getChildren("descriptor");
                resolver = (SourceResolver) m_manager.lookup(SourceResolver.ROLE);
                parser = (DOMParser) m_manager.lookup(DOMParser.ROLE);

                for (int i = 0; i < services.length; ++i)
                {
                    final String location = services[i].getAttribute("src");
                    Source source = resolver.resolveURI(location);

                    final Document d =
                        parser.parseDocument(
                            new InputSource(
                                new InputStreamReader(source.getInputStream())
                            )
                        );

                    descriptors.add(new WSDDDocument(d));
                }
            }
            finally
            {
                if (resolver != null) m_manager.release(resolver);
                if (parser != null) m_manager.release((Component)parser);
            }
        }

        // convert the list of descriptors to an array, for easier iteration
        m_descriptors =
            (WSDDDocument[]) descriptors.toArray(new WSDDDocument[]{});
    }

    /**
     * Initialize this reader, creates AXIS server engine.
     *
     * @exception Exception if an error occurs
     */
    public void initialize()
        throws Exception
    {
        m_axisServer = createEngine();

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("SoapServerImpl.initialize() complete");
        }
    }

    /**
     * Starts this reader. Deploys all managed services as specified at
     * configuration time.
     *
     * @exception Exception if an error occurs
     */
    public void start()
        throws Exception
    {
        // deploy all configured services
        for (int i = 0; i < m_descriptors.length; ++i)
        {
            WSDDDeployment deployment = m_engineConfig.getDeployment();
            m_descriptors[i].deploy(deployment);

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(
                    "Deployed Descriptor:\n" +
                    XMLUtils.DocumentToString(m_descriptors[i].getDOMDocument())
                );
            }
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("SoapServerImpl.start() complete");
        }
    }

    /**
     * Stops this reader. Undeploys all managed services this reader
     * currently manages (includes services dynamically added to the reader
     * during runtime).
     *
     * @exception Exception if an error occurs
     */
    public void stop()
        throws Exception
    {
        WSDDDeployment deployment = m_engineConfig.getDeployment();
        WSDDService[] services = deployment.getServices();

        // undeploy all deployed services
        for (int i = 0; i < services.length; ++i)
        {
            deployment.undeployService(services[i].getQName());

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Undeployed: " + services[i].toString());
            }
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("SoapServerImpl.stop() complete");
        }
    }

    public void invoke(MessageContext message)
        throws Exception
    {
        m_axisServer.invoke(message);
    }

    /**
     * Place the Request message in the MessagContext object - notice
     * that we just leave it as a 'ServletRequest' object and let the
     * Message processing routine convert it - we don't do it since we
     * don't know how it's going to be used - perhaps it might not
     * even need to be parsed.
     */
    public MessageContext createMessageContext(
        HttpServletRequest req,
        HttpServletResponse res,
        ServletContext con
    )
    {
        MessageContext msgContext = new MessageContext(m_axisServer);
        String webInfPath = con.getRealPath("/WEB-INF");
        String homeDir = con.getRealPath("/");

        // Set the Transport
        msgContext.setTransportName(m_transportName);

        // Add Avalon specifics to MessageContext
        msgContext.setProperty(LOGGER, getLogger());
        msgContext.setProperty(AvalonProvider.COMPONENT_MANAGER, m_manager);

        // Save some HTTP specific info in the bag in case someone needs it
        msgContext.setProperty(Constants.MC_JWS_CLASSDIR, m_jwsClassDir);
        msgContext.setProperty(Constants.MC_HOME_DIR, homeDir);
        msgContext.setProperty(Constants.MC_RELATIVE_PATH, req.getServletPath());
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLET, this );
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, req );
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE, res );
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETLOCATION, webInfPath);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETPATHINFO,
                               req.getPathInfo() );
        msgContext.setProperty(HTTPConstants.HEADER_AUTHORIZATION,
                               req.getHeader(HTTPConstants.HEADER_AUTHORIZATION));
        msgContext.setProperty(Constants.MC_REMOTE_ADDR, req.getRemoteAddr());


        // Set up a javax.xml.rpc.server.ServletEndpointContext
        ServletEndpointContextImpl sec = new ServletEndpointContextImpl();
        msgContext.setProperty(Constants.MC_SERVLET_ENDPOINT_CONTEXT, sec);

        // Save the real path
        String realpath = con.getRealPath(req.getServletPath());

        if (realpath != null)
        {
            msgContext.setProperty(Constants.MC_REALPATH, realpath);
        }

        msgContext.setProperty(Constants.MC_CONFIGPATH, webInfPath);

        if (m_securityProvider != null)
            msgContext.setProperty("securityProvider", m_securityProvider);

        // write out the contents of the message context for debugging purposes
        if (getLogger().isDebugEnabled())
        {
            debugMessageContext(msgContext);
        }

        return msgContext;
    }

    /**
     * Helper method to log the contents of a given message context 
     *
     * @param context a <code>MessageContext</code> instance
     */
    private void debugMessageContext(final MessageContext context)
    {
        for (final Iterator i = context.getPropertyNames();
             i.hasNext();
        )
        {
            final String key = (String) i.next();
            getLogger().debug(
                "MessageContext: Key:" + key + ": Value: " + context.getProperty(key)
            );
        }
    }


    /**
     * This is a uniform method of initializing AxisServer in a servlet
     * context.
     */
    public AxisServer createEngine()
        throws Exception
    {
        AxisServer engine = AxisServer.getServer(getEngineEnvironment());

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Axis engine created");
        }

        return engine;
    }

    protected Map getEngineEnvironment()
        throws Exception
    {
        Map env = new HashMap();

        // use FileProvider directly with a Avalon Source object instead of going
        // through the EngineConfigurationFactoryServlet class
        m_engineConfig = new FileProvider(m_serverWSDD.getInputStream());

        env.put(EngineConfiguration.PROPERTY_NAME, m_engineConfig);
        env.put(AxisEngine.ENV_ATTACHMENT_DIR, m_attachmentDir);
        // REVISIT(MC): JNDI Factory support ?
        //env.put(AxisEngine.ENV_SERVLET_CONTEXT, context);

        return env;
    }

    /*
     * Helper method to convert a <code>Message</code> structure
     * into a <code>String</code>.
     *
     * @param msg a <code>Message</code> value
     * @return a <code>String</code> value
     */
    /* FIXME (SM): this method appears to be unused, should we remove it?
    private String messageToString(final Message msg)
    {
        try
        {
            OutputStream os = new ByteArrayOutputStream();
            msg.writeTo(os);
            return os.toString();
        }
        catch (Exception e)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn(
                    "Warning, could not convert message (" + msg + ") into string", e
                );
            }

            return null;
        }
    } */
}
