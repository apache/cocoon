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
package org.apache.cocoon.environment.background;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.WrapperComponentManager;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.environment.AbstractEnvironment;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.commandline.CommandLineContext;
import org.apache.cocoon.environment.commandline.CommandLineRequest;
import org.apache.cocoon.environment.commandline.CommandLineResponse;
import org.apache.cocoon.util.NullOutputStream;

/**
 * A simple implementation of <code>org.apache.cocoon.environment.Environment</code>
 * for pipeline calls which are not externally triggered.
 * 
 * @author <a href="http://apache.org/~reinhard">Reinhard Poetz</a> 
 * @version CVS $Id: BackgroundEnvironment.java,v 1.3 2004/03/11 15:38:31 sylvain Exp $
 *
 * @since 2.1.4
 */
public class BackgroundEnvironment extends AbstractEnvironment {
	
	private ComponentManager manager;
	
	public BackgroundEnvironment(Logger logger, Context ctx, ServiceManager manager) throws MalformedURLException {
		super("", null, new File(ctx.getRealPath("/")), null);
		
		this.enableLogging(logger);
		
		this.manager = new WrapperComponentManager(manager);
		
		this.outputStream = new NullOutputStream();    
     
		// TODO Would special Background*-objects have advantages?
		Request request = new CommandLineRequest(this, "", "", null, null, null);
		this.objectModel.put(ObjectModelHelper.REQUEST_OBJECT, request);  
		this.objectModel.put(ObjectModelHelper.RESPONSE_OBJECT,
							 new CommandLineResponse());
		this.objectModel.put(ObjectModelHelper.CONTEXT_OBJECT, ctx);
	}
	
	/** Needed by CocoonComponentManager.enterEnvironment */
	public ComponentManager getManager() {
		return this.manager;
	}
	
	/** Needed by CocoonComponentManager.enterEnvironment */
	public Processor getProcessor() {
		return NullProcessor.INSTANCE;
	}
	
    /**
     * @param uri
     * @param view
     * @param context
     * @param stream
     * @param log
     * @throws MalformedURLException
     */
    public BackgroundEnvironment(String uri, String view, File context, OutputStream stream, Logger log) 
        throws MalformedURLException {
            
        super(uri, view, context);
        this.enableLogging(log);
        this.outputStream = stream;    
     
        // TODO Would special Background*-objects have advantages?
        Request request = new CommandLineRequest(this, "", uri, null, null, null);
        this.objectModel.put(ObjectModelHelper.REQUEST_OBJECT, request);  
        this.objectModel.put(ObjectModelHelper.RESPONSE_OBJECT,
                             new CommandLineResponse());
        this.objectModel.put(ObjectModelHelper.CONTEXT_OBJECT,
                             new CommandLineContext(context.getAbsolutePath()) );

    }

    /**
     * @see org.apache.cocoon.environment.AbstractEnvironment#redirect(boolean, java.lang.String)
     */
    public void redirect(boolean sessionmode, String newURL) throws IOException {
        
    }

    /**
     * @see org.apache.cocoon.environment.Environment#setContentType(java.lang.String)
     */
    public void setContentType(String mimeType) {
        
    }

    /**
     * @see org.apache.cocoon.environment.Environment#getContentType()
     */
    public String getContentType() {
        return null;
    }

    /**
     * @see org.apache.cocoon.environment.Environment#setContentLength(int)
     */
    public void setContentLength(int length) {
        
    }

    /**
     * Always return false
     * 
     * @see org.apache.cocoon.environment.Environment#isExternal()
     */
    public boolean isExternal() {
        return false;
    }
    
    /** Dumb implementation needed by CocoonComponentManager.enterEnvironment() */
    public static class NullProcessor implements Processor {
    	
    	public static final Processor INSTANCE = new NullProcessor();

		public boolean process(Environment environment) throws Exception {
			throw new UnsupportedOperationException();
		}
		public ProcessingPipeline buildPipeline(Environment environment) throws Exception {
			throw new UnsupportedOperationException();
		}

		public Map getComponentConfigurations() {
			throw new UnsupportedOperationException();
		}

		public Processor getRootProcessor() {
			throw new UnsupportedOperationException();
		}
    }
}
