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
 * @since 1.0.0
 */
public class CallScope implements Scope {

    public Object get(String name, ObjectFactory objectFactory) {
        CallFrame frame = CallStack.getCurrentFrame();
        Object scopedObject = frame.getAttribute(name);
        if (scopedObject == null) {
            scopedObject = objectFactory.getObject();
            frame.setAttribute(name, scopedObject);
        }

        return scopedObject;
    }

    public Object remove(String name) {
        CallFrame frame = CallStack.getCurrentFrame();
        Object scopedObject = frame.getAttribute(name);
        if (scopedObject != null) {
            frame.removeAttribute(name);
        }

        return scopedObject;
    }

    public String getConversationId() {
        // There is no conversation id concept for the call stack
        return null;
    }

    public void registerDestructionCallback(String name, Runnable callback) {
        CallStack.getCurrentFrame().registerDestructionCallback(name, callback);
    }

}
