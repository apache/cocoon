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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.validation.ValidationHandler;
import org.apache.cocoon.components.validation.Validator;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLMulticaster;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

/**
 * <p>The {@link ValidatingTransformer} provides a very simple {@link Transformer}
 * validating documents while being processed in a Cocoon pipeline.</p>
 * 
 * <p>The only defined (but not required) configuration for this component is
 * <code>&lt;grammar&gt;<i>...string...</i>&lt;/grammar&gt;</code>
 * indicating the default grammar language of the schemas to use.</p>
 * 
 * <p>This configuration parameter can be overridden by specifying the
 * <code>grammar</code> parameter when using this {@link Transformer} in a
 * pipeline.</p>
 * 
 * <p>If no grammar is specified (either as a configuration, or a parameter) this
 * transformer will instruct the {@link Validator} to try and guess the grammar
 * of the schema being parsed.</p>
 *
 */
public class ValidatingTransformer extends AbstractTransformer
implements Configurable, Serviceable, Disposable, CacheableProcessingComponent {

    /** <p>The configured {@link ServiceManager} instance.</p> */
    private ServiceManager serviceManager = null;
    /** <p>The configured {@link Validator} instance.</p> */
    private Validator validator = null;
    /** <p>The configured default grammar language.</p> */
    private String grammar = null;

    /** <p>The {@link ValidationHandler} to use in this transformation.</p> */
    private ValidationHandler handler = null;
    /** <p>A unique key identifying the schema source for caching.</p> */
    private String key = null;

    /**
     * <p>Create a new {@link ValidatingTransformer} instance.</p>
     */
    public ValidatingTransformer() {
        super();
    }

    /**
     * <p>Contextualize this component instance specifying its associated
     * {@link ServiceManager} instance.</p>
     * 
     * @param manager the {@link ServiceManager} to associate with this component.
     * @throws ServiceException if a dependancy of this could not be resolved.
     */
    public void service(ServiceManager manager)
    throws ServiceException {
        this.serviceManager = manager;
        this.validator = (Validator) manager.lookup(Validator.ROLE);
    }

    /**
     * <p>Configure this component instance.</p>
     * 
     * <p>The only defined (but not required) configuration for this component is
     * <code>&lt;grammar&gt;<i>...string...</i>&lt;/grammar&gt;</code>
     * indicating the default grammar used by this transformer used for parsing
     * schemas.</p>
     *
     * @param configuration a {@link Configuration} instance for this component.
     * @throws ConfigurationException never thrown.
     */
    public void configure(Configuration configuration)
    throws ConfigurationException {
        this.grammar = configuration.getChild("grammar").getValue(null);
    }

    /**
     * <p>Dispose of this component instance releasing all previously acquired
     * required instances back to the {@link ServiceManager}.</p>
     */
    public void dispose() {
        this.serviceManager.release(this.validator);
    }

    /**
     * <p>Contextualize this component in the scope of a pipeline when a request
     * is processed.</p>
     *
     * @param resolver the {@link SourceResolver} contextualized in this request.
     * @param objectModel unused.
     * @param source the source URI of the schema to validate against.
     * @param parameters unused.
     */
    public void setup(SourceResolver resolver, Map objectModel, String source,
                      Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        Source s = null;
        try {
            s = resolver.resolveURI(source);
            String g = parameters.getParameter("grammar", this.grammar);
            if (g == null) {
                this.handler = this.validator.getValidationHandler(s);
            } else{
                this.handler = this.validator.getValidationHandler(s, g);
            }
        } finally {
            if (source != null) resolver.release(s);
        }
    }

    /**
     * <p>Specify the {@link XMLConsumer} receiving SAX events emitted by this
     * {@link Transformer} instance in the scope of a request.</p>
     *
     * @param consumer the {@link XMLConsumer} to send SAX events to.
     */
    public void setConsumer(XMLConsumer consumer) {
        XMLConsumer handler = new ContentHandlerWrapper(this.handler, this.handler);
        super.setConsumer(new XMLMulticaster(handler, consumer));
    }

    /**
     * <p>Return the unique key to associated with the schema being processed in
     * the scope of the request being processed for caching.</p>
     *
     * @return a non null {@link String} representing the unique key for the schema.
     */
    public Serializable getKey() {
        return this.key;
    }

    /**
     * <p>Return the {@link SourceValidity} associated with the schema currently
     * being processed in the scope of the request being processed.</p>
     *
     * @return a non null {@link SourceValidity} instance.
     */
    public SourceValidity getValidity() {
        return this.handler.getValidity();
    }

    /**
     * <p>Recycle this component instance at the end of request processing.</p>
     */
    public void recycle() {
        this.handler = null;
        this.key = null;
        super.recycle();
    }
}
