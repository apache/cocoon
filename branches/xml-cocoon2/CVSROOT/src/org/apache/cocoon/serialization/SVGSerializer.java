/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.serialization;

import org.apache.avalon.*;
import org.apache.avalon.utils.*;
import org.apache.cocoon.*;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.xml.*;
import org.apache.cocoon.xml.util.*;
import com.sun.image.codec.jpeg.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.*;
import java.net.URL;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.ext.*;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Notation;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.csiro.svgv.display.*;

/**
 * The ImagePrinter Printer writes images.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-07-11 23:46:51 $
 */
public class SVGSerializer extends DOMBuilder implements Serializer, Composer {
   
    int R=0;
    int G=1;
    int B=2;
    int A=3;

    /** The <code>ContentHandler</code> receiving SAX events. */
    private ContentHandler contentHandler=null;
    /** The <code>LexicalHandler</code> receiving SAX events. */
    private LexicalHandler lexicalHandler=null;
    /** The component manager instance */
    private ComponentManager manager=null;
    /** The current <code>Request</code>. */
    private Request request=null;
    /** The current <code>Response</code>. */
    private Response response=null;
    /** The current <code>Parameters</code>. */
    private Parameters parameters=null;
    /** The source URI associated with the request or <b>null</b>. */
    private String source=null;
    /** The current <code>OutputStream</code>. */
    private OutputStream output=null;

    /**
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        this.output=new BufferedOutputStream(out);
    }

    /**
     * Set the <code>Request</code>, <code>Response</code> and sitemap
     * <code>Parameters</code> used to process the request.
     */
    public void setup(Request req, Response res, String src, Parameters par) {
        this.request=req;
        this.response=res;
        this.source=src;
        this.parameters=par;
        super.factory=(Parser)this.manager.getComponent("parser");
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
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composer</code>.
     */
    public void setComponentManager(ComponentManager manager) {
        this.manager=manager;
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
    public void notify(Document doc)
    throws SAXException {
        this.response.setContentType("image/jpeg");
        try {
            BufferedImage img=SvgToAwtConverter.convert(doc,this.source);
            OutputStream out=this.output;
            // Write out image (highest quality for jpeg data)
            JPEGEncodeParam jpar=JPEGCodec.getDefaultJPEGEncodeParam(img);
            jpar.setQuality(1,true);
            JPEGImageEncoder jenc=JPEGCodec.createJPEGEncoder(out,jpar);
            jenc.encode(img);
            out.flush();

        } catch(IOException e) {
            throw new SAXException("IOException writing image ",e);
        }
    }
}
