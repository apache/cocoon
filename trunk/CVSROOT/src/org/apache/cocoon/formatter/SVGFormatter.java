/*-- $Id: SVGFormatter.java,v 1.1 2001-07-27 09:20:16 sylvain Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.
 
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
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.cocoon.formatter;

import java.awt.Color;
import java.io.*;
import java.util.*;

import org.apache.batik.css.CSSDocumentHandler;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.*;

import org.apache.cocoon.framework.*;
import org.apache.cocoon.logger.Logger;
import org.w3c.dom.*;

/**
 * Formatter from SVG to PNG/JPEG/TIFF using Batik. It handles mime
 * types "image/jpeg", "image/jpe", "image/jpg", "image/png", and
 * "image/tiff".<br/>
 *
 * To use it, add the following in <code>cocoon.properties</code> :
 * <pre>
 * formatter.type.image/jpeg=org.apache.cocoon.formatter.SVGFormatter
 * formatter.image/jpeg.MIME-type=image/jpeg
 * formatter.image/jpeg.hint.background_color.type=color
 * formatter.image/jpeg.hint.background_color.value=#FFFFFF
 * formatter.image/jpeg.hint.quality.type=float
 * formatter.image/jpeg.hint.quality.value=1.0
 * </pre>
 * See <code>org.apache.batik.transcoder.image.ImageTranscoder</code> and
 * its subclasses for other available hints.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version $Revision: 1.1 $ $Date: 2001-07-27 09:20:16 $
 */

public class SVGFormatter implements Formatter, Configurable, Actor {

    private Logger logger;
    private String mimeType = "image/jpeg";
    private ImageTranscoder transcoder;
    
    public void init(Director director) {
        this.logger = (Logger) director.getActor("logger");
    }

    public void init(Configurations conf)
    throws InitializationException {

        // Get mime type
        String mt = (String) conf.get("MIME-type");
        if (mt != null) {
            mimeType = mt;
        }
        
        // Create transcoder
        if (mimeType.equals("image/jpg") ||
            mimeType.equals("image/jpeg") ||
            mimeType.equals("image/jpe")) {
            transcoder = new JPEGTranscoder();
        } else if (mimeType.equals("image/png")) {
            transcoder = new PNGTranscoder();
        } else if (mimeType.equals("image/tiff")) {
            transcoder = new TIFFTranscoder();
        } else {
            throw new InitializationException("No transcoder found for mime type : " + mimeType);
        }

        // Pass hints to the transcoder
        Configurations hints = conf.getConfigurations("hint");
        Enumeration hintNames = hints.keys();
 
        while (hintNames.hasMoreElements()) {
            
            String hintName  = (String)hintNames.nextElement();
            
            if (hintName.endsWith(".type")) {
                hintName = hintName.substring(0, hintName.length() - 5);
            
                Configurations hintConf = hints.getConfigurations(hintName);
                
                String stringValue = (String)hintConf.get("value");
    
                try {
                    // Turn it into a key name (assume the current Batik style continues!)
                    hintName = ("KEY_" + hintName).toUpperCase();
                    // Use reflection to get a reference to the key object
                    TranscodingHints.Key key = (TranscodingHints.Key)
                        (transcoder.getClass().getField(hintName).get(transcoder));
    
                    String keyType = ((String)hintConf.get("type", "STRING")).toUpperCase();
                    Object value = null;
                    
                    if ("FLOAT".equals(keyType)) {
                        // Can throw an exception.
                        value = Float.valueOf(stringValue);
                    } else if ("INTEGER".equals(keyType)) {
                        // Can throw an exception.
                        value = Integer.valueOf(stringValue);
                    } else if ("BOOLEAN".equals(keyType)) {
                        // Can throw an exception.
                        value = Boolean.valueOf(stringValue);
                    } else if ("COLOR".equals(keyType)) {
                        // Can throw an exception
                        if (stringValue.startsWith("#")) {
                            stringValue = stringValue.substring(1);
                        }
                        value = new Color(Integer.parseInt(stringValue, 16));
                    } else {
                        // Assume String, and get the value. Allow an empty string.
                        value = stringValue;
                    }
                    
                    logger.log("SVG Serializer: adding hint \"" + hintName + "\" with value \"" + value.toString() + "\"", Logger.DEBUG);
                    transcoder.addTranscodingHint(key, value);
                    
                } catch (ClassCastException ex) {
                    // This is only thrown from the String keyType... line
                    throw new InitializationException("Specified key (" + hintName + ") is not a valid Batik Transcoder key.");
                } catch (IllegalAccessException ex) {
                    throw new InitializationException("Cannot access the key for parameter \"" + hintName + "\" " + ex.getMessage());
                } catch (NoSuchFieldException ex) {
                    throw new InitializationException("No field available for parameter \"" + hintName + "\" " + ex.getMessage());
                }
            }
        }
    }

