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
package org.apache.cocoon.webapps.portal.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.webapps.portal.PortalConstants;
import org.apache.cocoon.webapps.portal.context.SessionContextImpl;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.dom.DOMUtil;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.apache.excalibur.xml.xslt.XSLTProcessor;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * This is the thread for loading one coplet in the background.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: CopletThread.java,v 1.7 2004/03/19 14:16:54 cziegeler Exp $
*/
public final class CopletThread implements Runnable {

    private Logger           logger;
    private String           copletID;
    private Map              objectModel;
    private Object[]         loadedCoplet;
    private ComponentManager  manager;
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
                XSLTProcessor xslt = null;
                ArrayList transformers = null;
                ArrayList sources = null;
                Request request = ObjectModelHelper.getRequest(this.objectModel);
                XMLConsumer stylesheet =null;
                
                try {
                    if (transformations != null && transformations.getLength() > 0) {
                        transformers = new ArrayList();
                        sources = new ArrayList();
                        
                        nextConsumer = new IncludeXMLConsumer(nextConsumer);
                        for(int k = transformations.getLength()-1; k >=0; k--) {
                            xslt = (XSLTProcessor)this.manager.lookup(XSLTProcessor.ROLE);
                            transformers.add(xslt);
                            Source source = this.resolver.resolveURI(DOMUtil.getValueOfNode(transformations.item(k)));
                            sources.add(source);
                            TransformerHandler handler = xslt.getTransformerHandler(source);

                            final SAXResult result = new SAXResult(nextConsumer);
                            result.setLexicalHandler(nextConsumer);
                            handler.setResult(result);
                            nextConsumer = new ContentHandlerWrapper(handler, handler);
                            stylesheet = nextConsumer;
                        }
                        stylesheet.startDocument();
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
                    if ( stylesheet != null ) {
                        stylesheet.endDocument();
                    }
                } finally {
                    SessionContextImpl.copletInfo.set(null);
                    if ( transformers != null ) {
                        for(int i=0; i < transformers.size(); i++) {
                            this.manager.release( (Component)transformers.get(i));
                            this.resolver.release( (Source)sources.get(i));
                        }
                    }
                }
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
