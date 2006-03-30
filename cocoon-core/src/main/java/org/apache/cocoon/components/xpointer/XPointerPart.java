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

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

/**
 * Partly implementation of the xpointer() scheme. Only the XPath subset of xpointer is supported.
 */
public class XPointerPart implements PointerPart {
    private String expression;

    public XPointerPart(String expression) {
        this.expression = expression;
    }

    public boolean process(XPointerContext xpointerContext) throws SAXException, ResourceNotFoundException {
        Document document = xpointerContext.getDocument();
        ServiceManager manager = xpointerContext.getServiceManager();
        XPathProcessor xpathProcessor = null;
        try {
            try {
                xpathProcessor = (XPathProcessor)manager.lookup(XPathProcessor.ROLE);
            } catch (Exception e) {
                throw new SAXException("XPointerPart: error looking up XPathProcessor.", e);
            }
            NodeList nodeList = xpathProcessor.selectNodeList(document, expression, xpointerContext);
            if (nodeList.getLength() > 0) {
                XMLConsumer consumer = xpointerContext.getXmlConsumer();
                LocatorImpl locator = new LocatorImpl();
                locator.setSystemId(xpointerContext.getSource().getURI());
                consumer.setDocumentLocator(locator);
                for (int i = 0; i < nodeList.getLength(); i++) {
                    DOMStreamer streamer = new DOMStreamer();
                    streamer.setConsumer(consumer);
                    streamer.stream(nodeList.item(i));
                }
                return true;
            } else {
                if (xpointerContext.getLogger().isDebugEnabled())
                    xpointerContext.getLogger().debug("XPointer: expression \"" + expression + "\" gave no results.");
                return false;
            }
        } finally {
            if (xpathProcessor != null)
                manager.release(xpathProcessor);
        }
    }
}
