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
import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.SchemaParser;
import org.apache.cocoon.components.validation.Validator;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLMulticaster;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

/**
 * <p>The {@link ValidatingTransformer} provides a very simple {@link Transformer}
 * validating documents while being processed in a Cocoon pipeline.</p>
 * 
 * <p>The only defined (but not required) configuration for this component is
 * <code>&lt;language&gt;<i>...string...</i>&lt;/language&gt;</code>
 * indicating the language (or optionally the component name) for the
 * {@link Validator} instance providing access to {@link Schema}s.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class ValidatingTransformer extends AbstractTransformer
implements Configurable, Serviceable, Disposable, CacheableProcessingComponent {

    private ServiceManager serviceManager = null;
    private Validator validator = null;
    private SchemaParser schemaParser = null;
    private Schema schema = null;
    private String key = null;

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
     * <code>&lt;language&gt;<i>...string...</i>&lt;/language&gt;</code>
     * indicating the language (or optionally the component name) for the
     * {@link Validator} instance providing access to {@link Schema}s.</p>
     *
     * @param configuration a {@link Configuration} instance for this component.
     * @throws ConfigurationException never thrown.
     */
    public void configure(Configuration configuration)
    throws ConfigurationException {
        String key = configuration.getChild("language").getValue();
        try {
            this.schemaParser = (SchemaParser) this.validator.select(key);
        } catch (ServiceException exception) {
            String message = "Language or instance \"" + key + "\" not recognized";
            throw new ConfigurationException(message, configuration, exception);
        }
    }

    /**
     * <p>Dispose of this component instance releasing all previously acquired
     * required instances back to the {@link ServiceManager}.</p>
     */
    public void dispose() {
        this.validator.release(this.schemaParser);
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
        this.schema = this.schemaParser.getSchema(source);
        this.key = this.getClass().getName() + ":" +
                   this.schemaParser.getClass().getName() + ":" + source;
    }

    /**
     * <p>Specify the {@link XMLConsumer} receiving SAX events emitted by this
     * {@link Transformer} instance in the scope of a request.</p>
     *
     * @param consumer the {@link XMLConsumer} to send SAX events to.
     */
    public void setConsumer(XMLConsumer consumer) {
        XMLConsumer handler = new ContentHandlerWrapper(this.schema.newValidator());
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
        return this.schema.getValidity();
    }

    /**
     * <p>Recycle this component instance at the end of request processing.</p>
     */
    public void recycle() {
        this.schema = null;
        this.key = null;
        super.recycle();
    }
}
