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
package org.apache.cocoon.components.xpointer;

import org.w3c.dom.Document;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.xpath.PrefixResolver;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.ResourceNotFoundException;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A context object used during the evaluating of XPointers.
 */
public class XPointerContext implements PrefixResolver {
    private Source source;
    private Document document;
    private XMLConsumer xmlConsumer;
    private Logger logger;
    private String xpointer;
    private HashMap prefixes = new HashMap();
    private ServiceManager manager;

    /**
     * Constructs an XPointerContext object.
     *
     * @param xpointer the original fragment identifier string, used for debugging purposes
     * @param source the source into which the xpointer points
     * @param xmlConsumer the consumer to which the result of the xpointer evaluation should be send
     */
    public XPointerContext(String xpointer, Source source, XMLConsumer xmlConsumer, Logger logger, ServiceManager manager) {
        this.source = source;
        this.xmlConsumer = xmlConsumer;
        this.logger = logger;
        this.manager = manager;
        this.xpointer = xpointer;

        prefixes.put("xml", "http://www.w3.org/XML/1998/namespace");
    }

    public Document getDocument() throws SAXException, ResourceNotFoundException {
        if (document == null) {
            try {
                document = SourceUtil.toDOM(source);
            } catch (ResourceNotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new SAXException("Error during XPointer evaluation while trying to load " + source.getURI(), e);
            }
        }
        return document;
    }

    public Source getSource() {
        return source;
    }

    public XMLConsumer getXmlConsumer() {
        return xmlConsumer;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getXPointer() {
        return xpointer;
    }

    public ServiceManager getServiceManager() {
        return manager;
    }

    public void addPrefix(String prefix, String namespace) throws SAXException {
        // according to the xmlns() scheme spec, these should not result to any change in namespace context
        if (prefix.equalsIgnoreCase("xml"))
            return;
        else if (prefix.equals("xmlns"))
            return;
        else if (namespace.equals("http://www.w3.org/XML/1998/namespace"))
            return;
        else if (namespace.equals("http://www.w3.org/2000/xmlns/"))
            return;

        prefixes.put(prefix, namespace);
    }

    public String prefixToNamespace(String prefix) {
        return (String)prefixes.get(prefix);
    }
}
