/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.processing.impl;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.processing.ProcessInfoProvider;

/**
 * Default implementation of the process info provider.
 *
 * For a simpler implementation we currently use Avalon functionality.
 *
 * @version $Id$
 * @since 2.2
 */
public class ProcessInfoProviderImpl
    implements ProcessInfoProvider, ThreadSafe, Contextualizable {

    protected Context context;

    protected ServletContext servletContext;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
        this.servletContext = (ServletContext) this.context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }

    /**
     * @see org.apache.cocoon.processing.ProcessInfoProvider#getRequest()
     */
    public HttpServletRequest getRequest() {
        return (HttpServletRequest) ContextHelper.getObjectModel(this.context).get(HttpEnvironment.HTTP_REQUEST_OBJECT);
    }

    /**
     * @see org.apache.cocoon.processing.ProcessInfoProvider#getResponse()
     */
    public HttpServletResponse getResponse() {
        return (HttpServletResponse) ContextHelper.getObjectModel(this.context).get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
    }

    /**
     * @see org.apache.cocoon.processing.ProcessInfoProvider#getServletContext()
     */
    public ServletContext getServletContext() {
        return this.servletContext;
    }
}
