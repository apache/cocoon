/*
 * Copyright 2006 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.cocoon.core.container.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Own implementation of a {@link XmlWebApplicationContext} which is configured with
 * a base url specifying the root directory for this web application context.
 *
 * @since 2.2
 * @version $Id$
 */
public class CocoonWebApplicationContext extends XmlWebApplicationContext {

    protected String baseUrl;

    public CocoonWebApplicationContext(ApplicationContext parent, String url) {
        this.setParent(parent);
        this.baseUrl = url;
    }

    /**
     * @see org.springframework.web.context.support.AbstractRefreshableWebApplicationContext#getResourceByPath(java.lang.String)
     */
    protected Resource getResourceByPath(String path) {
        // TODO
        return super.getResourceByPath(path);
    }
}
