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
 * @version CVS $Id: PortalApplicationConfig.java,v 1.3 2003/12/07 13:27:55 cziegeler Exp $
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
     * @param	name the id of the coplet
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
