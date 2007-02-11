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
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Factory for building PortalApplicationConfig objects.
 * 
 * @author <a href="mailto:gerald.kahrer@rizit.at">Gerald Kahrer</a>
 * 
 * @version CVS $Id: PortalApplicationConfigFactory.java,v 1.4 2004/03/05 13:02:09 bdelacretaz Exp $
 */
public class PortalApplicationConfigFactory {
    /**
     * The factory instance.
     */
    private static PortalApplicationConfigFactory instance = null;

    /**
     * The container for holding the PortalApplicationConfig objects
     */
    private Map portalAppConfigs = new HashMap();

    /**
     * Cocoon SourceResolver.
     */
    private SourceResolver resolver = null;

    private PortalApplicationConfigFactory() {}

    /**
     * Returns the one and only instance of the factory.
     * @param	resolver the cocoon source resolver, which is used for getting the xml resource file
     * @return	PortalApplicationConfigFactory the instance of the factory
     */
    public static synchronized PortalApplicationConfigFactory getInstance(SourceResolver resolver)
        throws ProcessingException {
        if (instance == null) {
            instance = new PortalApplicationConfigFactory();

            instance.resolver = resolver;

            createConfig();
        }

        return instance;
    }

    /**
     * Method creates a PortalApplicationConfig Object for each configured application
     * and holds these objects in a private container.
     */
    private static void createConfig() throws ProcessingException {
        try {
            NodeList apps = getApplicationList();
            String copletId;
            Element e;
            for (int i = 0; i < apps.getLength(); i++) {
                e = (Element) apps.item(i);
                copletId = e.getAttribute("copletId");
                PortalApplicationConfig cfg = new PortalApplicationConfig(e);

                instance.portalAppConfigs.put(copletId, cfg);
            }
        }
        catch (Exception e) {
            throw new ProcessingException(
                "Could not create PortalApplicationConfiguration",
                e);
        }
    }

    /**
     * Returns a list of all application configurations
     * @return List a list of DOM elements, which hold the portal application config
     */
    private static NodeList getApplicationList() throws Exception {
        Source s =
            instance.resolver.resolveURI(
                "profiles/applications/application-coplet-binding.xml");
        try {
            Document doc = SourceUtil.toDOM(s);
            NodeList apps = doc.getDocumentElement().getElementsByTagName("application");

            return apps;
        } finally {
            instance.resolver.release(s);
        }
    }

    /**
     * Returns the PortalApplicationConfig object for a given coplet.
     * @param	copletId the id of the coplet
     * @return	PortalApplicationConfig the config object
     */
    public PortalApplicationConfig getConfig(String copletId)
        throws ProcessingException {
        PortalApplicationConfig cfg =
            (PortalApplicationConfig) portalAppConfigs.get(copletId);

        if ("???".equals(copletId)) {
            cfg = getSpecialConfig(copletId);
        }
        else {
            cfg = (PortalApplicationConfig) portalAppConfigs.get(copletId);
        }

        if (cfg == null) {
            throw new ProcessingException(
                "No PortalApplicationConfig available for coplet "
                    + copletId
                    + ".");
        }

        return cfg;
    }

    /**
     * Returns the PortalApplicationConfig object for a given coplet. This is a modification of
     * getConfig with the special feature, that the configuration is parsed again before the
     * searched configuration is returned.
     * @param	specialId the id of the coplet
     * @return	PortalApplicationConfig the config object
     */
    private PortalApplicationConfig getSpecialConfig(String specialId)
        throws ProcessingException {
        try {
            NodeList apps = getApplicationList();

            Element e;
            String copletId;
            for (int i = 0; i < apps.getLength(); i++) {
                e = (Element) apps.item(i);
                copletId = e.getAttribute("copletId");

                if (specialId.equals(copletId)) {
                    return new PortalApplicationConfig(e);
                }
            }
        }
        catch (Exception e) {
            throw new ProcessingException("Error while getting configuration for special coplet");
        }

        return null;
    }
}
