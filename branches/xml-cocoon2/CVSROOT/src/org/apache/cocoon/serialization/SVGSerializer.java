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
import org.apache.cocoon.components.image.ImageEncoder;
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
import org.csiro.svg.dom.SVGDocumentImpl;

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
    /** The source URI associated with the request or <b>null</b>. */
    private String source=null;
    /** The current <code>OutputStream</code>. */
    private OutputStream output=null;
    /** The current <code>ImageEncoder</code>. */
    private ImageEncoder encoder;
    /** Does the produced image have a transparent background? */
    private boolean transparent;
    /** The background colour of this image if not transparent */
    private Color backgroundColour = null;

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

        // What image encoder do I use?
        String enc = this.config.getChild("encoder").getValue();
        if (enc == null) {
            throw new ConfigurationException("No Image Encoder specified.");
        }

        try {
        log.debug("Selecting " + Roles.IMAGE_ENCODER + ": " + enc);
        ComponentSelector selector = (ComponentSelector) this.manager.lookup(Roles.IMAGE_ENCODER);
            this.encoder = (ImageEncoder) selector.select(enc);
        } catch (Exception e) {
        log.error("Could not select " + Roles.IMAGE_ENCODER, e);
            throw new ConfigurationException("The ImageEncoder '"
                + enc + "' cannot be found. Check your component configuration in the sitemap"/*, conf*/);
        }

        // Configure the encoder
        if (this.encoder instanceof Configurable) {
            ((Configurable)this.encoder).configure(conf);
        }
        // Transparent or a solid colour background?
        this.transparent = this.config.getChild("transparent").getValueAsBoolean(false);
        if (!transparent) {
            String bg = this.config.getChild("background").getValue("#FFFFFF").trim();
            if (bg.startsWith("#")) {
                bg = bg.substring(1);
            }

            try {
                this.backgroundColour = new Color(Integer.parseInt(bg, 16));
            } catch (NumberFormatException e) {
                throw new ConfigurationException(bg + " is not a valid color."/*, conf*/);
            }
        }
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
            SVGDocumentImpl svg = new SVGDocumentImpl(doc);
            SVGSVGElement root = svg.getRootElement();
            SVGLength width = root.getWidth().getBaseVal();
            SVGLength height = root.getHeight().getBaseVal();
            width.convertToSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX);
            height.convertToSpecifiedUnits(SVGLength.SVG_LENGTHTYPE_PX);
            int w = (int) width.getValue();
            int h = (int) height.getValue();
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            // Either leave this image transparent, or fill it with a solid colour
            Graphics2D gra = img.createGraphics();
            if (!transparent) {
                gra.setColor(backgroundColour);
                gra.fillRect(0, 0, w, h);
            }
            svg.draw(gra);
            encoder.encode(img, this.output);
            this.output.flush();
        } catch (IOException ex) {
            throw new SAXException("IOException writing image ", ex);
        }
    }

    /**
     * Return the MIME type.
     */
    public String getMimeType() {
        return encoder.getMimeType();
    }
}
