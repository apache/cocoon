/*
$Id: NamespaceHelper.java,v 1.1 2004/02/29 17:34:48 gregor Exp $
<License>

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

 4. The names "Apache Lenya" and  "Apache Software Foundation"  must  not  be
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
 Michael Wechner <michi@apache.org>. For more information on the Apache Soft-
 ware Foundation, please see <http://www.apache.org/>.

 Lenya includes software developed by the Apache Software Foundation, W3C,
 DOM4J Project, BitfluxEditor, Xopus, and WebSHPINX.
</License>
*/
package org.apache.lenya.xml;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;


/**
 * A NamespaceHelper object simplifies the creation of elements in a certain
 * namespace. All elements are owned by a document that is passed to the
 * {@link #NamespaceHelper(Document, String, String)} constructor or created
 * using the {@link #NamespaceHelper(String, String, String)} constructor.
 *
 * @author andreas
 */
public class NamespaceHelper {
    private String namespaceUri;
    private String prefix;
    private Document document;

    /**
     * Creates a new instance of NamespaceHelper using an existing document. The
     * document is not affected. If you omit the prefix, the default namespace is used.
     *
     * @param document The document.
     * @param namespaceUri The namespace URI.
     * @param prefix The namespace prefix.
     */
    public NamespaceHelper(String namespaceUri, String prefix, Document document) {
        this.namespaceUri = namespaceUri;
        this.prefix = prefix;
        this.document = document;
    }

    /**
     * <p>
     * Creates a new instance of NamespaceHelper. A new document is created
     * using a document element in the given namespace with the given prefix.
     * If you omit the prefix, the default namespace is used.
     * </p>
     * <p>
     * NamespaceHelper("http://www.w3.org/2000/svg", "svg", "svg"):<br/>
     * &lt;?xml version="1.0"&gt;<br/>
     * &lt;svg:svg xmlns:svg="http://www.w3.org/2000/svg"&gt;<br/>
     * &lt;/svg:svg&gt;
     * </p>
     *
     * @param localName The local name of the document element.
     * @param namespaceUri The namespace URI.
     * @param prefix The namespace prefix.
     * 
     * @throws ParserConfigurationException if an error occured
     */
    public NamespaceHelper(String namespaceUri, String prefix, String localName)
        throws ParserConfigurationException {
        this.namespaceUri = namespaceUri;
        this.prefix = prefix;
        setDocument(DocumentHelper.createDocument(getNamespaceURI(), getQualifiedName(localName),
                null));
    }

    /**
     * Sets the document of this NamespaceHelper.
     * 
     * @param document the document
     */
    protected void setDocument(Document document) {
        this.document = document;
    }

    /**
     * Returns the document that is used to create elements.
     *
     * @return A document object.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Returns the namespace URI of this NamespaceHelper.
     *
     * @return The namespace URI.
     */
    public String getNamespaceURI() {
        return namespaceUri;
    }

    /**
     * Returns the namespace prefix that is used to create elements.
     *
     * @return The namespace prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the qualified name for a local name using the prefix of this
     * NamespaceHelper.
     *
     * @param localName The local name.
     * @return The qualified name, i.e. prefix:localName.
     */
    public String getQualifiedName(String localName) {
        if (getPrefix().equals("")) {
            return localName;
        } else {
            return getPrefix() + ":" + localName;
        }
    }

    /**
     * <p>
     * Creates an element within the namespace of this NamespaceHelper object with
     * a given local name containing a text node.<br/>
     * </p>
     * <p>
     * <code>createElement("text")</code>: <code>&lt;prefix:text/&gt;<code>.
     * </p>
     *
     * @param localName The local name of the element.
     * @return A new element.
     */
    public Element createElement(String localName) {
        return getDocument().createElementNS(getNamespaceURI(), getQualifiedName(localName));
    }

    /**
     * <p>
     * Creates an element within the namespace of this NamespaceHelper object with
     * a given local name containing a text node.
     * </p>
     * <p>
     * <code>createElement("text", "Hello World!")</code>:
     * <code>&lt;prefix:text&gt;Hello World!&lt;/prefix:text&gt;</code>.
     * </p>
     *
     * @param localName The local name of the element.
     * @param text The text for the text node inside the element.
     * @return A new element containing a text node.
     */
    public Element createElement(String localName, String text) {
        Element element = createElement(localName);
        Text textNode = getDocument().createTextNode(text);
        element.appendChild(textNode);

        return element;
    }

    /**
     * Returns all children of an element in the namespace
     * of this NamespaceHelper.
     *
     * @param element The parent element.
     * 
     * @return the children.
     */
    public Element[] getChildren(Element element) {
        return DocumentHelper.getChildren(element, getNamespaceURI());
    }

    /**
     * Returns all children of an element with a local name in the namespace
     * of this NamespaceHelper.
     *
     * @param element The parent element.
     * @param localName The local name of the children to return.
     * 
     * @return the children.
     */
    public Element[] getChildren(Element element, String localName) {
        return DocumentHelper.getChildren(element, getNamespaceURI(), localName);
    }

    /**
     * Returns the first childr of an element with a local name in the namespace
     * of this NamespaceHelper or <code>null</code> if none exists.
     *
     * @param element The parent element.
     * @param localName The local name of the children to return.
     * 
     * @return the first child.
     */
    public Element getFirstChild(Element element, String localName) {
        return DocumentHelper.getFirstChild(element, getNamespaceURI(), localName);
    }

	/**
	 * Returns the next siblings of an element with a local name in the namespace
	 * of this NamespaceHelper or <code>null</code> if none exists.
	 *
	 * @param element The parent element.
	 * @param localName The local name of the children to return.
	 * 
	 * @return the next siblings.
	 */
	public Element[] getNextSiblings(Element element, String localName) {
		return DocumentHelper.getNextSiblings(element, getNamespaceURI(), localName);
	}
}
