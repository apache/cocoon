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
package org.apache.cocoon.portal.services.aspects.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.cocoon.portal.om.CopletAdapter;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletInstanceFeatures;
import org.apache.cocoon.portal.services.aspects.ResponseProcessorAspect;
import org.apache.cocoon.portal.services.aspects.ResponseProcessorAspectContext;
import org.apache.cocoon.portal.util.AbstractBean;
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
	extends AbstractBean
	implements ResponseProcessorAspect {

    /** Handler that simply ignores all sax events. */
    protected static final ContentHandler nullHandler = new DefaultHandler();

    /**
     * @see org.apache.cocoon.portal.services.aspects.ResponseProcessorAspect#render(org.apache.cocoon.portal.services.aspects.ResponseProcessorAspectContext, org.xml.sax.ContentHandler, java.util.Properties)
     */
    public void render(ResponseProcessorAspectContext context,
                       ContentHandler                   ch,
                       Properties                       properties)
    throws SAXException {
        // we should be the first aspect for rendering
        // preload all changed coplets
        final List changedCoplets = CopletInstanceFeatures.getChangedCopletInstanceDataObjects(context.getPortalService());
        final Iterator i = changedCoplets.iterator();
        while (i.hasNext()) {
            final CopletInstance cid = (CopletInstance)i.next();
            final CopletAdapter adapter = cid.getCopletDefinition().getCopletType().getCopletAdapter();
            adapter.toSAX(cid, nullHandler );
        }
        // start "real" rendering
        context.invokeNext(ch, properties);
    }
}
