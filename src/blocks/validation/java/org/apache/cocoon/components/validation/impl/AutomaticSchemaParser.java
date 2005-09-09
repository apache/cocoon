/* ========================================================================== *
 * Copyright (C) 2004-2005 Pier Fumagalli <http://www.betaversion.org/~pier/> *
 *                            All rights reserved.                            *
 * ========================================================================== *
 *                                                                            *
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.       *
 *                                                                            *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software *
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT *
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the *
 * License for the  specific language  governing permissions  and limitations *
 * under the License.                                                         *
 *                                                                            *
 * ========================================================================== */

package org.apache.cocoon.components.validation.impl;

import java.io.IOException;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.SchemaParser;
import org.apache.cocoon.components.validation.Validator;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.sax.NOPContentHandler;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>The {@link AutomaticSchemaParser} is a {@link SchemaParser} implementation
 * automatically detecting the grammar used by the supplied grammar file.</p>
 * 
 * <p>Although detection might not be perfect, the detection algorithm implemented
 *  in the {@link #parseSchema(String)} method should be usable enough for normal
 *  operation.</p> 
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class AutomaticSchemaParser extends CachingSchemaParser {

    /** <p>The grammars identified by this {@link SchemaParser}.</p> */
    private static final String GRAMMARS [] = 
                                        new String[] { Validator.GRAMMAR_AUTOMATIC }; 

    /** <p>The {@link Validator} providing access to grammar parsers.</p> */
    protected Validator validator = null;
    
    /**
     * <p>Initialize this component instance.</p>
     * 
     * <p>A this point component resolution will happen.</p>
     */
    public void initialize()
    throws Exception {
        super.initialize();
        this.validator = (Validator) this.serviceManager.lookup(Validator.ROLE);
    }

    /**
     * <p>Dispose this component instance.</p>
     */
    public void dispose() {
        try {
            super.dispose();
        } finally {
            if (this.validator != null) this.serviceManager.release(this.validator);
        }
    }

    /**
     * <p>Freshly parsed a brand new {@link Schema} instance.</p>
     * 
     * <p>This method will first try to identify the grammar used in the schema from
     * the namespace of the root element, or (in case of a {@link SAXParseException})
     * it will assume it to be an {@link Validator#GRAMMAR_XML_DTD XML DTD}.</p>
     * 
     * <p>It will then return the schema parsed by the appropriate schema parser
     * registered in the {@link Validator}, so that it can be cached again.</p>
     * 
     * <p>Note that the implementation of the caching algorithm in the
     * {@link CachingSchemaParser} will create an extra entry in the current
     * configured {@link CachingSchemaParser#transientStore Store}, but it will
     * map simply a new name with the same {@link Schema} instance (so, there
     * shouldn't be major memory problems here).</p>
     */
    public Schema parseSchema(String uri)
    throws SAXException, IOException {
        SAXParser xmlParser = null;
        Source source = null;
        String grammar = null;

        try {
            ContentHandler handler = new DetectionHandler();

            source = this.sourceResolver.resolveURI(uri);
            
            if (source instanceof XMLizable) {
                XMLizable xmlizable = (XMLizable) source;
                xmlizable.toSAX(handler);
            } else {
                xmlParser = (SAXParser) this.serviceManager.lookup((SAXParser.ROLE)); 
                InputSource input = new InputSource();
                input.setSystemId(source.getURI());
                input.setByteStream(source.getInputStream());
                xmlParser.parse(input, handler);
            }
        } catch (DetectionException exception) {
            grammar = exception.grammar;
        } catch (SAXParseException exception) {
            grammar = Validator.GRAMMAR_XML_DTD;
        } catch (ServiceException exception) {
            throw new SAXException("Unable to locate XML parser", exception);
        } finally {
            if (source != null) this.sourceResolver.release(source);
            if (xmlParser != null) this.serviceManager.release(xmlParser);
        }
        
        if (grammar == null) throw new SAXException("Unable to detect grammar");
        
        SchemaParser schemaParser = null;
        try {
            schemaParser = (SchemaParser) this.validator.select(grammar);
            return schemaParser.getSchema(uri);
        } catch (ServiceException exception) {
            throw new SAXException("Unsupported grammar " + grammar, exception);
        } finally {
            if (schemaParser != null) this.validator.release(schemaParser);
        }
    }

    /**
     * <p>This {@link SchemaParser} supports the {@link Validator#GRAMMAR_AUTOMATIC}
     * grammar.</p>
     */
    public String[] getSupportedGrammars() {
        return GRAMMARS;
    }

    /**
     * <p>A simple implementation of a {@link ContentHandler} detecting schemas
     * based on the namespace of the root element.</p>
     */
    private static final class DetectionHandler extends NOPContentHandler {
        
        /**
         * <p>Detect the namespace of the root element and always throw a
         * {@link SAXException} or a {@link DetectionException}.</p>
         */
        public void startElement(String ns, String lnam, String qnam, Attributes a)
        throws SAXException {
            if ((Validator.GRAMMAR_RELAX_CORE.equals(ns)) || 
                (Validator.GRAMMAR_RELAX_NG.equals(ns)) ||
                (Validator.GRAMMAR_RELAX_NS.equals(ns)) ||
                (Validator.GRAMMAR_TREX.equals(ns)) ||
                (Validator.GRAMMAR_XML_SCHEMA.equals(ns))) {
                throw new DetectionException(ns);
            }
            throw new SAXException("Unknown validation grammar");
        }
    }

    /**
     * <p>An exception thrown by {@link DetectionHandler} representing  simple implementation of a {@link ContentHandler} detecting schemas
     * based on the namespace of the root element.</p>
     */
    private static final class DetectionException extends SAXException {

        private final String grammar;

        private DetectionException(String grammar) {
            super ("Grammar detected: " + grammar);
            this.grammar = grammar;
        }
    }
}
