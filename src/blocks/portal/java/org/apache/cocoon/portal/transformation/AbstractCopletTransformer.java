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
package org.apache.cocoon.portal.transformation;

import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.xml.sax.SAXException;

/**
 * Abstract transformer implementation that provides some useful methods and
 * functionality. The portal service is stored in the instance variable
 * {@link #portalService} and can be used.
 * There are some methods to fetch a coplet instance data. {@link #getCopletInstanceData()}
 * tries to get the instance associated with the current request and
 * {@link #getCopletInstanceData(String)} fetches an instance with a given id.
 *
 * If you want to get the coplet instance data associated with the current request,
 * there are three possibilities how the transformer obtains the information required
 * for getting the coplet instance data - or more precisly its id:<br><br>
 * 1) If it is used within a coplet pipeline and this pipeline is called using
 *    the "cocoon:" protocol, all required information is passed automatically.<br>
 * 2) The id can be passed to the transformer as sitemap paremeters in the following way:
 *    <pre>&lt;map:transform type="coplet"&gt;
 * 	    &lt;map:parameter name="copletId" type="examplecoplet"/&gt;
 *    &lt;/map:transform&gt;</pre>
 * 3) Any component can set the id as a string in the object model of the current request.
 *    This is the name of the key to be used: {@link org.apache.cocoon.portal.Constants#COPLET_ID_KEY}.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id$
 */
public abstract class AbstractCopletTransformer 
extends AbstractSAXTransformer {

    /**
     * Parameter name for the coplet id.
     */
    public static final String COPLET_ID_PARAM = "copletId";

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
     * Get the portal service.
     * @deprecated Use directly the instance variable.
     */
    protected PortalService getPortalService()
    throws SAXException {
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

        CopletInstanceData object = this.portalService.getComponentManager().getProfileManager().getCopletInstanceData( copletId );
            
        return object;
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.portalService = (PortalService)this.manager.lookup(PortalService.ROLE);        
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.portalService != null ) {
            this.manager.release( this.portalService );
            this.portalService = null;            
        }
        super.dispose();
    }
}
