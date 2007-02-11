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

import org.apache.cocoon.components.serializers.encoding.HTMLEncoder;
import org.apache.cocoon.components.serializers.util.DocType;
import org.apache.cocoon.components.serializers.util.SGMLDocType;
import org.xml.sax.SAXException;


/**
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: HTMLSerializer.java,v 1.4 2004/04/30 22:57:22 joerg Exp $
 */
public class HTMLSerializer extends XHTMLSerializer {

    /** A cross-browser compatible very simple document type declaration. */
    public static final DocType HTML401_DOCTYPE_COMPATIBLE = new SGMLDocType(
            "HTML", "-//W3C//DTD HTML 4.01 Transitional//EN", null);

    /** A representation of the HTML 4.01 strict document type. */
    public static final DocType HTML401_DOCTYPE_STRICT = new SGMLDocType(
            "HTML", "-//W3C//DTD HTML 4.01//EN",
            "http://www.w3.org/TR/html4/strict.dtd");

    /** A representation of the HTML 4.01 transitional document type. */
    public static final DocType HTML401_DOCTYPE_TRANSITIONAL = new SGMLDocType(
            "HTML", "-//W3C//DTD HTML 4.01 Transitional//EN",
            "http://www.w3.org/TR/html4/loose.dtd");

    /** A representation of the HTML 4.01 frameset document type. */
    public static final DocType HTML401_DOCTYPE_FRAMESET = new SGMLDocType(
            "HTML", "-//W3C//DTD HTML 4.01 Frameset//EN",
            "http://www.w3.org/TR/html4/frameset.dtd");

    /* ====================================================================== */

    private static final HTMLEncoder HTML_ENCODER = new HTMLEncoder();

    /**
     * Create a new instance of this <code>HTMLSerializer</code>
     */
    public HTMLSerializer() {
        super(HTML_ENCODER);
    }

    /* ====================================================================== */

    /** Empty namespaces declaration. */
    private static final String NAMESPACES[][] = new String[0][0];

    /** Check if the URI is allowed by this serializer. */
    private boolean checkNamespace(String nsuri) {
        if (nsuri.length() == 0) return(true);
        if (XHTML1_NAMESPACE.equals(nsuri)) return(true);
        return(false);
    }

    /* ====================================================================== */

    /**
     * Write the XML document header.
     * <p>
     * This method overrides the default <code>XMLSerializer</code>.behaviour.
     * </p>
     */
    public void head()
    throws SAXException {
        // NO NOTHING!
    }

    /**
     * Receive notification of the beginning of the document body.
     *
     * @param nsuri The namespace URI of the root element.
     * @param local The local name of the root element.
     * @param qual The fully-qualified name of the root element.
     */
    public void body(String nsuri, String local, String qual)
    throws SAXException {
        String name = local.toUpperCase();
        if (! this.checkNamespace(nsuri)) {
            throw new SAXException("Unsupported namespace \"" + nsuri + "\" "
                                   + "for HTML root element \"" + qual + "\""
                                   + this.getLocation());
        }

        if (this.doctype == null) {
            this.doctype = HTML401_DOCTYPE_COMPATIBLE;
        } else if (XHTML1_DOCTYPE_STRICT.equals(this.doctype)) {
            this.doctype = HTML401_DOCTYPE_STRICT;
        } else if (XHTML1_DOCTYPE_TRANSITIONAL.equals(this.doctype)) {
            this.doctype = HTML401_DOCTYPE_TRANSITIONAL;
        } else if (XHTML1_DOCTYPE_FRAMESET.equals(this.doctype)) {
            this.doctype = HTML401_DOCTYPE_FRAMESET;
        } else {
            this.doctype = new DocType(this.doctype.getName().toUpperCase(),
                                       this.doctype.getPublicId(),
                                       this.doctype.getSystemId());
        }
        super.body(XHTML1_NAMESPACE, name, name);
    }


    /**
     * Receive notification of the beginning of an element.
     */
    public void startElementImpl(String nsuri, String local, String qual,
                             String namespaces[][], String attributes[][])
    throws SAXException {
        String name = local.toUpperCase();
        if (! this.checkNamespace(nsuri)) {
            throw new SAXException("Unsupported namespace \"" + nsuri + "\" "
                                   + "for HTML element \"" + qual + "\""
                                   + this.getLocation());
        }

        int length = 0;
        for (int x = 0; x < attributes.length; x ++) {
            if (checkNamespace(attributes[x][ATTRIBUTE_NSURI])) length ++;
        }

        String at[][] = new String[length][ATTRIBUTE_LENGTH];
        length = 0;
        for (int x = 0; x < attributes.length; x ++) {
            if (!checkNamespace(attributes[x][ATTRIBUTE_NSURI])) continue;

            String at_name = attributes[x][ATTRIBUTE_LOCAL].toLowerCase();
            at[length][ATTRIBUTE_NSURI] = XHTML1_NAMESPACE;
            at[length][ATTRIBUTE_LOCAL] = at_name;
            at[length][ATTRIBUTE_QNAME] = at_name;
            at[length][ATTRIBUTE_VALUE] = attributes[x][ATTRIBUTE_VALUE];
            length++;
        }

        super.startElementImpl(XHTML1_NAMESPACE, name, name, NAMESPACES, at);
    }

    /**
     * Receive notification of the end of an element.
     */
    public void endElementImpl(String nsuri, String local, String qual)
    throws SAXException {
        this.closeElement(false);

        String name = local.toUpperCase();
        if (! this.checkNamespace(nsuri)) {
            throw new SAXException("Unsupported namespace \"" + nsuri + "\" "
                                   + "for HTML element \"" + qual + "\""
                                   + this.getLocation());
        }

        if (name.equals("AREA")) return;
        if (name.equals("BASE")) return;
        if (name.equals("BASEFONT")) return;
        if (name.equals("BR")) return;
        if (name.equals("COL")) return;
        if (name.equals("FRAME")) return;
        if (name.equals("HR")) return;
        if (name.equals("IMG")) return;
        if (name.equals("INPUT")) return;
        if (name.equals("ISINDEX")) return;
        if (name.equals("LINK")) return;
        if (name.equals("META")) return;
        if (name.equals("PARAM")) return;

        super.endElementImpl(XHTML1_NAMESPACE, name, name);
    }
}
