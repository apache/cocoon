/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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
 * @version CVS $Id: DeliTransformer.java,v 1.3 2004/02/06 22:46:53 joerg Exp $
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
