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
package org.apache.cocoon.components;

import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;

/**
 * A set of constants and methods to access the content of the context
 * object. Some of the constants are defined in {@link org.apache.cocoon.Constants}.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ContextHelper.java,v 1.8 2004/05/26 08:39:49 cziegeler Exp $
 */

public final class ContextHelper {

    /** Application <code>Context</code> Key for the current object model */
    public static final String CONTEXT_OBJECT_MODEL = "object-model";

    /** Application <code>Context</code> Key for the current request object */
    public static final String CONTEXT_REQUEST_OBJECT = CONTEXT_OBJECT_MODEL + '.' + ObjectModelHelper.REQUEST_OBJECT;

    /** Application <code>Context</code> Key for the current response object */
    public static final String CONTEXT_RESPONSE_OBJECT = CONTEXT_OBJECT_MODEL + '.' + ObjectModelHelper.RESPONSE_OBJECT;

    /** Application <code>Context</code> Key for the current sitemap service manager */
    public static final String CONTEXT_SITEMAP_SERVICE_MANAGER = "sitemap-service-manager";
    
    /** Application root directory @since 2.2 */
    public static final String CONTEXT_ROOT_URL = "root-url";
    
    private ContextHelper() {
        // Forbid instantiation
    }

    /**
     * Return the current request
     * @param context The component context
     * @return The request object
     */
    public static final Request getRequest(Context context) {
        // the request object is always present
        try {
            return (Request)context.get(CONTEXT_REQUEST_OBJECT);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Unable to get the request object from the context.", ce);
        }
    }

    /**
     * Return the current response
     * @param context The component context
     * @return The response
     */
    public static final Response getResponse(Context context) {
        // the response object is always present
        try {
            return (Response)context.get(CONTEXT_RESPONSE_OBJECT);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Unable to get the response object from the context.", ce);
        }
    }

    /**
     * Return the current object model
     * @param context The component context
     * @return The object model
     */
    public static final Map getObjectModel(Context context) {
        // the object model is always present
        try {
            return (Map)context.get(CONTEXT_OBJECT_MODEL);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Unable to get the object model from the context.", ce);
        }
    }
    
    /**
     * Return the current sitemap service manager
     * @param context The component context
     * @return The sitemap manager if currently a request is processed
     * @since 2.2
     */
    public static final ServiceManager getSitemapServiceManager(Context context) {
        // the service manager is always present
        try {
            return (ServiceManager)context.get(CONTEXT_SITEMAP_SERVICE_MANAGER);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Unable to get the service manager from the context.", ce);
        }        
    }
}
