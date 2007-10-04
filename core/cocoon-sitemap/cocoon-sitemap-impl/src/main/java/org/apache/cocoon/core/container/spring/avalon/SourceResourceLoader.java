/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring.avalon;

import java.io.IOException;

import org.apache.excalibur.source.SourceResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class SourceResourceLoader implements ResourceLoader {

    protected final ResourceLoader wrappedLoader;

    protected final SourceResolver resolver;

    public SourceResourceLoader(ResourceLoader wrappedLoader, SourceResolver resolver) {
        this.wrappedLoader = wrappedLoader;
        this.resolver = resolver;
    }

    /**
     * @see org.springframework.core.io.ResourceLoader#getClassLoader()
     */
    public ClassLoader getClassLoader() {
        return this.wrappedLoader.getClassLoader();
    }

    /**
     * @see org.springframework.core.io.ResourceLoader#getResource(java.lang.String)
     */
    public Resource getResource(String location) {
        if ( location != null && (location.indexOf(':') > 0 || !location.startsWith("/"))) {
            try {
                return new SourceResource(this.resolver.resolveURI(location), this.resolver);
            } catch (IOException e) {
                // we ignore it and leave it up to the wrapped loader
            }
        }
        return this.wrappedLoader.getResource(location);
    }
}
