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
package org.apache.cocoon.environment;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathIntrospector;


/**
 * This is an utility class to create an object model which is similar to the one
 * used in flow, that can be used from every component.
 * 
 * Work-in-progress, derived from JXTemplateGenerator
 * 
 * @version CVS $Id: TemplateObjectModelHelper.java,v 1.1 2004/04/01 11:54:52 cziegeler Exp $
 */
public class TemplateObjectModelHelper {

    /** Avoid instantiation */
    private TemplateObjectModelHelper() {}

    private static void fillContext(Object contextObject, Map map) {
        // Hack: I use jxpath to populate the context object's properties
        // in the jexl context
        final JXPathBeanInfo bi =
            JXPathIntrospector.getBeanInfo(contextObject.getClass());
        if (bi.isDynamic()) {
            Class cl = bi.getDynamicPropertyHandlerClass();
            try {
                DynamicPropertyHandler h =
                    (DynamicPropertyHandler) cl.newInstance();
                String[] result = h.getPropertyNames(contextObject);
                int len = result.length;
                for (int i = 0; i < len; i++) {
                    try {
                        map.put(result[i], h.getProperty(contextObject, result[i]));
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        } else {
            PropertyDescriptor[] props =  bi.getPropertyDescriptors();
            int len = props.length;
            for (int i = 0; i < len; i++) {
                try {
                    Method read = props[i].getReadMethod();
                    if (read != null) {
                        map.put(props[i].getName(),
                                read.invoke(contextObject, null));
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    /**
     * Create the object model.
     * Currently the object model is a map with one single entry:
     *  cocoon + request         The Request Object
     *         + session         The Session (if available)
     *         + context         The Context
     *         + continuation    The Continuation (if available)
     *         + parameters      The parameters (if provided)
     */
    public static Object getTemplateObjectModel(final Map objectModel, 
                                                final Parameters parameters) {

        // first create the "cocoon object":
        final Map cocoon = new HashMap();
        
        // cocoon.request
        final Request request = ObjectModelHelper.getRequest( objectModel );
        cocoon.put("request", request);
        
        // cocoon.session
        final Session session = request.getSession(false);
        if (session != null) {
            cocoon.put("session", session);
        }
        
        // cocoon.context
        cocoon.put("context", ObjectModelHelper.getContext(objectModel));
        
        // cocoon.continuation
        final Object cont = FlowHelper.getWebContinuation(objectModel);
        if ( cont != null ) {
            cocoon.put("continuation", cont);
        }
        
        // cocoon.parameters
        if ( parameters != null ) {
            cocoon.put("parameters", Parameters.toProperties(parameters));
        }

        final Map map = new HashMap();
        map.put("cocoon", cocoon);

        // Now add objects from flow context (if any)
        final Object contextObject = FlowHelper.getContextObject(objectModel);
        if (contextObject instanceof Map) {
            map.putAll((Map)contextObject);
        } else if ( contextObject != null ) {
            fillContext(contextObject, map);
        }
        
        return map;
    }

}