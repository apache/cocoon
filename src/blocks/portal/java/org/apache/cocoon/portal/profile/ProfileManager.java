/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
     * Save the profile
     */
    void saveUserProfiles(String layoutKey);
    
    /**
     * Get all instances
     */
    Collection getCopletInstanceDatas();
    
    /**
     * Get all coplets
     */
    Collection getCopletDatas();
    
    /**
     * Copy the current (default) layout and store it under
     * the provided key.
     * The copy includes copying of layout objects (with attached
     * items) and coplet instance datas.
     */
    Layout copyProfile(String layoutKey);
    
    /**
     * Store the provided profile under the layoutKey.
     * This method can be used to overwrite a profile with another
     * one.
     */
    void storeProfile(Layout rootLayout, String layoutKey);
}
