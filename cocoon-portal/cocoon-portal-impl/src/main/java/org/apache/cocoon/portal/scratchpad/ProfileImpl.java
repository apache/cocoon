/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.scratchpad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.portal.coplet.CopletBaseData;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;

/**
 * The profile for a single user.
 * WORK IN PROGRESS
 *
 * @version $Id$
 * @since 2.2
 */
public class ProfileImpl implements Profile {

    /** The name (unique key) of this profile. */
    protected final String profileName;

    /** A map of all coplet base datas. */
    protected Map copletBaseDatas;

    /** A map of all coplet datas. */
    protected Map copletDatas;

    /** A map of all coplet instance datas. */
    protected Map copletInstanceDatas;

    /** A list of all layouts. */
    protected List layouts;

    /** A map of all layouts having an id. */
    protected Map keyedLayouts;

    /** The root element of the layout. */
    protected Layout rootLayout;

    public ProfileImpl(String profileName) {
        this.profileName = profileName;
    }

    /**
     * Set the root layout for this profile.
     */
    public void setRootLayout(Layout rootLayout) {
        this.rootLayout = rootLayout;
        this.createLayoutCollections();
    }

    public void setCopletBaseDatas(Map copletBaseDatas) {
        this.copletBaseDatas = copletBaseDatas;
    }

    public void setCopletDatas(Map copletDatas) {
        this.copletDatas = copletDatas;
    }

    public void setCopletInstanceDatas(Collection copletInstanceDatas) {
        this.copletInstanceDatas = new HashMap();
        final Iterator i = copletInstanceDatas.iterator();
        while ( i.hasNext() ) {
            final CopletInstanceData current = (CopletInstanceData) i.next();
            this.copletInstanceDatas.put(current.getId(), current);
        }
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#getRootLayout()
     */
    public Layout getRootLayout() {
        return this.rootLayout;
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#getCopletBaseDataObjects()
     */
    public Collection getCopletBaseDataObjects() {
        return this.copletBaseDatas.values();
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#getCopletDataObjects()
     */
    public Collection getCopletDataObjects() {
        return this.copletDatas.values();
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#getCopletInstanceDataObjects()
     */
    public Collection getCopletInstanceDataObjects() {
        return this.copletInstanceDatas.values();
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#getLayoutObjects()
     */
    public Collection getLayoutObjects() {
        return this.layouts;
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#getProfileName()
     */
    public String getProfileName() {
        return this.profileName;
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#searchCopletBaseData(java.lang.String)
     */
    public CopletBaseData searchCopletBaseData(String copletBaseDataId) {
        return (CopletBaseData) this.copletBaseDatas.get(copletBaseDataId);
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#searchCopletData(java.lang.String)
     */
    public CopletData searchCopletData(String copletDataId) {
        return (CopletData) this.copletDatas.get(copletDataId);
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#searchCopletDataObjects(org.apache.cocoon.portal.coplet.CopletBaseData)
     */
    public Collection searchCopletDataObjects(CopletBaseData copletBaseData) {
        final List list = new ArrayList();
        final Iterator i = this.getCopletDataObjects().iterator();
        while ( i.hasNext() ) {
            final CopletData current = (CopletData)i.next();
            if ( current.getCopletBaseData().equals(copletBaseData) ) {
                list.add(current);
            }
        }
        return list;
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#searchCopletDataObjects(java.lang.String)
     */
    public Collection searchCopletDataObjects(String copletBaseDataId) {
        final CopletBaseData cbd = this.searchCopletBaseData(copletBaseDataId);
        if ( cbd != null ) {
            return this.searchCopletDataObjects(cbd);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#searchCopletInstanceData(java.lang.String)
     */
    public CopletInstanceData searchCopletInstanceData(String copletId) {
        return (CopletInstanceData) this.copletInstanceDatas.get(copletId);
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#searchCopletInstanceDataObjects(org.apache.cocoon.portal.coplet.CopletData)
     */
    public Collection searchCopletInstanceDataObjects(CopletData copletData) {
        final List list = new ArrayList();
        final Iterator i = this.getCopletInstanceDataObjects().iterator();
        while ( i.hasNext() ) {
            final CopletInstanceData current = (CopletInstanceData)i.next();
            if ( current.getCopletData().equals(copletData) ) {
                list.add(current);
            }
        }
        return list;
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#searchCopletInstanceDataObjects(java.lang.String)
     */
    public Collection searchCopletInstanceDataObjects(String copletDataId) {
        final CopletData copletData = this.searchCopletData(copletDataId);
        if ( copletData != null ) {
            return this.searchCopletInstanceDataObjects(copletData);
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.scratchpad.Profile#searchLayout(java.lang.String)
     */
    public Layout searchLayout(String layoutId) {
        return (Layout)this.keyedLayouts.get(layoutId);
    }

    protected void createLayoutCollections() {
        this.layouts = new ArrayList();
        this.keyedLayouts = new HashMap();
        this.traverseLayouts(this.rootLayout);
    }

    protected void traverseLayouts(Layout layout) {
        if ( layout != null ) {
            this.layouts.add(layout);
            if ( layout.getId() != null ) {
                this.keyedLayouts.put( layout.getId(), layout );
            }
            if ( layout instanceof CompositeLayout ) {
                final CompositeLayout cl = (CompositeLayout)layout;
                final Iterator i = cl.getItems().iterator();
                while ( i.hasNext() ) {
                    final Item current = (Item)i.next();
                    this.traverseLayouts( current.getLayout() );
                }
            }
        }        
    }

    public void add(CopletInstanceData cid) {
        if ( cid != null ) {
            this.copletInstanceDatas.put(cid.getId(), cid);
        }
    }

    public void remove(CopletInstanceData cid) {
        if ( cid != null ) {
            this.copletInstanceDatas.remove(cid.getId());
        }
    }

    public void add(Layout layout) {
        if ( layout != null ) {
            this.layouts.add(layout);
            if ( layout.getId() != null ) {
                this.keyedLayouts.put(layout.getId(), layout);
            }
        }
    }

    public void remove(Layout layout) {
        if ( layout != null ) {
            if ( layout.getId() != null ) {
                this.keyedLayouts.remove(layout.getId());
            }
            this.layouts.remove(layout);
        }
    }

    public Map getCopletInstanceDatasMap() {
        return this.copletInstanceDatas;
    }

    public Map getCopletDatasMap() {
        return this.copletDatas;
    }

    public Map getCopletBaseDatasMap() {
        return this.copletBaseDatas;
    }
}
