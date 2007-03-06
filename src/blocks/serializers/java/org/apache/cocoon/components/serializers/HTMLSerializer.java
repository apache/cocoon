/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.serializers.encoding.HTMLEncoder;
import org.apache.cocoon.components.serializers.util.DocType;
import org.apache.cocoon.components.serializers.util.SGMLDocType;
import org.xml.sax.SAXException;

/**
 * <p>A serializer converting XHTML into plain old HTML.</p>
 *
 * <p>For configuration options of this serializer, please look at the
 * {@link XHTMLSerializer} and {@link EncodingSerializer}.</p>
 *
 * <p>Any of the XHTML document type declared or used will be converted into
 * its HTML 4.01 counterpart, and in addition to those a "compatible" doctype
 * can be supported to exploit a couple of shortcuts into MSIE's rendering
 * engine. The values for the <code>doctype-default</code> can then be:</p>
 *
 * <dl>
 *   <dt>"<code>none</code>"</dt>
 *   <dd>Not to emit any dococument type declaration.</dd>
 *   <dt>"<code>compatible</code>"</dt>
 *   <dd>The HTML 4.01 Transitional (exploiting MSIE shortcut).</dd>
 *   <dt>"<code>strict</code>"</dt>
 *   <dd>The HTML 4.01 Strict document type.</dd>
 *   <dt>"<code>loose</code>"</dt>
 *   <dd>The HTML 4.01 Transitional document type.</dd>
 *   <dt>"<code>frameset</code>"</dt>
 *   <dd>The HTML 4.01 Frameset document type.</dd>
 * </dl>
 *
 * @version CVS $Id$
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

    protected boolean encodeCharacters = true;

    /**
     * Create a new instance of this <code>HTMLSerializer</code>
     */
    public HTMLSerializer() {
        super(HTML_ENCODER);
    }

    /**
     * Configure this instance by selecting the default document type to use.
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        super.configure(conf);

        String doctype = conf.getChild("doctype-default").getValue(null);
        if ("none".equalsIgnoreCase(doctype)) {
            this.doctype_default = null;
        } else if ("compatible".equalsIgnoreCase(doctype)) {
            this.doctype_default = HTML401_DOCTYPE_COMPATIBLE;
        } else if ("strict".equalsIgnoreCase(doctype)) {
            this.doctype_default = HTML401_DOCTYPE_STRICT;
        } else if ("loose".equalsIgnoreCase(doctype)) {
            this.doctype_default = HTML401_DOCTYPE_TRANSITIONAL;
        } else if ("frameset".equalsIgnoreCase(doctype)) {
            this.doctype_default = HTML401_DOCTYPE_FRAMESET;
        } else {
            /* Default is compatible (MSIE hack) */
            this.doctype_default = HTML401_DOCTYPE_COMPATIBLE;
        }
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

        if (this.doctype == null) this.doctype = this.doctype_default;

        if (XHTML1_DOCTYPE_STRICT.equals(this.doctype)) {
            this.doctype = HTML401_DOCTYPE_STRICT;
        } else if (XHTML1_DOCTYPE_TRANSITIONAL.equals(this.doctype)) {
            this.doctype = HTML401_DOCTYPE_TRANSITIONAL;
        } else if (XHTML1_DOCTYPE_FRAMESET.equals(this.doctype)) {
            this.doctype = HTML401_DOCTYPE_FRAMESET;
        } else if (this.doctype != null) {
            /* The root element is uppercase, always!!! */
            this.doctype = new SGMLDocType(this.doctype.getName().toUpperCase(),
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

        // script and style are CDATA sections by default, so no encoding
        if ( "SCRIPT".equals(name) || "STYLE".equals(name) ) {
            this.encodeCharacters = false;
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

        // script and style are CDATA sections by default, so no encoding
        if ( "SCRIPT".equals(name) || "STYLE".equals(name) ) {
            this.encodeCharacters = true;
        }
        super.endElementImpl(XHTML1_NAMESPACE, name, name);
    }

    /**
     * Encode and write a specific part of an array of characters.
     */
    protected void encode(char data[], int start, int length)
    throws SAXException {
        if ( !this.encodeCharacters ) {
            this.write(data, start, length);
            return;
        }
        super.encode(data, start, length);
    }
}
