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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Profiler component implementation. Stores profiler data for
 * all pipelines.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: ProfilerImpl.java,v 1.2 2004/03/05 13:02:20 bdelacretaz Exp $
 */
public class ProfilerImpl extends AbstractLogEnabled
    implements Profiler, ThreadSafe, Configurable {

    // Maximal count of entries, which should be stored.
    private int results_count = 10;

    private Map results;

    public ProfilerImpl()
    {
        results = new HashMap();
    }

    /**
     * Pass the Configuration to the Configurable class. This method must 
     * always be called after the constructor and before any other method.
     * 
     * @param configuration the class configurations.
     */
    public void configure(Configuration configuration)
        throws ConfigurationException {

        this.results_count = configuration.getAttributeAsInteger("results", 10);
    }

    /**
     * Clear the results.
     */
    public void clearResults()
    {
        results.clear();
    }

    /**
     * Remove the specified result.
     */
    public void clearResult(Object key)
    {
        results.remove(key);
    }

    /**
     * Returns a collection of all keys
     *
     * @return Keys of all results.
     */
    public Collection getResultKeys()
    {
        return results.keySet();
    }

    /**
     * Returns a collection of the results.
     *
     * @return Collection of results.
     */
    public Collection getResults()
    {
        return results.values();
    }

    /**
     * Returns a result of a specifed key.
     *
     * @param key Key of the result.
     * @return Result of the profiling
     */
    public ProfilerResult getResult(Object key)
    {
        return (ProfilerResult)results.get(key);
    }

    /** 
     * Add a result for a request.
     * 
     * @param uri URI of the request
     * @param data Result of the profiling
     */
    public void addResult(String uri, ProfilerData data)
    {
        Long key = new Long(data.getKey(uri));
        ProfilerResult result = (ProfilerResult)results.get(key);
        if(result == null){
            synchronized(results){
                if((result = (ProfilerResult)results.get(key)) == null)
                    results.put(key, result = new ProfilerResult(uri, results_count));
            }
        }

        result.addData(data);
    }
}
