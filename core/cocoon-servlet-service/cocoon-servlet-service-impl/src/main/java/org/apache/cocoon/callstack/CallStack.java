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

import java.util.Map;
import java.util.Stack;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Stack used for storing objects in the current call frame.
 *
 * @version $Id$
 * @since 2.2 
 */
public class CallStack {

    /** The call stack */
    private static final ThreadLocal callStack = new ThreadLocal();
    
    /** Content map and objects that should be desctructed when the call frame is left */
    private static class CallStackInfo {
        public CallStackInfo(Map attributes, Map destructionCallbacks) {
            this.attributes = attributes;
            this.destructionCallbacks = destructionCallbacks;
        }
        public Map attributes;
        public Map destructionCallbacks;
    };

    /**
     * This hook must be called each time a call frame is entered.
     *
     * <p>This method should never raise an exception, except when the
     * parameters are not set!</p>
     *
     * @throws ServletException if block is null
     */
    public static void enter(Map attributes)
    throws ServletException {
        if (null == attributes) {
            throw new ServletException("Block is not set.");
        }

        Stack stack = (Stack)callStack.get();
        if (stack == null) {
            stack = new Stack();
            callStack.set(stack);
        }
        CallStackInfo info = new CallStackInfo(attributes, null);
        stack.push(info);
    }

    /**
     * This hook must be called each time a block is left.
     *
     * <p>It's the counterpart to the {@link #enterBlock(Block)}
     * method.</p>
     */
    public static void leave() {
        final Stack stack = (Stack)callStack.get();
        stack.pop();
    }

    /**
     * Use this method for getting the context that should be used for
     * resolving a block protocol call to a super block 
     * @return a servlet context
     */
    public static Map getCurrentFrame() {
        final Stack stack = (Stack)callStack.get();
        if (stack != null && !stack.isEmpty()) {
            return ((CallStackInfo)stack.peek()).attributes;
        }
        return null;
    }
}
