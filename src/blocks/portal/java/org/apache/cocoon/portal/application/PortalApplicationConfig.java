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
package org.apache.cocoon.portal.application;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class holds the configuration of an external portal application.
 * 
 * @author <a href="mailto:gerald.kahrer@rizit.at">Gerald Kahrer</a>
 * 
 * @version CVS $Id: PortalApplicationConfig.java,v 1.5 2004/03/05 13:02:09 bdelacretaz Exp $
 */
public class PortalApplicationConfig {
    /**
     * Holds the configuration data.
     */
    private HashMap data = new HashMap();

    /**
     * The name of the portal application.
     */
    private String name = "";

    /**
     * The id of the portal application.
     */
    private String id = "";

    /**
     * Creates the PortalApplicationConfig object.
     * @param	doc a DOM document that holds all portal application configurations
     * @param	copletId the id of the coplet for which the portal application configuration should
     * 			be created
     */
    public static PortalApplicationConfig create(Document doc, String copletId)
        throws Exception {
        Element app = null;
        NodeList apps = doc.getDocumentElement().getElementsByTagName("application");
        for (int i = 0; i < apps.getLength() && app == null; i++) {
            if (isCoplet((Element) apps.item(i), copletId)) {
                app = (Element) apps.item(i);
            }
        }

        if (app == null)
            throw new Exception(
                "Application with copletId " + copletId + " not found.");

        PortalApplicationConfig cfg = new PortalApplicationConfig();

        setConfiguration(app, cfg);

        return cfg;
    }

    /**
     * Sets the configuration parameters in the PortalApplicationConfig object.
     * @param	application a DOM element, that holds the configuration of one portal application
     * @param	cfg the PortalApplicationConfiguration object, which gets populated with the parameters
     */
    private static void setConfiguration(
        Element application,
        PortalApplicationConfig cfg) {
        cfg.name = application.getAttribute("name");
        cfg.id = application.getAttribute("id");

        NodeList settings = application.getChildNodes();

        for (int i = 0; i < settings.getLength(); i++) {
            Node current = settings.item(i);
            if ( current.getNodeType() == Node.ELEMENT_NODE) {
                cfg.data.put(
                    ((Element) current).getNodeName(),
                    ((Element) current).getNodeValue());
            }
        }

    }
    // returns true, if element has an attribute "copletId", and its
    // value equals the given name
    /**
     * Returns true, if configuration element is the expected one.
     * @param 	elem a DOM element, that holds the configuration of one portal application
     * @param	id the id of the coplet
     */
    private static boolean isCoplet(Element elem, String id) {
        String nameAttr = elem.getAttribute("copletId");
        if (nameAttr != null) {
            if (nameAttr.equals(id))
                return true;
        }

        return false;
    }

    /**
     * Defaultconstructor should not be called from outside.
     */
    private PortalApplicationConfig() {}

    /**
     * Constructor, which builds a PortalApplicationConfig from a given DOM element.
     * @param	app the DOM element, that holds the configuration of one portal application
     */
    protected PortalApplicationConfig(Element app) {
        name = app.getAttribute("name");
        id = app.getAttribute("id");

        NodeList settings = app.getChildNodes();

        for (int i = 0; i < settings.getLength(); i++) {
            Node current = settings.item(i);
            if ( current.getNodeType() == Node.ELEMENT_NODE) {
                NodeList content = current.getChildNodes();
                for (int j = 0; j < content.getLength(); j++) {
                    Node text = content.item(j);
                    if (text.getNodeType() == Node.TEXT_NODE) {
                        data.put(((Element) current).getNodeName(),
                                 ((Text) text).getNodeValue());
                    }
                }
            }
        }
    }

    /**
     * Returns the specified attribute.
     * @param	name the name of the attribute
     * @return	String the value of the attribute
     */
    public String getAttribute(String name) {
        return (String) data.get(name);
    }

    /**
     * Returns the name of the application as String.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the id of the application as String.
     */
    public String getId() {
        return id;
    }
}
