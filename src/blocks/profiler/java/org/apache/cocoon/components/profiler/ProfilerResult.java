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



/**
 * A ProfilerResult stores a collection of the lastest n ProfilerDatas 
 * for one pipeline.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:bruno@outerthought.org">Bruno Dumon</a>
 * @version CVS $Id: ProfilerResult.java,v 1.3 2004/03/05 13:02:20 bdelacretaz Exp $
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
