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
package org.apache.cocoon.components.xpointer;

import org.w3c.dom.Document;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.xpath.PrefixResolver;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.component.ComponentManager;
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
    private ComponentManager componentManager;

    /**
     * Constructs an XPointerContext object.
     *
     * @param xpointer the original fragment identifier string, used for debugging purposes
     * @param source the source into which the xpointer points
     * @param xmlConsumer the consumer to which the result of the xpointer evaluation should be send
     */
    public XPointerContext(String xpointer, Source source, XMLConsumer xmlConsumer, Logger logger, ComponentManager componentManager) {
        this.source = source;
        this.xmlConsumer = xmlConsumer;
        this.logger = logger;
        this.componentManager = componentManager;
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

    public ComponentManager getComponentManager() {
        return componentManager;
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
