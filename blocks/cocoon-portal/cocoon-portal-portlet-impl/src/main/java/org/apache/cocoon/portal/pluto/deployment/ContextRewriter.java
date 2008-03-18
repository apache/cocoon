/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.portal.pluto.deployment;

import org.apache.cocoon.portal.deployment.DeploymentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utilities for manipulating the context.xml deployment descriptor.
 *
 * @version $Id$
 */
public class ContextRewriter
{
    private Document document;
    private String portletApplication;
    private boolean changed = false;
    
    public ContextRewriter(Document doc, String portletApplication) {
        this.document = doc;
        this.portletApplication = portletApplication;
    }

    public boolean processContextXML()
    throws DeploymentException {
        if (this.document != null) {
            try {
                // get root Context
                Element root = null;
                if (!document.hasChildNodes())
                {
                    root = document.createElement("Context");
                    document.appendChild(root);
                } else {
                    root = document.getDocumentElement();
                }   
                
                // set Context path
                String pathAttribute = root.getAttribute("path");
                if ((pathAttribute == null) || !pathAttribute.equals("/" + portletApplication)) {
                    root.setAttribute("path", "/" + portletApplication);
                    changed = true;
                }
                
                // set Context docBase
                String docBaseAttribute = root.getAttribute("docBase");
                if ((docBaseAttribute == null) || !docBaseAttribute.equals(portletApplication)) {
                    root.setAttribute("docBase", portletApplication);
                    changed = true;
                }
            } catch (Exception e) {
                throw new DeploymentException("Unable to process context.xml for infusion " + e.toString(), e);
            }
        }
        return changed;
    }
}
