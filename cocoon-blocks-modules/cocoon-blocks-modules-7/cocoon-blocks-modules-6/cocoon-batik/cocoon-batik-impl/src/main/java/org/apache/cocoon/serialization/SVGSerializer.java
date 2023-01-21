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
package org.apache.cocoon.serialization;

import java.awt.Color;
import java.io.OutputStream;
import java.io.Serializable;

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
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.util.ParsedURL;
import org.apache.cocoon.Constants;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.transcoder.ExtendableTranscoderFactory;
import org.apache.cocoon.components.transcoder.TranscoderFactory;
import org.apache.cocoon.components.url.ParsedContextURLProtocolHandler;
import org.apache.cocoon.components.url.ParsedResourceURLProtocolHandler;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.xml.dom.SVGBuilder;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A <a href="http://xml.apache.org/batik/">Batik</a> based Serializer for
 * generating PNG/JPEG images
 *
 * @cocoon.sitemap.component.documentation
 * A <a href="http://xml.apache.org/batik/">Batik</a> based Serializer for
 * generating PNG/JPEG images
 * @cocoon.sitemap.component.documentation.caching Yes
 *
 * @version $Id$
 */
public class SVGSerializer extends SVGBuilder
                           implements Contextualizable, Configurable, CacheableProcessingComponent,
                                      Serializer {

    /**
     * Get the context
     */
    public void contextualize(Context context) throws ContextException {
        ParsedContextURLProtocolHandler.setContext(
            (org.apache.cocoon.environment.Context)context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT));
        ParsedURL.registerHandler(new ParsedContextURLProtocolHandler());
        ParsedURL.registerHandler(new ParsedResourceURLProtocolHandler());
    }

    /** The current <code>OutputStream</code>. */
    private OutputStream output;

    /** The current <code>mime-type</code>. */
    private String mimetype;

    /** The current <code>Transcoder</code>.  */
    Transcoder transcoder;

    /** The Transcoder Factory to use */
    TranscoderFactory factory = ExtendableTranscoderFactory.getTranscoderFactoryImplementation();
    
    // private ServiceManager manager;
    // private SourceResolver resolver;

    /**
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        this.output = out;
        
        // Give the source resolver to Batik
        //SourceProtocolHandler.setup(this.resolver);
    }
    
/*    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    public void dispose() {
        this.manager.release(this.resolver);
    }
*/
    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        this.mimetype = conf.getAttribute("mime-type");
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("mime-type: " + mimetype);
        }

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
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Cannot load  class " + transcoderName, ex);
                    }
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
            if ("transcoder".equals(name)) {
                continue;
            }

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
                    value = Boolean.valueOf(parameters[i].getAttributeAsBoolean("value"));
                } else if ("COLOR".equals(keyType)) {
                    // Can throw an exception
                    String stringValue = parameters[i].getAttribute("value");
                    if (stringValue.startsWith("#")) {
                        stringValue = stringValue.substring(1);
                    }
                    value = new Color(Integer.parseInt(stringValue, 16));
                } else {
                    // Assume String, and get the value. Allow an empty string.
                    value = parameters[i].getAttribute("value", "");
                }
                if(getLogger().isDebugEnabled()) {
                    getLogger().debug("Adding hint \"" + name + "\" with value \"" + value.toString() + "\"");
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
     * Receive notification of a successfully completed DOM tree generation.
     */
    public void notify(Document doc) throws SAXException {
        
        try {
            TranscoderInput transInput = new TranscoderInput(doc);

            // Buffering is done by the pipeline (See shouldSetContentLength)
            TranscoderOutput transOutput = new TranscoderOutput(this.output);
            transcoder.transcode(transInput, transOutput);
        } catch (TranscoderException ex) {
            if (ex.getException() != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Got transcoder exception writing image, rethrowing nested exception", ex);
                }
                throw new SAXException("Exception writing image", ex.getException());
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Got transcoder exception writing image, rethrowing", ex);
            }
            throw new SAXException("Exception writing image", ex);
        } catch (Exception ex) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Got exception writing image, rethrowing", ex);
            }
            throw new SAXException("Exception writing image", ex);
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
     * This method must be invoked before the getValidity() method.
     *
     * @return The generated key or <code>0</code> if the component
     *              is currently not cacheable.
     */
    public Serializable getKey() {
        return "1";
    }

    /**
     * Generate the validity object.
     * Before this method can be invoked the getKey() method
     * must be invoked.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

    /**
     * Returns true so the pipeline implementation will buffer generated
     * output and write content length to the response.
     *
     * <p>Batik's PNGTranscoder closes the output stream, therefore we
     * cannot pass the output stream directly to Batik and have to
     * instruct pipeline to buffer it. If we do not buffer, we would get
     * an exception when
     * {@link org.apache.cocoon.Cocoon#process(org.apache.cocoon.environment.Environment)}
     * tries to close the stream.
     */
    public boolean shouldSetContentLength() {
        return true;
    }
}
