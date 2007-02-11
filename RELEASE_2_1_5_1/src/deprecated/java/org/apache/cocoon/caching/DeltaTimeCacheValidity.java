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
package org.apache.cocoon.caching;

/**
 * A validation object that remains valid for a specified amount of time.
 *
 * @deprecated Use the Avalon Excalibur SourceValidity implementations instead
 * @author <a href="mailto:M.Homeijer@devote.nl">Michael Homeijer</a>
 * @version CVS $Id: DeltaTimeCacheValidity.java,v 1.2 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public final class DeltaTimeCacheValidity implements CacheValidity {

    private long cachedDateTime;  // Holds the store-time in miliseconds
    private long timeInCache;     // maximum time allowed in cache in minutes

    /**
     * Creates validity object with timeout in minutes.
     */
    public DeltaTimeCacheValidity(long minutes) {
        this.cachedDateTime = System.currentTimeMillis();
        this.timeInCache = minutes * 60000;
    }

    /**
     * Creates validity object with timeout in minutes, seconds.
     */
    public DeltaTimeCacheValidity(long minutes, long seconds) {
        this.cachedDateTime = System.currentTimeMillis();
        this.timeInCache = minutes * 60000 + seconds * 1000;
    }

    /**
     * Creates validity object with timeout in minutes, seconds and milliseconds.
     */
    public DeltaTimeCacheValidity(long minutes, long seconds, long milliseconds) {
        this.cachedDateTime = System.currentTimeMillis();
        this.timeInCache = minutes * 60000 + seconds * 1000 + milliseconds;
    }

    public boolean isValid(CacheValidity validity) {
        if (validity instanceof DeltaTimeCacheValidity) {
            return Math.abs((((DeltaTimeCacheValidity)validity).getCachedDateTime() - this.cachedDateTime)) < this.timeInCache;
        }
        return false;
    }

    public long getCachedDateTime() {
        return this.cachedDateTime;
    }

    public String toString() {
        return "Delta Validity[" + this.cachedDateTime + '+' + this.timeInCache + "ms]";
    }
}
