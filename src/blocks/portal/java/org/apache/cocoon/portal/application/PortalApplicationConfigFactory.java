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
 * @version CVS $Id: PortalApplicationConfigFactory.java,v 1.3 2003/12/23 15:28:32 joerg Exp $
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
