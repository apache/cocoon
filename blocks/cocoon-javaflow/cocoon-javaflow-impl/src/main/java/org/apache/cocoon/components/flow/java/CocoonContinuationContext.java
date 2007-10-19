/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.flow.java;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.commons.logging.Log;

import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.util.avalon.CLLoggerWrapper;

/**
 * Helper class to associate cocoon flow informations to the continuation.
 *
 * @version $Id$
 */
public class CocoonContinuationContext {

    private Logger logger;
    private Context avalonContext;
    private ServiceManager manager;
    private Redirector redirector;
    
    private Parameters parameters;

    public void setAvalonContext(Context avalonContext) {
        this.avalonContext = avalonContext;
    }

    public Context getAvalonContext() {
        return avalonContext;
    }

    public void setLogger(Log logger) {
        this.logger = new CLLoggerWrapper(logger);
    }

    /** @deprecated Use commons logging */
    public Logger getLogger() {
        return logger;
    }
    
    public void setServiceManager(ServiceManager manager) {
        this.manager = manager;
    }

    public ServiceManager getServiceManager() {
        return manager;
    }

    public void setRedirector(Redirector redirector) {
        this.redirector = redirector;
    }
 
    public Redirector getRedirector() {
        return redirector;
    }
    
	public Parameters getParameters() {
		return parameters;
	}
	
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
}
