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
package org.apache.cocoon.components.flow;

import java.util.List;
import java.util.Set;

/**
 * The interface of the Continuations manager.
 *
 * The continuation manager maintains a forest of {@link
 * WebContinuation} trees. Each tree defines the flow of control for a
 * user within the application.
 *
 * A <code>WebContinuation</code> is created for a continuation object
 * from the scripting language used. A continuation object in the
 * implementation of the scripting language is an opaque object
 * here. It is only stored inside the <code>WebContinuation</code>,
 * without being interpreted in any way.
 *
 * @since March 19, 2002
 * @see WebContinuation
 * @version $Id$
 */
public interface ContinuationsManager {
    public final String ROLE = ContinuationsManager.class.getName();

    /**
     * Create a <code>WebContinuation</code> object given a native
     * continuation object and its parent. If the parent continuation is
     * null, the <code>WebContinuation</code> returned becomes the root
     * of a tree in the forest.
     *
     * @param kont an <code>Object</code> value
     * @param parentKont a <code>WebContinuation</code> value
     * @param timeToLive an <code>int</code> value indicating how long
     * in seconds this continuation will live in the server if not
     * accessed
     * @param interpreterId id of interpreter invoking continuation creation
     * @param disposer a <code>ContinuationsDisposer</code> instance to called when 
     * the continuation gets cleaned up.
     * @return a <code>WebContinuation</code> value
     * @see WebContinuation
     */
    public WebContinuation createWebContinuation(Object kont,
                                                 WebContinuation parentKont,
                                                 int timeToLive,
                                                 String interpreterId,
                                                 ContinuationsDisposer disposer);

    /**
     * Invalidates a <code>WebContinuation</code>. This effectively
     * means that the continuation object associated with it will no
     * longer be accessible from Web pages. Invalidating a
     * <code>WebContinuation</code> invalidates all the
     * <code>WebContinuation</code>s which are children of it.
     *
     * @param k a <code>WebContinuation</code> value
     */
    public void invalidateWebContinuation(WebContinuation k);

    /**
     * Given a <code>WebContinuation</code> id, retrieve the associated
     * <code>WebContinuation</code> object.
     * @param id a <code>String</code> value
     * @param interpreterId Id of an interpreter that queries for 
     * the continuation
     *
     * @return a <code>WebContinuation</code> object, null if no such
     * <code>WebContinuation</code> could be found. Also null if 
     * <code>WebContinuation</code> was found but interpreter id does 
     * not match the one that the continuation was initially created for.
     */
    public WebContinuation lookupWebContinuation(String id, String interpreterId);

    /**
     * Prints debug information about all web continuations into the log file.
     * 
     * @deprecated Use {@link #getForest()}. This method will be removed from
     * the interface.
     */
    public void displayAllContinuations();
    
    /**
     * Get a list of all continuations as <code>WebContinuationDataBean</code> objects. 
     * 
     * @deprecated Use {@link #getForest()}. This method will be removed.
     */
    public List getWebContinuationsDataBeanList();
    
    /**
     * Get a set of all web continuations. The set itself will only contain the
     * root continuations. Those will provide access to their children.
     * 
     * Since it should not be possible to mess up the actual managed
     * continuations the returned list will contain clones of them.
     * 
     * The purpose of this method is clearly monitoring or for debugging the
     * application. It has no direct relationship to functionality of the
     * ContinuationsManager.
     */
    public Set getForest();
    
}
