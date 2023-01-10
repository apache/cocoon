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
package org.apache.cocoon.components.source.impl.validity;

import org.apache.excalibur.source.SourceValidity;

/**
 * Delays validity check for a specified interval.
 *
 * <p>
 * This is wrapper validity which can be used to reduce count of
 * filesystem (or network) accesses just to check the source
 * validity.
 *
 * @since 2.1.8
 * @version $Id$
 */
public class DelayedValidity implements SourceValidity {

    private long delay;
    private long expires;

    private SourceValidity delegate;


    public DelayedValidity(long delay, SourceValidity validity) {
        this.delay = delay;
        this.expires = System.currentTimeMillis() + delay;
        this.delegate = validity;
    }

    public int isValid() {
        final long currentTime = System.currentTimeMillis();
        if (currentTime <= this.expires) {
            // The delay has not passed yet - assuming source is valid.
            return SourceValidity.VALID;
        }

        // The delay has passed, prepare for the next interval.
        this.expires = currentTime + this.delay;

        return this.delegate.isValid();
    }

    public int isValid(SourceValidity newValidity) {
        // Always delegate
        return this.delegate.isValid(newValidity);
    }
}
