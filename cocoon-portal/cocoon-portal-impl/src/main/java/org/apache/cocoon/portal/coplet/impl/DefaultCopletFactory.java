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
package org.apache.cocoon.portal.coplet.impl;

import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.coplet.CopletBaseData;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.event.coplet.CopletDataAddedEvent;
import org.apache.cocoon.portal.event.coplet.CopletInstanceDataAddedEvent;
import org.apache.cocoon.portal.event.coplet.CopletInstanceDataRemovedEvent;
import org.apache.cocoon.portal.impl.AbstractComponent;

/**
 * This factory is for creating and managing coplet objects.
 *
 * @version $Id$
 */
public class DefaultCopletFactory  
    extends AbstractComponent 
    implements CopletFactory {

    protected static long idCounter = System.currentTimeMillis();

    /**
     * @see org.apache.cocoon.portal.coplet.CopletFactory#newInstance(org.apache.cocoon.portal.coplet.CopletData)
     */
    public CopletInstanceData newInstance(CopletData copletData)
    throws PortalException {
        return this.newInstance(copletData, null);
    }

    /**
     * @see org.apache.cocoon.portal.coplet.CopletFactory#newInstance(org.apache.cocoon.portal.coplet.CopletData, String)
     */
    public CopletInstanceData newInstance(CopletData copletData, String id)
    throws PortalException {
        if (id == null ) {
            synchronized (this) {
                id = copletData.getId() + '-' + idCounter;
                idCounter += 1;
            }
        }
        CopletInstanceData instance = new CopletInstanceData(id);
        instance.setCopletData(copletData);

        // now lookup the adapter
        final String adapterName = copletData.getCopletBaseData().getCopletAdapterName();
        final CopletAdapter adapter = this.portalService.getCopletAdapter(adapterName);
        adapter.init( instance );
        adapter.login( instance );

        // send an event
        this.portalService.getEventManager().send(new CopletInstanceDataAddedEvent(instance));
        return instance;
    }

    /**
     * @see org.apache.cocoon.portal.coplet.CopletFactory#remove(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void remove(CopletInstanceData copletInstanceData) {
        if ( copletInstanceData != null ) {
            // now lookup the adapter
            final String adapterName = copletInstanceData.getCopletData().getCopletBaseData().getCopletAdapterName();
            final CopletAdapter adapter = this.portalService.getCopletAdapter(adapterName);
            adapter.logout( copletInstanceData );
            adapter.destroy( copletInstanceData );

            // send an event
            this.portalService.getEventManager().send(new CopletInstanceDataRemovedEvent(copletInstanceData));
        }
    }

    /**
     * @see org.apache.cocoon.portal.coplet.CopletFactory#newInstance(org.apache.cocoon.portal.coplet.CopletBaseData, java.lang.String)
     */
    public CopletData newInstance(CopletBaseData copletBaseData, String id)
    throws PortalException {
        if (id == null ) {
            synchronized (this) {
                id = copletBaseData.getId() + '-' + idCounter;
                idCounter += 1;
            }
        }
        final CopletData instance = new CopletData(id);
        instance.setCopletBaseData(copletBaseData);

        // send an event
        this.portalService.getEventManager().send(new CopletDataAddedEvent(instance));

        return instance;
    }
}
