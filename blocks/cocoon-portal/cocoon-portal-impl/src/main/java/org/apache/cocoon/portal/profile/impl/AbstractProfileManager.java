/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.profile.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstance;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.user.UserDidLoginEvent;
import org.apache.cocoon.portal.event.user.UserEvent;
import org.apache.cocoon.portal.event.user.UserWillLogoutEvent;
import org.apache.cocoon.portal.impl.AbstractComponent;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutException;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.profile.PortalUser;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.portal.profile.ProfileManagerAspect;
import org.apache.cocoon.portal.scratchpad.Profile;

/**
 * Base class for all profile managers.
 *
 * @version $Id$
 */
public abstract class AbstractProfileManager 
    extends AbstractComponent 
    implements ProfileManager, Receiver, Configurable {

    /** Attribute to store the current user. */
    public static final String USER_ATTRIBUTE = AbstractProfileManager.class.getName() + "/User";

    /** The configuration. */
    protected Configuration configuration;

    /** The chain for the configured profile manager aspects. */
    protected ProfileManagerAspectChain chain;

    /** The service selector for the profile manager aspects. */
    protected ServiceSelector aspectSelector;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        super.service(aManager);
        this.aspectSelector = (ServiceSelector) this.manager.lookup( ProfileManagerAspect.ROLE+"Selector");
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            if ( this.chain != null) {
                this.chain.dispose( this.aspectSelector );
            }
            this.manager.release( this.aspectSelector );
            this.aspectSelector = null;
        }
        super.dispose();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.configuration = config;
        this.chain = new ProfileManagerAspectChain();
        this.chain.configure(this.aspectSelector, config.getChild("aspects"));
    }

    /**
     * Receives any user related event and invokes login, logout etc.
     * @see Receiver
     */
    public void inform(UserEvent event, PortalService service) {
        this.portalService.setTemporaryAttribute(USER_ATTRIBUTE, event.getPortalUser());
        if ( event instanceof UserDidLoginEvent ) {
            this.login(event.getPortalUser());
        } else if ( event instanceof UserWillLogoutEvent ) {
            this.logout(event.getPortalUser());
        }
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#register(org.apache.cocoon.portal.layout.Layout)
     */
    public void register(Layout layout) {
        // overwrite in subclass
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#saveUserProfiles(String)
     */
    public void saveUserProfiles(String layoutKey) {
        this.saveUserCopletInstanceDatas(layoutKey);
        this.saveUserLayout(layoutKey);
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#saveUserCopletInstanceDatas(java.lang.String)
     */
    public void saveUserCopletInstanceDatas(String layoutKey) {
        // override in subclass
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#saveUserLayout(String)
     */
    public void saveUserLayout(String layoutKey) {
        // override in subclass
    }

    /**
     * @see org.apache.cocoon.portal.profile.ProfileManager#unregister(org.apache.cocoon.portal.layout.Layout)
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
     * @see org.apache.cocoon.portal.profile.ProfileManager#getProfile(java.lang.String)
     */
    public Profile getProfile(String profileName) {
        return null;
    }

    /**
     * Prepares the object by using the specified factory.
     */
    protected void prepareObject(Object object)
    throws LayoutException {
        if ( object != null ) {
            Object preparableObject = object;
            if ( object instanceof Map ) {
                preparableObject = ((Map)object).values();
            }
            if (object instanceof Layout) {
                preparableObject = this.checkAvailability((Layout)object);
                if ( preparableObject != null ) {
                    this.portalService.getLayoutFactory().prepareLayout((Layout)preparableObject);
                }
            } else if (object instanceof Collection) {
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

    protected Layout checkAvailability(Layout layout) {
        // is the coplet instance available?
        if ( layout instanceof CopletLayout ) {
            final CopletLayout cl = (CopletLayout)layout;
            if ( cl.getCopletInstanceData() == null || cl.getCopletInstanceData().getCopletDefinition() == null ) {
                return null;
            }
        } else if ( layout instanceof CompositeLayout ) {
            final CompositeLayout cl = (CompositeLayout)layout;
            final Iterator i = cl.getItems().iterator();
            while ( i.hasNext() ) {
                Layout current = ((Item)i.next()).getLayout();
                if ( this.checkAvailability(current) == null ) {
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
            DefaultProfileManagerAspectContext aspectContext = new DefaultProfileManagerAspectContext(this.chain, this.portalService);
            aspectContext.invokeNext(profile);
            return aspectContext.getProfile();
        }
        return profile;
    }
}
