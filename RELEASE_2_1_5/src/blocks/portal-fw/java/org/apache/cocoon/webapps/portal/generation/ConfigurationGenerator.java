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
package org.apache.cocoon.webapps.portal.generation;

import java.io.IOException;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.webapps.portal.components.PortalManager;
import org.xml.sax.SAXException;

/**
 * This generator generates the configuration of the portal
 * for the current user.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: ConfigurationGenerator.java,v 1.4 2004/03/05 13:02:18 bdelacretaz Exp $
*/
public final class ConfigurationGenerator
extends ServiceableGenerator {

    public void generate()
    throws IOException, SAXException, ProcessingException {

        PortalManager portal = null;
        try {
            portal = (PortalManager) this.manager.lookup(PortalManager.ROLE);
            this.xmlConsumer.startDocument();

            Request request = ObjectModelHelper.getRequest(this.objectModel);
            if (request.getSession(false) != null) {
                if (this.source == null
                    || this.source.equals("")
                    || this.source.equals("user")) {
                    portal.showPortal(this.xmlConsumer, true, false);
                } else {
                    portal.showAdminConf(this.xmlConsumer);
                }
            }

            this.xmlConsumer.endDocument();
        } catch (ServiceException ce) {
            throw new ProcessingException("Lookup of portal failed.", ce);
        } finally {
            this.manager.release(portal);
        }
    }

}
