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
package org.apache.cocoon.components.cprocessor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.NamespacedSAXConfigurationHandler;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.CompilingProcessor;
import org.apache.cocoon.Constants;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.EnvironmentHelper;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;
import org.apache.cocoon.environment.wrapper.MutableEnvironmentFacade;
import org.apache.cocoon.xml.LocationAugmentationPipe;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.xslt.XSLTProcessor;
import org.xml.sax.InputSource;

/**
 * 
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * 
 * @avalon.component
 * @avalon.service type=CompilingProcessor
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=sitemap
 */
public class TreeProcessor extends AbstractLogEnabled 
implements CompilingProcessor, Contextualizable, Serviceable, Configurable, Initializable, Disposable {

    /** Environment attribute key for redirection status communication */
    public static final String COCOON_REDIRECT_ATTR = "cocoon: redirect url";
    
    /** The sitemap namespace */
    public static final String SITEMAP_NS = "http://apache.org/cocoon/sitemap/1.0";
    
    /** The xsl transformation location for turning a sitemap into a Fortress container configuration */
    private static final String SITEMAP2XCONF_URL = 
//        "resource://org/apache/cocoon/components/treeprocessor/sitemap2xconf.xsl";
        "file://d:/apache/cocoon-2.2/src/java/org/apache/cocoon/components/cprocessor/sitemap2xconf.xsl";
    /** The parent TreeProcessor, if any */
    private TreeProcessor m_parent;
    private EnvironmentHelper m_environmentHelper;
    
    private Context m_context;
    private ServiceManager m_manager;
    private SourceResolver m_resolver;
    
    /** The object that manages the sitemap container */
    private DefaultContainerManager m_cm;
    
    /* some configuration options */
    private String m_fileName;
    private boolean m_checkReload;
    private long m_lastModifiedDelay;
    
    /* the tree configuration source (the sitemap) */
    private DelayedRefreshSourceWrapper m_source;
    private long m_lastModified;
    
    /* the sitemap2xconf.xsl stylesheet */
    private Source m_transform;
    
    /** The root node of the processing tree */
    private ProcessingNode m_rootNode;
    
    // ---------------------------------------------------- lifecycle
    
    public TreeProcessor() {
    }
    
    private TreeProcessor(TreeProcessor parent) throws Exception {
        m_parent = parent;
        ContainerUtil.enableLogging(this,parent.getLogger());
        ContainerUtil.contextualize(this,parent.m_context);
        ContainerUtil.service(this,parent.m_manager);
        m_fileName = parent.m_fileName;
        m_checkReload = parent.m_checkReload;
        m_lastModifiedDelay = parent.m_lastModifiedDelay;
    }
    
    public void contextualize(Context context) throws ContextException {
        m_context = context;
    }
    
    /**
     * @avalon.dependency  type="SourceResolver"
     * @avalon.dependency  type="SAXParser"
     * @avalon.dependency  type="XSLTProcessor"
     */
    public void service(ServiceManager manager) throws ServiceException {
        m_manager = manager;
        m_resolver = (SourceResolver) m_manager.lookup(SourceResolver.ROLE);
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        m_fileName = config.getAttribute("file", null);
        m_checkReload = config.getAttributeAsBoolean("check-reload", true);
        
        // Reload check delay. Default is 1 second.
        m_lastModifiedDelay = config.getChild("reload").getAttributeAsLong("delay",1000L);
        
    }

    public void initialize() throws Exception {
        // setup the environment helper
        if (m_environmentHelper == null ) {
            m_environmentHelper = new EnvironmentHelper(
                (String) m_context.get(Constants.CONTEXT_ROOT_URL));
        }
        ContainerUtil.enableLogging(m_environmentHelper,getLogger());
        ContainerUtil.service(m_environmentHelper,m_manager);
        m_transform = m_resolver.resolveURI(SITEMAP2XCONF_URL);
    }

    public void dispose() {
        ContainerUtil.dispose(m_cm);
        m_cm = null;
        ContainerUtil.dispose(m_environmentHelper);
        m_environmentHelper = null;
        if (m_manager != null) {
            if (m_source != null ) {
                m_resolver.release(m_source.getSource());
                m_source = null;
            }
            m_manager.release(m_resolver);
            m_resolver = null;
            m_manager = null;
        }
    }
    
    // ---------------------------------------------------- Processor implementation
    
    public boolean process(Environment environment) throws Exception {
        InvokeContext context = new InvokeContext();
        context.enableLogging(getLogger());
        try {
            return process(environment, context);
        } finally {
            context.dispose();
        }
    }
    
    /**
     * Do the actual processing, be it producing the response or just building the pipeline.
     * 
     * @param environment
     * @param context
     * @return
     * @throws Exception
     */
    private boolean process(Environment environment, InvokeContext context)
    throws Exception {
    
        // first, check whether we need to load the sitemap
        if (m_rootNode == null || (m_checkReload && m_source.getLastModified() > m_lastModified)) {
            setupRootNode(environment);
        }
        
        // and now process
        EnvironmentHelper.enterProcessor(this, m_manager, environment);
        try {
            boolean success = m_rootNode.invoke(environment, context);
            if (success) {
                // Do we have a cocoon: redirect ?
                String cocoonRedirect = (String) environment.getAttribute(COCOON_REDIRECT_ATTR);
                if (cocoonRedirect != null) {
                    // Remove the redirect indication
                    environment.removeAttribute(COCOON_REDIRECT_ATTR);
                    // and handle the redirect
                    return handleCocoonRedirect(cocoonRedirect, environment, context);
                } else {
                    // "normal" success
                    return true;
                }
            } else {
                return false;
            }
        } finally {
            EnvironmentHelper.leaveProcessor();
        }
    }
    
    private synchronized void setupRootNode(Environment env) throws Exception {

        // Now that we entered the synchronized area, recheck what's already
        // been checked in process().
        if (m_rootNode != null && m_source.getLastModified() <= m_lastModified) {
            // Nothing changed
            return;
        }
        
        long startTime = System.currentTimeMillis();

        if (m_source == null) {
            Source source = m_resolver.resolveURI(m_fileName);
            m_source = new DelayedRefreshSourceWrapper(source,m_lastModifiedDelay);
        }        

        ContainerUtil.dispose(m_cm);
        // create the sitemap container
        FortressConfig config = new FortressConfig(m_context);
        config.setContainerClass(Thread.currentThread().getContextClassLoader().
            loadClass(SitemapContainer.class.getName()));
        config.setContextRootURL(new URL((String) m_context.get(Constants.CONTEXT_ROOT_URL)));
        config.setContextClassLoader(Thread.currentThread().getContextClassLoader());
        config.setContainerConfiguration(buildConfiguration(m_source));
        config.setServiceManager(m_manager);
        config.setLoggerManager((LoggerManager) m_manager.lookup(LoggerManager.ROLE));
        m_cm = new DefaultContainerManager(config.getContext(),getLogger());
        m_cm.initialize();
        ProcessingNode root = ((SitemapContainer) m_cm.getContainer()).getRootNode();
        
        m_lastModified = System.currentTimeMillis();

        if (getLogger().isDebugEnabled()) {
            double time = (m_lastModified - startTime) / 1000.0;
            getLogger().debug("TreeProcessor built in " + time + " secs from " + m_source.getURI());
        }

        // Finished
        m_rootNode = root;
    }
    
    private Configuration buildConfiguration(Source source) throws Exception {
        
        SAXParser parser = null;
        XSLTProcessor xsltProcessor = null;
        try {
            // get the SAX parser
            parser = (SAXParser) m_manager.lookup(SAXParser.ROLE);
            
            // setup the sitemap2xconf transformation handler
            xsltProcessor = (XSLTProcessor) m_manager.lookup(XSLTProcessor.ROLE);
            final TransformerHandler transformHandler = xsltProcessor.getTransformerHandler(m_transform);
            
            final NamespacedSAXConfigurationHandler configHandler = new NamespacedSAXConfigurationHandler();
            transformHandler.setResult(new SAXResult(configHandler));
            
            final LocationAugmentationPipe pipe = new LocationAugmentationPipe();
            pipe.setConsumer(XMLUtils.getConsumer(transformHandler));
            
            parser.parse(new InputSource(source.getInputStream()),pipe);
            return configHandler.getConfiguration();
        }
        finally {
            if (parser != null) {
                m_manager.release(parser);
            }
            if (xsltProcessor != null) {
                m_manager.release(xsltProcessor);
            }
        }
    }
    
    private boolean handleCocoonRedirect(String uri, Environment environment, InvokeContext context) throws Exception
    {
        
        // Build an environment wrapper
        // If the current env is a facade, change the delegate and continue processing the facade, since
        // we may have other redirects that will in turn also change the facade delegate
        
        MutableEnvironmentFacade facade = environment instanceof MutableEnvironmentFacade ?
            ((MutableEnvironmentFacade)environment) : null;
        
        if (facade != null) {
            // Consider the facade delegate (the real environment)
            environment = facade.getDelegate();
        }
        
        Environment newEnv = new ForwardEnvironmentWrapper(environment, m_manager, uri, getLogger());
        
        if (facade != null) {
            // Change the facade delegate
            facade.setDelegate((EnvironmentWrapper)newEnv);
            newEnv = facade;
        }
        
        // Get the processor that should process this request
        TreeProcessor processor;
        if (getRootProcessor().getContext().equals(this.getContext())) {
            processor = (TreeProcessor) getRootProcessor();
        } else {
            processor = this;
        }
        
        // Process the redirect
        context.reset();
        return processor.process(newEnv, context);
    }
    
    /**
     * Process the given <code>Environment</code> to assemble
     * a <code>ProcessingPipeline</code>.
     * @since 2.1
     */
    public ProcessingPipeline buildPipeline(Environment environment) throws Exception {
        InvokeContext context = new InvokeContext( true );

        context.enableLogging(getLogger());

        try {
            if ( process(environment, context) ) {
                return context.getProcessingPipeline();
            } else {
                return null;
            }
        } finally {
            context.dispose();
        }
    }

    /**
     * TODO: do we still need this?
     */
    public Map getComponentConfigurations() {
        return null;
    }
    
    /**
     * TODO: do we still need this?
     */
    public String getContext() {
        return getEnvironmentHelper().getContext();
    }
    
    /**
     * TODO: do we still need this?
     * 
     * Get the root parent of this processor
     * @since 2.1.1
     */
    public Processor getRootProcessor() {
        TreeProcessor result = this;
        while(result.m_parent != null) {
            result = result.m_parent;
        }
        
        return result;
    }

    public EnvironmentHelper getEnvironmentHelper() {
        return m_environmentHelper;
    }

    /**
     * Create a new child of this processor (used for mounting submaps).
     *
     * @param manager the component manager to be used by the child processor.
     * @param language the language to be used by the child processor.
     * @return a new child processor.
     */
    public TreeProcessor createChildProcessor(Source source) throws Exception {
        TreeProcessor child = new TreeProcessor(this);
        child.m_source = new DelayedRefreshSourceWrapper(source, m_lastModifiedDelay);
        return child;
    }
    
    /**
     * Local extension of EnvironmentWrapper to propagate otherwise blocked
     * methods to the actual environment.
     */
    private static final class ForwardEnvironmentWrapper extends EnvironmentWrapper {

        public ForwardEnvironmentWrapper(Environment env,
            ServiceManager manager, String uri, Logger logger) throws MalformedURLException {
            super(env, manager, uri, logger);
        }

        public void setStatus(int statusCode) {
            environment.setStatus(statusCode);
        }

        public void setContentLength(int length) {
            environment.setContentLength(length);
        }

        public void setContentType(String contentType) {
            environment.setContentType(contentType);
        }

        public String getContentType() {
            return environment.getContentType();
        }
    }

    public void precompile(
        String fileName,
        Environment environment,
        String markupLanguage,
        String programmingLanguage)
        throws Exception {
        // TODO Auto-generated method stub

    }

    public boolean modifiedSince(long date) {
        // TODO Auto-generated method stub
        return false;
    }
}
