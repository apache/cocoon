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
import org.apache.avalon.framework.parameters.ParameterException;
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
 * @version CVS $Id: XPathSourceInspector.java,v 1.8 2004/03/27 21:49:09 unico Exp $
 */
public class XPathSourceInspector extends AbstractLogEnabled 
implements SourceInspector, Serviceable, Parameterizable, ThreadSafe {

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
    
    private String m_namespace;
    private String m_propertyname;
    private String m_extension;
    private String m_xpath;
    
    private ServiceManager manager = null;
    
    public XPathSourceInspector() {
    }
    
    public void service(ServiceManager manager) {
        this.manager = manager;
    }
    
    public void parameterize(Parameters params) throws ParameterException {
        this.m_namespace = params.getParameter("namespace", DEFAULT_PROPERTY_NS);
        this.m_propertyname = params.getParameter("name", DEFAULT_PROPERTY_NAME);
        this.m_extension = params.getParameter("extension", ".xml");
        this.m_xpath = params.getParameter("xpath", "/*");
    }
    
    public SourceProperty getSourceProperty(Source source, String namespace, String name) 
    throws SourceException {

        if ((namespace.equals(m_namespace)) && 
                (name.equals(m_propertyname)) && 
                (source.getURI().endsWith(m_extension))) {

            DOMParser parser = null;
            Document doc = null;
            try { 
                parser = (DOMParser) manager.lookup(DOMParser.ROLE);
                doc = parser.parseDocument(new InputSource(source.getInputStream()));
            } catch (SAXException se) {
                this.getLogger().error(source.getURI() + " is not a valid XML file");
            } catch (IOException ioe) {
                this.getLogger().error("Could not read file", ioe);
            } catch (ServiceException ce) {
                this.getLogger().error("Missing service dependency: DOMParser", ce);
            } finally {
                if (parser != null) {
                    this.manager.release(parser);
                }
            }

            if (doc != null) {
                XPathProcessor processor = null;
                try {
                    processor = (XPathProcessor)manager.lookup(XPathProcessor.ROLE);
                    NodeList nodelist = processor.selectNodeList(doc.getDocumentElement(), m_xpath);
                    SourceProperty property = new SourceProperty(m_namespace, m_propertyname);
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
        SourceProperty property = getSourceProperty(source, this.m_namespace, this.m_propertyname);
        if (property!=null)
            return new SourceProperty[]{property};
        return null;
    }
    
    public boolean handlesProperty(String namespace, String name) {
        return this.m_namespace.equals(namespace) && this.m_propertyname.equals(name);
    }

    /**
     * Returns NOPValidity
     */
    public SourceValidity getValidity(Source source) {
        return NOPValidity.SHARED_INSTANCE;
    }
    
}

