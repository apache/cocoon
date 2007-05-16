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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @version $Id$
 * @since 2.1.10
 */
public class PageReportImpl implements PageReport {

    protected final List statistics = new ArrayList();
    protected final String id;
    protected final Date   date = new Date();

    public PageReportImpl(String id) {
        this.id = id;
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.PageReport#getId()
     */
    public String getId() {
        return this.id;
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.PageReport#getDate()
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.PageReport#getStatistics()
     */
    public List getStatistics() {
        return this.statistics;
    }

    /**
     * @param stats
     */
    public void addStatistics(Statistics stats) {
        if ( stats != null ) {
            this.statistics.add(new SimpleStats(stats.getCategory(), stats.getDuration()));
        }
    }

    protected final static class SimpleStats implements Statistics {

        protected final String category;
        protected final long   duration;

        public SimpleStats(String category, long duration) {
            this.category = category;
            this.duration = duration;
        }

        /**
         * @see org.apache.cocoon.profiling.statistics.Statistics#getCategory()
         */
        public String getCategory() {
            return this.category;
        }

        /**
         * @see org.apache.cocoon.profiling.statistics.Statistics#getDuration()
         */
        public long getDuration() {
            return this.duration;
        }
    }
}
