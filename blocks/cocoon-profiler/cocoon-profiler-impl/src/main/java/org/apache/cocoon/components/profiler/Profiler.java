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
package org.apache.cocoon.components.profiler;

import java.util.Collection;

/**
 * Profiler component interface.
 *
 * @version $Id$
 */
public interface Profiler {
    
    String ROLE = Profiler.class.getName();

    /**
     * Clear the results.
     */
    void clearResults();

    /**
     * Remove a specified result.
     *
     * @param key Key of the result.
     */
    void clearResult(Object key);

    /**
     * Add a result for a request.
     * 
     * @param uri URI of the request
     * @param data Result of the profiling
     */
    void addResult(String uri, ProfilerData data);

    /**
     * Returns a collection of all keys
     *
     * @return Keys of all results.
     */
    Collection getResultKeys();
 
    /**
     * Returns a collection of the results.
     *
     * @return Collection of results.
     */
    Collection getResults();
 
    /**
     * Returns a result of a specifed key.
     *
     * @param key Key of the result.
     * @return Result.
     */
    ProfilerResult getResult(Object key);
}
