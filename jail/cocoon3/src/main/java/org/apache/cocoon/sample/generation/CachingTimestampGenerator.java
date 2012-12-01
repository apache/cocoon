/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.sample.generation;

import org.apache.cocoon.pipeline.caching.AbstractCacheKey;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.component.CachingPipelineComponent;
import org.apache.cocoon.util.murmurhash.MurmurHashCodeBuilder;

public class CachingTimestampGenerator extends TimestampGenerator implements CachingPipelineComponent {

    private static final int CACHING_PERIOD = 1500;

    public CacheKey constructCacheKey() {
        return new CacheKeyImplementation(System.currentTimeMillis());
    }

    private final class CacheKeyImplementation extends AbstractCacheKey {

        private static final long serialVersionUID = 1L;
        private final long timestamp;

        public CacheKeyImplementation(long timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CacheKeyImplementation;
        }

        public long getLastModified() {
            return this.timestamp;
        }

        @Override
        public int hashCode() {
            return new MurmurHashCodeBuilder().append(this.getClass().getName()).toHashCode();
        }

        public boolean isValid(CacheKey other) {
            if (!(other instanceof CacheKeyImplementation)) {
                return false;
            }

            CacheKeyImplementation otherCacheKey = (CacheKeyImplementation) other;
            return Math.abs(this.timestamp - otherCacheKey.timestamp) < CACHING_PERIOD;
        }
    }
}
