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

import java.io.CharArrayWriter;

import org.apache.cocoon.components.serializers.encoding.Encoder;
import org.apache.cocoon.components.serializers.encoding.XMLEncoder;
import org.apache.cocoon.components.serializers.util.DocType;
import org.apache.cocoon.components.serializers.util.Namespaces;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: XMLSerializer.java,v 1.3 2004/04/30 19:34:46 pier Exp $
 */
public class XMLSerializer extends EncodingSerializer {

    private static final XMLEncoder XML_ENCODER = new XMLEncoder();

    private static final char S_EOL[] =
            System.getProperty("line.separator").toCharArray();

    private static final char S_DOCUMENT_1[] = "<?xml version=\"1.0".toCharArray();
    private static final char S_DOCUMENT_2[] = "\" encoding=\"".toCharArray();
    private static final char S_DOCUMENT_3[] = "\"?>".toCharArray();

    private static final char S_DOCTYPE_1[] = "<!DOCTYPE ".toCharArray();
    private static final char S_DOCTYPE_2[] = " PUBLIC \"".toCharArray();
    private static final char S_DOCTYPE_3[] = "\" \"".toCharArray();
    private static final char S_DOCTYPE_4[] = " SYSTEM \"".toCharArray();
    private static final char S_DOCTYPE_5[] = "\">".toCharArray();

    private static final char S_ELEMENT_1[] = "=\"".toCharArray();
    private static final char S_ELEMENT_2[] = "</".toCharArray();
    private static final char S_ELEMENT_3[] = " />".toCharArray();
    private static final char S_ELEMENT_4[] = " xmlns".toCharArray();

    private static final char S_CDATA_1[] = "<[CDATA[".toCharArray();
    private static final char S_CDATA_2[] = "]]>".toCharArray();

    private static final char S_COMMENT_1[] = "<!--".toCharArray();
    private static final char S_COMMENT_2[] = "-->".toCharArray();

    private static final char S_PROCINSTR_1[] = "<?".toCharArray();
    private static final char S_PROCINSTR_2[] = "?>".toCharArray();

    private static final char C_LT = '<';
    private static final char C_GT = '>';
    private static final char C_SPACE = ' ';
    private static final char C_QUOTE = '"';
    private static final char C_NSSEP = ':';

    private static final boolean DEBUG = false;

    /* ====================================================================== */

    /** Whether an element is left open like &quot;&lt;name &quot;. */
    private boolean hanging_element = false;

    /** True if we are processing the prolog. */
    private boolean processing_prolog = true;

    /** True if we are processing the DTD. */
    private boolean processing_dtd = false;

    /** A <code>Writer</code> for prolog elements. */
    private PrologWriter prolog = new PrologWriter();

    /* ====================================================================== */

    /** The <code>DocType</code> instance representing the document. */
    protected DocType doctype = null;

    /* ====================================================================== */

    /**
     * Create a new instance of this <code>XMLSerializer</code>
     */
    public XMLSerializer() {
        super(XML_ENCODER);
    }

    /**
     * Create a new instance of this <code>XMLSerializer</code>
     */
    protected XMLSerializer(XMLEncoder encoder) {
        super(encoder);
    }
    
    /**
     * Reset this <code>XMLSerializer</code>.
     */
    public void recycle() {
        super.recycle();
        this.doctype = null;
        this.hanging_element = false;
        this.processing_prolog = true;
        this.processing_dtd = false;
        if (this.prolog != null) this.prolog.reset();
    }

    /**
     * Return the MIME Content-Type produced by this serializer.
     */
    public String getMimeType() {
        if (this.charset == null) return("text/xml");
        return("text/xml; charset=" + this.charset.getName());
    }

