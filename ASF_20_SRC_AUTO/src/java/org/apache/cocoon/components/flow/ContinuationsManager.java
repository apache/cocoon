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
package org.apache.cocoon.components.flow;

/**
 * The interface of the Continuations manager.
 *
 * The continuation manager maintains a forrest of {@link
 * WebContinuation} trees. Each tree defines the flow of control for a
 * user within the application.
 *
 * A <code>WebContinuation</code> is created for a continuation object
 * from the scripting language used. A continuation object in the
 * implementation of the scripting language is an opaque object
 * here. It is only stored inside the <code>WebContinuation</code>,
 * without being interpreted in any way.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @since March 19, 2002
 * @see WebContinuation
 * @version CVS $Id: ContinuationsManager.java,v 1.6 2004/03/05 13:02:46 bdelacretaz Exp $
 */
public interface ContinuationsManager {
    public final String ROLE = ContinuationsManager.class.getName();

    /**
     * Create a <code>WebContinuation</code> object given a native
     * continuation object and its parent. If the parent continuation is
     * null, the <code>WebContinuation</code> returned becomes the root
     * of a tree in the forrest.
     *
     * @param kont an <code>Object</code> value
     * @param parentKont a <code>WebContinuation</code> value
     * @param timeToLive an <code>int</code> value indicating how long
     * in seconds this continuation will live in the server if not
     * accessed
     * @param disposer a <code>ContinuationsDisposer</code> instance to called when 
     * the continuation gets cleaned up.
     * @return a <code>WebContinuation</code> value
     * @see WebContinuation
     */
    public WebContinuation createWebContinuation(Object kont,
                                                 WebContinuation parentKont,
                                                 int timeToLive,
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
     *
     * @param id a <code>String</code> value
     * @return a <code>WebContinuation</code> object, or null if no such
     * <code>WebContinuation</code> could be found.
     */
    public WebContinuation lookupWebContinuation(String id);

    /**
     * Prints debug information about all web continuations into the log file.
     * @see WebContinuation#display()
     */
    public void displayAllContinuations();
}
