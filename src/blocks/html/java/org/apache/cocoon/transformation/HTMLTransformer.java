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
package org.apache.cocoon.transformation;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.excalibur.source.Source;
import org.w3c.tidy.Tidy;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Converts (escaped) HTML snippets into JTidied HTML. 
 * This transformer expects a list of elements, passed as comma separated
 * values of the "tags" parameter. It records the text enclosed in such
 * elements and pass it thru JTidy to obtain valid XHTML.
 *
 * <p>TODO: Add namespace support.
 * <p><strong>WARNING:</strong> This transformer should be considered unstable.
 *
 * @author <a href="mailto:d.madama@pro-netics.com">Daniele Madama</a>
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 *
 * @version CVS $Id: HTMLTransformer.java,v 1.5 2004/06/08 19:02:52 vgritsenko Exp $
 */
public class HTMLTransformer
    extends AbstractSAXTransformer
    implements Configurable {

    /**
     * Properties for Tidy format
     */
    private Properties properties;
    
    /**
     * Tags that must be normalized
     */
    private Map tags;

    /**
     * React on endElement calls that contain a tag to be
     * tidied and run Jtidy on it, otherwise passthru.
     *
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String name, String raw)
        throws SAXException {
        if (this.tags.containsKey(name)) {
            String toBeNormalized = this.endTextRecording();
            try {
                this.normalize(toBeNormalized);
            } catch (ProcessingException e) {
                e.printStackTrace();
            }
        }
        super.endElement(uri, name, raw);
    }

    /**
     * Start buffering text if inside a tag to be normalized,
     * passthru otherwise.
     *
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(
        String uri,
        String name,
        String raw,
        Attributes attr)
        throws SAXException {
        super.startElement(uri, name, raw, attr);
		if (this.tags.containsKey(name)) {
            this.startTextRecording();
        }
    }

    /**
     * Configure this transformer, possibly passing to it
     * a jtidy configuration file location.
     */
    public void configure(Configuration config) throws ConfigurationException {
        String configUrl = config.getChild("jtidy-config").getValue(null);
        if (configUrl != null) {
            org.apache.excalibur.source.SourceResolver resolver = null;
            Source configSource = null;
            try {
                resolver = (org.apache.excalibur.source.SourceResolver)
                           this.manager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
                configSource = resolver.resolveURI(configUrl);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                        "Loading configuration from " + configSource.getURI());
                }
                this.properties = new Properties();
                this.properties.load(configSource.getInputStream());

            } catch (Exception e) {
                getLogger().warn("Cannot load configuration from " + configUrl);
                throw new ConfigurationException(
                    "Cannot load configuration from " + configUrl,
                    e);
            } finally {
                if (null != resolver) {
                    this.manager.release(resolver);
                    resolver.release(configSource);
                }
            }
        }
    }

    /**
     * The beef: run JTidy on the buffered text and stream
     * the result
     *
     * @param text the string to be tidied
     */
    private void normalize(String text) throws ProcessingException {
        try {
            // Setup an instance of Tidy.
            Tidy tidy = new Tidy();
            tidy.setXmlOut(true);

            if (this.properties == null) {
                tidy.setXHTML(true);
            } else {
                tidy.setConfigurationFromProps(this.properties);
            }

            //Set Jtidy warnings on-off
            tidy.setShowWarnings(getLogger().isWarnEnabled());
            //Set Jtidy final result summary on-off
            tidy.setQuiet(!getLogger().isInfoEnabled());
            //Set Jtidy infos to a String (will be logged) instead of System.out
            StringWriter stringWriter = new StringWriter();
            PrintWriter errorWriter = new PrintWriter(stringWriter);
            tidy.setErrout(errorWriter);

            // Extract the document using JTidy and stream it.
            ByteArrayInputStream bais =
                new ByteArrayInputStream(text.getBytes());
            org.w3c.dom.Document doc =
                tidy.parseDOM(new BufferedInputStream(bais), null);

            // FIXME: Jtidy doesn't warn or strip duplicate attributes in same
            // tag; stripping.
            XMLUtils.stripDuplicateAttributes(doc, null);

            errorWriter.flush();
            errorWriter.close();
            if (getLogger().isWarnEnabled()) {
                getLogger().warn(stringWriter.toString());
            }

            IncludeXMLConsumer.includeNode(doc, this.contentHandler, this.lexicalHandler);
        } catch (Exception e) {
            throw new ProcessingException(
                "Exception in HTMLTransformer.normalize()",
                e);
        }
    }

    /**
     * Setup this component, passing the tag names to be tidied.
     */

    public void setup(
        SourceResolver resolver,
        Map objectModel,
        String src,
        Parameters par)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        String tagsParam = par.getParameter("tags", "");        
        if (getLogger().isDebugEnabled()) {
        	getLogger().debug("tags: " + tagsParam);
        }        
        this.tags = new HashMap();
        StringTokenizer tokenizer = new StringTokenizer(tagsParam, ",");
        while (tokenizer.hasMoreElements()) {
            String tok = tokenizer.nextToken().trim();
            this.tags.put(tok, tok);
        }
    }
}
