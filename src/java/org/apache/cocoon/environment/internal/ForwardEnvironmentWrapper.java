/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.net.MalformedURLException;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.components.source.impl.SitemapSourceInfo;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;

/**
 * Local extension of EnvironmentWrapper to propagate otherwise blocked
 * methods to the actual environment.
 * 
 * @version CVS $Id: ForwardEnvironmentWrapper.java,v 1.1 2004/06/25 15:36:38 cziegeler Exp $
*/
public final class ForwardEnvironmentWrapper extends EnvironmentWrapper {

    public ForwardEnvironmentWrapper(Environment env,
                                     SitemapSourceInfo info, 
                                     Logger logger) 
    throws MalformedURLException {
        super(env, info, logger);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setStatus(int)
     */
    public void setStatus(int statusCode) {
        environment.setStatus(statusCode);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setContentLength(int)
     */
    public void setContentLength(int length) {
        environment.setContentLength(length);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setContentType(java.lang.String)
     */
    public void setContentType(String contentType) {
        environment.setContentType(contentType);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#getContentType()
     */
    public String getContentType() {
        return environment.getContentType();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#isResponseModified(long)
     */
    public boolean isResponseModified(long lastModified) {
        return environment.isResponseModified(lastModified);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Environment#setResponseIsNotModified()
     */
    public void setResponseIsNotModified() {
        environment.setResponseIsNotModified();
    }
}