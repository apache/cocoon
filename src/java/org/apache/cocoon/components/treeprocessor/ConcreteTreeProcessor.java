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
package org.apache.cocoon.components.treeprocessor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.ChainedConfiguration;
import org.apache.cocoon.components.container.ComponentManagerWrapper;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ForwardRedirector;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;
import org.apache.cocoon.environment.wrapper.MutableEnvironmentFacade;


/**
 * The concrete implementation of {@link Processor}, containing the evaluation tree and associated
 * data such as component manager.
 * 
 * @version CVS $Id: ConcreteTreeProcessor.java,v 1.1 2004/06/05 08:18:50 sylvain Exp $
 */
public class ConcreteTreeProcessor extends AbstractLogEnabled implements Processor {

	/** The processor that wraps us */
	private TreeProcessor wrappingProcessor;
	
	/** Component manager defined by the &lt;map:components&gt; of this sitemap */
    ComponentManager sitemapComponentManager;
    
    private ServiceManager serviceManager;
 
    	/** Processing nodes that need to be disposed with this processor */
    private List disposableNodes;
   
    /** Root node of the processing tree */
    private ProcessingNode rootNode;
    
    private Map sitemapComponentConfigurations;
    
    private Configuration componentConfigurations;
    
    /** Number of simultaneous uses of this processor (either by concurrent request or by internal requests) */
    private int requestCount;
    
	/** Builds a concrete processig, given the wrapping processor */
	public ConcreteTreeProcessor(TreeProcessor wrappingProcessor) {
		this.wrappingProcessor = wrappingProcessor;
	}
	
	/** Set the processor data, result of the treebuilder job */
	public void setProcessorData(ComponentManager manager, ProcessingNode rootNode, List disposableNodes) {
		if (this.sitemapComponentManager != null) {
			throw new IllegalStateException("setProcessorData() can only be called once");
		}
		
		this.sitemapComponentManager = manager;
		this.serviceManager = new ComponentManagerWrapper(manager);
		this.rootNode = rootNode;
		this.disposableNodes = disposableNodes;
	}
	
	/** Set the sitemap component configurations (called as part of the tree building process) */
    public void setComponentConfigurations(Configuration componentConfigurations) {
        this.componentConfigurations = componentConfigurations;
        this.sitemapComponentConfigurations = null;
    }

    /**
     * Get the sitemap component configurations
     * @since 2.1
     */
    public Map getComponentConfigurations() {
        // do we have the sitemap configurations prepared for this processor?
        if ( null == this.sitemapComponentConfigurations ) {
            
            synchronized (this) {

                if ( this.sitemapComponentConfigurations == null ) {
                    // do we have configurations?
                    final Configuration[] childs = (this.componentConfigurations == null 
                                                     ? null 
                                                     : this.componentConfigurations.getChildren());
                    
                    if ( null != childs ) {
        
                        if ( null == this.wrappingProcessor.parent ) {
                            this.sitemapComponentConfigurations = new HashMap(12);
                        } else {
                            // copy all configurations from parent
                            this.sitemapComponentConfigurations = new HashMap(
                            			this.wrappingProcessor.parent.getComponentConfigurations()); 
                        }
                        
                        // and now check for new configurations
                        for(int m = 0; m < childs.length; m++) {
                            
                            final String r = this.wrappingProcessor.roleManager.getRoleForName(childs[m].getName());
                            this.sitemapComponentConfigurations.put(r, new ChainedConfiguration(childs[m], 
                                                                             (ChainedConfiguration)this.sitemapComponentConfigurations.get(r)));
                        }
                    } else {
                        // we don't have configurations
                        if ( null == this.wrappingProcessor.parent ) {
                            this.sitemapComponentConfigurations = Collections.EMPTY_MAP;
                        } else {
                            // use configuration from parent
                            this.sitemapComponentConfigurations = this.wrappingProcessor.parent.getComponentConfigurations(); 
                        }
                    }
                }
            }
        }
        return this.sitemapComponentConfigurations;    }
	
    /**
     * Mark this processor as needing to be disposed. Actual call to {@link #dispose()} will occur when
     * all request processings on this processor will be terminated.
     */
	public void markForDisposal() {
		// Decrement the request count (negative number means dispose)
		synchronized(this) {
			this.requestCount--;
		}
		
		if (this.requestCount < 0) {
			// No more users : dispose right now
			dispose();
		}
	}
	
	public TreeProcessor getWrappingProcessor() {
		return this.wrappingProcessor;
	}
	
	public Processor getRootProcessor() {
		return this.wrappingProcessor.getRootProcessor();
	}
	
    /**
     * Process the given <code>Environment</code> producing the output.
     * @return If the processing is successfull <code>true</code> is returned.
     *         If not match is found in the sitemap <code>false</code>
     *         is returned.
     * @throws org.apache.cocoon.ResourceNotFoundException If a sitemap component tries
     *                                   to access a resource which can not
     *                                   be found, e.g. the generator
     *         ConnectionResetException  If the connection was reset
     */
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
     * Process the given <code>Environment</code> to assemble
     * a <code>ProcessingPipeline</code>.
     * @since 2.1
     */
    public InternalPipelineDescription buildPipeline(Environment environment)
    throws Exception {
        InvokeContext context = new InvokeContext( true );

        context.enableLogging(getLogger());
        context.setLastProcessor(this);
        try {
            if ( process(environment, context) ) {
                return context.getInternalPipelineDescription(environment);
            } else {
                return null;
            }
        } finally {
            context.dispose();
        }
    }

