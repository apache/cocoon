/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.flow.java;

import java.util.HashMap;

/**
 * Continations object to store the current execution. The contiunation
 * object can only used once. 
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id: Continuation.java,v 1.3 2004/06/26 18:29:30 stephan Exp $
 */
public class Continuation {
    private ContinuationStack stack;
    private Object context;

    private static HashMap continuationsmap = new HashMap();

    public boolean restoring = false;
    public boolean capturing = false;

    /**
     * Create new continuation.
     */
    public Continuation(Object context) {
        stack = new ContinuationStack();
        this.context = context;
    }

    /**
     * Create a new continuation, which continue a previous continuation.
     */
    public Continuation(Continuation parent, Object context) {
        if (parent == null)
            throw new NullPointerException("Parent continuation is null");

        stack = new ContinuationStack(parent.stack);
        this.context = context;
        restoring = true;
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
     * Stop the running continuation.
     */
    public static void suspend() {
    	
    	System.out.println("suspend()");

        Continuation continuation = Continuation.currentContinuation();

        if (continuation == null)
            throw new IllegalStateException("No continuation is running");

        if (continuation.restoring) {
            continuation.capturing = false;
        } else {
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
     * True, is the continuation freeze the strack trace, and stops the continuation.
     */
    public boolean isCapturing() {
        return capturing;
    }

    /**
     * Bind the continuation to running thread.
     */
    public void registerThread() {
        synchronized (continuationsmap) {
            continuationsmap.put(Thread.currentThread(), this);
        }
    }

    /**
     * Unbind the continuation to running thread.
     */
    public void deregisterThread() {
        synchronized (continuationsmap) {
            continuationsmap.remove(Thread.currentThread());
        }
    }

    /**
     * Return the continuation, which is associated to the
     * current thread.
     */
    public static Continuation currentContinuation() {
        synchronized (continuationsmap) {
            Thread t = Thread.currentThread();
            return (Continuation) continuationsmap.get(t);
        }
    }
}
