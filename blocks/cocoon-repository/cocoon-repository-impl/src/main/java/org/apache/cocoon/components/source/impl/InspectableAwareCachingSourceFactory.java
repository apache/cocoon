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
package org.apache.cocoon.components.source.impl;

import org.apache.cocoon.components.source.InspectableSource;
import org.apache.excalibur.source.Source;

/**
 * The InspectableAwareCachingSourceFactory inherits from {@link CachingSourceFactory} and only
 * adds support for wrapped sources of type {@link InspectableSource}.
 * 
 * @version $Id$
 */
public class InspectableAwareCachingSourceFactory extends CachingSourceFactory {

    protected CachingSource instantiateSource(String uri, String wrappedUri, Source wrappedSource, int expires, String cacheName, boolean fail) {
        if (wrappedSource instanceof InspectableSource) {
            return new InspectableTraversableCachingSource(
                            this, 
                            this.scheme, 
                            uri, 
                            wrappedUri,
                            (InspectableSource) wrappedSource, 
                            expires, 
                            cacheName, 
                            this.async,
                            this.validityStrategy, 
                            fail);
        } else {
            return super.instantiateSource(uri, wrappedUri, wrappedSource, expires, cacheName, fail);
        }
    }
    
}
