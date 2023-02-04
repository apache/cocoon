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
package org.apache.cocoon.portal.services.impl;

import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.event.coplet.CopletDefinitionAddedEvent;
import org.apache.cocoon.portal.event.coplet.CopletDefinitionRemovedEvent;
import org.apache.cocoon.portal.event.coplet.CopletInstanceAddedEvent;
import org.apache.cocoon.portal.event.coplet.CopletInstanceRemovedEvent;
import org.apache.cocoon.portal.om.CopletAdapter;
import org.apache.cocoon.portal.om.CopletDefinition;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletType;
import org.apache.cocoon.portal.services.CopletFactory;
import org.apache.cocoon.portal.util.AbstractBean;

/**
 * This factory is for creating and managing coplet objects.
 *
 * @version $Id$
 */
public class DefaultCopletFactory
    extends AbstractBean
    implements CopletFactory {

    protected static long idCounter = System.currentTimeMillis();

    /**
     * @see org.apache.cocoon.portal.services.CopletFactory#newInstance(org.apache.cocoon.portal.om.CopletDefinition)
     */
    public CopletInstance newInstance(CopletDefinition copletData)
    throws PortalException {
        return this.newInstance(copletData, null);
    }

    /**
     * @see org.apache.cocoon.portal.services.CopletFactory#newInstance(org.apache.cocoon.portal.om.CopletDefinition, String)
     */
    public CopletInstance newInstance(CopletDefinition copletDefinition, String key)
    throws PortalException {
        String id = key;
        if (id == null ) {
            synchronized (this) {
                id = copletDefinition.getId() + '-' + idCounter;
                idCounter += 1;
            }
        }
        CopletInstance instance = new CopletInstance(id, copletDefinition);

        // now lookup the adapter
        final CopletAdapter adapter = copletDefinition.getCopletType().getCopletAdapter();
        adapter.login( instance );

        // send an event
        this.portalService.getEventManager().send(new CopletInstanceAddedEvent(instance));
        return instance;
    }

    /**
     * @see org.apache.cocoon.portal.services.CopletFactory#remove(org.apache.cocoon.portal.om.CopletInstance)
     */
    public void remove(CopletInstance copletInstanceData) {
        if ( copletInstanceData != null ) {
            final CopletAdapter adapter = copletInstanceData.getCopletDefinition().getCopletType().getCopletAdapter();
            adapter.logout( copletInstanceData );

            // send an event
            this.portalService.getEventManager().send(new CopletInstanceRemovedEvent(copletInstanceData));
        }
    }

    /**
     * @see org.apache.cocoon.portal.services.CopletFactory#newInstance(org.apache.cocoon.portal.om.CopletType, java.lang.String)
     */
    public CopletDefinition newInstance(CopletType copletType, String key)
    throws PortalException {
        String id = key;
        if (id == null ) {
            synchronized (this) {
                id = copletType.getId() + '_' + idCounter;
                idCounter += 1;
            }
        }
        final CopletDefinition definition = new CopletDefinition(id, copletType);

        final CopletAdapter adapter = definition.getCopletType().getCopletAdapter();
        adapter.init( definition );

        // send an event
        this.portalService.getEventManager().send(new CopletDefinitionAddedEvent(definition));

        return definition;
    }

    /**
     * @see org.apache.cocoon.portal.services.CopletFactory#remove(org.apache.cocoon.portal.om.CopletDefinition)
     */
    public void remove(CopletDefinition copletDefinition) {
        if ( copletDefinition != null ) {
            final CopletAdapter adapter = copletDefinition.getCopletType().getCopletAdapter();
            adapter.destroy( copletDefinition );

            // send an event
            this.portalService.getEventManager().send(new CopletDefinitionRemovedEvent(copletDefinition));
        }
    }

}
