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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.validation.SchemaParser;
import org.apache.cocoon.components.validation.ValidationHandler;
import org.apache.cocoon.components.validation.Validator;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.excalibur.xmlizer.XMLizer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <p>TODO: ...</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public abstract class AbstractValidator
implements Validator, Configurable, Serviceable, Initializable, Disposable {

    private boolean ignoreWarning = true;
    private boolean ignoreError = false;
    private boolean ignoreFatalError = false;
    private ServiceSelector serviceSelector = null;
    private String schemaParserName = null;

    protected ServiceManager serviceManager = null;
    protected SourceResolver sourceResolver = null;
    protected SchemaParser schemaParser = null;

    public void service(ServiceManager manager)
    throws ServiceException {
        this.serviceManager = manager;
    }

    public void configure(Configuration configuration)
    throws ConfigurationException {
        this.ignoreWarning =    configuration.getChild("ignore-warning").getValueAsBoolean(true);
        this.ignoreError =      configuration.getChild("ignore-error").getValueAsBoolean(false);
        this.ignoreFatalError = configuration.getChild("ignore-fatal-error").getValueAsBoolean(false);
        this.schemaParserName = configuration.getChild("schema-parser").getValue();
    }

    public void initialize()
    throws Exception {
        this.sourceResolver =  (SourceResolver) this.serviceManager.lookup(SourceResolver.ROLE);
        this.serviceSelector = (ServiceSelector) this.serviceManager.lookup(SchemaParser.SELECTOR);
        this.schemaParser =    (SchemaParser) this.serviceSelector.select(this.schemaParserName);
    }

    public void dispose() {
        if (this.sourceResolver != null)  this.serviceManager.release(this.sourceResolver);
        if (this.schemaParser != null)    this.serviceSelector.release(this.schemaParser);
        if (this.serviceSelector != null) this.serviceManager.release(this.serviceSelector);
    }

    public void validate(String xmlUri, String schemaUri)
    throws IOException, SAXException {
        this.validate(xmlUri, schemaUri, new DefaultErrorHandler(this));
    }

    public void validate(String xmlUri, String schemaUri, ErrorHandler errorHandler)
    throws IOException, SAXException {
        Source xmlSource = null;
        Source schemaSource = null;
        try {
            xmlSource = this.sourceResolver.resolveURI(xmlUri);
            schemaSource = this.sourceResolver.resolveURI(schemaUri);
            this.validate(xmlSource, schemaSource, errorHandler);
        } finally {
            if (xmlSource != null) this.sourceResolver.release(xmlSource);
            if (schemaSource != null) this.sourceResolver.release(schemaSource);
        }
    }

    public void validate(Source xmlSource, Source schemaSource)
    throws IOException, SAXException {
        this.validate(xmlSource, schemaSource, new DefaultErrorHandler(this));
    }

    public void validate(Source xmlSource, Source schemaSource, ErrorHandler errorHandler)
    throws IOException, SAXException {
        ValidationHandler validationHandler = this.getValidationHandler(schemaSource, errorHandler);
        XMLizer xmlizer = null;
        try {
            if (xmlSource instanceof XMLizable) {
                XMLizable xmlizableSource = (XMLizable) xmlSource;
                xmlizableSource.toSAX(validationHandler);
            } else {
                xmlizer = (XMLizer) this.serviceManager.lookup(XMLizer.ROLE);
                xmlizer.toSAX(xmlSource.getInputStream(), xmlSource.getMimeType(),
                              xmlSource.getURI(), validationHandler);
            }
        } catch (ServiceException exception) {
            throw new SAXException("Unable to look up XMLizer instance", exception);
        } finally {
            if (xmlizer != null) this.serviceManager.release(xmlizer);
        }
    }

    public ValidationHandler getValidationHandler(String schemaUri)
    throws IOException, SAXException {
        return this.getValidationHandler(schemaUri, new DefaultErrorHandler(this));
    }

    public ValidationHandler getValidationHandler(String schemaUri, ErrorHandler errorHandler)
    throws IOException, SAXException {
        Source schemaSource = null;
        try {
            schemaSource = this.sourceResolver.resolveURI(schemaUri);
            return this.getValidationHandler(schemaSource, errorHandler);
        } finally {
            if (schemaSource != null) this.sourceResolver.release(schemaSource);
        }
    }

    public ValidationHandler getValidationHandler(Source schemaSource)
    throws IOException, SAXException {
        return this.getValidationHandler(schemaSource, new DefaultErrorHandler(this));
    }

    public abstract ValidationHandler getValidationHandler(Source schemaSource, ErrorHandler errorHandler)
    throws IOException, SAXException;

    private static final class DefaultErrorHandler implements ErrorHandler {
        
        private final AbstractValidator validator;
        
        protected DefaultErrorHandler(AbstractValidator validator) {
            this.validator = validator;
        }

        public void warning(SAXParseException exception)
        throws SAXException {
            if (this.validator.ignoreWarning) return;
            throw exception;
        }

        public void error(SAXParseException exception)
        throws SAXException {
            if (this.validator.ignoreError) return;
            throw exception;
        }

        public void fatalError(SAXParseException exception)
        throws SAXException {
            if (this.validator.ignoreFatalError) return;
            throw exception;
        }
    }
}
