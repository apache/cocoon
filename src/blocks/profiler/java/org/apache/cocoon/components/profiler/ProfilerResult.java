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



/**
 * A ProfilerResult stores a collection of the lastest n ProfilerDatas 
 * for one pipeline.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:bruno@outerthought.org">Bruno Dumon</a>
 * @version CVS $Id: ProfilerResult.java,v 1.2 2003/03/20 15:04:14 stephan Exp $
 */
public class ProfilerResult {

    // URI of the request
    private String uri;

    // Roles of the sitemap components
    private String[] roles;

    // Sources of the sitemap components
    private String[] sources;

    // Count of the ProfilerData entries
    private int count = 0;

    // Environment information of each request.
    private EnvironmentInfo[] latestEnvironmentInfo;

    // Total times of each request
    private long[] totalTime;

    // Setup times of each component of the latest n-requests
    private long[][] latestSetupTimes;

    // Processing times of each component of the latest n-requests
    private long[][] latestProcessingTimes;

    // SAX fragments of eac component of the latest n-requests
    private Object[][] latestFragments;

    public ProfilerResult(String uri, int latestResultsCount) {
        this.uri = uri;
        this.latestEnvironmentInfo = new EnvironmentInfo[(latestResultsCount>0)?latestResultsCount:5];
        this.latestSetupTimes = new long[(latestResultsCount>0)?latestResultsCount:5][];
        this.latestProcessingTimes = new long[(latestResultsCount>0)?latestResultsCount:5][];
        this.totalTime = new long[(latestResultsCount>0)?latestResultsCount:5];
        this.latestFragments = new Object[(latestResultsCount>0)?latestResultsCount:5][];
        this.count = 0;
    }

    /**
     * Add a new profiler data from a request to the result.
     */
    public void addData(ProfilerData data) {
        ProfilerData.Entry[] entries = data.getEntries();
        synchronized(this){
            if(roles == null || roles.length != entries.length){
                // Reinitialize arrays about the pipeline
                roles = new String[entries.length];
                sources = new String[entries.length];
                for(int i=0; i<entries.length; i++){
                    roles[i] = entries[i].role;
                    sources[i] = entries[i].source;
                }

                // Clear counter
                this.count = 0;
            }

            if (latestProcessingTimes != null) {
                // move the current data 
                for (int i = latestProcessingTimes.length - 1; i > 0; i--) {
                    latestEnvironmentInfo[i] = latestEnvironmentInfo[i - 1];
                    totalTime[i] = totalTime[i - 1];
                    latestSetupTimes[i] = latestSetupTimes[i - 1];
                    latestProcessingTimes[i] = latestProcessingTimes[i - 1];
                    latestFragments[i] = latestFragments[i - 1];
                }
                latestEnvironmentInfo[0] = data.getEnvironmentInfo();
                totalTime[0] = data.getTotalTime();

                latestSetupTimes[0] = new long[entries.length];
                for(int i=0; i<entries.length; i++)
                    this.latestSetupTimes[0][i] = entries[i].setup;

                latestProcessingTimes[0] = new long[entries.length];
                for(int i=0; i<entries.length; i++)
                    this.latestProcessingTimes[0][i] = entries[i].time;

                latestFragments[0] = new Object[entries.length];
                for(int i=0; i<entries.length; i++)
                    latestFragments[0][i] = entries[i].fragment;

                if (count<latestProcessingTimes.length)
                    count++;
            }
        }
    }

    /**
     * The URI of the request.
     */
    public String getURI() {
        return uri;
    }

    /**
     * Roles of the sitemap components.
     */
    public String[] getRoles() {
        return roles;
    }

    /**
     * Sources of the sitemap components.
     */
    public String[] getSources() {
        return sources;
    }

    /**
     * Count of the ProfilerData entries
     */
    public int getCount() {
        return count;
    }

    /**
     * Environment infomation of the latest n-requests
     */
    public EnvironmentInfo[] getLatestEnvironmentInfos()  {
        return latestEnvironmentInfo;
    }

    /**
     * Total times of each request.
     */
    public long[] getTotalTime() {
        return totalTime;
    }

    /**
     * Setup times of each component of the latest n-requests
     */
    public long[][] getSetupTimes() {
        return latestSetupTimes;
    }

    /**
     * Processing times of each component of the latest n-requests
     */
    public long[][] getProcessingTimes() {
        return latestProcessingTimes;
    }

    /**
     * SAX fragment of each component of the latest n-requests
     */
    public Object[][] getSAXFragments() {
        return latestFragments;
    }
}
