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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.source.SourceInspector;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.excalibur.xml.dom.DOMParser;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This source inspector inspects XML files with a xpath expression.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: XPathSourceInspector.java,v 1.7 2004/03/05 13:02:21 bdelacretaz Exp $
 */
public class XPathSourceInspector extends AbstractLogEnabled implements 
    SourceInspector, Serviceable, Parameterizable, ThreadSafe {

    /**
     * The default namespace uri of the property exposed by this SourceInspector.
     * <p>
     * The value is <code>http://apache.org/cocoon/inspector/xpath/1.0</code>.
     * </p>
     */
    public static final String DEFAULT_PROPERTY_NS = "http://apache.org/cocoon/inspector/xpath/1.0";
        
    /**
     * The default property name exposed by this SourceInspector.
     * <p>
     * The value is <code>result</code> .
     * </p>
     */
    public static final String DEFAULT_PROPERTY_NAME = "result";
    
    private static final SourceValidity VALIDITY = new NOPValidity();
    
    private String propertynamespace;
    private String propertyname;
    private String extension;
    private String xpath;

    private ServiceManager manager = null;

    public void service(ServiceManager manager) {
        this.manager = manager;
    }
    
    public void parameterize(Parameters params)  {
        this.propertynamespace = params.getParameter("namespace", DEFAULT_PROPERTY_NS);
        this.propertyname = params.getParameter("name", DEFAULT_PROPERTY_NAME);
        this.extension = params.getParameter("extension", ".xml");
        this.xpath = params.getParameter("xpath", "/*");
    }
    
    public SourceProperty getSourceProperty(Source source, String namespace, String name) 
        throws SourceException {

        if ((namespace.equals(propertynamespace)) && (name.equals(propertyname)) && 
            (source.getURI().endsWith(extension))) {

            DOMParser parser = null;
            Document doc = null;
            try { 
                parser = (DOMParser)manager.lookup(DOMParser.ROLE);

                doc = parser.parseDocument(new InputSource(source.getInputStream()));
            } catch (SAXException se) {
                this.getLogger().error(source.getURI()
                                        + " is not a valid XML file");
            } catch (IOException ioe) {
                this.getLogger().error("Could not read file", ioe);
            } catch (ServiceException ce) {
                this.getLogger().error("Could not retrieve component", ce);
            } finally {
                if (parser != null) {
                    this.manager.release(parser);
                }
            }

            if (doc != null) {

                XPathProcessor processor = null;
                try {
                    processor = (XPathProcessor)manager.lookup(XPathProcessor.ROLE);

                    NodeList nodelist = processor.selectNodeList(doc.getDocumentElement(), this.xpath);

                    SourceProperty property = new SourceProperty(this.propertynamespace, this.propertyname);
                    property.setValue(nodelist);

                    return property;
                } catch (ServiceException se) {
                    this.getLogger().error("Could not retrieve component", se);
                } finally {
                    if (processor != null) {
                        this.manager.release(processor);
                    }
                }
            }
        } 
        return null;  
    }

    public SourceProperty[] getSourceProperties(Source source) throws SourceException {

        SourceProperty property = getSourceProperty(source, this.propertynamespace, this.propertyname);
        if (property!=null)
            return new SourceProperty[]{property};
        return null;
    }
    
    public boolean handlesProperty(String namespace, String name) {
        return this.propertynamespace.equals(namespace) && this.propertyname.equals(name);
    }

    /**
     * Returns NOPValidity
     */
    public SourceValidity getValidity(Source source) {
        return VALIDITY;
    }
    
}

