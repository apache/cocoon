/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: AbstractCopletTransformer.java,v 1.5 2003/12/11 15:36:04 cziegeler Exp $
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

    protected CopletInstanceData getCopletInstanceData() 
    throws SAXException {
        return this.getCopletInstanceData(null);
    }
    
    protected CopletInstanceData getCopletInstanceData(String copletId) 
    throws SAXException {
        PortalService portalService = null;
        try {

            portalService = (PortalService)this.manager.lookup(PortalService.ROLE);

            // determine coplet id
            Map context = (Map)objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
            if ( context == null ) {
                // set portal name
                try {
                    portalService.setPortalName(this.parameters.getParameter(PORTAL_NAME_PARAM));
                } catch (ParameterException e) {
                    throw new SAXException("portalName must be passed as parameter or in the object model within the parent context.");
                }
            }
            
            if ( copletId == null ) {
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
                
            if (object == null) {
                throw new SAXException("Could not find coplet instance data for " + copletId);
            }
                
            return object;
        } catch (ServiceException e) {
            throw new SAXException("Error getting portal service.", e);
        } finally {
            this.manager.release( portalService );
        }
    }
}
