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
package org.apache.cocoon.environment.internal;

import org.apache.cocoon.components.source.impl.SitemapSourceInfo;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;

/**
 * Local extension of EnvironmentWrapper to propagate otherwise blocked
 * methods to the actual environment.
 * 
 * @version $Id$
 * @since 2.2
 */
public final class ForwardEnvironmentWrapper extends EnvironmentWrapper {

    public ForwardEnvironmentWrapper(Environment env,
                                     SitemapSourceInfo info) {
        super(env, info, false);
    }

    /**
     * @see org.apache.cocoon.environment.Environment#setStatus(int)
     */
    public void setStatus(int statusCode) {
        environment.setStatus(statusCode);
    }

    /**
     * @see org.apache.cocoon.environment.Environment#setContentLength(int)
     */
    public void setContentLength(int length) {
        environment.setContentLength(length);
    }

    /**
     * @see org.apache.cocoon.environment.Environment#setContentType(java.lang.String)
     */
    public void setContentType(String contentType) {
        environment.setContentType(contentType);
    }

    /**
     * @see org.apache.cocoon.environment.Environment#getContentType()
     */
    public String getContentType() {
        return environment.getContentType();
    }

    /**
     * @see org.apache.cocoon.environment.Environment#isResponseModified(long)
     */
    public boolean isResponseModified(long lastModified) {
        return environment.isResponseModified(lastModified);
    }

    /**
     * @see org.apache.cocoon.environment.Environment#setResponseIsNotModified()
     */
    public void setResponseIsNotModified() {
        environment.setResponseIsNotModified();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#redirect(java.lang.String, boolean, boolean)
    public void redirect(String newURL, boolean global, boolean permanent)
    throws IOException {
        this.environment.redirect(newURL, global, permanent);
    }
     */
}
