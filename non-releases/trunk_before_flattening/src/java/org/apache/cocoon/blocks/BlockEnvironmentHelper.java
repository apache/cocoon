/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.blocks;

import java.util.Stack;

import org.apache.cocoon.ProcessingException;

/**
 * Stack used for geting hold on the current block manager
 *
 * @version $Id$
 * @since 2.2 
 */
public class BlockEnvironmentHelper {

    /** The block stack */
    static protected final ThreadLocal blockStack = new ThreadLocal();

    /**
     * This hook must be called each time a block is entered.
     *
     * <p>This method should never raise an exception, except when the
     * parameters are not set!</p>
     *
     * @throws ProcessingException if block is null
     */
    public static void enterBlock(Block block)
    throws ProcessingException {
        if (null == block) {
            throw new ProcessingException("Block is not set.");
        }

        Stack stack = (Stack)blockStack.get();
        if (stack == null) {
            stack = new Stack();
            blockStack.set(stack);
        }
        stack.push(block);
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

    public static Block getCurrentBlock() {
        final Stack stack = (Stack)blockStack.get();
        if ( stack != null && !stack.isEmpty()) {
        	return (Block)stack.peek();
        }
        return null;
    }
}
