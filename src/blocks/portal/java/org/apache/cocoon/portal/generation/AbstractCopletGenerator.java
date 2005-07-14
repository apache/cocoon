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

import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.xml.sax.SAXException;

/**
 * Abstract generator implementation that provides a method getCopletInstanceData().
 * There are two possibilities how the generator obtains the information required for 
 * getting the coplet instance data:<br><br>
 * 1) If it is used within a coplet pipeline and this pipeline is called using the "cocoon:" protocol,
 * all required information are passed automatically.<br>
 * 2) Otherwise the portal name and the coplet id must be passed to the generator 
 * as paremeters in the following way:
 *
 * <pre>&lt;map:generator type="coplet"&gt;
 * 	&lt;map:parameter name="portalName" type="exampleportal"/&gt;
 * 	&lt;map:parameter name="copletId" type="examplecoplet"/&gt;
 * &lt;/map:generator&gt;</pre>
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: AbstractCopletTransformer.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public abstract class AbstractCopletGenerator 
extends ServiceableGenerator {

    /**
     * Parameter name.
     */
    public static final String COPLET_ID_PARAM = "copletId";

    /**
     * Parameter name.
     */
    public static final String PORTAL_NAME_PARAM = "portalName";

    /** The portal service. @since 2.1.8 */
    protected PortalService portalService;
    
    /**
     * Try to get the coplet instance data belonging to the current request
     * @return The coplet instance data
     * @throws SAXException If an errors occurs or the instance data is not available
     */
    protected CopletInstanceData getCopletInstanceData() 
    throws SAXException {
        CopletInstanceData cid = this.getCopletInstanceData(null);
        if ( cid == null ) {
            throw new SAXException("Could not find coplet instance data for the current pipeline.");
        }
        return cid;
    }
    
    
    /**
     * Get the portal service
     */
    protected PortalService getPortalService() {
        return this.portalService;
    }
    
    
    /**
     * Try to get the coplet instance data with the given id
     * @param copletId  The id of the coplet instance or null if this transformer
     *                   is used inside a coplet pipeline
     * @return The coplet instance data or null
     * @throws SAXException If an error occurs
     */
    protected CopletInstanceData getCopletInstanceData(String copletId) 
    throws SAXException {
        final Map context = (Map)objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        if ( copletId == null ) {
            // determine coplet id
            if (context != null) {
                copletId = (String)context.get(Constants.COPLET_ID_KEY);
            } else {
                copletId = (String)objectModel.get(Constants.COPLET_ID_KEY);
                if ( copletId == null ) {
                    try {
                        copletId = this.parameters.getParameter(COPLET_ID_PARAM);
                            
                    } catch (ParameterException e) {
                        throw new SAXException("copletId must be passed as parameter or in the object model within the parent context.");
                    }
                }
            }
        }
        if (copletId == null) {
            throw new SAXException("copletId must be passed as parameter or in the object model within the parent context.");
        }

        CopletInstanceData object = this.getPortalService().getComponentManager().getProfileManager().getCopletInstanceData( copletId );
            
        return object;
    }

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
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.portalService = (PortalService)this.manager.lookup(PortalService.ROLE);
    }
}
