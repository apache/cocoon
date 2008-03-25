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

import org.apache.cocoon.spring.configurator.ResourceUtils;

/**
 * @version $Id$
 */
public class SourceResourceLoader implements ResourceLoader {

    protected final ResourceLoader delegate;

    protected final SourceResolver resolver;


    public SourceResourceLoader(ResourceLoader delegate, SourceResolver resolver) {
        this.delegate = delegate;
        this.resolver = resolver;
    }

    /**
     * @see org.springframework.core.io.ResourceLoader#getClassLoader()
     */
    public ClassLoader getClassLoader() {
        return this.delegate.getClassLoader();
    }

    /**
     * @see org.springframework.core.io.ResourceLoader#getResource(java.lang.String)
     */
    public Resource getResource(String location) {
        if (location != null) {
            // If this is Spring 'classpath:' resource, call delegate before
            // source resolver - it can not handle 'classpath:'.
            if (ResourceUtils.isClasspathUri(location)) {
                return this.delegate.getResource(location);
            }

            // If this is absolute URL with protocol, try source resolver
            if (location.indexOf(':') > 0 || !location.startsWith("/")) {
                try {
                    return new SourceResource(this.resolver.resolveURI(location), this.resolver);
                } catch (IOException e) {
                    // we ignore it and leave it up to the wrapped loader
                }
            }
        }

        // Fallback to delegate
        return this.delegate.getResource(location);
    }
}
