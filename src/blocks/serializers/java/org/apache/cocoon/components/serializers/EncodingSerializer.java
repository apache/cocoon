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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.serializers.encoding.Charset;
import org.apache.cocoon.components.serializers.encoding.CharsetFactory;
import org.apache.cocoon.components.serializers.encoding.Encoder;
import org.apache.cocoon.components.serializers.util.Namespaces;
import org.apache.cocoon.serialization.Serializer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: EncodingSerializer.java,v 1.3 2004/06/01 17:16:28 tcurdt Exp $
 */
public abstract class EncodingSerializer implements Serializer, Locator, Recyclable, Configurable  {

    /** The line separator string */
    private static final char S_EOL[] =
            System.getProperty("line.separator").toCharArray();

    /* ====================================================================== */

    /** The position of the namespace URI in the attributes array. */
    public static final int ATTRIBUTE_NSURI = 0;

    /** The position of the local name in the attributes array. */
    public static final int ATTRIBUTE_LOCAL = 1;

    /** The position of the qualified name in the attributes array. */
    public static final int ATTRIBUTE_QNAME = 2;

    /** The position of the value in the attributes array. */
    public static final int ATTRIBUTE_VALUE = 3;

    /** The length of the array of strings representing an attribute. */
    public static final int ATTRIBUTE_LENGTH = 4;

    /* ====================================================================== */

    /** Our <code>Encoder</code> instance. */
    private Encoder encoder = null;
    
    /** Our <code>Locator</code> instance. */
    private Locator locator = null;

    /** Our <code>Writer</code> instance. */
    private OutputStreamWriter out = null;

    /** Flag indicating if the document prolog is being processed. */
    private boolean prolog = true;

    /** Flag indicating if the document is being processed. */
    private boolean processing = false;

    /** Current nesting level */
    private int level = 0;

    /** Whitespace buffer for indentation */
    private char[] indentBuffer = null;

    /* ====================================================================== */

    /** The <code>Charset</code> associated with the character encoding. */
    protected Charset charset = null;

    /** The <code>Namespace</code> associated with this instance. */
    protected Namespaces namespaces = new Namespaces();

    /** Per level indent spaces */
    protected int indentPerLevel = 0;
    /* ====================================================================== */

    /**
     * Create a new instance of this <code>EncodingSerializer</code>
     */
    protected EncodingSerializer(Encoder encoder) {
        super();
        this.encoder = encoder;
        this.recycle();
    }
    
    /* ====================================================================== */

    /**
     * Test if the component wants to set the content length.
     */
    public boolean shouldSetContentLength() {
        return(false);
    }

    /**
     * Reset this <code>EncodingSerializer</code>.
     */
    public void recycle() {
        if (processing) throw new IllegalStateException();
        this.namespaces = new Namespaces();
        this.locator = null;
        this.out = null;
        this.prolog = true;
    }

    /**
     * Set the <code>OutputStream</code> where this serializer will
     * write data to.
     *
     * @param out The <code>OutputStream</code> used for output.
     */
    public void setOutputStream(OutputStream out)
    throws IOException {
        if (out == null) throw new NullPointerException("Null output");
        
        this.out = new OutputStreamWriter(out, this.charset.getName());
    }

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        String encoding = conf.getChild("encoding").getValue(null);
        try {
            this.charset = CharsetFactory.newInstance().getCharset(encoding);
        } catch (UnsupportedEncodingException exception) {
            throw new ConfigurationException("Encoding not supported: "
                                             + encoding, exception);
        }

