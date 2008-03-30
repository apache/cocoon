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
package org.apache.cocoon.components.flow.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Continuation object to store the current execution. The continuation
 * object can only used once. 
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id$
 */
public class Continuation {
    
    private static final Map continuations = Collections.synchronizedMap(new HashMap());

    private final String functionName;
    private final ContinuationStack stack;
    private Object context;

    public boolean restoring = false;
    public boolean capturing = false;

    /**
     * Create new continuation.
     */
    public Continuation(final String functionName, final Object context) {
        this.functionName = functionName;
        this.stack = new ContinuationStack();
        this.context = context;
    }

    /**
     * Create a new continuation, which continue a previous continuation.
     */
    public Continuation(final Continuation parent, final Object context) {
        if (parent == null)
            throw new NullPointerException("Parent continuation is null");

        this.functionName = parent.functionName;
        this.stack = new ContinuationStack(parent.stack);
        this.context = context;
        this.restoring = true;
    }

    public String getFunctionName() {
        return functionName;
    }

    /**
     * Return the stack, which is used to store the frame information.
     */
    public ContinuationStack getStack() {
        return stack;
    }

    /**
     * Return context object, which is associated to this continuation.
     */ 
    public Object getContext() {
        return context;
    }

    /**
     * Suspend the running continuation or restore the suspended continuation.
     * 
     * With the help of byte code manipulation this method is run twice, once at
     * the end of request 1 to suspend the continuation and a second time at the
     * beginning of the request 2.
     */
    public static void suspend() {
        Continuation continuation = Continuation.currentContinuation();

        if (continuation == null)
            throw new IllegalStateException("No continuation is running");

        if (continuation.restoring) {
            // restoring
            continuation.capturing = false;
        } else {
            // suspending
            continuation.context = null;
            continuation.capturing = true;
        }
        continuation.restoring = false;
    }

    /**
     * True, if the continuation restores the previous stack trace to the
     * last invocation of suspend().
     */
    public boolean isRestoring() {
        return restoring;
    }

    /**
     * True, if the continuation freezes the stack trace, and stops the continuation.
     */
    public boolean isCapturing() {
        return capturing;
    }

    /**
     * Bind the continuation to running thread.
     */
    public void registerThread() {
        continuations.put(Thread.currentThread(), this);
    }

    /**
     * Unbind the continuation to running thread.
     */
    public void deregisterThread() {
        continuations.remove(Thread.currentThread());
    }

    /**
     * Return the continuation, which is associated to the
     * current thread.
     */
    public static Continuation currentContinuation() {
        Thread t = Thread.currentThread();
        return (Continuation) continuations.get(t);
    }

}