    /* ====================================================================== */

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument()
    throws SAXException {
        super.startDocument();
        this.head();
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument()
    throws SAXException {
        this.writeln();
        super.endDocument();
    }

    /**
     * Write the XML document header.
     * <p>
     * This method will write out the <code>&lt;?xml version=&quot;1.0&quot
     * ...&gt;</code> header.
     * </p>
     */
    protected void head()
    throws SAXException {
        this.write(S_DOCUMENT_1); // [<?xml version="1.0]
        if (this.charset != null) {
            this.write(S_DOCUMENT_2); // [" encoding="]
            this.write(this.charset.getName());
        }
        this.write(S_DOCUMENT_3); // ["?>]
        this.writeln();
    }

    /**
     * Report the start of DTD declarations, if any.
     */
    public void startDTD(String name, String public_id, String system_id)
    throws SAXException {
        this.processing_dtd = true;
        this.doctype = new DocType(name, public_id, system_id);
    }

    /**
     * Report the start of DTD declarations, if any.
     */
    public void endDTD()
    throws SAXException {
        this.processing_dtd = false;
    }

    /**
     * Receive notification of the beginning of the document body.
     *
     * @param uri The namespace URI of the root element.
     * @param local The local name of the root element.
     * @param qual The fully-qualified name of the root element.
     */
    public void body(String uri, String local, String qual)
    throws SAXException {
        this.processing_prolog = false;

        this.writeln();

        /* We have a document type. */
        if (this.doctype != null) {

            String root_name = this.doctype.getName();
            String public_id = this.doctype.getPublicId();
            String system_id = this.doctype.getSystemId();

            /* Check the DTD and the root element */
            if (!root_name.equals(qual)) {
                throw new SAXException("Root element name \"" + root_name
                        + "\" declared by document type declaration differs "
                        + "from actual root element name \"" + qual + "\"");
            }

            /* Output a <!DOCTYPE ...> declaration. */
            this.write(S_DOCTYPE_1); // [<!DOCTYPE ]
            this.write(root_name);
            if (public_id != null) {
                this.write(S_DOCTYPE_2); // [ PUBLIC "]
                this.write(public_id);
                /* This is wring in XML, but not in SGML/HTML */
                if (system_id != null) {
                    this.write(S_DOCTYPE_3); // [" "]
                    this.write(system_id);
                }
                this.write(S_DOCTYPE_5); // [">]
            } else if (system_id != null) {
                this.write(S_DOCTYPE_4); // [ SYSTEM "]
                this.write(system_id);
                this.write(S_DOCTYPE_5); // [">]
            } else {
                this.write(C_GT); // [>]
            }
            this.writeln();
        }

        /* Output all PIs and comments we cached in the prolog */
        this.prolog.writeTo(this);
        this.writeln();
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
        this.closeElement(false);
        this.write(C_LT); // [<]
        if (DEBUG) {
            this.write('[');
            this.write(uri);
            this.write(']');
        }
        this.write(qual);

        for (int x = 0; x < namespaces.length; x++) {
            this.write(S_ELEMENT_4); // [ xmlns]
            if (namespaces[x][Namespaces.NAMESPACE_PREFIX].length() > 0) {
                this.write(C_NSSEP); // [:]
                this.write(namespaces[x][Namespaces.NAMESPACE_PREFIX]);
            }
            this.write(S_ELEMENT_1); // [="]
            this.encode(namespaces[x][Namespaces.NAMESPACE_URI]);
            this.write(C_QUOTE); // ["]
        }

        for (int x = 0; x < attributes.length; x++) {
            this.write(C_SPACE); // [ ]
            if (DEBUG) {
                this.write('[');
                this.write(attributes[x][ATTRIBUTE_NSURI]);
                this.write(']');
            }
            this.write(attributes[x][ATTRIBUTE_QNAME]);
            this.write(S_ELEMENT_1); // [="]
            this.encode(attributes[x][ATTRIBUTE_VALUE]);
            this.write(C_QUOTE); // ["]
        }

        this.hanging_element = true;
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
        if (closeElement(true)) return;
        this.write(S_ELEMENT_2); // [</]
        if (DEBUG) {
            this.write('[');
            this.write(uri);
            this.write(']');
        }
        this.write(qual);
        this.write(C_GT); // [>]
    }

    /**
     * Write the end part of a start element (if necessary).
     *
     * @param end_element Whether this method was called because an element
     *                    is being closed or not.
     * @return <b>true</b> if this call successfully closed the element (and
     *         no further <code>&lt;/element&gt;</code> is required.
     */
    protected boolean closeElement(boolean end_element)
    throws SAXException {
        if (!hanging_element) return(false);
        if (end_element) this.write(S_ELEMENT_3); // [ />]
        else this.write(C_GT); // [>]
        this.hanging_element = false;
        return(true);
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA()
    throws SAXException {
        if (this.processing_prolog) return;
        this.closeElement(false);
        this.write(S_CDATA_1); // [<[CDATA[]
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA()
    throws SAXException {
        if (this.processing_prolog) return;
        this.closeElement(false);
        this.write(S_CDATA_2); // []]>]
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char data[], int start, int length)
    throws SAXException {
        if (this.processing_prolog) return;
        this.closeElement(false);
        this.encode(data, start, length);
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace(char data[], int start, int length)
    throws SAXException {
        this.characters(data, start, length);
    }

    /**
     * Report an XML comment anywhere in the document.
     */
    public void comment(char data[], int start, int length)
    throws SAXException {
        if (this.processing_dtd) return;

        if (this.processing_prolog) {
            this.prolog.write(S_COMMENT_1); // [<!--]
            this.prolog.write(data, start, length);
            this.prolog.write(S_COMMENT_2); // [-->]
            this.prolog.write(S_EOL);
            return;
        }

        this.closeElement(false);
        this.write(S_COMMENT_1); // [<!--]
        this.write(data, start, length);
        this.write(S_COMMENT_2); // [-->]
    }

    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        if (this.processing_dtd) return;

        if (this.processing_prolog) {
            this.prolog.write(S_PROCINSTR_1); // [<?]
            this.prolog.write(target);
            if (data != null) {
                this.prolog.write(C_SPACE); // [ ]
                this.prolog.write(data);
            }
            this.prolog.write(S_PROCINSTR_2);  // [?>]
            this.prolog.write(S_EOL);
            return;
        }

        this.closeElement(false);

        this.write(S_PROCINSTR_1); // [<?]
        this.write(target);
        if (data != null) {
            this.write(C_SPACE); // [ ]
            this.write(data);
        }
        this.write(S_PROCINSTR_2);  // [?>]
    }

    /**
     * Report the beginning of some internal and external XML entities.
     */
    public void startEntity(String name)
    throws SAXException {
    }

    /**
     * Report the end of an entity.
     */
    public void endEntity(String name)
    throws SAXException {
    }

    /**
     * Receive notification of a skipped entity.
     */
    public void skippedEntity(String name)
    throws SAXException {
    }

    /* ====================================================================== */

    /**
     * The <code>PrologWriter</code> is a simple extension to a
     * <code>CharArrayWriter</code>.
     */
    private static final class PrologWriter extends CharArrayWriter {

        /** Create a new <code>PrologWriter</code> instance. */
        private PrologWriter() {
            super();
        }

        /**
         * Write an array of characters.
         * <p>
         * The <code>CharArrayWriter</code> implementation of this method
         * throws an unwanted <code>IOException</code>.
         * </p>
         */
        public void write(char c[]) {
            this.write(c, 0, c.length);
        }

        /**
         * Write a <code>String</code>.
         * <p>
         * The <code>CharArrayWriter</code> implementation of this method
         * throws an unwanted <code>IOException</code>.
         * </p>
         */
        public void write(String str) {
            this.write(str, 0, str.length());
        }

        /**
         * Write our contents to a <code>BaseSerializer</code> without
         * copying the buffer.
         */
        public void writeTo(XMLSerializer serializer)
        throws SAXException {
            serializer.write(this.buf, 0, this.count);
        }
    }
}
