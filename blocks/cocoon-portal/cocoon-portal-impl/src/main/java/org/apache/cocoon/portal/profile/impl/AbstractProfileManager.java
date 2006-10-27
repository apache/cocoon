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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.portal.LayoutException;
import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.user.UserDidLoginEvent;
import org.apache.cocoon.portal.event.user.UserEvent;
import org.apache.cocoon.portal.event.user.UserWillLogoutEvent;
import org.apache.cocoon.portal.impl.AbstractComponent;
import org.apache.cocoon.portal.om.CompositeLayout;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletLayout;
import org.apache.cocoon.portal.om.Item;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.PortalUser;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.portal.scratchpad.Profile;
import org.apache.cocoon.portal.services.aspects.ProfileManagerAspect;
import org.apache.cocoon.portal.services.aspects.impl.support.ProfileManagerAspectContextImpl;
import org.apache.cocoon.portal.services.aspects.support.AspectChain;
import org.springframework.core.Ordered;

/**
 * Base class for all profile managers.
 *
 * @version $Id$
 */
public abstract class AbstractProfileManager 
    extends AbstractComponent 
    implements ProfileManager, Receiver, Configurable, Ordered {

    /** The chain for the configured profile manager aspects. */
    protected AspectChain chain;

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            if ( this.chain != null) {
                this.chain.dispose( this.manager );
            }
        }
        super.dispose();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        try {
            this.chain = new AspectChain(ProfileManagerAspect.class);
            this.chain.configure(this.manager, config);
        } catch (PortalException pe) {
            throw new ConfigurationException("Unable to configure profile manager aspects.", pe);
        }
    }

    /**
     * Receives any user related event and invokes login, logout etc.
     * @see Receiver
     */
    public void inform(UserEvent event) {
        if ( event instanceof UserDidLoginEvent ) {
            this.login(event.getPortalUser());
        } else if ( event instanceof UserWillLogoutEvent ) {
            this.logout(event.getPortalUser());
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#register(org.apache.cocoon.portal.om.Layout)
     */
    public void register(Layout layout) {
        // overwrite in subclass
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#saveUserProfiles()
     */
    public void saveUserProfiles() {
        // override in subclass
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#unregister(org.apache.cocoon.portal.om.Layout)
     */
    public void unregister(Layout layout) {
        // overwrite in subclass
    }

    /**
     * This method is invoked when a user logs in.
     */
    protected void login(PortalUser user) {
        // overwrite in subclass
    }

    /**
     * This method is invoked when a user logs out.
     */
    protected void logout(PortalUser user) {
        // overwrite in subclass
    }

    /**
     * Prepares the object by using the specified factory.
     */
    protected void prepareObject(Profile profile, Object object)
    throws LayoutException {
        if ( object != null ) {
            Object preparableObject = object;
            if ( object instanceof Map ) {
                preparableObject = ((Map)object).values();
            }
            if (object instanceof Layout) {
                preparableObject = this.checkAvailability(profile, (Layout)object);
            } else if (preparableObject instanceof Collection) {
                final Iterator iterator = ((Collection)preparableObject).iterator();
                while (iterator.hasNext()) {
                    final Object o = iterator.next();
                    if ( o instanceof CopletInstance) {
                        CopletInstance cid = (CopletInstance)o;
                        // check if the coplet data is set; if not the instance
                        // will be removed later on
                        if ( cid.getCopletDefinition() != null ) {
                            // now invoke login on each instance
                            CopletAdapter adapter;
                            adapter = this.portalService.getCopletAdapter(cid.getCopletDefinition().getCopletType().getCopletAdapterName());                            
                            adapter.login( cid );
                        }
                    }
                }
            }
        }
    }

    protected Layout checkAvailability(Profile profile, Layout layout) {
        // is the coplet instance available?
        if ( layout instanceof CopletLayout ) {
            final CopletLayout cl = (CopletLayout)layout;
            if ( cl.getCopletInstanceId() == null ) {
                return null;
            }
            final CopletInstance instance = profile.searchCopletInstance(cl.getCopletInstanceId());
            if ( instance == null || instance.getCopletDefinition() == null ) {
                return null;
            }
        } else if ( layout instanceof CompositeLayout ) {
            final CompositeLayout cl = (CompositeLayout)layout;
            final Iterator i = cl.getItems().iterator();
            while ( i.hasNext() ) {
                Layout current = ((Item)i.next()).getLayout();
                if ( this.checkAvailability(profile, current) == null ) {
                    // coplet or instance is not available: remove layout
                    // FIXME: We could display a dummy coplet instead?
                    i.remove();
                }
            }
        }
        return layout;
    }

    /**
     * Process a freshly loaded profile.
     */
    protected Profile processProfile(Profile profile) {
        // FIXME we should add the calls to prepareObject here as well
        if ( this.chain.hasAspects() ) {
            ProfileManagerAspectContextImpl aspectContext = new ProfileManagerAspectContextImpl(this.portalService, this.chain);
            aspectContext.invokeNext(profile);
        }
        return profile;
    }

    /**
     * This component should have a high priority (low order) as
     * other components might access it during event processing.
     * But the priority must be lower than the priority of the user service!
     * @see org.springframework.core.Ordered#getOrder()
     */
    public int getOrder() {
        return -1;
    }

}
