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
package org.apache.cocoon.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.resolver.tools.CatalogResolver;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;


/**
 * Various utility methods to work with JAXP.
 *
 * @author <a href="mailto:andreas@apache.org">Andreas Hartmann</a>
 * @version $Id: DocumentHelper.java,v 1.3 2004/03/01 20:54:30 cziegeler Exp $
 */
public class DocumentHelper {
    
    /**
     * Creates a non-validating and namespace-aware DocumentBuilder.
     *
     * @return A new DocumentBuilder object.
     * @throws ParserConfigurationException if an error occurs
     */
    protected static DocumentBuilder createBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();    

		CatalogResolver cr = new CatalogResolver();
		builder.setEntityResolver(cr);
        return builder;
    }

    /**
     * Creates a document. A xmlns:prefix="namespaceUri" attribute is added to
     * the document element.
     *
     * @param namespaceUri The namespace URL of the root element.
     * @param qualifiedName The qualified name of the root element.
     * @param documentType The type of document to be created or null. When doctype is not null,
     *        its Node.ownerDocument attribute is set to the document being created.
     * @return A new Document object.
     * 
     * @throws DOMException if an error occurs
     * @throws ParserConfigurationException if an error occurs
     * 
     * @see org.w3c.dom.DOMImplementation#createDocument(String, String, DocumentType)
     */
    public static Document createDocument(String namespaceUri, String qualifiedName,
        DocumentType documentType) throws DOMException, ParserConfigurationException {
        DocumentBuilder builder = createBuilder();
        Document document = builder.getDOMImplementation().createDocument(namespaceUri,
                qualifiedName, documentType);

        // add xmlns:prefix attribute
        String name = "xmlns";
        int index = qualifiedName.indexOf(":");

        if (index > -1) {
            name += (":" + qualifiedName.substring(0, index));
        }

        document.getDocumentElement().setAttribute(name, namespaceUri);

        return document;
    }

    /**
     * Reads a document from a file.
     * @return A document.
     * @param file The file to load the document from.
     * 
     * @throws ParserConfigurationException if an error occurs
     * @throws SAXException if an error occurs
     * @throws IOException if an error occurs
     */
    public static Document readDocument(File file)
        throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = createBuilder();
        return builder.parse(file);
    }

    /** 
     * Writes a document to a file. A new file is created if it does not exist.
     *
     * @param document The document to save.
     * @param file The file to save the document to.
     * 
     * @throws IOException if an error occurs
     * @throws TransformerConfigurationException if an error occurs
     * @throws TransformerException if an error occurs
     */
    public static void writeDocument(Document document, File file)
        throws TransformerConfigurationException, TransformerException, IOException {
        file.getParentFile().mkdirs();
        file.createNewFile();

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(file);
        getTransformer(document.getDoctype()).transform(source, result);
    }

	/**
	 * Get the tranformer.
	 * 
	 * @param documentType the document type
	 * @return a transformer
	 * 
	 * @throws TransformerConfigurationException if an error occurs
	 */
    protected static Transformer getTransformer(DocumentType documentType)
        throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");

        if (documentType != null) {
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, documentType.getPublicId());
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, documentType.getSystemId());
        }

        return transformer;
    }

    /**
     * Returns all child elements of an element that belong to a certain namespace
     * and have a certain local name.
     * 
     * @param element The parent element.
     * @param namespaceUri The namespace that the childen must belong to.
     * @param localName The local name of the children.
     * 
     * @return The child elements.
     */
    public static Element[] getChildren(Element element, String namespaceUri, String localName) {
        List childElements = new ArrayList();
        NodeList children = element.getElementsByTagNameNS(namespaceUri, localName);

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getParentNode() == element) {
                childElements.add(children.item(i));
            }
        }

        return (Element[]) childElements.toArray(new Element[childElements.size()]);
    }

    /**
     * Returns the text inside an element. Only the child text nodes of this
     * element are collected.
     * @param element The element.
     * @return The text inside the element.
     */
    public static String getSimpleElementText(Element element) {
        StringBuffer buffer = new StringBuffer();
        NodeList children = element.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child instanceof Text) {
                buffer.append(child.getNodeValue());
            }
        }

        return buffer.toString();
    }


}
