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
package org.apache.cocoon.portal.transformation;

import java.util.Map;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.xml.sax.SAXException;

/**
 * Abstract transformer implementation that provides a method getCopletInstanceData().
 * There are two possibilities how the transformer obtains the information required for 
 * getting the coplet instance data:<br><br>
 * 1) If it is used within a coplet pipeline and this pipeline is called using the "cocoon:" protocol,
 * all required information are passed automatically.<br>
 * 2) Otherwise the portal name and the coplet id must be passed to the transformer 
 * as paremeters in the following way:
 *
 * <pre>&lt;map:transform type="coplet"&gt;
 * 	&lt;map:parameter name="portalName" type="exampleportal"/&gt;
 * 	&lt;map:parameter name="copletId" type="examplecoplet"/&gt;
 * &lt;/map:transform&gt;</pre>
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: AbstractCopletTransformer.java,v 1.8 2004/03/05 13:02:16 bdelacretaz Exp $
 */
public abstract class AbstractCopletTransformer 
extends AbstractSAXTransformer {

    /**
     * Parameter name.
     */
    public static final String COPLET_ID_PARAM = "copletId";

    /**
     * Parameter name.
     */
    public static final String PORTAL_NAME_PARAM = "portalName";

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
     * Try to get the coplet instance data with the given id
     * @param copletId  The id of the coplet instance or null if this transformer
     *                   is used inside a coplet pipeline
     * @return The coplet instance data or null
     * @throws SAXException If an error occurs
     */
    protected CopletInstanceData getCopletInstanceData(String copletId) 
    throws SAXException {
        PortalService portalService = null;
        try {

            portalService = (PortalService)this.manager.lookup(PortalService.ROLE);

            final Map context = (Map)objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
            
            if ( portalService.getPortalName() == null && context == null) {
                // set portal name
                try {
                    portalService.setPortalName(this.parameters.getParameter(PORTAL_NAME_PARAM));
                } catch (ParameterException e) {
                    throw new SAXException("portalName must be passed as parameter or in the object model within the parent context.");
                }
            }
            
            if ( copletId == null ) {
                // determine coplet id
                if (context != null) {
                    copletId = (String)context.get(Constants.COPLET_ID_KEY);
                } else {
                    try {
                        copletId = this.parameters.getParameter(COPLET_ID_PARAM);
                            
                    } catch (ParameterException e) {
                        throw new SAXException("copletId must be passed as parameter or in the object model within the parent context.");
                    }
                }
            }
            if (copletId == null) {
                throw new SAXException("copletId must be passed as parameter or in the object model within the parent context.");
            }


            CopletInstanceData object = portalService.getComponentManager().getProfileManager().getCopletInstanceData( copletId );
                
            return object;
        } catch (ServiceException e) {
            throw new SAXException("Error getting portal service.", e);
        } finally {
            this.manager.release( portalService );
        }
    }
}