        indentPerLevel = conf.getChild("indent").getValueAsInteger(0);
        if (indentPerLevel > 0) {
            assureIndentBuffer(indentPerLevel * 6);
        }
    }

    /* ====================================================================== */

    private char[] assureIndentBuffer( int size ) {
        if (indentBuffer == null || indentBuffer.length < size) {
            indentBuffer = new char[size];
            Arrays.fill(indentBuffer,' ');
        }
        return indentBuffer;
    }

    /**
     * Encode and write a <code>String</code>
     */
    protected void encode(String data)
    throws SAXException {
        char array[] = data.toCharArray();
        this.encode(array, 0, array.length);
    }
    
    /**
     * Encode and write an array of characters.
     */
    protected void encode(char data[])
    throws SAXException {
        this.encode(data, 0, data.length);
    }
    
    /**
     * Encode and write a specific part of an array of characters.
     */
    protected void encode(char data[], int start, int length)
    throws SAXException {
        int end = start + length;
        
        if (data == null) throw new NullPointerException("Null data");
        if ((start < 0) || (start > data.length) || (length < 0) ||
            (end > data.length) || (end < 0))
            throw new IndexOutOfBoundsException("Invalid data");
        if (length == 0) return;
        
        for (int x = start; x < end; x++) {
            char c = data[x];
            
            if (this.charset.allows(c) && this.encoder.allows(c)) {
                continue;
            }
            
            if (start != x) this.write(data, start, x - start );
            this.write(this.encoder.encode(c));
            start = x + 1;
            continue;
        }
        if (start != end) this.write(data, start, end - start );
    }

    /* ====================================================================== */

    /**
     * Receive an object for locating the origin of SAX document events.
     */
    public final void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
     * Return the public identifier for the current document event.
     *
     * @return A <code>String</code> containing the public identifier,
     *         or <b>null</b> if none is available.
     */
    public String getPublicId() {
        return(this.locator == null? null: this.locator.getPublicId());
    }

    /**
     * Return the system identifier for the current document event.
     *
     * @return A <code>String</code> containing the system identifier,
     *         or <b>null</b> if none is available.
     */
    public String getSystemId() {
        return(this.locator == null? null: this.locator.getSystemId());
    }
    
    /**
     * Return the line number where the current document event ends.
     *
     * @return The line number, or -1 if none is available.
     */
    public int getLineNumber() {
        return(this.locator == null? -1: this.locator.getLineNumber());
    }

    /**
     * Return the column number where the current document event ends.
     *
     * @return The column number, or -1 if none is available.
     */
    public int getColumnNumber() {
        return(this.locator == null? -1: this.locator.getColumnNumber());
    }

    /**
     * Return a <code>String</code> describing the current location.
     */
    protected String getLocation() {
        if (this.locator == null) return("");
        StringBuffer buf = new StringBuffer(" (");
        if (this.getSystemId() != null) {
            buf.append(this.getSystemId());
            buf.append(' ');
        }
        buf.append("line " + this.getLineNumber());
        buf.append(" col " + this.getColumnNumber());
        buf.append(')');
        return(buf.toString());
    }

    /* ====================================================================== */

    /**
     * Flush the stream.
     */
    protected void flush()
    throws SAXException {
        try {
            this.out.flush();
        } catch (IOException e) {
            throw new SAXException("I/O error flushing: " + e.getMessage(), e);
        }
    }

    /**
     * Write an array of characters.
     */
    protected void write(char data[])
    throws SAXException {
        try {
            this.out.write(data, 0, data.length);
        } catch (IOException e) {
            throw new SAXException("I/O error writing: " + e.getMessage(), e);
        }
    }

    /**
     * Write a portion of an array of characters.
     */
    protected void write(char data[], int start, int length)
    throws SAXException {
        try {
            this.out.write(data, start, length);
        } catch (IOException e) {
            throw new SAXException("I/O error writing: " + e.getMessage(), e);
        }
    }

    /**
     * Write a single character.
     */
    protected void write(int c)
    throws SAXException {
        try {
            this.out.write(c);
        } catch (IOException e) {
            throw new SAXException("I/O error writing: " + e.getMessage(), e);
        }
    }

    /**
     * Write a string.
     */
    protected void write(String data)
    throws SAXException {
        try {
            this.out.write(data);
        } catch (IOException e) {
            throw new SAXException("I/O error writing: " + e.getMessage(), e);
        }
    }

    /**
     * Write a portion of a string.
     */
    protected void write(String data, int start, int length)
    throws SAXException {
        try {
            this.out.write(data, start, length);
        } catch (IOException e) {
            throw new SAXException("I/O error writing: " + e.getMessage(), e);
        }
    }

    /**
     * Write a end-of-line character.
     */
    protected void writeln()
    throws SAXException {
        try {
            this.out.write(S_EOL);
        } catch (IOException e) {
            throw new SAXException("I/O error writing: " + e.getMessage(), e);
        }
    }

    /**
     * Write a string and a end-of-line character.
     */
    protected void writeln(String data)
    throws SAXException {
        try {
            this.out.write(data);
            this.out.write(S_EOL);
        } catch (IOException e) {
            throw new SAXException("I/O error writing: " + e.getMessage(), e);
        }
    }

    /**
     * Write out character to indent the output according
     * to the level of nesting
     * @param indent
     * @throws SAXException
     */
    protected void writeIndent(int indent) throws SAXException {
        this.charactersImpl("\n".toCharArray(),0,1);
        if (indent > 0) {
            this.charactersImpl(assureIndentBuffer(indent),0,indent);
        }
    }

    /* ====================================================================== */

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument()
    throws SAXException {
        this.processing = true;
        this.level = 0;
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument()
    throws SAXException {
        this.processing = false;
        this.flush();
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        this.namespaces.push(prefix, uri);
    }

    /**
     * End the scope of a prefix-URI mapping.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        this.namespaces.pop(prefix);
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement(String nsuri, String local, String qual,
                                   Attributes attributes)
    throws SAXException {
        if (indentPerLevel > 0) {
            this.writeIndent(indentPerLevel*level);
            level++;
        }

        String name = this.namespaces.qualify(nsuri, local, qual);

        if (this.prolog) {
            this.body(nsuri, local, name);
            this.prolog = false;
        }

        String ns[][] = this.namespaces.commit();

        String at[][] = new String [attributes.getLength()][4];
        for (int x = 0; x < at.length; x++) {
            at[x][ATTRIBUTE_NSURI] = attributes.getURI(x);
            at[x][ATTRIBUTE_LOCAL] = attributes.getLocalName(x);
            at[x][ATTRIBUTE_QNAME] = namespaces.qualify(
                            attributes.getURI(x),
                            attributes.getLocalName(x),
                            attributes.getQName(x));
            at[x][ATTRIBUTE_VALUE] = attributes.getValue(x);
        }

        this.startElementImpl(nsuri, local, name, ns, at);
    }

    public void characters (char ch[], int start, int length)
    throws SAXException {
        if (indentPerLevel > 0) {
            this.writeIndent(indentPerLevel*level + 1);
        }
        this.charactersImpl(ch, start, length);
    }

    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String nsuri, String local, String qual)
    throws SAXException {
        if (indentPerLevel > 0) {
            level--;
            this.writeIndent(indentPerLevel*level);
        }

        String name = this.namespaces.qualify(nsuri, local, qual);
        this.endElementImpl(nsuri, local, name);
    }

    /**
     * Receive notification of the beginning of the document body.
     *
     * @param uri The namespace URI of the root element.
     * @param local The local name of the root element.
     * @param qual The fully-qualified name of the root element.
     */
    public abstract void body(String uri, String local, String qual)
    throws SAXException;

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The namespace URI of the root element.
     * @param local The local name of the root element.
     * @param qual The fully-qualified name of the root element.
     * @param namespaces An array of <code>String</code> objects containing
     *                   the namespaces to be declared by this element.
     * @param attributes An array of <code>String</code> objects containing
     *                   all attributes of this element.
     */
    public abstract void startElementImpl(String uri, String local, String qual,
                                  String namespaces[][], String attributes[][])
    throws SAXException;

    /**
     * Receive character notifications
     * @param ch
     * @param start
     * @param length
     * @throws SAXException
     */
    public abstract void charactersImpl (char ch[], int start, int length)
    throws SAXException;

    /**
     * Receive notification of the end of an element.
     *
     * @param uri The namespace URI of the root element.
     * @param local The local name of the root element.
     * @param qual The fully-qualified name of the root element.
     */
    public abstract void endElementImpl(String uri, String local, String qual)
    throws SAXException;
}
