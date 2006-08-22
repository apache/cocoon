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
package org.apache.cocoon.portal.profile.impl;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 * Base class for all profile managers
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id$
 */
public abstract class AbstractProfileManager 
    extends AbstractLogEnabled 
    implements Serviceable, ProfileManager, ThreadSafe {

    protected ServiceManager manager;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#register(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void register(CopletInstanceData coplet) {
        // overwrite in subclass
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#register(org.apache.cocoon.portal.layout.Layout)
     */
    public void register(Layout layout) {
        // overwrite in subclass
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#saveUserProfiles(String)
     */
    public void saveUserProfiles(String layoutKey) {
        this.saveUserCopletInstanceDatas(layoutKey);
        this.saveUserLayout(layoutKey);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#saveUserCopletInstanceDatas(java.lang.String)
     */
    public void saveUserCopletInstanceDatas(String layoutKey) {
        // override in subclass
    }

    /* (non-Javadoc)
    * @see org.apache.cocoon.portal.profile.ProfileManager#saveUserLayout(String)
    */
    public void saveUserLayout(String layoutKey) {
        // override in subclass
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#unregister(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void unregister(CopletInstanceData coplet) {
        // overwrite in subclass
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#unregister(org.apache.cocoon.portal.layout.Layout)
     */
    public void unregister(Layout layout) {
        // overwrite in subclass
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#login()
     */
    public void login() {
        // overwrite in subclass
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.profile.ProfileManager#logout()
     */
    public void logout() {
        // overwrite in subclass
    }

}
