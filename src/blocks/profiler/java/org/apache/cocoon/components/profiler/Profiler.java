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
package org.apache.cocoon.components.profiler;

import org.apache.avalon.framework.component.Component;

import java.util.Collection;

/**
 * Profiler component interface.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: Profiler.java,v 1.2 2004/03/05 13:02:20 bdelacretaz Exp $
 */
public interface Profiler extends Component
{
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
