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
package org.apache.cocoon.portal.generation;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.services.PortalManager;
import org.xml.sax.SAXException;

/**
 * This generator renders the complete portal.
 * More precisly, this generator is the starting point for the portal
 * rendering. The generator delegates the rendering process to
 * to {@link PortalManager} component.
 * This generator needs one runtime configuration: the name of
 * the portal to render as a sitemap parameter named "portal-name".
 *
 * @version $Id$
 */
public class PortalGenerator 
extends ServiceableGenerator {

    /** The portal service. */
    protected PortalService portalService;

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.portalService);
            this.portalService = null;
        }
        super.dispose();
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        super.service(aManager);
        this.portalService = (PortalService)this.manager.lookup(PortalService.class.getName());
    }

    /**
	 * @see org.apache.cocoon.generation.Generator#generate()
	 */
	public void generate()
    throws IOException, SAXException, ProcessingException {
        // start the portal rendering
        // 1. event processing
        // 2. rendering
        final PortalManager pm = this.portalService.getPortalManager();
        try {
            pm.process();
        } catch (PortalException pe) {
            throw new ProcessingException(pe);
        }
        pm.render(this.xmlConsumer, Parameters.toProperties(this.parameters));
	}

    /**
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String src,
                      Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        // This is a fix: if we don't use the link service here, we get
        // in some rare cases a wrong uri!
        this.portalService.getLinkService().getRefreshLinkURI();
    }
}
