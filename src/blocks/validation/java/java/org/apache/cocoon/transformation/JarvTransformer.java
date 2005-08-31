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
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLMulticaster;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.xml.EntityResolver;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>TODO: ...</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class JarvTransformer extends AbstractTransformer
implements Configurable, Serviceable, Disposable,
           CacheableProcessingComponent, ErrorHandler, EntityResolver {
    
    /** <p>The class name of the MSV verifier factory.</p> */
    private static final String MSV = "com.sun.msv.verifier.jarv.TheFactoryImpl";

    /* VARIABLES CREATED BY SERVICE() OR CONFIGURE() AND DESTROYED BY DISPOSE() */

    /** <p>The {@link ServiceManager} associated with this instance.</p> */
    private ServiceManager serviceManager = null;
    /** <p>Cocoon's global {@link EntityResolver} for catalog resolution.</p> */
    private EntityResolver entityResolver = null;
    /** <p>Cocoon's global temporary cache {@link Store} for parsed schemas.</p> */
    private Store transientStore = null;
    /** <p>The {@link VerifierFactory} configured for this instance.</p> */
    private VerifierFactory verifierFactory = null; 
    /** <p>The language of the configured {@link VerifierFactory}.</p> */
    private String verifierLanguage = null;
    /** <p>A {@link String} for unique IDs stored into caches.</p> */
    private String uniqueIdentifier = null;
    /** <p>A flag indicating whether caching is enabled or not.</p> */
    private boolean cachingEnabled = true;

    /* TEMPORARY VARIABLES SET BY SETUP() AND RESET BY RECYCLE() */

    /** <p>The {@link InputSource} associated with the current schema.</p> */
    private InputSource inputSource = null;
    /** <p>The {@link SourceValidity} associated with the schema.</p> */
    private AggregatedValidity sourceValidity = null;
    /** <p>The {@link SourceResolver} to use for resolving URIs.</p> */
    private SourceResolver sourceResolver = null;
    /** <p>The {@link XMLConsumer} of the validator.</p> */
    private XMLConsumer validationHandler = null;


    /* =========================================================================== */
    /* COMPONENT LIFECYCLE METHODS: CONFIGURE(), SERVICE() AND DISPOSE()           */
    /* =========================================================================== */

    public void service(ServiceManager manager)
    throws ServiceException {
        this.serviceManager = manager;
        this.transientStore = (Store) manager.lookup(Store.TRANSIENT_STORE);
        this.entityResolver = (EntityResolver) manager.lookup(EntityResolver.ROLE);
    }

    public void configure(Configuration configuration)
    throws ConfigurationException {
        boolean c = configuration.getChild("enable-caching").getValueAsBoolean(true);
        String l = configuration.getChild("verifier-language").getValue(null);
        String x = configuration.getChild("verifier-factory").getValue(MSV);
        try {
            /* Creation of the verifier on a language-or-factory basis */
            if (l != null) { 
                this.verifierFactory = VerifierFactory.newInstance(l);
                this.verifierLanguage = l;
            } else {
                Class clazz = Class.forName(x);
                this.verifierFactory = (VerifierFactory) clazz.newInstance();
                this.verifierLanguage = "java:" + x;
            }

            /* Normal stuff for all factories */ 
            this.verifierFactory.setEntityResolver(this);
            this.cachingEnabled = c;
        } catch (Exception exception) {
            String message = "Unable to configure verifier for language " + l;
            throw new ConfigurationException(message, configuration, exception);
        }
    }

    public void dispose() {
        this.serviceManager.release(this.entityResolver);
        this.serviceManager.release(this.transientStore);
        this.serviceManager = null;
        this.verifierFactory = null;
        this.verifierLanguage = null;
        this.cachingEnabled = true;
    }


    /* =========================================================================== */
    /* REQUEST LIFECYCLE METHODS: SETUP() AND RECYCLE()                            */
    /* =========================================================================== */

    public void setup(SourceResolver resolver, Map objectModel, String source,
                      Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        this.sourceValidity = new AggregatedValidity();
        this.sourceResolver = resolver;
        this.inputSource = this.resolveEntity(null, source);
        
        try {
            /* This will throw an NPE, unless you're using Sun's MSV */
            Verifier verifier = this.schema().newVerifier();
            ContentHandler handler = verifier.getVerifierHandler();
            this.validationHandler = new ContentHandlerWrapper(handler);
        } catch (NullPointerException exception) {
            String message = "Apparently JARV does not work with JING";
            throw new ProcessingException(message, exception);
        } catch (VerifierConfigurationException exception) {
            String message = "Unable to retrieve validation handler for " + source;
            throw new ProcessingException(message, exception);
        }
    }

    public void setConsumer(XMLConsumer consumer) {
        super.setConsumer(new XMLMulticaster(this.validationHandler, consumer));
    }

    public Serializable getKey() {
        if (this.uniqueIdentifier == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(this.getClass().getName());
            buffer.append(':');
            buffer.append(this.verifierLanguage);
            buffer.append(':');
            buffer.append(this.inputSource.getSystemId());
            this.uniqueIdentifier = buffer.toString();
        }
        return this.uniqueIdentifier;
    }

    public SourceValidity getValidity() {
        return this.sourceValidity;
    }

    public void recycle() {
        this.validationHandler = null;
        this.sourceResolver = null;
        this.sourceValidity = null;
        this.inputSource = null;

        super.recycle();
    }

    /* =========================================================================== */
    /* SAX2 ERROR HANDLER INTERFACE IMPLEMENTATION                                 */
    /* =========================================================================== */

    public void warning(SAXParseException exception)
    throws SAXException {
        throw exception;
    }

    public void error(SAXParseException exception)
    throws SAXException {
        throw exception;
    }

    public void fatalError(SAXParseException exception)
    throws SAXException {
        throw exception;
    }

    /* =========================================================================== */
    /* SAX2 ENTITY RESOLVER INTERFACE IMPLEMENTATION                               */
    /* =========================================================================== */

    public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException, IOException {
        if (this.sourceResolver == null) throw new IllegalStateException();

        /* Try to resolve the public id if we don't have a system id */
        if (systemId == null) {
            InputSource source = this.entityResolver.resolveEntity(publicId, null);
            if ((source == null) || (source.getSystemId() == null)) {
                throw new IOException("Can't resolve \"" + publicId + "\"");
            } else {
                systemId = source.getSystemId();
            }
        }

        /* Use Cocoon's SourceResolver to resolve the system id */
        Source source = this.sourceResolver.resolveURI(systemId);
        try {
            this.sourceValidity.add(source.getValidity());
            InputSource inputSource = new InputSource();
            inputSource.setSystemId(source.getURI());
            inputSource.setPublicId(publicId);
            inputSource.setByteStream(source.getInputStream());
            return inputSource;
        } finally {
            this.sourceResolver.release(source);
        }
    }


    /* =========================================================================== */
    /* CALL JARV TO ACCESS A CACHED OR FRESHLY PARSED SCHEMA INSTANCE              */
    /* =========================================================================== */

    private Schema schema()
    throws SAXException, IOException, VerifierConfigurationException {
        if (this.cachingEnabled) {
            Entry entry = (Entry) this.transientStore.get(this.getKey());
            if ((entry == null) || (entry.expired())) {
                Schema schema = this.verifierFactory.compileSchema(this.inputSource);
                entry = new Entry(schema, this.getValidity());
                this.transientStore.store(this.getKey(), entry);
            }
            return entry.schema;
        }
        
        return this.verifierFactory.compileSchema(this.inputSource);
    }

    /* =========================================================================== */
    /* INNER CLASSES USED TO WRAP JARV SCHEMA AND VALIDITY IN CACHES               */
    /* =========================================================================== */

    private static final class Entry {

        private final Schema schema;
        private final SourceValidity validity;
        
        private Entry(Schema schema, SourceValidity validity) {
            this.validity = validity;
            this.schema = schema;
        }
        
        private boolean expired() {
            return (this.validity.isValid() != SourceValidity.VALID);
        }
    }
}
