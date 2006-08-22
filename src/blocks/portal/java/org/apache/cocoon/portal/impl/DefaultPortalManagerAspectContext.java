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
package org.apache.cocoon.portal.impl;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalManagerAspect;
import org.apache.cocoon.portal.PortalManagerAspectPrepareContext;
import org.apache.cocoon.portal.PortalManagerAspectRenderContext;
import org.apache.cocoon.portal.PortalService;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * The aspect context is passed to every aspect.
 * @since 2.1.8
 * @version SVN $Id$
 */
public final class DefaultPortalManagerAspectContext
    implements PortalManagerAspectRenderContext, PortalManagerAspectPrepareContext {

    private final PortalService service;
    private final Map objectModel;
    private Iterator iterator;
    private Iterator configIterator;
    private Parameters config;
    
    public DefaultPortalManagerAspectContext(PortalManagerAspectChain chain,
                                             PortalService service,
                                             Map objectModel) {
        this.service = service;
        this.objectModel = objectModel;
        this.iterator = chain.getIterator();
        this.configIterator = chain.getConfigIterator();
    }
    
	/**
	 * @see org.apache.cocoon.portal.PortalManagerAspectPrepareContext#invokeNext()
	 */
	public void invokeNext() 
    throws ProcessingException {
        if (this.iterator.hasNext()) {
            this.config = (Parameters)this.configIterator.next();
            final PortalManagerAspect aspect = (PortalManagerAspect) iterator.next();
            aspect.prepare(this, this.service);
        }
    }

    /**
     * @see org.apache.cocoon.portal.PortalManagerAspectPrepareContext#getAspectParameters()
     */
    public Parameters getAspectParameters() {
        return this.config;
    }

    /**
     * @see org.apache.cocoon.portal.PortalManagerAspectRenderContext#invokeNext(org.xml.sax.ContentHandler, org.apache.avalon.framework.parameters.Parameters)
     */
    public void invokeNext(ContentHandler ch, Parameters parameters) 
    throws SAXException {
        if (this.iterator.hasNext()) {
            this.config = (Parameters)this.configIterator.next();
            final PortalManagerAspect aspect = (PortalManagerAspect) iterator.next();
            aspect.render(this, this.service, ch, parameters);
        }
    }

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext#getObjectModel()
     */
    public Map getObjectModel() {
        return this.objectModel;
    }
}