    /**
     * Renders the given SVG document as an image.
     */
    public void format(Document document, OutputStream stream, Dictionary parameters)
    throws Exception {
        try {
            // Batik only handles documents of its own DOM implementation (passing
            // a Xerces DOM produces a ClassCastException), even for importNode() !
            // So we need to copy mnually the document with Batik's DOM implementation.
            Document svgDoc = asBatikDOM(document);
            
            // FIXME: Hack copied from Cocoon 2, but why is it needed ?
            ((org.apache.batik.dom.svg.SVGOMDocument)svgDoc).setURLObject(new java.net.URL("http://xml.apache.org"));

            // Generate image        
            transcoder.transcode(new TranscoderInput(svgDoc), new TranscoderOutput(stream));

        } catch(TranscoderException te) {
            if (te.getException() != null)
                throw te.getException();
            else
                throw te;
        }
    }

    /**
     * Returns the encoding used by this formatter for output.
     */
    public String getEncoding() {
        return null;
    }
    
    /**
     * Returns the MIME type used by this formatter for output.
     */
    public String getMIMEType() {
        return mimeType;
    }
    
    /**
     * Copy a <code>Document</code> in Batik's own DOM implementation, and put
     * non-namespaced elements in the SVG namespace.
     *
     * @param document the source <code>Document</code>
     * @return the document as a Batik DOM
     */
    private Document asBatikDOM(Document document) throws Exception {
        
        Element docRoot = document.getDocumentElement();
        
        SVGDOMImplementation impl = new SVGDOMImplementation();
        // Create Batik document, and force SVG namespace.
        Document svgDoc = impl.createDocument(
            SVGDOMImplementation.SVG_NAMESPACE_URI, docRoot.getNodeName(), null);

        Element svgRoot = svgDoc.getDocumentElement();
        
        // Clone across DOM implementations
        copyChildNodes(docRoot, svgRoot);
        
        return svgDoc;
    }
    
    /**
     *  Copy child nodes (including attributes) from the source element to the dest element
     */
    private void copyChildNodes(Element source, Element dest) {
        
        Document destDoc = dest.getOwnerDocument();
        
        // copy element attributes
        NamedNodeMap docAttrs = source.getAttributes();
        int docAttrsLength = docAttrs.getLength();
        for (int i=0; i < docAttrsLength; i++) {
            Node attr = docAttrs.item(i);
            // Don't force to the SVG namespace here. When an attribute has
            // no namespace, it inherits the one of its element.
            dest.setAttributeNS(attr.getNamespaceURI(), attr.getNodeName(), attr.getNodeValue());
        }
        
        // copy recursively all child nodes
        NodeList sourceNodes = source.getChildNodes();
        int sourceNodesLength = sourceNodes.getLength();
        for (int i=0; i < sourceNodesLength; i++) {
            Node node = sourceNodes.item(i);
            switch(node.getNodeType()) {
                
                case Document.ELEMENT_NODE :
                    // Force to SVG namespace if none is set.
                    String uri = node.getNamespaceURI();
                    if (uri == null)
                        uri = SVGDOMImplementation.SVG_NAMESPACE_URI;
                    Element newElt = destDoc.createElementNS(uri, node.getNodeName());
                    dest.appendChild(newElt);
                    copyChildNodes((Element)node, newElt);
                    break;
                
                case Document.TEXT_NODE :
                    dest.appendChild(destDoc.createTextNode(node.getNodeValue()));
                    break;
                    
                case Document.CDATA_SECTION_NODE :
                    dest.appendChild(destDoc.createCDATASection(node.getNodeValue()));
                    break;
                
                case Document.COMMENT_NODE :
                    dest.appendChild(destDoc.createComment(node.getNodeValue()));
                    break;
                
                default :
                    // ignore
                    logger.log("SVGFormatter : unhandled node type " + node.getNodeType(), logger.DEBUG);
                    //System.err.println("Node of type " + node.getNodeType());
            }
        }
    }
}
