/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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
package org.apache.cocoon.serialization;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.util.ParsedURL;
import org.apache.cocoon.Constants;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.transcoder.ExtendableTranscoderFactory;
import org.apache.cocoon.components.transcoder.TranscoderFactory;
import org.apache.cocoon.components.url.ParsedContextURLProtocolHandler;
import org.apache.cocoon.components.url.ParsedResourceURLProtocolHandler;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.dom.SVGBuilder;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * A Batik based Serializer for generating PNG/JPEG images
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:rossb@apache.org">Ross Burton</a>
 * @version CVS $Id: SVGSerializer.java,v 1.3 2003/03/24 14:33:57 stefano Exp $
 */
public class SVGSerializer extends SVGBuilder
implements Composable, Serializer, Configurable, Poolable, CacheableProcessingComponent, Contextualizable {

    /**
     * Get the context
     */
    public void contextualize(Context context) throws ContextException {
        ParsedContextURLProtocolHandler.setContext(
            (org.apache.cocoon.environment.Context)context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT));
        ParsedURL.registerHandler(new ParsedContextURLProtocolHandler());
        ParsedURL.registerHandler(new ParsedResourceURLProtocolHandler());
    }

    /** The <code>ContentHandler</code> receiving SAX events. */
    private ContentHandler contentHandler=null;
    /** The <code>LexicalHandler</code> receiving SAX events. */
    private LexicalHandler lexicalHandler=null;
    /** The component manager instance */
    private ComponentManager manager=null;
    /** The current <code>OutputStream</code>. */
    private OutputStream output=null;
    /** The current <code>mime-type</code>. */
    private String mimetype = null;
    /** The current <code>Transcoder</code>.  */
    Transcoder transcoder = null;
    /** The Transcoder Factory to use */
    TranscoderFactory factory = ExtendableTranscoderFactory.getTranscoderFactoryImplementation();

    /**
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        this.output = out;
    }

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        this.mimetype = conf.getAttribute("mime-type");
        log.debug("SVGSerializer mime-type:" + mimetype);

        // Using the Transcoder Factory, get the default transcoder
        // for this MIME type.
        this.transcoder = factory.createTranscoder(mimetype);
        // Iterate through the parameters, looking for a transcoder reference
        Configuration[] parameters = conf.getChildren("parameter");
        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getAttribute("name");
            if ("transcoder".equals(name)) {
                String transcoderName = parameters[i].getAttribute("value");
                try {
                    this.transcoder = (Transcoder)ClassUtils.newInstance(transcoderName);
                } catch (Exception ex) {
                    log.error("Cannot load  class " + transcoderName, ex);
                    throw new ConfigurationException("Cannot load class " + transcoderName, ex);
                }
            }
        }
        // Do we have a transcoder yet?
        if (this.transcoder == null ) {
            throw new ConfigurationException(
                "Could not autodetect transcoder for SVGSerializer and "
                + "no transcoder was specified in the sitemap configuration."
            );
        }

        // Now run through the other parameters, using them as hints
        // to the transcoder
        for (int i = 0; i < parameters.length; i++ ) {
            String name = parameters[i].getAttribute("name");
            // Skip over the parameters we've dealt with. Ensure this
            // is kept in sync with the above list!
            if ("transcoder".equals(name)) continue;
            // Now try and get the hints out
            try {
                // Turn it into a key name (assume the current Batik style continues!
                name = ("KEY_" + name).toUpperCase();
                // Use reflection to get a reference to the key object
                TranscodingHints.Key key = (TranscodingHints.Key)
                    (transcoder.getClass().getField(name).get(transcoder));
                Object value;
                String keyType = parameters[i].getAttribute("type", "STRING").toUpperCase();
                if ("FLOAT".equals(keyType)) {
                    // Can throw an exception.
                    value = new Float(parameters[i].getAttributeAsFloat("value"));
                } else if ("INTEGER".equals(keyType)) {
                    // Can throw an exception.
                    value = new Integer(parameters[i].getAttributeAsInteger("value"));
                } else if ("BOOLEAN".equals(keyType)) {
                    // Can throw an exception.
                    value = new Boolean(parameters[i].getAttributeAsBoolean("value"));
                } else if ("COLOR".equals(keyType)) {
                  // Can throw an exception
                  String stringValue = parameters[i].getAttribute("value");
                  if (stringValue.startsWith("#")) {
                    stringValue = stringValue.substring(1);
                  }
                  value = new Color(Integer.parseInt(stringValue, 16));
                } else {
                    // Assume String, and get the value. Allow an empty string.
                    value = parameters[i].getValue("");
                }
                if(log.isDebugEnabled()) {
                    log.debug("SVG Serializer: adding hint \"" + name + "\" with value \"" + value.toString() + "\"");
                }
                transcoder.addTranscodingHint(key, value);
            } catch (ClassCastException ex) {
                // This is only thrown from the String keyType... line
                throw new ConfigurationException("Specified key (" + name + ") is not a valid Batik Transcoder key.", ex);
            } catch (ConfigurationException ex) {
                throw new ConfigurationException("Name or value not specified.", ex);
            } catch (IllegalAccessException ex) {
                throw new ConfigurationException("Cannot access the key for parameter \"" + name + "\"", ex);
            } catch (NoSuchFieldException ex) {
                throw new ConfigurationException("No field available for parameter \"" + name + "\"", ex);
            }
        }
    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composable</code>.
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
            TranscoderInput transInput = new TranscoderInput(doc);
 
            // Batik's PNGTranscoder closes the output stream.
            // Therefore we cannot pass it this.output directly.
            // Otherwise we get an exception when Cocoon.process
            // tries to flush/close the stream again.
            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            TranscoderOutput transOutput = new TranscoderOutput(baos);
            transcoder.transcode(transInput, transOutput);
            baos.writeTo(this.output);
        } catch (Exception ex) {
            log.error("SVGSerializer: Exception writing image", ex);
            throw new SAXException("Exception writing image ", ex);
        }
    }

    /**
     * Return the MIME type.
     */
    public String getMimeType() {
        return mimetype;
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     * This method must be invoked before the generateValidity() method.
     *
     * @return The generated key or <code>0</code> if the component
     *              is currently not cacheable.
     */
    public java.io.Serializable getKey() {
        return "1";
    }

    /**
     * Generate the validity object.
     * Before this method can be invoked the generateKey() method
     * must be invoked.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

    /**
     * Test if the component wants to set the content length
     */
    public boolean shouldSetContentLength() {
        return false;
    }

}
