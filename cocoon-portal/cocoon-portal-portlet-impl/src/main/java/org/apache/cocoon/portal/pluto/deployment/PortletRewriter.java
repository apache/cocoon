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
package org.apache.cocoon.portal.pluto.deployment;

import javax.xml.transform.TransformerException;

import org.apache.cocoon.portal.bridges.ServletContextProviderImpl;
import org.apache.cocoon.portal.deployment.DeploymentException;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utilities for manipulating the portlet.xml deployment descriptor.
 *
 * @version $Id$
 */
public class PortletRewriter {

    /**
     * Check if the portlet is using the portals bridges project and add
     * information about Cocoon servlet context provider. Currently only
     * the Struts bridge is supported.
     */
    public static boolean process(Document document)
    throws DeploymentException {
        boolean changed = false;
        final Element root = document.getDocumentElement();
        final NodeList nodes = root.getChildNodes();
        try {
            for(int i=0; i<nodes.getLength(); i++) {
                final Node current = nodes.item(i);
                if ( current.getNodeType() == Node.ELEMENT_NODE
                     && ("portlet".equals(current.getLocalName()) || "portlet".equals(current.getNodeName()))) {
                    final Element e = (Element)current;
                    final Element classElement = (Element)XPathAPI.selectSingleNode(e, "*[local-name()='portlet-class']");
                    if ( classElement != null ) {
                        changed = true;
                        final String className = DOMUtil.getValueOfNode(classElement);
                        if ( className.equals("org.apache.portals.bridges.struts.StrutsPortlet") ) {
                            Element initParamName = (Element)XPathAPI.selectSingleNode(e, "*[local-name()='init-param']/*[local-name()='name' and contains(child::text(), \"ServletContextProvider\")]");
                            Element initParam;
                            if ( initParamName == null ) {
                                initParam = document.createElementNS(e.getNamespaceURI(), "init-param");
                                Element name = document.createElementNS(e.getNamespaceURI(), "name");
                                initParam.appendChild(name);
                                name.appendChild(document.createTextNode("ServletContextProvider"));
                                e.insertBefore(initParam, e.getFirstChild());
                            } else {
                                initParam = (Element)initParamName.getParentNode();
                            }
                            Element value = (Element)XPathAPI.selectSingleNode(initParam, "*[local-name()='value']");
                            if ( value == null ) {
                                value = document.createElementNS(e.getNamespaceURI(), "value");
                                initParam.appendChild(value);
                            } else {
                                while ( value.hasChildNodes() ) {
                                    value.removeChild(value.getFirstChild());
                                }
                            }
                            value.appendChild(document.createTextNode(ServletContextProviderImpl.class.getName()));
                        }
                    }
                }
            }
        } catch (TransformerException te) {
            throw new DeploymentException("Unable to process portlet.xml.", te);
        }
        return changed;
    }

}
