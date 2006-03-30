/*
 * Copyright 2005 The Apache Software Foundation.
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

/**
 * Action class which simply calls a function defined in the flow
 * script.
 *
 * @version $Id$
 */
public class FlowAction 
       extends ServiceableAction 
       implements Disposable, ThreadSafe {

    protected Core core;    

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.core = (Core)this.manager.lookup(Core.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.core);
            this.core = null;
        }
    }

    /**
     * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map act( Redirector redirector, 
                    SourceResolver resolver, 
                    Map objectModel, 
                    String source, 
                    Parameters param )
	throws Exception {
        final String language = param.getParameter("language", "javascript");
        final String name = param.getParameter("function");

        Interpreter interpreter = this.core.getCurrentSitemap().getInterpreter(language);
        // no arguments for now
        final List args = Collections.EMPTY_LIST;

        interpreter.callFunction(name, args, redirector);
        return EMPTY_MAP;
    }

}
