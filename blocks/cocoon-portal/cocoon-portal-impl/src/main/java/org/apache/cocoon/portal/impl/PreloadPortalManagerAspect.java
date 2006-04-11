/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.portal.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalManagerAspect;
import org.apache.cocoon.portal.PortalManagerAspectPrepareContext;
import org.apache.cocoon.portal.PortalManagerAspectRenderContext;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.CopletInstanceDataFeatures;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This aspect preloads all changed coplets.
 * TODO - preload all coplets for the first page rendering (as changed list is empty)
 * TODO - provide hook in CopletAdapter for preloading.
 *
 * @version $Id$
 */
public class PreloadPortalManagerAspect
	extends AbstractLogEnabled
	implements PortalManagerAspect {

    /** Handler that simply ignores all sax events. */
    protected static final ContentHandler nullHandler = new DefaultHandler();

    /**
     * @see org.apache.cocoon.portal.PortalManagerAspect#prepare(org.apache.cocoon.portal.PortalManagerAspectPrepareContext, org.apache.cocoon.portal.PortalService)
     */
    public void prepare(PortalManagerAspectPrepareContext context,
                        PortalService                     service)
    throws ProcessingException {
        // let's just invoke the next
        context.invokeNext();
    }

    /**
     * @see org.apache.cocoon.portal.PortalManagerAspect#render(org.apache.cocoon.portal.PortalManagerAspectRenderContext, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler, org.apache.avalon.framework.parameters.Parameters)
     */
    public void render(PortalManagerAspectRenderContext context,
                       PortalService                    service,
                       ContentHandler                   ch,
                       Parameters                       parameters)
    throws SAXException {
        // we should be the first aspect for rendering
        // preload all changed coplets
        final List changedCoplets = CopletInstanceDataFeatures.getChangedCopletInstanceDataObjects(service);
        final Iterator i = changedCoplets.iterator();
        while (i.hasNext()) {
            final CopletInstanceData cid = (CopletInstanceData)i.next();
            final String adapterName = cid.getCopletData().getCopletBaseData().getCopletAdapterName();
            final CopletAdapter adapter = service.getCopletAdapter(adapterName);
            adapter.toSAX(cid, nullHandler );
        }
        // start "real" rendering
        context.invokeNext(ch, parameters);
    }
}
