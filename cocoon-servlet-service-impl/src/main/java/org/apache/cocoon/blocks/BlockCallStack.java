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
package org.apache.cocoon.blocks;

import java.util.Stack;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Stack used for geting hold on the current block servlet
 *
 * @version $Id$
 * @since 2.2 
 */
public class BlockCallStack {

    /** The block stack */
    private static final ThreadLocal blockStack = new ThreadLocal();
    
    /** Keep track on if it is an ordinary or a super call */
    private static class BlockCallStackInfo {
        public BlockCallStackInfo(ServletContext servletContext, boolean superCall) {
            this.servletContext = servletContext;
            this.superCall = superCall;
        }
        public ServletContext servletContext;
        public boolean superCall;
    };

    /**
     * This hook must be called each time a block is entered.
     *
     * <p>This method should never raise an exception, except when the
     * parameters are not set!</p>
     *
     * @throws ServletException if block is null
     */
    public static void enterBlock(ServletContext context)
    throws ServletException {
        enterBlock(context, false);
    }

    /**
     * This hook must be called each time a super block is entered.
     *
     * <p>This method should never raise an exception, except when the
     * parameters are not set!</p>
     *
     * @throws ServletException if block is null
     */
    public static void enterSuperBlock(ServletContext context)
    throws ServletException {
        enterBlock(context, true);
    }

    private static void enterBlock(ServletContext context, boolean superCall)
    throws ServletException {
        if (null == context) {
            throw new ServletException("Block is not set.");
        }

        Stack stack = (Stack)blockStack.get();
        if (stack == null) {
            stack = new Stack();
            blockStack.set(stack);
        }
        BlockCallStackInfo info = new BlockCallStackInfo(context, superCall);
        stack.push(info);
    }

    /**
     * This hook must be called each time a block is left.
     *
     * <p>It's the counterpart to the {@link #enterBlock(Block)}
     * method.</p>
     */
    public static void leaveBlock() {
        final Stack stack = (Stack)blockStack.get();
        stack.pop();
    }

    /**
     * Use this method for getting the context that should be used for
     * resolving a polymorphic block protocol call 
     * @return a servlet context
     */
    public static ServletContext getBaseBlockContext() {
        final Stack stack = (Stack)blockStack.get();
        if (stack != null) {
            for(int i = stack.size() - 1; i >= 0; i--) {
                BlockCallStackInfo info = (BlockCallStackInfo) stack.elementAt(i);
                if (!info.superCall)
                    return info.servletContext;
            }
        }
        return null;
    }

    /**
     * Use this method for getting the context that should be used for
     * resolving a block protocol call to a super block 
     * @return a servlet context
     */
    public static ServletContext getCurrentBlockContext() {
        final Stack stack = (Stack)blockStack.get();
        if (stack != null && !stack.isEmpty()) {
            return ((BlockCallStackInfo)stack.peek()).servletContext;
        }
        return null;
    }
}
