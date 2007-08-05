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
package org.apache.cocoon.template.environment;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.objectmodel.ObjectModel;
import org.apache.cocoon.objectmodel.helper.ParametersMap;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathIntrospector;


/**
 * Creation of an Expression context from the TemplateObjectModelHelper
 * 
 * @version SVN $Id$
 */
public class FlowObjectModelHelper {

    /** Avoid instantiation. */
    private FlowObjectModelHelper() {}

    /**
     * Create an expression context that contains the object model
     * @param newObjectModel TODO
     */
    public static void fillNewObjectModelWithFOM(ObjectModel newObjectModel, 
                                                            final Map objectModel, final Parameters parameters) {
        
        // Now add objects from flow context (if any)
        final Object contextObject = newObjectModel.get(ObjectModel.CONTEXTBEAN);
        if (contextObject instanceof Map) {
            newObjectModel.putAll((Map)contextObject);
        } else if ( contextObject != null ) {
            FlowObjectModelHelper.fillContext(contextObject, newObjectModel);
        }
        
        ((Map)newObjectModel.get("cocoon")).put("parameters", new ParametersMap(parameters));
        
        //newObjectModel.put(org.apache.cocoon.objectmodel.ObjectModel.CONTEXTBEAN, FlowHelper.getContextObject(objectModel));
    }

    public static void fillContext(Object contextObject, Map map) {
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

}
