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
package org.apache.cocoon.servletscope;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;


/**
 * Stack based scope implementation. It is based on the CallStack and
 * an object is in scope when it is in the top frame of the stack.
 *
 * @version $Id: CallScope.java 562806 2007-08-05 02:26:41Z vgritsenko $
 * @since 2.2 
 */
public class ServletScope implements Scope {
    
    static private String destructionCallbacksAttributeName = ServletScope.class.getName() + "/destructionCallbacks";
    
    private ServletContext servletContext;
    
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#get(java.lang.String, org.springframework.beans.factory.ObjectFactory)
     */
    public Object get(String name, ObjectFactory objectFactory) {
        Object scopedObject = servletContext.getAttribute(name);
        if (scopedObject == null) {
            scopedObject = objectFactory.getObject();
            servletContext.setAttribute(name, scopedObject);
        }

        return scopedObject;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#remove(java.lang.String)
     */
    public Object remove(String name) {
        Object scopedObject = servletContext.getAttribute(name);
        if (scopedObject != null) {
            servletContext.removeAttribute(name);
        }

        return scopedObject;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#getConversationId()
     */
    public String getConversationId() {
        // There is no conversation id concept for the call stack
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#registerDestructionCallback(java.lang.String, java.lang.Runnable)
     */
    public void registerDestructionCallback(String name, Runnable callback) {
        Map destructionCallbacks = getDestructionCallbacks(servletContext);
        destructionCallbacks.put(name, callback);
    }
    
    /**
     * @param servletContext
     * @return the destruction callbacks map that is stored as a attribute of servletContext
     */
    private static Map getDestructionCallbacks(ServletContext servletContext) {
        Map destructionCallbacks = (Map)servletContext.getAttribute(destructionCallbacksAttributeName);
        if (destructionCallbacks == null) {
            destructionCallbacks = new HashMap();
            servletContext.setAttribute(destructionCallbacksAttributeName, destructionCallbacks);
        }
        return destructionCallbacks;
    }
    
    /**
     * Executes destruction callbacks of beans from servlet scope. This method should be called once the Servlet that the scope
     * is tied to is being destroyed.
     * @param servletContext
     */
    public static void executeDestructionCallbacks(ServletContext servletContext) {
        Map destructionCallbacks = getDestructionCallbacks(servletContext);
        Iterator i = destructionCallbacks.values().iterator();
        while (i.hasNext()) {
            ((Runnable) i.next()).run();
        }
    }
}
