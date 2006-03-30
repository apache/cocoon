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
 * ContinuationsDisposer declares the contract for the clean-up of specfic 
 * continuations.
 * <p>
 * Typically a {@link Interpreter} implementation that produces continuation 
 * objects which require proper clean up will implement this interface to get
 * a call-back in the event of the ContinuationsManager deciding to invalidate 
 * a WebContinuation. 
 */
public interface ContinuationsDisposer {
    /**
     * Disposes the passed continuation object.
     * <p>
     * This method is called from the ContinuationsManager in the event of
     * the invalidation of a continuation upon the {@link ContinuationsDisposer}
     * object passed during the creation of the WebContinuation.
     * 
     * @param webContinuation the {@link WebContinuation} value representing the  
     * continuation object. 
     */
    public void disposeContinuation(WebContinuation webContinuation);
}
