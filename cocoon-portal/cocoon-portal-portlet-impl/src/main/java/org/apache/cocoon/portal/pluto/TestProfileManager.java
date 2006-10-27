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
package org.apache.cocoon.portal.pluto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.portal.LayoutException;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.user.UserEvent;
import org.apache.cocoon.portal.event.user.UserIsAccessingEvent;
import org.apache.cocoon.portal.om.CompositeLayout;
import org.apache.cocoon.portal.om.CopletDefinition;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletLayout;
import org.apache.cocoon.portal.om.Item;
import org.apache.cocoon.portal.om.PortalUser;
import org.apache.cocoon.portal.profile.ProfileException;
import org.apache.cocoon.portal.profile.impl.GroupBasedProfileManager;
import org.apache.cocoon.portal.scratchpad.Profile;
import org.apache.cocoon.portal.scratchpad.ProfileImpl;
import org.apache.commons.lang.StringUtils;

/**
 * This is an extension of the {@link GroupBasedProfileManager} to test
 * all deployed JSR 168 portlets. It provides a generated profile for
 * the user with the id "test".
 * This manager can be used to test own portlets or in combination with the
 * TCK to test the compatibility of Cocoon's JSR 168 implementation.
 *
 * This implementation has some limitations. It expects some default configuration
 * in order to run properly:
 * a) the user must have the id "test".
 * b) a composite layout named "row" must exist.
 * c) the coplet adapter for portlets must be named "portlet".
 * d) The layout for coplets must be named "coplet".
 * e) The renderer for portlets must be named "portlet-window".
 *
 * @version $Id$
 */
public class TestProfileManager extends GroupBasedProfileManager {

    protected Profile loadProfile() 
    throws ProfileException {
        final PortalUser info = this.portalService.getUserService().getUser();
        final String layoutKey = this.portalService.getUserService().getDefaultProfileName();
        if ( info.getUserName().equals("test") ) {
            try {
                // if the request parameter 'portletName' is available we only
                // display the portlets specified with the parameter. Otherwise
                // we show all portlets
                final List portletNames = new ArrayList();
                final HttpServletRequest r = this.portalService.getProcessInfoProvider().getRequest();
                final String[] values = r.getParameterValues("portletName");
                if ( values != null && values.length > 0 ) {
                    for(int i=0; i<values.length; i++) {
                        portletNames.add(StringUtils.replaceChars(values[i], '/', '.'));
                    }
                }
                final ProfileImpl profile = new ProfileImpl();
    
                // first "load" the global data
                profile.setCopletTypes( this.getGlobalCopletTypes( layoutKey) );
                profile.setCopletDefinitions( this.getGlobalDatas( info, profile, layoutKey) );
    
                // create root layout
                CompositeLayout rootLayout = new CompositeLayout("root", "row");
    
                // create coplet instances and layouts
                final List instances = new ArrayList();
                final Iterator i = this.deployedCopletDefinitions.values().iterator();
                while ( i.hasNext() ) {
                    final CopletDefinition cd = (CopletDefinition)i.next();
                    // check for portlets
                    if ( "portlet".equals(cd.getCopletType().getCopletAdapterName()) ) {
                        final String id = StringUtils.replaceChars(cd.getId() + "-1", '_', '-');
                        final CopletInstance cid = new CopletInstance(id);
                        cid.setCopletDefinition(cd);
                        instances.add(cid);
                        if ( portletNames.size() == 0 || portletNames.contains(cd.getId())) {
                            final CopletLayout copletLayout = new CopletLayout(null, "coplet");
                            copletLayout.setCopletInstanceId(cid.getId());
                            final Item item = new Item();
                            item.setLayout(copletLayout);
                            rootLayout.addItem(item);
                        }
                    }
                }
                profile.setCopletInstances(instances);
                this.prepareObject(profile, instances);
    
                this.prepareObject(profile, rootLayout);
                profile.setRootLayout(rootLayout);
    
                final Profile processedProfile = this.processProfile(profile);    
                this.storeUserProfile(processedProfile);
                return processedProfile;
            } catch (ProfileException e) {
                throw e;
            } catch (Exception e) {
                throw new ProfileException("Unable to load profile for '" + layoutKey + "'.", e);
            }
        }
        return super.loadProfile();
    }

    /**
     * Receives any user related event and invokes login, logout etc.
     * @see Receiver
     */
    public void inform(UserEvent event) {
        super.inform(event);
        if ( event instanceof UserIsAccessingEvent ) {
            if ( "test".equals(event.getPortalUser().getUserName()) ) {
                final List portletNames = new ArrayList();
                final HttpServletRequest r = this.portalService.getProcessInfoProvider().getRequest();
                final String[] values = r.getParameterValues("portletName");
                if ( values != null && values.length > 0 ) {
                    for(int i=0; i<values.length; i++) {
                        portletNames.add(StringUtils.replaceChars(values[i], '/', '.'));
                    }
                }
                if ( portletNames.size() > 0 ) {
                    final CompositeLayout rootLayout = (CompositeLayout)this.getLayout(null);
                    // we only remove items and their layout but not the coplet instances
                    while ( rootLayout.getItems().size() > 0 ) {
                        rootLayout.removeItem(rootLayout.getItem(0));
                    }
                    // create new set
                    final Iterator i = this.getCopletInstances().iterator();
                    while ( i.hasNext() ) {
                        final CopletInstance cid = (CopletInstance)i.next();
                        if ( portletNames.contains(cid.getCopletDefinition().getId())) {
                            final CopletLayout copletLayout = new CopletLayout(null, "coplet");
                            copletLayout.setCopletInstanceId(cid.getId());
                            final Item item = new Item();
                            item.setLayout(copletLayout);
                            rootLayout.addItem(item);
                            try {
                                 this.prepareObject(this.getUserProfile(), copletLayout);
                            } catch (LayoutException le) {
                                // ignore this
                            }
                        }
                    }
                }
            }
        }
    }
}
