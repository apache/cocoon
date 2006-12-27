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
package org.apache.cocoon.caching;

import org.apache.excalibur.source.SourceValidity;
import java.io.Serializable;

/**
 * This interface declares a (sitemap) component as cacheable.
 * This interface deprecates the org.apache.cocoon.caching.Cacheable interface!
 * <p>
 * Just about everything can be cached, so it makes sense to provide some
 * controls to make sure that the user always gets valid results. There are
 * two aspects to a cacheable component: the key and the validition. The key
 * is used to determine within the component's scheme of things whether a
 * result is unique. For example, if your generator provides dynamic
 * information based on an ID and a user, you want to combine the two elements
 * into one key. That way Cocoon can determine whether to use the cached
 * information for the given ID/User combination or create it from scratch.
 * </p>
 * <p>
 * The CachingPipeline will check the component's key to see if it even has
 * the information cached to begin with. Next, it will check the validity of
 * the cached value if there is one. If the cache has the resource and it is
 * valid, the CachingPipeline will return the cached results. If either
 * condition is false, then the CachingPipeline will generate the results and
 * cache it for later use. It is important to realize that only the
 * <code>CachingPipeline</code> will respect the contracts defined in this
 * interface.
 * </p>
 *
 * @since 2.1
 * @version $Id$
 */
public interface CacheableProcessingComponent {

    /**
     * Generate the unique key for the resource being rendered.
     * <p>
     * The cache key is the single most important part of the caching
     * implementation. If you don't get it right, you can introduce more load
     * on the caching engine than is necessary. It is important that the cache
     * key has the following attributes:
     * </p>
     * <ul>
     * <li>It must be Serializable (part of the contract of this method).</li>
     * <li>It must be Immutable--the key is used as a lookup value.</li>
     * <li>It must be Unique within the space of the component (i.e. the key
     *     "1" for MyCacheableComponent must be for the same resource every
     *     time, but we don't have to worry about the key "1" for
     *     YourCacheableComponent).</li>
     * <li>The equals() and hashCode() methods must be consistent (i.e. if two
     *     keys are equal, the hashCode must also be equal).</li>
     * </ul>
     * <p>
     * Thankfully there is a perfectly suitable object that satisfies these
     * obligations from Java's core: <code>java.lang.String</code>. You can
     * also use your own specific key objects provided they respect the above
     * contracts.
     * </p>
     * <p>
     * <strong>Important:</strong>If the cache key is <code>null</code> then
     * your component will not be cached at all. You can use this to your
     * advantage to cache some things but not others.
     * </p>
     *
     * @return The generated key or <code>null</code> if the component
     *              is currently not cacheable.
     */
    Serializable getKey();

    /**
     * Generate the validity object.  This method is invoked after the
     * <code>getKey()</code> method.
     * <p>
     * The caching contracts use the Excalibur <code>SourceValidity</code>
     * interface to determine whether a resource is valid or not. The validity
     * can be a compound check that incorporates time since creation, parameter
     * values, etc. As long as the sitemap can determine whether the cached
     * resource is valid or not. More information is available on the 
     * <a href="http://excalibur.apache.org/sourceresolve/index.html">Apache
     * Excalibur site</a>. 
     * </p>
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    SourceValidity getValidity();
}
