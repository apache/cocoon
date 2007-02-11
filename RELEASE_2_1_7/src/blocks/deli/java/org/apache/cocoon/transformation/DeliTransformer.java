/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.cocoon.transformation;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.components.deli.Deli;

import java.util.Map;
import java.util.HashMap;

/**
 * This Transformer is used to transform this incoming SAX stream using
 * a XSLT stylesheet and have parameters available to the stylesheet
 * augmented by the DELI CC/PP user-agent profile database
 *
 * This transformer extends the default TraxTransformer and thus inherits
 * all the properties and configuration parameters of that transformer. 
 * Please refer to its documentation for more information.
 *
 * @author <a href="mailto:marbut@hplb.hpl.hp.com">Mark H. Butler</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: DeliTransformer.java,v 1.4 2004/03/05 13:01:55 bdelacretaz Exp $
 */
public class DeliTransformer extends TraxTransformer {

    /** The DELI service instance */
    private Deli deli;

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);

        this.deli = (Deli) this.manager.lookup(Deli.ROLE);
    }

    /**
     * Get the parameters for the logicsheet
     */
    protected Map getLogicSheetParameters() {
        Map map = super.getLogicSheetParameters();
        
        if (this.deli != null) {
            try {
                Request request = ObjectModelHelper.getRequest(objectModel);
                if (map == null) {
                    map = new HashMap();
                }

                org.w3c.dom.Document deliCapabilities = this.deli.getUACapabilities(request);
                map.put("deli-capabilities", deliCapabilities);

                String accept = request.getParameter("accept");
                if (accept == null) {
                   accept = request.getHeader("accept");
                }
                
                // add the accept param 
                map.put("accept", accept);
            } catch (Exception e) {
                getLogger().error("Error setting DELI info", e);
            }
        }
        
        this.logicSheetParameters = map;
        return this.logicSheetParameters;
    }
    
    /**
     * Disposable
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.deli);
            this.deli = null;
        }
        super.dispose();
    }    
}
