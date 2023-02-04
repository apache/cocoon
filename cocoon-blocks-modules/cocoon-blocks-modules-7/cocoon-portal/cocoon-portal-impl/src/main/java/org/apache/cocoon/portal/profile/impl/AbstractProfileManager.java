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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.user.UserDidLoginEvent;
import org.apache.cocoon.portal.event.user.UserEvent;
import org.apache.cocoon.portal.event.user.UserWillLogoutEvent;
import org.apache.cocoon.portal.om.CompositeLayout;
import org.apache.cocoon.portal.om.CopletAdapter;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletLayout;
import org.apache.cocoon.portal.om.CopletType;
import org.apache.cocoon.portal.om.Item;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.om.PortalUser;
import org.apache.cocoon.portal.services.ProfileManager;
import org.apache.cocoon.portal.services.aspects.AspectChain;
import org.apache.cocoon.portal.services.aspects.impl.support.ProfileManagerAspectContextImpl;
import org.apache.cocoon.portal.services.aspects.support.AspectChainImpl;
import org.apache.cocoon.portal.util.AbstractBean;
import org.springframework.core.Ordered;

/**
 * Base class for all profile managers.
 *
 * @version $Id$
 */
public abstract class AbstractProfileManager
    extends AbstractBean
    implements ProfileManager, Receiver, Ordered {

    /** The chain for the configured profile manager aspects. */
    protected AspectChainImpl chain;

    /** The map of coplet types. */
    protected Map copletTypesMap = Collections.EMPTY_MAP;

    /** The map of renderers. */
    protected Map rendererMap = Collections.EMPTY_MAP;

    public void setCopletTypesMap(final Map m) {
        this.copletTypesMap = (m != null ? m : Collections.EMPTY_MAP);
    }

    public void setRendererMap(final Map m) {
        this.rendererMap = (m != null ? m : Collections.EMPTY_MAP);
    }

    /**
     * Set the event chain.
     * @param a A chain.
     */
    public void setAspectChain(AspectChainImpl a) {
        this.chain = a;
    }

    /**
     * @see org.apache.cocoon.portal.services.ProfileManager#getProfileManagerAspectChain()
     */
    public AspectChain getProfileManagerAspectChain() {
        return this.chain;
    }

    /**
     * @see org.apache.cocoon.portal.services.ProfileManager#getCopletTypes()
     */
    public Collection getCopletTypes() {
        return this.copletTypesMap.values();
    }

    /**
     * @see org.apache.cocoon.portal.services.ProfileManager#getCopletType(java.lang.String)
     */
    public CopletType getCopletType(String id) {
        return (CopletType)this.copletTypesMap.get(id);
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
     * @see org.apache.cocoon.portal.services.ProfileManager#saveUserProfiles()
     */
    public void saveUserProfiles() {
        // override in subclass
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
    protected void prepareObject(ProfileHolder profile, Object object)
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
                            adapter = cid.getCopletDefinition().getCopletType().getCopletAdapter();
                            adapter.login( cid );
                        }
                    }
                }
            }
        }
    }

    protected Layout checkAvailability(ProfileHolder profile, Layout layout) {
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
    protected Collection processCopletDefinitions(Collection copletDefinitions)
    throws LayoutException {
        Collection result = copletDefinitions;
        if ( this.chain.hasAspects() ) {
            final ProfileManagerAspectContextImpl aspectContext = new ProfileManagerAspectContextImpl(this.portalService,
                                                                                                      this.chain,
                                                                                                      ProfileManagerAspectContextImpl.PHASE_COPLET_DEFINITIONS);
            aspectContext.invokeNext(copletDefinitions);
            result = (Collection)aspectContext.getResult();
        }
        this.prepareObject(null, result);
        return result;
    }

    /**
     * Process a freshly loaded profile.
     * TODO Why do we need the profile?
     */
    protected Collection processCopletInstances(ProfileHolder profile, Collection copletInstances)
    throws LayoutException {
        Collection result = copletInstances;
        if ( this.chain.hasAspects() ) {
            final ProfileManagerAspectContextImpl aspectContext = new ProfileManagerAspectContextImpl(this.portalService,
                                                                                                      this.chain,
                                                                                                      ProfileManagerAspectContextImpl.PHASE_COPLET_INSTANCES);
            aspectContext.invokeNext(copletInstances);
            result = (Collection)aspectContext.getResult();
        }
        this.prepareObject(profile, result);
        return result;
    }

    /**
     * Process a freshly loaded profile.
     * TODO Why do we need the profile?
     */
    protected Layout processLayout(ProfileHolder profile, Layout layout)
    throws LayoutException {
        Layout result = layout;
        if ( this.chain.hasAspects() ) {
            final ProfileManagerAspectContextImpl aspectContext = new ProfileManagerAspectContextImpl(this.portalService,
                                                                                                      this.chain,
                                                                                                      ProfileManagerAspectContextImpl.PHASE_COPLET_LAYOUT);
            aspectContext.invokeNext(layout);
            result = (Layout)aspectContext.getResult();
        }
        this.prepareObject(profile, result);
        return result;
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
