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
package org.apache.cocoon.components.flow.java;

import java.lang.reflect.Method;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.environment.Redirector;

/**
 * Helper class to associate cocoon flow informations to the continuation.
 *
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: ContinuationContext.java,v 1.2 2004/06/23 09:16:31 stephan Exp $
 */
public class ContinuationContext {

    private Object object;
    private Method method;

    private Logger logger;
    private Context avalonContext;
    private ServiceManager manager;
    private Redirector redirector;
    
    private Parameters parameters;

    public ContinuationContext() {
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public void setAvalonContext(Context avalonContext) {
        this.avalonContext = avalonContext;
    }

    public Context getAvalonContext() {
        return avalonContext;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

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