    /**
     * Do the actual processing, be it producing the response or just building the pipeline
     * @param environment
     * @param context
     * @return true if the pipeline was successfully built, false otherwise.
     * @throws Exception
     */
    protected boolean process(Environment environment, InvokeContext context)
    throws Exception {
    	
    		// Increment the concurrent requests count
    		synchronized(this) {
    			requestCount++;
    		}

    		try {
    			
    	        // and now process
    	        EnvironmentHelper.enterProcessor(this, this.serviceManager, environment);

    	        final Redirector oldRedirector = context.getRedirector();

    	        // Build a redirector
    	        TreeProcessorRedirector redirector = new TreeProcessorRedirector(environment, context);
    	        setupLogger(redirector);
    	        context.setRedirector(redirector);

    	        try {
    	            boolean success = this.rootNode.invoke(environment, context);
    	            
    	            return success;

    	        } finally {
    	            EnvironmentHelper.leaveProcessor();
    	            // Restore old redirector 
    	            context.setRedirector(oldRedirector);
    	        }

    		} finally {
    			
    			// Decrement the concurrent request count
    			synchronized(this) {
    				requestCount--;
    			}
    			
    			if(requestCount < 0) {
    				// Marked for disposal and no more concurrent requests.
    				dispose();
    			}
    		}
    }
        
    
    private boolean handleCocoonRedirect(String uri, Environment environment, InvokeContext context) throws Exception {
        
        // Build an environment wrapper
        // If the current env is a facade, change the delegate and continue processing the facade, since
        // we may have other redirects that will in turn also change the facade delegate
        
        MutableEnvironmentFacade facade = environment instanceof MutableEnvironmentFacade ?
            ((MutableEnvironmentFacade)environment) : null;
        
        if (facade != null) {
            // Consider the facade delegate (the real environment)
            environment = facade.getDelegate();
        }
        
        // test if this is a call from flow
        boolean isRedirect = (environment.getObjectModel().remove("cocoon:forward") == null);
        Environment newEnv = new ForwardEnvironmentWrapper(environment, uri, getLogger());
        if ( isRedirect ) {
            ((ForwardEnvironmentWrapper)newEnv).setInternalRedirect(true);
        }
        
        if (facade != null) {
            // Change the facade delegate
            facade.setDelegate((EnvironmentWrapper)newEnv);
            newEnv = facade;
        }
        
        // Get the processor that should process this request
        ConcreteTreeProcessor processor;
        if ( newEnv.getURIPrefix().equals("") ) {
            processor = ((TreeProcessor)getRootProcessor()).concreteProcessor;
        } else {
            processor = this;
        }
        
        // Process the redirect
// No more reset since with TreeProcessorRedirector, we need to pop values from the redirect location
//        context.reset();
        return processor.process(newEnv, context);
    }
    
	public void dispose() {
        if (this.disposableNodes != null) {
            // we must dispose the nodes in reverse order
            // otherwise selector nodes are freed before the components node
            for(int i=this.disposableNodes.size()-1; i>-1; i--) {
                ((Disposable)disposableNodes.get(i)).dispose();
            }
            this.disposableNodes = null;
        }
        
        // Ensure it won't be used anymore
        this.rootNode = null;
	}
    
    private class TreeProcessorRedirector extends ForwardRedirector {
        
        private InvokeContext context;
        public TreeProcessorRedirector(Environment env, InvokeContext context) {
            super(env);
            this.context = context;
        }
        
        protected void cocoonRedirect(String uri) throws IOException, ProcessingException {
            try {
                ConcreteTreeProcessor.this.handleCocoonRedirect(uri, this.env, this.context);
            } catch(IOException ioe) {
                throw ioe;
            } catch(ProcessingException pe) {
                throw pe;
            } catch(RuntimeException re) {
                throw re;
            } catch(Exception ex) {
                throw new ProcessingException(ex);
            }
        }
    }
    
    /**
     * Local extension of EnvironmentWrapper to propagate otherwise blocked
     * methods to the actual environment.
     */
    private static final class ForwardEnvironmentWrapper extends EnvironmentWrapper {

        public ForwardEnvironmentWrapper(Environment env,
            String uri, Logger logger) throws MalformedURLException {
            super(env, uri, logger);
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

        public boolean isResponseModified(long lastModified) {
            return environment.isResponseModified(lastModified);
        }
        
        public void setResponseIsNotModified() {
            environment.setResponseIsNotModified();
        }
    }

	public SourceResolver getSourceResolver() {
		return wrappingProcessor.getSourceResolver();
	}

	public String getContext() {
		return wrappingProcessor.getContext();
	}
}
