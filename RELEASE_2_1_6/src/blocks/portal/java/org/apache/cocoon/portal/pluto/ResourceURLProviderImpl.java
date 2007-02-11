/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto;

import org.apache.pluto.services.information.ResourceURLProvider;

/**
 * Create the URL for a resource
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: ResourceURLProviderImpl.java,v 1.2 2004/03/05 13:02:14 bdelacretaz Exp $
 */
public class ResourceURLProviderImpl 
implements ResourceURLProvider {

    protected final String base;
    protected String url;
    
    /**
     * Constructor
     */
    public ResourceURLProviderImpl(PortalContextProviderImpl provider) {
        // TODO What if https is used?
        this.base = provider.getBaseURLexcludeContext(false);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.ResourceURLProvider#setAbsoluteURL(java.lang.String)
     */
    public void setAbsoluteURL(String path) {
        this.url = path;
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.ResourceURLProvider#setFullPath(java.lang.String)
     */
    public void setFullPath(String path) {
        this.url = this.base + path;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.url;
    }

}
