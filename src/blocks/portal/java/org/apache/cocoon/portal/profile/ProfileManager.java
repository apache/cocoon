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
package org.apache.cocoon.portal.profile;

import java.util.Collection;
import java.util.List;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.Layout;

/**
 * The profile manager.
 * Via this component you can get the profile (or parts of it) of the
 * current 'user'.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
 */
public interface ProfileManager extends Component {
    
    String ROLE = ProfileManager.class.getName();
    
    /**
     * Get the portal layout defined by the layout key. This
     * usually addresses the layout profile.
     * With the optional subKey it's possible to retrieve
     * a specific layout object in the profile defined by
     * the layout key.
     * @param layoutKey A key describing the layout or null for the default
     * @param layoutID    The id of a layout object or null for the root object
     * @return The layout
     */
	Layout getPortalLayout(String layoutKey, String layoutID);
    
    CopletInstanceData getCopletInstanceData(String copletID);
    
    List getCopletInstanceData(CopletData data);
    
    /**
     * Return the coplet data object
     */
    CopletData getCopletData(String copletDataId);
    
    /**
     * This method is invoked when the user logs into the portal.
     */
    void login();
    
    /**
     * This method is invoked when the user logs out of the portal
     *
     */
    void logout();
    
    /**
     * New coplet instance datas have to be registered using this method.
     */
    void register(CopletInstanceData coplet);
    
    /**
     * Removed coplet instance datas have to be unregistered using this method.
     */
    void unregister(CopletInstanceData coplet);

    /**
     * New layouts have to be registered using this method.
     */
    void register(Layout layout);
    
    /**
     * New layouts have to be unregistered using this method.
     */
    void unregister(Layout layout);

    /**
     * Save the profile. Usually this just calls {@link #saveUserCopletInstanceDatas(String)}
     * and {@link #saveUserLayout(String)}, but implementations are free to
     * implement this method in a different way.
     * @param layoutKey
     */
    void saveUserProfiles(String layoutKey);

    /**
     * Save the layout
     * @param layoutKey
     */
    void saveUserLayout(String layoutKey);

    /**
     * Save the coplet instance data
     * @param layoutKey
     */
    void saveUserCopletInstanceDatas(String layoutKey);

    /**
     * Get all instances
     */
    Collection getCopletInstanceDatas();
    
    /**
     * Get all coplets
     */
    Collection getCopletDatas();
    
    /**
     * Store the provided profile under the layoutKey.
     * This method can be used to overwrite a profile with another
     * one.
     */
    void storeProfile(Layout rootLayout, String layoutKey);
    
    /**
     * Get current user information
     */
    PortalUser getUser();
}
