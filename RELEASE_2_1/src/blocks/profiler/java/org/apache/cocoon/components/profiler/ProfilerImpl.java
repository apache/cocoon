/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: ProfilerImpl.java,v 1.1 2003/03/09 00:05:52 pier Exp $
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
