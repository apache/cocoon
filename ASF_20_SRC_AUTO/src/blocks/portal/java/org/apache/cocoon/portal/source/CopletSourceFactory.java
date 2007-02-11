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
package org.apache.cocoon.portal.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;

/**
 * The source factory for the coplet sources
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: CopletSourceFactory.java,v 1.6 2004/03/05 13:02:16 bdelacretaz Exp $
 */
public class CopletSourceFactory     
    extends AbstractLogEnabled
    implements SourceFactory, Serviceable, ThreadSafe {

    protected ServiceManager manager;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
    }

	/**
	 * @see org.apache.excalibur.source.SourceFactory#getSource(String, Map)
	 */
	public Source getSource(String location, Map parameters)
		throws MalformedURLException, IOException {
        
        String uri = location;
        String protocol = null;
        
        // remove the protocol
        int position = location.indexOf(':') + 1;
        if (position != 0) {
            protocol = location.substring(0, position);
            location = location.substring(position+2);
        }
        PortalService service = null;
        CopletInstanceData coplet = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            coplet = service.getComponentManager().getProfileManager().getCopletInstanceData(location);
            CopletSource copletSource =
                new CopletSource(uri, protocol,
                                 coplet);
            copletSource.service(this.manager);
            return copletSource;
        } catch (ServiceException ce) {
            throw new SourceException("Unable to lookup profile manager.", ce);
        } finally {
            this.manager.release(service);
        }
	}

    /**
     * @see org.apache.excalibur.source.SourceFactory#release(Source)
     */
    public void release(Source source) {
    }

}
