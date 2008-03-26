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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.portal.om.CompositeLayout;
import org.apache.cocoon.portal.om.CopletDefinition;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletType;
import org.apache.cocoon.portal.om.Item;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutInstance;
import org.apache.cocoon.portal.om.LayoutType;

/**
 * This class is an utility class holding all profile information of the
 * current user.
 *
 * @version $Id$
 * @since 2.2
 */
public class ProfileHolder {

    /** A map of all coplet definitions. */
    protected Map copletDefinitions;

    /** A map of all coplet instances. */
    protected Map copletInstances;

    /** A list of all layouts. */
    protected List layouts;

    /** A list of all layout instances. */
    protected Map layoutInstances;

    /** A map of all layouts having an id. */
    protected Map keyedLayouts;

    /** The root element of the layout. */
    protected Layout rootLayout;

    /** A map of all layout types. */
    protected Map layoutTypes;

    /**
     * Set the root layout for this profile.
     */
    public void setRootLayout(Layout rootLayout) {
        this.rootLayout = rootLayout;
        this.createLayoutCollections();
    }

    /**
     * Set all coplet definitions.
     * @param copletDefinitions An id based map of the definitions.
     */
    public void setCopletDefinitions(Map copletDefinitions) {
        this.copletDefinitions = copletDefinitions;
    }

    /**
     * Set all coplet instances.
     * @param copletInstances An id based map of the instances.
     */
    public void setCopletInstances(Collection copletInstances) {
        this.copletInstances = new HashMap();
        final Iterator i = copletInstances.iterator();
        while ( i.hasNext() ) {
            final CopletInstance current = (CopletInstance) i.next();
            this.copletInstances.put(current.getId(), current);
        }
    }

    /**
     * Return the root layout.
     */
    public Layout getRootLayout() {
        return this.rootLayout;
    }

    /**
     * Return all coplet definitions.
     */
    public Collection getCopletDefinitions() {
        return this.copletDefinitions.values();
    }

    /**
     * Return all coplet instances.
     */
    public Collection getCopletInstances() {
        return this.copletInstances.values();
    }

    /**
     * Return all layouts.
     */
    public Collection getLayoutObjects() {
        return this.layouts;
    }

    /**
     * Search for a coplet definition.
     */
    public CopletDefinition searchCopletDefinition(String copletDefinitionId) {
        return (CopletDefinition) this.copletDefinitions.get(copletDefinitionId);
    }

    /**
     * Search for all coplet definitions of the coplet type.
     */
    public Collection searchCopletDefinitions(CopletType copletType) {
        final List list = new ArrayList();
        final Iterator i = this.getCopletDefinitions().iterator();
        while ( i.hasNext() ) {
            final CopletDefinition current = (CopletDefinition)i.next();
            if ( current.getCopletType().equals(copletType) ) {
                list.add(current);
            }
        }
        return list;
    }

    /**
     * Search for a coplet instance.
     */
    public CopletInstance searchCopletInstance(String copletId) {
        return (CopletInstance) this.copletInstances.get(copletId);
    }

    /**
     * Search for all coplet instances of the coplet definition.
     */
    public Collection searchCopletInstances(CopletDefinition copletDefinition) {
        final List list = new ArrayList();
        final Iterator i = this.getCopletInstances().iterator();
        while ( i.hasNext() ) {
            final CopletInstance current = (CopletInstance)i.next();
            if ( current.getCopletDefinition().equals(copletDefinition) ) {
                list.add(current);
            }
        }
        return list;
    }

    /**
     * Search for all coplet instances of the coplet definition.
     */
    public Collection searchCopletInstances(String copletDefinitionId) {
        final CopletDefinition copletDef = this.searchCopletDefinition(copletDefinitionId);
        if ( copletDef != null ) {
            return this.searchCopletInstances(copletDef);
        }
        return null;
    }

    /**
     * Search for a layout.
     */
    public Layout searchLayout(String layoutId) {
        return (Layout)this.keyedLayouts.get(layoutId);
    }

    protected void createLayoutCollections() {
        this.layouts = new ArrayList();
        this.keyedLayouts = new HashMap();
        this.traverseLayouts(this.rootLayout);
        this.layoutInstances = new HashMap();
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

    /**
     * Add a coplet instance.
     */
    public void add(CopletInstance cid) {
        if ( cid != null ) {
            this.copletInstances.put(cid.getId(), cid);
        }
    }

    /**
     * Remove a coplet instance.
     */
    public void remove(CopletInstance cid) {
        if ( cid != null ) {
            this.copletInstances.remove(cid.getId());
        }
    }

    /**
     * Add a layout.
     */
    public void add(Layout layout) {
        if ( layout != null ) {
            this.layouts.add(layout);
            if ( layout.getId() != null ) {
                this.keyedLayouts.put(layout.getId(), layout);
            }
        }
    }

    /**
     * Add a layout instance.
     */
    public void add(LayoutInstance instance) {
        if ( instance != null) {
            this.layoutInstances.put(instance.getLayout(), instance);
        }
    }

    /**
     * Search the layout instance for a layout object.
     * @param layout
     * @return The layout instance or null.
     */
    public LayoutInstance searchLayoutInstance(Layout layout) {
        if ( layout != null ) {
            return (LayoutInstance) this.layoutInstances.get(layout);
        }
        return null;
    }

    /**
     * Remove a layout.
     */
    public void remove(Layout layout) {
        if ( layout != null ) {
            if ( layout.getId() != null ) {
                this.keyedLayouts.remove(layout.getId());
            }
            this.layouts.remove(layout);
            this.layoutInstances.remove(layout);
        }
    }

    public Map getCopletInstancesMap() {
        return this.copletInstances;
    }

    public Map getCopletDefinitionsMap() {
        return this.copletDefinitions;
    }

    public Map getLayoutTypesMap() {
        return this.layoutTypes;
    }

    public void setLayoutTypes(final Collection c) {
        final Map types = new HashMap();
        final Iterator i = c.iterator();
        while ( i.hasNext() ) {
            final LayoutType layoutType = (LayoutType)i.next();
            types.put(layoutType.getId(), layoutType);
        }
        this.layoutTypes = types;
    }
}
