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

import org.apache.cocoon.util.HashUtil;

import java.util.ArrayList;

/**
 * Request-time profiler information.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:bruno@outerthought.org">Bruno Dumon</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: ProfilerData.java,v 1.2 2003/03/20 15:04:14 stephan Exp $
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

        private Entry(String role, String source) {
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
