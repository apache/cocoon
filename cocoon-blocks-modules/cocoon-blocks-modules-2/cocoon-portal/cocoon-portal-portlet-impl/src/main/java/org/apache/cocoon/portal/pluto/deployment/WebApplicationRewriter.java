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

import java.util.Arrays;
import java.util.List;

import org.apache.cocoon.portal.deployment.DeploymentException;
import org.apache.cocoon.portal.pluto.servlet.PortletServlet;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utilities for manipulating the web.xml deployment descriptor
 *
 * @version $Id$
 */
public class WebApplicationRewriter {

    public static final String CONTAINER = "CocoonContainer";
    public static final String SERVLET_XPATH = "/web-app/servlet/servlet-name[contains(child::text(), \"CocoonContainer\")]";
    public static final String SERVLET_MAPPING_XPATH = "/web-app/servlet-mapping/servlet-name[contains(child::text(), \"CocoonContainer\")]";
    public static final String PORTLET_TAGLIB_XPATH = "/web-app/taglib/taglib-uri[contains(child::text(), \"http://java.sun.com/portlet\")]";
    protected static final String WEB_XML_PATH = "WEB-INF/web.xml";

    protected static final String[] ELEMENTS_BEFORE_SERVLET = new String[]{"icon", "display-name", "description",
            "distributable", "context-param", "filter", "filter-mapping", "listener", "servlet"};
    protected static final String[] ELEMENTS_BEFORE_SERVLET_MAPPING = new String[]{"icon", "display-name",
            "description", "distributable", "context-param", "filter", "filter-mapping", "listener", "servlet",
            "servlet-mapping"};

    protected static final String[] ELEMENTS_BEFORE_TAGLIB_MAPPING = new String[]{"icon", "display-name",
            "description", "distributable", "context-param", "filter", "filter-mapping", "listener", "servlet",
            "servlet-mapping", "session-config", "mime-mapping", "welcome-file-list", "error-page", "taglib"};

    protected Document document;
    protected boolean changed = false;
    protected boolean portletTaglibAdded = false;

    public WebApplicationRewriter(Document doc) {
        this.document = doc;
    }

    /**
     * Infuses this PortletApplicationWar's web.xml file with
     * <code>servlet</code> and a <code>servlet-mapping</code> element for
     * the CocoonContainer servlet. This is only done if the descriptor does
     * not already contain these items.
     */
    public boolean processWebXML()
    throws DeploymentException {
        try {
            Element root = this.document.getDocumentElement();
        
            Node servlet = XPathAPI.selectSingleNode(this.document, SERVLET_XPATH);
            Node servletMapping = XPathAPI.selectSingleNode(this.document, SERVLET_MAPPING_XPATH);
            Node portletTaglib = XPathAPI.selectSingleNode(document, PORTLET_TAGLIB_XPATH);
            
            if (!this.document.hasChildNodes()) {
                root = document.createElement("web-app");
                document.appendChild(root);
            }
        
            if (servlet == null) {
                Element servletElement = this.document.createElement("servlet");

                Element servletName = this.document.createElement("servlet-name");
                servletName.appendChild(document.createTextNode(CONTAINER));

                Element servletDspName = this.document.createElement("display-name");
                servletDspName.appendChild(document.createTextNode("Cocoon Container"));

                Element servletDesc = this.document.createElement("description");
                servletDesc.appendChild(this.document.createTextNode("Servlet for Cocoon Portal Container"));

                Element servletClass = this.document.createElement("servlet-class");
                servletClass.appendChild(this.document.createTextNode(PortletServlet.class.getName()));

                servletElement.appendChild(servletName);
                servletElement.appendChild(servletDspName);
                servletElement.appendChild(servletDesc);
                servletElement.appendChild(servletClass);
                this.insertElementCorrectly(root, servletElement, ELEMENTS_BEFORE_SERVLET);
                this.changed = true;
            }
    
            if (servletMapping == null) {
                Element servletMappingElement = this.document.createElement("servlet-mapping");

                Element servletMapName = this.document.createElement("servlet-name");
                servletMapName.appendChild(this.document.createTextNode(CONTAINER));

                Element servletUrlPattern = this.document.createElement("url-pattern");
                servletUrlPattern.appendChild(this.document.createTextNode("/"+CONTAINER+"/*"));

                servletMappingElement.appendChild(servletMapName);
                servletMappingElement.appendChild(servletUrlPattern);

                this.insertElementCorrectly(root, servletMappingElement, ELEMENTS_BEFORE_SERVLET_MAPPING);
                this.changed = true;
            }
            
            if (portletTaglib == null) {
                Element taglib = document.createElement("taglib");
                Element taguri = document.createElement("taglib-uri");
                taguri.appendChild(this.document.createTextNode("http://java.sun.com/portlet"));

                Element taglocation = document.createElement("taglib-location");
                taglocation.appendChild(this.document.createTextNode("/WEB-INF/tld/portlet.tld"));

                taglib.appendChild(taguri);
                taglib.appendChild(taglocation);

                this.insertElementCorrectly(root, taglib, ELEMENTS_BEFORE_TAGLIB_MAPPING);
                this.changed = true;
                this.portletTaglibAdded = true;
            }
        } catch (Exception e) {
            throw new DeploymentException("Unable to process web.xml for infusion " + e.toString(), e);
        }
        return this.changed;
    }

    public boolean isPortletTaglibAdded() {
        return this.portletTaglibAdded;
    }

    /**
     * Insert an element at the correct position in the dom.
     * @param root element representing the &lt; web-app &gt;
     * @param toInsert element to insert into the web.xml hierarchy.
     * @param elementsBefore
     *            an array of web.xml elements that should be defined before the
     *            element we want to insert. This order should be the order
     *            defined by the web.xml's DTD type definition.
     */
    protected void insertElementCorrectly(Element root,
                                          Element toInsert,
                                          String[] elementsBefore)
    throws Exception {
        NodeList allChildren = root.getChildNodes();
        List elementsBeforeList = Arrays.asList(elementsBefore);

        int insertAfter = -1;
        int count = 0;
        for (int i = 0; i < allChildren.getLength(); i++) {
            Node element = allChildren.item(i);
            if ( element.getNodeType() == Node.ELEMENT_NODE
                 && elementsBeforeList.contains(element.getNodeName())) {
                // determine the Content index of the element to insert after
                insertAfter = i;
            }
            count++;
        }
        insertAfter++;
        if ( insertAfter == allChildren.getLength() ) {
            root.appendChild(toInsert);
        } else {
            root.insertBefore(toInsert, allChildren.item(insertAfter));                
        }
    }
}
