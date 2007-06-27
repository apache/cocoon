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

package org.apache.cocoon.components.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

/**
 * An adapter for the Excalibur SourceResolver.
 *
 * @version $Id$
 */
public class SourceResolverAdapter implements SourceResolver
{
    private org.apache.excalibur.source.SourceResolver resolver;

    public SourceResolverAdapter(org.apache.excalibur.source.SourceResolver resolver, ServiceManager manager) {
        this.resolver = resolver;
    }

    /**
     * Get a <code>Source</code> object.
     * This is a shortcut for <code>resolve(location, null, null)</code>
     * @throws org.apache.excalibur.source.SourceException if the source cannot be resolved
     */
    public Source resolveURI( String location )
        throws MalformedURLException, IOException, SourceException {
  
        return this.resolver.resolveURI(location);
    }

    /**
     * Get a <code>Source</code> object.
     * @param location - the URI to resolve. If this is relative it is either
     *                   resolved relative to the base parameter (if not null)
     *                   or relative to a base setting of the source resolver
     *                   itself.
     * @param base - a base URI for resolving relative locations. This
     *               is optional and can be <code>null</code>.
     * @param parameters - Additional parameters for the URI. The parameters
     *                     are specific to the used protocol.
     * @throws org.apache.excalibur.source.SourceException if the source cannot be resolved
     */
    public Source resolveURI( String location,
                                                          String base,
                                                          Map parameters )
        throws MalformedURLException, IOException, SourceException {

        return this.resolver.resolveURI(location, base, parameters);
    }

    /**
     * Releases a resolved resource
     */
    public void release( Source source ) {
        this.resolver.release(source);
    }

}
