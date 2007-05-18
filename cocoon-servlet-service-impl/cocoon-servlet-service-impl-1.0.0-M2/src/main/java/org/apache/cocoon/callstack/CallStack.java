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

import java.util.Stack;

/**
 * Stack used for storing objects in the current call frame.
 *
 * @version $Id$
 * @since 2.2 
 */
public class CallStack {

    /** The call stack */
    private static final ThreadLocal callStack = new ThreadLocal();
    
    /**
     * This hook must be called each time a call frame is entered.
     */
    public static void enter() {
        Stack stack = (Stack)callStack.get();
        if (stack == null) {
            stack = new Stack();
            callStack.set(stack);
        }
        CallFrame info = new CallFrame();
        stack.push(info);
    }

    /**
     * This hook must be called each time a call frame is left.
     *
     * <p>It's the counterpart to the {@link #enter()}
     * method.</p>
     */
    public static void leave() {
        final Stack stack = (Stack)callStack.get();
        CallFrame info = (CallFrame) stack.pop();
        info.executeDestructionCallbacks();
    }

    /**
     * Use this method for getting the current call frame
     * @return a call frame
     */
    public static CallFrame getCurrentFrame() {
        final Stack stack = (Stack)callStack.get();
        if (stack != null && !stack.isEmpty()) {
            return (CallFrame)stack.peek();
        }
        return null;
    }
    
    /**
     * @return the size of the call stack
     */
    public static int size() {
        final Stack stack = (Stack)callStack.get();
        return stack != null ? stack.size() : 0;
    }
    
    /**
     * Get the frame at the i:th position in the call stack
     * @param i
     * @return
     */
    public static CallFrame frameAt(int i) {
        final Stack stack = (Stack)callStack.get();
        return (CallFrame) (stack != null ? stack.elementAt(i) : null);
    }

}
