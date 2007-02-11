/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.generation;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.portal.PortalManager;
import org.apache.cocoon.portal.PortalService;
import org.xml.sax.SAXException;

/**
 * This generator renders the complete portal.
 * More precisly, this generator is the starting point for the portal
 * rendering. The generator delegates the rendering process to
 * to {@link PortalManager} component.
 * This generator needs one runtime configuration: the name of
 * the portal to render as a sitemap parameter named "portal-name".
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: PortalGenerator.java,v 1.8 2004/04/28 13:58:16 cziegeler Exp $
 */
public class PortalGenerator 
extends ServiceableGenerator {

	/* (non-Javadoc)
	 * @see org.apache.cocoon.generation.Generator#generate()
	 */
	public void generate()
    throws IOException, SAXException, ProcessingException {
        // start the portal rendering
        // 1. event processing
        // 2. rendering
        PortalManager pm = null;
        try {
            pm = (PortalManager)this.manager.lookup(PortalManager.ROLE);
            pm.process();
            pm.showPortal(this.xmlConsumer, this.parameters);
        } catch (ServiceException ce) {
            throw new ProcessingException("Unable to lookup portal manager.", ce);
        } finally {
            this.manager.release(pm);
        }
	}

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String src,
                      Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        
        // instantiate the portal service for this request
        // and set the portal-name
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            service.setPortalName(par.getParameter("portal-name"));
            
            // This is a fix: if we don't use the link service here, we get
            // in some rare cases a wrong uri!
            service.getComponentManager().getLinkService().getRefreshLinkURI();
            
        } catch (ParameterException pe) {
            throw new ProcessingException("Parameter portal-name is required.");
        } catch (ServiceException ce) {
            throw new ProcessingException("Unable to lookup portal service.", ce);
        } finally {
            this.manager.release(service);
        }
    }

}
