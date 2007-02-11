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

import org.apache.cocoon.util.HashUtil;

import java.util.ArrayList;

/**
 * Request-time profiler information.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:bruno@outerthought.org">Bruno Dumon</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: ProfilerData.java,v 1.4 2004/03/05 13:02:20 bdelacretaz Exp $
 */
public class ProfilerData {

    /**
     * Entry, which stores the role and source of a component from a pipeline.
     */
    public class Entry {

        public String role;
        public String source;
        public long setup;
        public long time;
        public Object fragment;

        protected Entry(String role, String source) {
            this.role = role;
            this.source = source;
        }
    }

    // List of all entries
    private ArrayList entries = null;

    // Environment information
    private EnvironmentInfo environmentinfo;

    // Measured total time
    private long totaltime = 0;

    /**
     * Create a new profiler dataset.
     */
    public ProfilerData() {
        entries = new ArrayList();
    }

    /**
     * Add new component from the pipeling, which should be measured.
     *
     * @param component Component of the pipeline.
     * @param role Role of the component.
     * @param source Source attribute of the component.
     */
    public void addComponent(Object component, String role, String source) {
        entries.add(new Entry((role!=null)
                              ? role
                              : component.getClass().getName(), source));
    }

    /**
     * Returns the count of components.
     *
     * @return Count of components.
     */
    public int getCount() {
        return entries.size();
    }

    /**
     * Set the environment information.
     *
     * @param environmentinfo Environment information.
     */
    public void setEnvironmentInfo(EnvironmentInfo environmentinfo) {
        this.environmentinfo = environmentinfo;
    }

    /**
     * Returns the environment information.
     *
     * @return Environment information.
     */
    public EnvironmentInfo getEnvironmentInfo() {
        return this.environmentinfo;
    }

    /**
     * Set measured time of precessing from the pipeline.
     *
     * @param time Total time of all components.
     */
    public void setTotalTime(long time) {
        this.totaltime = time;
    }

    /**
     * Return measured time of precessing from the pipeline.
     *
     * @return Total time of all components.
     */
    public long getTotalTime() {
        return this.totaltime;
    }

    /**
     * Set measured setup time of the i-th component of the pipeline.
     *
     * @param index Index of the component.
     * @param time Measured setup time of the component.
     */
    public void setSetupTime(int index, long time) {
        ((Entry) entries.get(index)).setup = time;
    }

    /**
     * Get measured setup time of the i-th component of the pipeline.
     *
     * @param index Index of the component.
     * @return Measured setup time of the component.
     */
    public long getSetupTime(int index) {
        return ((Entry) entries.get(index)).setup;
    }

    /**
     * Set measured processing time of the i-th component of the pipeline.
     *
     * @param index Index of the component.
     * @param time Measured processing time of the component.
     */
    public void setProcessingTime(int index, long time) {
        ((Entry) entries.get(index)).time = time;
    }

    /**
     * Get measured processing time of the i-th component of the pipeline.
     *
     * @param index Index of the component.
     * @return Measured processing time of the component.
     */
    public long getProcessingTime(int index) {
        return ((Entry) entries.get(index)).time;
    }

    /**
     * Set the SAX fragment for the i-th component of the pipeline.
     *
     * @param index Index of the component.
     * @param fragment SAX fragment of the component.
     */
    public void setSAXFragment(int index, Object fragment) {
        ((Entry) entries.get(index)).fragment = fragment;
    }

    /**
     * Returns all measured times.
     *
     * @return Array of all entries.
     */
    public Entry[] getEntries() {
        return (Entry[]) entries.toArray(new Entry[entries.size()]);
    }

    /**
     * Generate a key for a given URI for this pipeline
     *
     * @param uri URI
     * @return Hash key.
     */
    public long getKey(String uri) {
        StringBuffer key = new StringBuffer(uri);

        for (int i = 0; i<entries.size(); i++) {
            Entry entry = (Entry) entries.get(i);

            key.append(':');
            key.append(entry.role);
            key.append(':');
            key.append(entry.source);
        }
        return HashUtil.hash(key);
    }
}
