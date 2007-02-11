/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.validation.impl;

import java.io.IOException;

import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.SchemaParser;
import org.apache.cocoon.components.validation.ValidatorException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.store.Store;
import org.xml.sax.SAXException;

/**
 * <p>An extension of the {@link DefaultValidator} class allowing {@link Schema}
 * instances to be cached.</p>
 * 
 * <p>The {@link #getSchema(SchemaParser, Source, String)} method will manage
 * whether to return a cached or a freshly parsed {@link Schema} instance.</p>
 *
 */
public class CachingValidator extends DefaultValidator {

    /** <p>The {@link Store} used for caching {@link Schema}s (if enabled).</p> */
    private Store store = null;

    /**
     * <p>Create a new {@link CachingValidator} instance.</p>
     */
    public CachingValidator() {
        super();
    }
    
    /**
     * <p>Initialize this component instance.</p>
     */
    public void initialize()
    throws Exception {
        this.store = (Store) this.manager.lookup(Store.TRANSIENT_STORE);
        super.initialize();
    }
    
    /**
     * <p>Dispose this component instance.</p>
     */
    public void dispose() {
        try {
            super.dispose();
        } finally {
            if (this.store != null) this.manager.release(this.store);
        }
    }

    /**
     * <p>Return a {@link Schema} instance from the specified {@link SchemaParser}
     * associated with the given {@link Source} and grammar language.</p>
     * 
     * <p>This method will overriding the default behaviour specified by the
     * {@link AbstractValidator#getSchema(SchemaParser, Source, String)} method,
     * and supports cacheability of {@link Schema} instances through the use of
     * a {@link Store} looked up using the {@link Store#TRANSIENT_STORE} Avalon
     * role.</p>
     * 
     * <p>Cached {@link Schema} instances will be retained in the configured
     * {@link Store} until the checks on the validity obtained calling the
     * {@link Schema#getValidity()} method will declare that the schema is still
     * valid.</p>
     * 
     * @param parser the {@link SchemaParser} producing the {@link Schema}.
     * @param source the {@link Source} associated with the {@link Schema} to return.
     * @param grammar the grammar language of the schema to produce.
     * @throws SAXException if a grammar error occurred parsing the schema.
     * @throws IOException if an I/O error occurred parsing the schema.
     */
    public Schema getSchema(SchemaParser parser, Source source, String grammar)
    throws IOException, SAXException {

        /* Prepare a key, and try to get the cached copy of the schema */
        String uri = source.getURI();
        String key = this.getClass().getName() + "[" + parser.getClass().getName()
                     + ":" + grammar + "]@" + source.getURI();
        Schema schema = null;
        SourceValidity validity = null;
        schema = (Schema) this.store.get(key);

        /* If the schema was found verify its validity and optionally clear */
        if (schema != null) {
            validity = schema.getValidity();
            if (validity == null) {
                /* Why did we cache it in the first place? */
                this.logger.warn("Cached schema " + uri + " has null validity");
                this.store.remove(key);
                schema = null;
            } else if (validity.isValid() != SourceValidity.VALID) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Cached schema " + uri + " no longer valid");
                }
                this.store.remove(key);
                schema = null;
            } else if (this.logger.isDebugEnabled()) {
                this.logger.debug("Valid cached schema found for " + uri);
            }
        } else if (this.logger.isDebugEnabled()) {
            this.logger.debug("Schema " + uri + " not found in cache");
        }

        /* If the schema was not cached or was cleared, parse and cache it */
        if (schema == null) {
            schema = super.getSchema(parser, source, grammar);
            validity = schema.getValidity();
            if (validity != null) {
                if (validity.isValid() == SourceValidity.VALID) {
                    this.store.store(key, schema);
                }
            }
        }

        /* Return the parsed or cached schema */
        return schema;
    }    

    /**
     * <p>Attempt to detect the grammar language used by the schema identified
     * by the specified {@link Source}.</p>
     * 
     * <p>The grammar languages detected will be cached until the {@link Source}'s
     * {@link SourceValidity} declares that the schema is valid.</p>
     * 
     * @param source a {@link Source} instance pointing to the schema to be analyzed.
     * @throws IOException if an I/O error occurred accessing the schema.
     * @throws SAXException if an error occurred parsing the schema.
     * @throws ValidatorException if the language of the schema could not be guessed.
     */
    protected String detectGrammar(Source source)
    throws IOException, SAXException, ValidatorException {
        /* Prepare a key, and try to get the cached copy of the schema */
        String uri = source.getURI();
        String key = this.getClass().getName() + "@" + source.getURI();

        CachedGrammar grammar = null;
        grammar = (CachedGrammar) this.store.get(key);

        /* If the schema was found verify its validity and optionally clear */
        if (grammar != null) {
            if (grammar.validity == null) {
                /* Why did we cache it in the first place? */
                this.logger.warn("Grammar for " + uri + " has null validity");
                this.store.remove(key);
                grammar = null;
            } else if (grammar.validity.isValid() != SourceValidity.VALID) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Grammar for " + uri + " no longer valid");
                }
                this.store.remove(key);
                grammar = null;
            } else if (this.logger.isDebugEnabled()) {
                this.logger.debug("Valid cached grammar " + grammar + " for " + uri);
            }
        }

        /* If the schema was not cached or was cleared, parse and cache it */
        if (grammar != null) {
            return grammar.grammar;
        } else {
            String language = super.detectGrammar(source);
            SourceValidity validity = source.getValidity();
            if (validity != null) {
                if (validity.isValid() == SourceValidity.VALID) {
                    this.store.store(key, new CachedGrammar(validity, language));
                }
            }
            return language;
        }
    }
    
    /**
     * <p>A simple inner class associating grammar languages and source validity
     * for caching of schema grammar detection.</p>
     */
    private static final class CachedGrammar {
        private final SourceValidity validity;
        private final String grammar;
        private CachedGrammar(SourceValidity validity, String grammar) {
            this.validity = validity;
            this.grammar = grammar;
        }
    }
}
