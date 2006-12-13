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
 * 
 * @version $Id$
 * @since 2.1.10
 */
public class ReportImpl implements Report {

    protected int count;
    protected long min = -1;
    protected long max = 0;
    protected long accumulated;
    protected long last;
    protected String category;
    protected StringBuffer buffer = new StringBuffer();

    public ReportImpl(String category) {
        this.category = category;
    }

    public void add(Statistics stat) {
        if ( stat != null ) {
            long duration = stat.getDuration();
            if ( duration == 0 ) {
                duration = 1;
            }
            this.count++;
            this.accumulated += duration;
            if ( this.min == -1 || duration < this.min ) {
                this.min = duration;
            }
            if ( duration > this.max ) {
                this.max = duration;
            }
            this.last = duration;
            if ( buffer.length() > 0 ) {
                buffer.append(", ");
            }
            buffer.append(this.getTime(duration));
        }
    }

    protected String getTime(long msecs) {
        long secs = msecs / 1000;
        StringBuffer b = new StringBuffer();
        b.append(secs);
        b.append('.');
        long rest = (msecs - secs * 1000);
        if ( rest < 100 ) {
            b.append('0');
        }
        if ( rest < 10 ) {
            b.append('0');
        }
        b.append(rest);
        b.append('s');
        return b.toString();
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Report#getAverage()
     */
    public long getAverage() {
        if ( count > 0 ) {
            return accumulated / count;
        }
        return 0;
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Report#getCategory()
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Report#getCount()
     */
    public int getCount() {
       return this.count;
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Report#getMax()
     */
    public long getMax() {
        return this.max;
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Report#getMin()
     */
    public long getMin() {
        return this.min;
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Report#getLast()
     */
    public long getLast() {
        return this.last;
    }

    /**
     * @see org.apache.cocoon.profiling.statistics.Report#getAll()
     */
    public String getAll() {
        return buffer.toString();
    }
}
