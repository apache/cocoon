/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.tools.transformation;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.portal.tools.PortalToolManager;
import org.apache.cocoon.portal.tools.model.User;
import org.apache.cocoon.portal.tools.service.UserrightsService;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * 
 * @version CVS $Id$
 */
public class CheckAccessTransformer extends AbstractSAXTransformer implements Parameterizable, Disposable {

    public static final String RIGHTS_NAMESPACE_URI =
        "http://apache.org/cocoon/portal/tools/rights/1.0";
    public static final String ACCESS_TAG = "access";
    public static final String RIGHT_ID = "id";
    
    private UserrightsService urs;
    private PortalToolManager ptm;
    private User user;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters para) throws ParameterException {
        this.user = new User(para.getParameter("user"), para.getParameter("role"));
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        ptm = (PortalToolManager) this.manager.lookup(PortalToolManager.ROLE);
        urs = ptm.getUserRightsService();
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String name, String raw,
            Attributes attr) throws SAXException {
        if(RIGHTS_NAMESPACE_URI.equals(uri) && ACCESS_TAG.equals(name)) {
            String id = attr.getValue(RIGHT_ID);
            if(!urs.userIsAllowed(id, user)) {
                this.stack.push(new Boolean(false));
            } 
            this.startRecording();
        } else {
            super.startElement(uri, name, raw, attr);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String name, String raw)
            throws SAXException {
        if(RIGHTS_NAMESPACE_URI.equals(uri) && ACCESS_TAG.equals(name)) {
            DocumentFragment frag = this.endRecording();
            Boolean ignore = (Boolean) this.stack.pop();
            if(!ignore.booleanValue())
                IncludeXMLConsumer.includeNode(frag, this.contentHandler, this.lexicalHandler);
        } else {
            super.endElement(uri, name, raw);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(ptm);
            ptm = null;
            this.manager = null;
        }
    }

}
