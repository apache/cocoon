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
package org.apache.cocoon.callstack;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;


/**
 * Stack based scope implementation. It is based on the CallStack and
 * an object is in scope when it is in the top frame of the stack.
 *
 * @version $Id$
 * @since 2.2 
 */
public class CallScope implements Scope {

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#get(java.lang.String, org.springframework.beans.factory.ObjectFactory)
     */
    public Object get(String name, ObjectFactory objectFactory) {
        CallFrame frame = CallStack.getCurrentFrame();
        Object scopedObject = frame.getAttribute(name);
        if (scopedObject == null) {
            scopedObject = objectFactory.getObject();
            frame.setAttribute(name, scopedObject);
        }
        return scopedObject;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.Scope#remove(java.lang.String)
     */
    public Object remove(String name) {
        CallFrame frame = CallStack.getCurrentFrame();
        Object scopedObject = frame.getAttribute(name);
        if (scopedObject != null)
            frame.removeAttribute(name);
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
        CallStack.getCurrentFrame().registerDestructionCallback(name, callback);
    }

}
