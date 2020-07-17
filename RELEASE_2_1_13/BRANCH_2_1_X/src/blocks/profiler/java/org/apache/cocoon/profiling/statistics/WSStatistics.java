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
package org.apache.cocoon.profiling.statistics;

/**
 * @version $Id$
 * @since 2.1.10
 */
public class WSStatistics implements Statistics {

    protected String category;
    protected long duration;
    protected String url;
    protected String method;

    public WSStatistics(String url, String method, long start) {
        this.duration = System.currentTimeMillis() - start;
        this.url = url;
        this.method = method;
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Statistics#getDuration()
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Statistics#getCategory()
     */
    public String getCategory() {
        if ( this.category == null ) {
            this.category = "WebService " + this.url + "/" + this.method;
        }
        return this.category;
    }
}
