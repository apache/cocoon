/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.serialization;

import org.apache.cocoon.*;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.xml.*;
import org.apache.cocoon.xml.dom.*;
import org.apache.avalon.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.w3c.dom.*;
import org.w3c.dom.svg.*;

import org.apache.batik.refimpl.transcoder.*;
import org.apache.batik.transcoder.*;

import org.apache.xml.serialize.SerializerFactory;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class SVGSerializer extends DOMBuilder implements Composer, Serializer, Configurable {

    /** The <code>ContentHandler</code> receiving SAX events. */
    private ContentHandler contentHandler=null;
    /** The <code>LexicalHandler</code> receiving SAX events. */
    private LexicalHandler lexicalHandler=null;
    /** The component manager instance */
    private ComponentManager manager=null;
    /** The current <code>Environment</code>. */
    private Environment environment=null;
    /** The current <code>Parameters</code>. */
    private Configuration config=null;
    /** The current <code>OutputStream</code>. */
    private OutputStream output=null;
    /** The current <code>mime-type</code>. */
    private String mimetype = null;

    /**
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        this.output=new BufferedOutputStream(out);
    }

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        this.config = conf;

        try {
            log.debug("Looking up " + Roles.PARSER);
            // First, get a DOM parser for the DOM Builder to work with.
            super.factory= (Parser) this.manager.lookup(Roles.PARSER);
        } catch (Exception e) {
            log.error("Could not find component", e);
            throw new ConfigurationException("Could not find Parser", e);
        }

        mimetype = this.config.getAttribute("mime-type");
        log.debug("SVGSerializer mime-type:" + mimetype);
    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     * <br>
     * This method will simply call <code>setContentHandler(consumer)</code>
     * and <code>setLexicalHandler(consumer)</code>.
     */
    public void setConsumer(XMLConsumer consumer) {
        this.contentHandler=consumer;
        this.lexicalHandler=consumer;
    }

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>ContentHandler</code> instance
     * accessing the protected <code>super.contentHandler</code> field.
     */
    public void setContentHandler(ContentHandler content) {
        this.contentHandler=content;
    }

    /**
     * Set the <code>LexicalHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>LexicalHandler</code> instance
     * accessing the protected <code>super.lexicalHandler</code> field.
     *
     * @exception IllegalStateException If the <code>LexicalHandler</code> or
     *                                  the <code>XMLConsumer</code> were
     *                                  already set.
     */
    public void setLexicalHandler(LexicalHandler lexical) {
        this.lexicalHandler=lexical;
    }

    /**
     * Receive notification of a successfully completed DOM tree generation.
     */
    public void notify(Document doc) throws SAXException {
        try {
            TranscoderFactory transcoderFactory =
	            ConcreteTranscoderFactory.getTranscoderFactoryImplementation();
            Transcoder transcoder = 
                transcoderFactory.createTranscoder(mimetype);

            // TODO: FIXME(DIMS) : Remove hard-coded parser name.
            transcoder.addTranscodingHint(
                        TranscodingHints.KEY_XML_PARSER_CLASSNAME,
		                "org.apache.xerces.parsers.SAXParser");

            // TODO: FIXME(DIMS) - batik is flaky in accepting DOM and don't 
            // support SAX yet. So we have to create a Byte Array as an input
            // stream for them.
            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
            BufferedOutputStream ostream = new BufferedOutputStream(bytestream);

            OutputFormat outformat = new OutputFormat();
            SerializerFactory serializerFactory = 
                        SerializerFactory.getSerializerFactory(Method.XML);
            org.apache.xml.serialize.Serializer serializer = 
                        serializerFactory.makeSerializer(outformat);
            serializer.setOutputByteStream(ostream);
            serializer.asDOMSerializer().serialize(doc);

            // Use the Byte Array as the InputSource for the transcoder.
            InputSource inputSource = new InputSource(new ByteArrayInputStream
                                                        (bytestream.toByteArray()));

            // TODO: FIXME(DIMS) - Why do they need a SystemId?
            inputSource.setSystemId("http://xml.apache.org");
            transcoder.transcodeToStream(inputSource,this.output);

            this.output.flush();
        } catch (Exception ex) {
            log.warn("SVGSerializer: Exception writing image", ex);
            throw new SAXException("Exception writing image ", ex);
        }
    }

    /**
     * Return the MIME type.
     */
    public String getMimeType() {
        return mimetype;
    }
}
