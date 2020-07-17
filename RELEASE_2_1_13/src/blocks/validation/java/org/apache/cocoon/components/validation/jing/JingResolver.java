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
package org.apache.cocoon.components.validation.jing;

import java.io.IOException;
import java.util.Stack;

import org.apache.cocoon.components.validation.impl.ValidationResolver;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.thaiopensource.xml.sax.XMLReaderCreator;

/**
 * <p>A simple resolver used when parsing RELAX NG schemas through the use of
 * <a href="http://www.thaiopensource.com/relaxng/jing.html">JING</a>.</p>
 *
 * <p>This is not thread safe and not recyclable. Once used, it <b>must</b> be
 * garbage collected.</p>
 *
 */
public class JingResolver extends ValidationResolver implements XMLReaderCreator {

    /** <p>The current {@link Stack} of {@link InputSource}s being parsed. </p> */
    private final Stack parsedSourceStack = new Stack();

    /**
     * <p>Create a new {@link JingResolver} instance.</p>
     */
    public JingResolver(SourceResolver sourceResolver,
                       EntityResolver entityResolver) {
        super(sourceResolver, entityResolver);
        this.parsedSourceStack.push(null);
    }

    /**
     * <p>Push a new {@link InputSource} in the stack used for relative URIs
     * resolution.</p>
     */
    public void pushInputSource(InputSource inputSource) {
        this.parsedSourceStack.push(inputSource);
    }

    /**
     * <p>Pop the last {@link InputSource} from the stack used for relative URIs
     * resolution.</p>
     */
    public InputSource popInputSource() {
        if (this.parsedSourceStack.empty()) return null;
        return (InputSource) this.parsedSourceStack.pop();
    }

    /**
     * <p>Return the {@link PropertyMap} associated with this instance and usable
     * by <a href="http://www.thaiopensource.com/relaxng/jing.html">JING</a>.</p>
     */
    //public PropertyMap getProperties() {
    //    return this.validatorProperties;
    //}

    /* =========================================================================== */
    /* SAX2 ENTITY RESOLVER INTERFACE IMPLEMENTATION                               */
    /* =========================================================================== */

    /**
     * <p>Resolve an {@link InputSource} from a public ID and/or a system ID.</p>
     * 
     * <p>This method can be called only while a schema is being parsed and will
     * resolve URIs against a dynamic {@link Stack} of {@link InputSource}s.</p>
     *
     * <p>Since <a href="http://www.thaiopensource.com/relaxng/jing.html">JING</a>
     * doesn't offer a complete URI resolution contract, a {@link Stack} is kept
     * for all the sources parsed while reading a schema. Keeping in mind that the
     * last {@link InputSource} pushed in the {@link Stack} can be considered to be
     * the "base URI" for the current resolution, full relative resolution of system
     * IDs can be achieved this way.<p>
     * 
     * <p>Note that this method of resolving URIs by keeping a {@link Stack} of
     * processed URIs is a <i>sort of a hack</i>, but it mimics the internal state
     * of <a href="http://www.thaiopensource.com/relaxng/jing.html">JING</a> itself:
     * if URI resolution fails, the {@link Stack} analysis is the first part to
     * look at.</p>
     * 
     * <p>Resolution will use the {@link EntityResolver} specified at construction
     * to resolve public IDs, and then the {@link SourceResolver} again specified at
     * construction to resolve the system IDs and to access the underlying byte
     * streams of the entities to be parsed.</p>
     * 
     * @param publicId the public ID of the entity to resolve.
     * @param systemId the system ID of the entity to resolve.
     * @return a <b>non-null</b> {@link InputSource} instance.
     * @throws IOException if an I/O error occurred resolving the entity.
     * @throws SAXException if an XML error occurred resolving the entity.
     */
    public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException, IOException {
        InputSource source = (InputSource) this.parsedSourceStack.peek();
        if (source == null) {
            return super.resolveEntity(publicId, systemId);
        } else {
            return super.resolveEntity(source.getSystemId(), publicId, systemId);
        }
    }


    /* =========================================================================== */
    /* CALL JING TO ACCESS A CACHED OR FRESHLY PARSED SCHEMA INSTANCE              */
    /* =========================================================================== */

    /**
     * <p>Create an {@link XMLReader} instance that can be used by
     * <a href="http://www.thaiopensource.com/relaxng/jing.html">JING</a> for
     * parsing schemas.</p>
     * 
     * <p>The returned {@link XMLReader} will keep track of populating/clearing the
     * {@link Stack} of {@link InputSource}s kept for URI resolution as explained
     * in the description of the {@link #resolveEntity(String, String)} method.</p>
     * 
     * @see JingReader
     * @return a <b>non-null</b> {@link XMLReader} instance.
     * @throws SAXException if an error occurrent creating the {@link XMLReader}.
     */
    public XMLReader createXMLReader()
    throws SAXException {
        return new JingReader(this);
    }
}
