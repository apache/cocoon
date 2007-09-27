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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** 
 * Attributes in the call frame and destruction callbacks that should be
 * executed when the call frame is left.
 *  
 * @version $Id$
 * @since 2.2 
 */
public class CallFrame {
    private Map attributes;
    private Map destructionCallbacks;
    
    public boolean hasAttribute(String name) {
        return this.attributes != null && this.attributes.containsKey(name);
    }

    public Object getAttribute(String name) {
        return this.attributes != null ? this.attributes.get(name) : null;
    }
    
    public void setAttribute(String name, Object value) {
        if (this.attributes == null) {
            this.attributes = new HashMap();
        }

        this.attributes.put(name, value);
    }
    
    public Object removeAttribute(String name) {
        return this.attributes != null ? this.attributes.remove(name) : null;
    }
    
    public void registerDestructionCallback(String name, Runnable callback) {
        if (this.destructionCallbacks == null) {
            this.destructionCallbacks = new HashMap();
        }

        this.destructionCallbacks.put(name, callback);
    }
    
    void executeDestructionCallbacks() {
        if (this.destructionCallbacks == null) {
            return;
        }

        Iterator i = this.destructionCallbacks.values().iterator();
        while (i.hasNext()) {
            ((Runnable) i.next()).run();
        }
    }
}
