/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:   "This product includes software
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
 * @version CVS $Id: SlopGenerator.java,v 1.3 2003/09/04 09:38:32 cziegeler Exp $
 */

public class SlopGenerator extends ServiceableGenerator
        implements CacheableProcessingComponent {

    private Source inputSource = null;
    private String encoding = null;
    private SlopParser parser = null;

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
        parser = null;

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
            inputSource = resolver.resolveURI(src);
            parser = new SimpleSlopParser();
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