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
package org.apache.cocoon.portal.tools.transformation;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.portal.tools.PortalToolManager;
import org.apache.cocoon.portal.tools.service.UserRightsService;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @version $Id$
 */
public class CheckAccessTransformer extends AbstractSAXTransformer {

    public static final String RIGHTS_NAMESPACE_URI =
        "http://apache.org/cocoon/portal/tools/rights/1.0";

    public static final String ACCESS_TAG = "access";
    public static final String RIGHT_ID = "id";

    protected UserRightsService urs;
    protected PortalToolManager ptm;

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager avaloManager) throws ServiceException {
        super.service(avaloManager);
        this.ptm = (PortalToolManager) this.manager.lookup(PortalToolManager.ROLE);
        this.urs = ptm.getUserRightsService();
    }

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String name, String raw, Attributes attr)
    throws SAXException {
        if (RIGHTS_NAMESPACE_URI.equals(uri) && ACCESS_TAG.equals(name)) {
            String id = attr.getValue(RIGHT_ID);
            if(!urs.userIsAllowed(id, this.ptm.getPortalObjects().getPortalService().getUserService().getUser())) {
                this.stack.push(Boolean.FALSE);
            }
            this.startRecording();
        } else {
            super.startElement(uri, name, raw, attr);
        }
    }

    /**
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

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.ptm);
            this.ptm = null;
            this.urs = null;
        }
        super.dispose();
    }
}
