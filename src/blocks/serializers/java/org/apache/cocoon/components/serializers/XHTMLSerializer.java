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
package org.apache.cocoon.components.serializers;

import org.apache.cocoon.components.serializers.util.DocType;
import org.xml.sax.SAXException;


/**
 *
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: XHTMLSerializer.java,v 1.1 2004/04/21 09:33:22 pier Exp $
 */
public class XHTMLSerializer extends XMLSerializer {

    /** The namespace URI for XHTML 1.0. */
    public static final String XHTML1_NAMESPACE =
            "http://www.w3.org/1999/xhtml";

    /** A representation of the XHTML 1.0 strict document type. */
    public static final DocType XHTML1_DOCTYPE_STRICT = new DocType(
            "html", "-//W3C//DTD XHTML 1.0 Strict//EN",
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");

    /** A representation of the XHTML 1.0 transitional document type. */
    public static final DocType XHTML1_DOCTYPE_TRANSITIONAL = new DocType(
            "html", "-//W3C//DTD XHTML 1.0 Transitional//EN",
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");

    /** A representation of the XHTML 1.0 frameset document type. */
    public static final DocType XHTML1_DOCTYPE_FRAMESET = new DocType(
            "html", "-//W3C//DTD XHTML 1.0 Frameset//EN",
            "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd");

    /* ====================================================================== */

    /**
     * Create a new instance of this <code>XHTMLSerializer</code>
     */
    public XHTMLSerializer() {
        super();
    }

    /**
     * Return the MIME Content-Type produced by this serializer.
     */
    public String getMimeType() {
        if (this.charset == null) return("text/html");
        return("text/html; charset=" + this.charset.getName());
    }

    /* ====================================================================== */

    /**
     * Receive notification of the beginning of the document body.
     *
     * @param uri The namespace URI of the root element.
     * @param local The local name of the root element.
     * @param qual The fully-qualified name of the root element.
     */
    public void body(String uri, String local, String qual)
    throws SAXException {
        if (this.doctype == null) this.doctype = XHTML1_DOCTYPE_TRANSITIONAL;
        if (this.namespaces.getUri("").length() == 0) {
            this.namespaces.push("", XHTML1_NAMESPACE);
        }
        super.body(uri, local, qual);
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The namespace URI of the root element.
     * @param local The local name of the root element.
     * @param qual The fully-qualified name of the root element.
     * @param namespaces An array of <code>String</code> objects containing
     *                   the namespaces to be declared by this element.
     * @param namespaces An array of <code>String</code> objects containing
     *                   all attributes of this element.
     */
    public void startElementImpl(String uri, String local, String qual,
                                 String namespaces[][], String attributes[][])
    throws SAXException {
        if (uri.length() == 0) uri = XHTML1_NAMESPACE;
        super.startElementImpl(uri, local, qual, namespaces, attributes);
    }

    /**
     * Receive notification of the end of an element.
     *
     * @param uri The namespace URI of the root element.
     * @param local The local name of the root element.
     * @param qual The fully-qualified name of the root element.
     */
    public void endElementImpl(String uri, String local, String qual)
    throws SAXException {
        if (uri.length() == 0) uri = XHTML1_NAMESPACE;

        if (XHTML1_NAMESPACE.equals(uri)) {
            if ((local.equalsIgnoreCase("textarea")) ||
                (local.equalsIgnoreCase("script")) ||
                (local.equalsIgnoreCase("style"))) {
                this.closeElement(false);
            } else if (local.equalsIgnoreCase("head")) {
                String loc = "meta";
                String pre = namespaces.getPrefix(XHTML1_NAMESPACE);
                String qua = namespaces.qualify(XHTML1_NAMESPACE, loc, "meta");
                String nsp[][] = new String[0][0];
                String att[][] = new String[2][ATTRIBUTE_LENGTH];

                att[0][ATTRIBUTE_NSURI] = att[1][ATTRIBUTE_NSURI] = "";
                att[0][ATTRIBUTE_LOCAL] = att[0][ATTRIBUTE_QNAME] = "http-equiv";
                att[1][ATTRIBUTE_LOCAL] = att[1][ATTRIBUTE_QNAME] = "content";
                att[0][ATTRIBUTE_VALUE] = "Content-Type";
                att[1][ATTRIBUTE_VALUE] = this.getMimeType();

                this.closeElement(false);
                this.startElementImpl(XHTML1_NAMESPACE, loc, qua, nsp, att);
                this.endElementImpl(XHTML1_NAMESPACE, loc, qua);
            }
        }
        super.endElementImpl(uri, local, qual);
    }
}
