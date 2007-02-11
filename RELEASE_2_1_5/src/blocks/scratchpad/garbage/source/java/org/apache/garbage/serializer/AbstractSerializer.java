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
package org.apache.garbage.serializer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.garbage.serializer.encoding.Charset;
import org.apache.garbage.serializer.encoding.CharsetFactory;
import org.apache.garbage.serializer.util.Namespaces;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: AbstractSerializer.java,v 1.4 2004/04/19 17:56:47 pier Exp $
 */
public abstract class AbstractSerializer
implements Serializer, Locator, org.apache.cocoon.serialization.Serializer {

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

    /** Our <code>Locator</code> instance. */
    private Locator locator = null;

    /** Our <code>PrintWriter</code> instance. */
    private Writer out = null;

    /** Flag indicating if the document prolog is being processed. */
    private boolean prolog = true;

    /** Flag indicating if the document is being processed. */
    private boolean processing = false;

    /* ====================================================================== */

    /** The character encoding (if known). */
    protected String encoding = null;

    /** The <code>Charset</code> associated with the character encoding. */
    protected Charset charset = null;

    /** The <code>Namespace</code> associated with this instance. */
    protected Namespaces namespaces = new Namespaces();

    /* ====================================================================== */

    /**
     * Create a new instance of this <code>AbstractSerializer</code>.
     */
    public AbstractSerializer() {
        super();
        this.reset();
    }

    /* ====================================================================== */

    public String getMimeType() {
        return(this.getContentType());
    }

    public void setOutputStream(OutputStream out) {
        this.setOutput(out);
    }

    public boolean shouldSetContentLength() {
        return(false);
    }

    /* ====================================================================== */

    /**
     * Reset this <code>AbstractSerializer</code>.
     */
    public void reset() {
        if (processing) throw new IllegalStateException();
        this.locator = null;
        this.out = null;
        this.prolog = true;
        this.encoding = null;
        this.charset = null;
        this.namespaces = new Namespaces();
    }

    /**
     * Set the <code>OutputStream</code> where this serializer will
     * write data to.
     *
     * @param out The <code>OutputStream</code> used for output.
     */
    public void setOutput(OutputStream out) {
        this.setOutput(new OutputStreamWriter(out));
    }

    /**
     * Set the <code>OutputStream</code> where this serializer will
     * write data to.
     *
     * @param out The <code>OutputStream</code> used for output.
     * @param encoding The character encoding to use.
     * @throws UnsupportedEncodingException If the specified encoding is not
     *                                      supported by this Java VM.
     */
    public void setOutput(OutputStream out, String encoding)
    throws UnsupportedEncodingException {
        this.setOutput(new OutputStreamWriter(out, encoding));
    }

    /**
     * Set the <code>Writer</code> where this serializer will write data to.
     *
     * @param out The <code>Writer</code> used for output.
     */
    public void setOutput(Writer out) {

        if (out == null) throw new NullPointerException("Null writer");

        if (out instanceof OutputStreamWriter) {
            this.encoding = ((OutputStreamWriter)out).getEncoding();
        }
        CharsetFactory factory = CharsetFactory.newInstance();
        try {
            this.charset = factory.getCharset(this.encoding);
        } catch (UnsupportedEncodingException e) {
            this.charset = factory.getCharset();
        }
        this.encoding = this.charset.getName();

        this.out = new BufferedWriter(out);
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

    /* ====================================================================== */

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument()
    throws SAXException {
        this.processing = true;
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

    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String nsuri, String local, String qual)
    throws SAXException {
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
     * Receive notification of the end of an element.
     *
     * @param uri The namespace URI of the root element.
     * @param local The local name of the root element.
     * @param qual The fully-qualified name of the root element.
     */
    public abstract void endElementImpl(String uri, String local, String qual)
    throws SAXException;
}
