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
package org.apache.cocoon.taglib.core;

import org.apache.cocoon.taglib.Tag;

/**
 * <p>Allows developers to write custom iteration tags by implementing
 * the LoopTag interface. (This is not to be confused with
 * org.apache.cocoon.taglib.IterationTag)
 * LoopTag establishes a mechanism for iteration tags to be recognized
 * and for type-safe communication with custom subtags.
 * 
 * <p>In most cases, it will not be necessary to implement this interface
 * manually, for a base support class (LoopTagSupport) is provided
 * to facilitate implementation.</p>
 * 
 * Migration from JSTL1.0
 * @see javax.servlet.jsp.jstl.core.LoopTag
 *
 * @version $Id$
 */
public interface LoopTag extends Tag {

    /**
     * Retrieves the current item in the iteration.  Behaves
     * idempotently; calling getCurrent() repeatedly should return the same
     * Object until the iteration is advanced.  (Specifically, calling
     * getCurrent() does <b>not</b> advance the iteration.)
     *
     * @return the current item as an object
     */
    public Object getCurrent();

    /**
     * Retrieves a 'status' object to provide information about the
     * current round of the iteration.
     *
     * @return the LoopTagStatus for the current LoopTag
     */
    public LoopTagStatus getIteratorStatus();
}
