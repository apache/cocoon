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
package org.apache.cocoon.components.pipeline.impl;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.components.pipeline.AbstractProcessingPipeline;
import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;

/**
 * This is the base class for all caching pipeline implementations.
 * The pipeline can be configured with the {@link Cache} to use
 * by specifying the <code>cache-role</code> parameter.
 *
 * @since 2.1
 * @version $Id$
 */
public abstract class BaseCachingProcessingPipeline extends AbstractProcessingPipeline
                                                    implements Disposable {

    /** This is the Cache holding cached responses */
    protected Cache cache;

    /** The deserializer */
    protected XMLByteStreamInterpreter xmlDeserializer;

    /** The serializer */
    protected XMLByteStreamCompiler xmlSerializer;

    /**
     * Parameterizable Interface - Configuration
     */
    public void parameterize(Parameters params)
    throws ParameterException {
        super.parameterize(params);

        String cacheRole = params.getParameter("cache-role", Cache.ROLE);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Using cache " + cacheRole);
        }

        try {
            this.cache = (Cache) this.manager.lookup(cacheRole);
        } catch (ServiceException ce) {
            throw new ParameterException("Unable to lookup cache: " + cacheRole, ce);
        }
    }

    /**
     * Recyclable Interface
     */
    public void recycle() {
        this.xmlDeserializer = null;
        this.xmlSerializer = null;

        super.recycle();
    }

    /**
     * Disposable Interface
     */
    public void dispose() {
        if (null != this.manager) {
            this.manager.release(this.cache);
        }
        this.cache = null;
        this.manager = null;
    }
}
