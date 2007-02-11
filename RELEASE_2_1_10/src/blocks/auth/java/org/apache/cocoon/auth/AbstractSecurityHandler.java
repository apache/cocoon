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
package org.apache.cocoon.auth;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * This is a base class that can be used for own {@link SecurityHandler}s. It
 * provides a save implementation for the {@link #getId()} method. The only
 * drawback is that a subclass has to use {@link Configurable} and can't
 * use {@link org.apache.avalon.framework.parameters.Parameterizable}.
 *
 * @version $Id$
*/
public abstract class AbstractSecurityHandler
    extends AbstractLogEnabled
    implements SecurityHandler, Configurable, Contextualizable, ThreadSafe {

    /** The unique identifier. */
    protected String id;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(final Context context) throws ContextException {
        String sitemapPrefix = null;
        try {
            // this is available starting with Cocoon 2.2
            sitemapPrefix = (String)context.get("env-prefix");
        } catch (ContextException ce) {
            // no prefix available, so we are running pre 2.2 which means
            // we only have one cocoon.xconf anyway
            sitemapPrefix = "cocoon-2.1.x";
        }
        this.id = sitemapPrefix + '/';
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(final Configuration conf) throws ConfigurationException {
        this.id = this.id + '/' + this.getClass().getName() + '/'
                  + conf.getAttribute( "role", this.getClass().getName());
    }

    /**
     * @see org.apache.cocoon.auth.SecurityHandler#getId()
     */
    public String getId() {
        return this.id;
    }
}
