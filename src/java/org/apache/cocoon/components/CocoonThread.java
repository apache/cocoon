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
package org.apache.cocoon.components;

/**
 * A special <code>Thread</code> that inherits the Cocoon environment context of its parent
 * thread. It is meant to be used when processing of a request can be split across several
 * cooperating threads (e.g. parallel aggregation).
 * <p>
 * <strong>Note</strong>: a <code>CocoonThread</code> should not live longer than the end of
 * the execution of the request in the parent thread, otherwise some unexpected behaviours
 * may happen because the parent's environment has been released.
 * 
 * @since 2.1.8
 * @version $Id$
 */
public class CocoonThread extends Thread {
    
    private Object parentStack = null;

    /**
     * @see Thread#Thread(java.lang.Runnable)
     */
    public CocoonThread(Runnable target) {
        super(target);
        init();
    }

    /**
     * @see Thread#Thread(java.lang.Runnable, java.lang.String)
     */
    public CocoonThread(Runnable target, String name) {
        super(target, name);
        init();
    }

    /**
     * @see Thread#Thread(java.lang.ThreadGroup, java.lang.Runnable)
     */
    public CocoonThread(ThreadGroup group, Runnable target) {
        super(group, target);
        init();
    }

    /**
     * @see Thread#Thread(java.lang.ThreadGroup, java.lang.Runnable, java.lang.String)
     */
    public CocoonThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
        init();
    }

    /**
     * Initialize this thread's environment stack by cloning that of the thread that creates
     * this CocoonThread.
     */
    private void init() {
        EnvironmentStack stack = (EnvironmentStack)CocoonComponentManager.environmentStack.get();
        if (stack != null) {
            this.parentStack = stack.clone();
        }
    }

    /**
     * Setup the environment stack copied from the parent thread, and run the <code>runnable</code>
     * that will do the actual job.
     */
    public final void run() {
        CocoonComponentManager.environmentStack.set(this.parentStack);
        super.run();
        // FIXME: Check the lifetime of this thread compared to its parent.
        // A CocoonThread is meant to start and die within the execution period of the parent request,
        // and it is an error if it lives longer as the parent environment is no more valid.
    }
}
