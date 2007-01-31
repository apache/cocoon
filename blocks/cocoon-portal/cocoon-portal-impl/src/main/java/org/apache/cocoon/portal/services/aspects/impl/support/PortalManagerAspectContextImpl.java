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
package org.apache.cocoon.portal.services.aspects.impl.support;

import java.util.Properties;

import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.services.aspects.PortalManagerAspect;
import org.apache.cocoon.portal.services.aspects.PortalManagerAspectPrepareContext;
import org.apache.cocoon.portal.services.aspects.PortalManagerAspectRenderContext;
import org.apache.cocoon.portal.services.aspects.support.AspectChain;
import org.apache.cocoon.portal.services.aspects.support.BasicAspectContextImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * The aspect context is passed to every aspect.
 * @since 2.1.8
 * @version $Id$
 */
public final class PortalManagerAspectContextImpl
    extends BasicAspectContextImpl
    implements PortalManagerAspectRenderContext,
               PortalManagerAspectPrepareContext {

    public PortalManagerAspectContextImpl(PortalService service,
                                          AspectChain    chain) {
        super(service, chain);
    }

	/**
	 * @see org.apache.cocoon.portal.services.aspects.PortalManagerAspectPrepareContext#invokeNext()
	 */
	public void invokeNext() 
    throws PortalException {
        final PortalManagerAspect aspect = (PortalManagerAspect)this.getNext();
        if ( aspect != null ) {
            aspect.prepare(this);
        }
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.PortalManagerAspectRenderContext#invokeNext(org.xml.sax.ContentHandler, java.util.Properties)
     */
    public void invokeNext(ContentHandler ch, Properties properties) 
    throws SAXException {
        final PortalManagerAspect aspect = (PortalManagerAspect)this.getNext();
        if ( aspect != null ) {
            aspect.render(this, ch, properties);
        }
    }
}
