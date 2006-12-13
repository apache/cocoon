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
package org.apache.cocoon.components.validation.jaxp;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.SchemaFactory;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.Validator;
import org.apache.cocoon.components.validation.impl.AbstractSchemaParser;
import org.apache.cocoon.components.validation.impl.DraconianErrorHandler;
import org.apache.excalibur.source.Source;
import org.xml.sax.SAXException;

/**
 * <p>An implementation of the {@link SchemaParser} interface wrapping JAXP
 * {@link SchemaFactory} instances.</p>
 *
 */
public class JaxpSchemaParser extends AbstractSchemaParser
implements Configurable, ThreadSafe {

    /** <p>The class name of the {@link SchemaFactory} to use.</p> */
    private String className = null;
    /** <p>The list of grammars supported by this instance.</p> */
    private String[] grammars = null;

    /**
     * <p>Create a new {@link JaxpSchemaParser} instance.</p>
     */
    public JaxpSchemaParser() {
        super();
    }

    /**
     * <p>Configure this instance.</p>
     *
     * <p>The {@link JaxpSchemaParser} requires at least one configuration element:
     * <code>&lt;factory-class&gt;<i>class name</i>&lt;/factory-class&gt;</code>.
     * This specifies the JAXP {@link SchemaFactory} class to be used by this
     * instance.</p>
     * 
     * <p>Grammars will be automatically detected if the {@link SchemaFactory}
     * supports one of the {@link Validator.GRAMMAR_RELAX_NG RELAX-NG} grammar,
     * {@link Validator.GRAMMAR_XML_SCHEMA XML-Schema} grammar, or the
     * {@link Validator.GRAMMAR_XML_DTD XML-DTD} grammar.</p>
     * 
     * <p>If the factory is known to support different grammars, the default
     * detection can be overridden specifying in the configuration something similar
     * to the following:</p>
     * 
     * <pre>
     *   &lt;grammars&gt;
     *     &lt;grammar&gt;... a first grammar identifier ...&lt;/grammar&gt;
     *     &lt;grammar&gt;... another grammar identifier ...&lt;/grammar&gt;
     *   &lt;/grammars&gt;
     * </pre>
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        this.className = conf.getChild("factory-class").getValue();
        final SchemaFactory fact;
        try {
            fact = (SchemaFactory) Class.forName(this.className).newInstance();
        } catch (Exception exception) {
            String message = "Unable to instantiate factory " + this.className;
            throw new ConfigurationException(message, conf, exception);
        }

        /* Detect languages or use the supplied ones */
        Configuration languages[] = conf.getChild("grammars").getChildren("grammar");
        Set grammars = new HashSet();
        if (languages.length > 0) {

            /* If the configuration specified (formally) a list of grammars use it */
            for (int x = 0; x < languages.length; x++) {
                String language = languages[x].getValue();
                if (fact.isSchemaLanguageSupported(language)) {
                    grammars.add(language);
                    continue;
                }
                /* If the configured language is not supported throw an exception */
                String message = "JAXP SchemaFactory \"" + this.className + "\" " +
                                 "does not support configured grammar " + language;
                throw new ConfigurationException(message, languages[x]);
            }
        } else {

            /* Attempt to detect the languages directly using the JAXP factory */
            if (fact.isSchemaLanguageSupported(Validator.GRAMMAR_XML_SCHEMA)) {
                grammars.add(Validator.GRAMMAR_XML_SCHEMA);
            }
            if (fact.isSchemaLanguageSupported(Validator.GRAMMAR_RELAX_NG)) {
                grammars.add(Validator.GRAMMAR_RELAX_NG);
            }
            if (fact.isSchemaLanguageSupported(Validator.GRAMMAR_XML_DTD)) {
                grammars.add(Validator.GRAMMAR_XML_DTD);
            }
        }
        
        /* Store our grammars */
        this.grammars = (String[]) grammars.toArray(new String[grammars.size()]);
    }

    /**
     * <p>Parse the specified {@link Source} and return a new {@link Schema}.</p>
     * 
     * <p>The returned {@link Schema} must be able to validate multiple documents
     * via multiple invocations of {@link Schema#createValidator(ErrorHandler)}.</p> 
     *
     * @param source the {@link Source} associated with the {@link Schema} to return.
     * @return a <b>non-null</b> {@link Schema} instance.
     * @throws SAXException if a grammar error occurred parsing the schema.
     * @throws IOException if an I/O error occurred parsing the schema.
     * @throws IllegalArgumentException if the specified grammar type is not one
     *                                  of the grammar types returned by the
     *                                  {@link #getSupportedGrammars()} method.  
     */
    public Schema parseSchema(Source source, String grammar)
    throws SAXException, IOException {
        final SchemaFactory factory;
        try {
            factory = (SchemaFactory) Class.forName(this.className).newInstance();
        } catch (Exception exception) {
            String message = "Unable to instantiate factory " + this.className;
            throw new SAXException(message, exception);
        }
        
        JaxpResolver r = new JaxpResolver(this.sourceResolver, this.entityResolver);
        SAXSource s = new SAXSource(r.resolveSource(source));
        factory.setErrorHandler(DraconianErrorHandler.INSTANCE);
        factory.setResourceResolver(r);
        
        return new JaxpSchema(factory.newSchema(s), r.close());
    }

    public String[] getSupportedGrammars() {
        return this.grammars;
    }
}
