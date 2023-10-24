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
package org.apache.cocoon.environment;

import org.apache.cocoon.environment.internal.EnvironmentHelper;

/**
 * A <code>Runnable</code> wrapper or base class that inherits the execution
 * context of the thread creating it, as it was at the time of creation.
 * <p>
 * It is meant to be used when processing of a request is to be split across several
 * cooperating threads (e.g. parallel aggregation).
 * <p>
 * <strong>Note</strong>: a <code>CocoonRunnable</code> should not live longer than the
 * end of the execution of the request in the creating thread, otherwise some unexpected
 * behaviours may happen because the parent's environment has been released.
 * 
 * @since 2.1.8
 * @version $Id$
 */
public class CocoonRunnable extends EnvironmentHelper.AbstractCocoonRunnable {
    Runnable target;

    /**
     * Creates an empty <code>CocoonRunnable</code> and copies the environment context
     * of the calling thread, for later use when calling {@link #doRun()}. Users of this
     * constructor will override the {@link #doRun()} method where the actual job gets done.
     */
    public CocoonRunnable() {
        // Nothing special here
    }

    /**
     * Wraps an existing <code>Runnable</code> and copies the environment context of
     * the calling thread, for later use when the <code>Runnable</code>'s <code>run()</code>
     * method is called.
     * 
     * @param target the wrapped <code>Runnable</code>
     */
    public CocoonRunnable(Runnable target) {
        this.target = target;
    }

    /**
     * Does the actual job, in the environment of the creating thread. Calls the wrapped
     * <code>Runnable</code> if one was given, and does nothing otherwise.
     */
    protected void doRun() {
        if (target != null) {
            target.run();
        }
    }
}
