/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.webapps.portal.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.webapps.portal.PortalConstants;
import org.apache.cocoon.webapps.portal.context.SessionContextImpl;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.dom.DOMUtil;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.xml.xpath.XPathProcessor;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * This is the thread for loading one coplet in the background.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: CopletThread.java,v 1.4 2003/12/18 14:29:03 cziegeler Exp $
*/
public final class CopletThread implements Runnable {

    private Logger           logger;
    private String           copletID;
    private Map              objectModel;
    private Object[]         loadedCoplet;
    private ComponentManager manager;
    private SourceResolver   resolver;
    private XPathProcessor   processor;
    
    /**
     * Initialise all instance variables.
     * The main information is the loadedCoplet array:
     * 0 : contains the result of the coplet loading, <code>null</code>or
     *     the compiled sax events
     * 1 : The coplet configuration element from the coplet profile
     * 2 : The resource parameters
     * 3 : The coplet element
     * 4 : Current time
     * 5 : The timeout
     * 6 : The thread (this)
     * 7 : The status profile
     */
    public void init(String  copletID,
                     Map     objectModel,
                     Logger  logger,
                     Response response,
                     Object[] loadedCoplet,
                     ComponentManager manager,
                     SourceResolver resolver,
                     XPathProcessor processor) {
        this.copletID = copletID;
        this.objectModel = objectModel;
        this.logger = logger;
        this.loadedCoplet = loadedCoplet;
        this.manager = manager;
        this.resolver = resolver;
        this.processor = processor;
    }

    /**
     * Process one coplet
     */
    public void run() {
        XMLSerializer compiler = null;
        Element copletConf = (Element)this.loadedCoplet[1];
        SourceParameters p = (SourceParameters)loadedCoplet[2];

        try {
            // Determine the resource to load
            // If the coplet is customizable and has no customization info
            // the customization resource is loaded, otherwise the resource
            String resource = null;
            boolean showCustomizePage = p.getParameterAsBoolean(PortalConstants.PARAMETER_CUSTOMIZE, false);
            if (showCustomizePage) {
                final String value = DOMUtil.getValueOf(copletConf, "customization/@uri", (String)null, this.processor);
                if (value == null) {
                    this.logger.error("The coplet '"+this.copletID+"' is customizable but has no customization info.");
                }
                resource = value;
            }
            if (resource == null) {
                resource = DOMUtil.getValueOf(copletConf, "resource/@uri", this.processor);
            }
            boolean handlesSizable = DOMUtil.getValueAsBooleanOf(copletConf, "configuration/handlesSizable", false, this.processor);

            if (!handlesSizable && !p.getParameter("size", "max").equals("max")) {
                // do nothing here
                loadedCoplet[0] = new byte[0];
            } else {

                compiler = (XMLSerializer)this.manager.lookup(XMLSerializer.ROLE);
                compiler.startDocument();

                XMLConsumer nextConsumer = compiler;
                NodeList transformations = DOMUtil.selectNodeList(copletConf,
                                                        "transformation/stylesheet", this.processor);
                Transformer xslT = null;
                ArrayList transformers = new ArrayList();
                ComponentSelector selector = null;
                Request request = ObjectModelHelper.getRequest(this.objectModel);

                try {
                    if (transformations != null && transformations.getLength() > 0) {
                        selector = (ComponentSelector) this.manager.lookup(Transformer.ROLE + "Selector");
                        nextConsumer = new IncludeXMLConsumer(nextConsumer);
                        for(int k = transformations.getLength()-1; k >=0; k--) {
                            xslT = (Transformer)selector.select("xslt");
                            transformers.add(xslT);
                            xslT.setup(resolver,
                                       objectModel,
                                       DOMUtil.getValueOfNode(transformations.item(k)),
                                       new Parameters());
                            xslT.setConsumer(nextConsumer);
                            nextConsumer = xslT;
                        }
                        nextConsumer.startDocument();
                    }
                    boolean includeFragment = true;
                    boolean handlesParameters = DOMUtil.getValueAsBooleanOf(copletConf, "configuration/handlesParameters", true, this.processor);
                    String size = p.getParameter("size", "max");
                    includeFragment = size.equals("max");
                    if (!includeFragment) {
                        if (this.logger.isWarnEnabled()) {
                            this.logger.warn("Minimized coplet '"+copletID+"' not handled correctly.");
                        }
                    }
                    if ( includeFragment ) {
                        if (this.logger.isDebugEnabled() ) {
                            this.logger.debug("portal: Loading coplet " + copletID);
                        }
                        // add the parameters to the request attributes
                        Map info = new HashMap(3);
                        SessionContextImpl.copletInfo.set(info);
                        info.put(PortalConstants.COPLETINFO_PARAMETERS, p);
                        info.put(PortalConstants.COPLETINFO_PORTALURI, request.getRequestURI());
                        info.put(PortalConstants.COPLETINFO_STATUSPROFILE, loadedCoplet[7]);
                        XMLConsumer xc = new IncludeXMLConsumer(nextConsumer);
                        Source source = null;
                        try {
                            source = SourceUtil.getSource(resource, 
                                                          null, 
                                                          (handlesParameters ? p : null), 
                                                          resolver);
                            SourceUtil.toSAX(source, xc);
                        } finally {
                            resolver.release(source);
                        }

                        if (this.logger.isDebugEnabled()) {
                            this.logger.debug("portal: Loaded coplet " + copletID);
                        }
                    }
                        
                    
                    if (xslT != null) {
                        xslT.endDocument();
                        xslT = null;
                    }
                } finally {
                    SessionContextImpl.copletInfo.set(null);
                    if (selector != null) {
                        for(int i=0; i<transformers.size(); i++) {
                            selector.release((Component)transformers.get(i));
                        }
                        this.manager.release(selector);
                    }
                }
                transformers.clear();
                nextConsumer = null;
                compiler.endDocument();
                loadedCoplet[0] = compiler.getSAXFragment();
            }
        } catch (Exception local) {
            // this exception is ignored and an error message is included
            // later on when the coplet is processed
            this.logger.error("Exception during processing of coplet: " + copletID, local);
        } catch (Throwable local) {
            // this exception is ignored and an error message is included
            // later on when the coplet is processed
            this.logger.error("Exception during processing of coplet: " + copletID, local);
        } finally {
            if (compiler != null) {
                this.manager.release(compiler);
            }
        }
        loadedCoplet[6] = null;
        copletID = null;
        copletConf = null;
        this.logger = null;
        objectModel = null;
        p = null;
        loadedCoplet = null;
        manager = null;
        resolver = null;
    } // END run
} // END CLASS
