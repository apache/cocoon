/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

package org.apache.cocoon.slop.generation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.slop.interfaces.SlopParser;
import org.apache.cocoon.slop.parsing.SimpleSlopParser;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.LocatorImpl;

/**
 * SlopGenerator: Simple Line-Oriented Parsing of text files.
 * General code structure lifted from the Chaperon TextGenerator - thanks Stephan!
 *
 * @author <a href="mailto:bdelacretaz@apache.org">Bertrand Delacretaz</a>
 * @version CVS $Id: SlopGenerator.java,v 1.5 2004/03/05 13:02:23 bdelacretaz Exp $
 */

public class SlopGenerator extends ServiceableGenerator
        implements CacheableProcessingComponent {

    private Source inputSource = null;
    private String encoding = null;
    private SlopParser parser = null;
    private boolean preserveSpace = false;
    private String validTagnameChars = null;

    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        if (inputSource != null) {
            super.resolver.release(inputSource);
        }
        inputSource = null;
        encoding = null;
        preserveSpace = false;
        parser = null;
        validTagnameChars = null;

        super.recycle();
    }

    /**
     * Set the SourceResolver, objectModel Map, the source and sitemap
     * Parameters used to process the request.
     *
     * @param resolver Source resolver
     * @param objectmodel Object model
     * @param src Source
     * @param parameters Parameters
     *
     * @throws java.io.IOException
     * @throws org.apache.cocoon.ProcessingException
     * @throws org.xml.sax.SAXException
     */
    public void setup(SourceResolver resolver, Map objectmodel, String src, Parameters parameters)
            throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectmodel, src, parameters);
        try {
            encoding = parameters.getParameter("encoding", null);
            preserveSpace = parameters.getParameterAsBoolean("preserve-space",false);
            validTagnameChars = parameters.getParameter("valid-tagname-chars",null);
            inputSource = resolver.resolveURI(src);

            final SimpleSlopParser ssp = new SimpleSlopParser();
            parser = ssp;
            ssp.setPreserveWhitespace(preserveSpace);
            ssp.setValidTagnameChars(validTagnameChars);
        } catch (SourceException se) {
            throw new ProcessingException("Error during resolving of '" + src + "'.", se);
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public Serializable getKey() {
        return inputSource.getURI();
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return this.inputSource.getValidity();
    }

    /**
     * Generate XML data.
     *
     * @throws java.io.IOException
     * @throws org.apache.cocoon.ProcessingException
     * @throws org.xml.sax.SAXException
     */
    public void generate()
            throws IOException, SAXException, ProcessingException {

        // access input data, using specified encoding if any
        InputStreamReader in = null;

        try {
            if (this.inputSource.getInputStream() == null) {
                throw new ProcessingException("Source '" + this.inputSource.getURI() + "' not found");
            }

            if (encoding != null) {
                in = new InputStreamReader(this.inputSource.getInputStream(), encoding);
            } else {
                in = new InputStreamReader(this.inputSource.getInputStream());
            }
        } catch (SourceException se) {
            throw new ProcessingException("Error during resolving of '" + this.source + "'.", se);
        }

        // setup a Locator in case parser detects input errors
        final LocatorImpl locator = new LocatorImpl();

        locator.setSystemId(this.inputSource.getURI());
        locator.setLineNumber(1);
        locator.setColumnNumber(1);

        contentHandler.setDocumentLocator(locator);

        // start parsing, read and process all input lines
        parser.startDocument(contentHandler);

        LineNumberReader reader = new LineNumberReader(in);
        String line, newline = null;

        while (true) {
            if (newline == null) {
                line = reader.readLine();
            } else {
                line = newline;
            }

            if (line == null) {
                break;
            }

            newline = reader.readLine();

            locator.setLineNumber(reader.getLineNumber());
            locator.setColumnNumber(1);
            parser.processLine(line);

            if (newline == null) {
                break;
            }
        }

        // done parsing
        parser.endDocument();
    }
}