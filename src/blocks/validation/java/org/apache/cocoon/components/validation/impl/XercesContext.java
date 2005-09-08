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

import java.util.Iterator;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.ParserConfigurationSettings;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * <p>An implementation of Xerces {@link XMLComponentManager} interface allowing
 * interoperation of components while parsing or validating.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class XercesContext extends ParserConfigurationSettings {

    private static final String P_ENTITY_MANAGER =        
            Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;
    private static final String P_ENTITY_RESOLVER =       
            Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    private static final String P_ERROR_REPORTER =        
            Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    private static final String P_NAMESPACE_CONTEXT =     
            Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_CONTEXT_PROPERTY;
    private static final String P_SYMBOL_TABLE =          
            Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    private static final String P_VALIDATION_MANAGER =    
            Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    private static final String P_XMLGRAMMAR_POOL =       
            Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    private static final String P_ERROR_HANDLER =
            Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;

    private static final String PROPERTIES[] = {
                P_ENTITY_MANAGER,
                P_ENTITY_RESOLVER,
                P_ERROR_REPORTER,
                P_NAMESPACE_CONTEXT,
                P_SYMBOL_TABLE,
                P_VALIDATION_MANAGER,
                P_XMLGRAMMAR_POOL,
                P_ERROR_HANDLER
            };       

    private static final String F_SCHEMA_VALIDATION =     
            Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;
    private static final String F_VALIDATION =            
            Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    private static final String F_USE_GRAMMAR_POOL_ONLY = 
            Constants.XERCES_FEATURE_PREFIX + Constants.USE_GRAMMAR_POOL_ONLY_FEATURE;
    private static final String F_PARSER_SETTINGS = 
            Constants.XERCES_FEATURE_PREFIX + Constants.PARSER_SETTINGS;
    
    private static final String FEATURES[] = {
                F_SCHEMA_VALIDATION,
                F_VALIDATION,
                F_USE_GRAMMAR_POOL_ONLY,
                F_PARSER_SETTINGS
            };

    /**
     * <p>Create a new {@link XercesContext} instance.</p>
     */
    public XercesContext(XMLGrammarPool grammar, XMLEntityResolver resolver) {
        this(grammar, resolver, null);
    }

    /**
     * <p>Create a new {@link XercesContext} instance specifying the SAX
     * {@link ErrorHandler} handling errors.</p>
     */
    public XercesContext(XMLGrammarPool grammar, XMLEntityResolver resolver,
                         ErrorHandler errorHandler) {
        super.addRecognizedFeatures(FEATURES);
        super.addRecognizedProperties(PROPERTIES);
        
        XMLErrorReporter errorReporter = new XMLErrorReporter();
        errorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN,
                                          new XSMessageFormatter());
        errorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN,
                                          new XMLMessageFormatter());
        errorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN,
                                          new XMLMessageFormatter());
        XMLErrorHandler xercesHandler = new ErrorHandlerWrapper(errorHandler);

        super.setProperty(P_XMLGRAMMAR_POOL,    grammar);
        super.setProperty(P_ENTITY_RESOLVER,    resolver);
        super.setProperty(P_ERROR_REPORTER,     errorReporter);
        super.setProperty(P_ERROR_HANDLER,      xercesHandler);

        super.setProperty(P_ENTITY_MANAGER,     new XMLEntityManager());
        super.setProperty(P_NAMESPACE_CONTEXT,  new NamespaceSupport());
        super.setProperty(P_VALIDATION_MANAGER, new ValidationManager());
        super.setProperty(P_SYMBOL_TABLE,       new SymbolTable());

        super.setFeature(F_USE_GRAMMAR_POOL_ONLY, true);
        super.setFeature(F_PARSER_SETTINGS,       true);
        super.setFeature(F_VALIDATION,            true);
        super.setFeature(F_SCHEMA_VALIDATION,     true);

        /* Initialize all known components */
        Iterator iterator = super.fProperties.values().iterator();
        while (iterator.hasNext()) this.initialize(iterator.next());
    }
    
    public Object initialize(Object object) {
        if (!(object instanceof XMLComponent)) return object;
        XMLComponent component = (XMLComponent) object;
        component.reset(this);

        /* Force setting all known features */
        Iterator features = super.fFeatures.keySet().iterator();
        while (features.hasNext()) try {
            String featureName = (String) features.next();
            component.setFeature(featureName, this.getFeature(featureName));
        } catch (XNIException exception) {
            // Swallow any exception;
        }

        /* Force setting all known properties */
        Iterator properties = super.fProperties.keySet().iterator();
        while (properties.hasNext()) try {
            String propertyName = (String) properties.next();
            component.setProperty(propertyName, this.getProperty(propertyName));
        } catch (XNIException exception) {
            // Swallow any exception;
        }
        
        return object;
    }

    /**
     * <p>A simple wrapper around a SAX {@link ErrorHandler} exposing the
     * handler as a Xerces {@link XMLErrorHandler}.</p>
     */
    private static final class ErrorHandlerWrapper implements XMLErrorHandler {
        
        private final ErrorHandler errorHandler;
        
        private ErrorHandlerWrapper(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
        }

        public void warning(String domain, String key, XMLParseException e)
        throws XNIException {
            System.err.println("DETECTED " + e.getMessage() + "/" + this.errorHandler);
            if (this.errorHandler != null) try {
                this.errorHandler.warning(this.makeException(e));
            } catch (SAXException saxException) {
                throw new XNIException(saxException);
            } else {
                System.err.println("THROWING " + e.getMessage());
                throw e;
            }
        }

        public void error(String domain, String key, XMLParseException e)
        throws XNIException {
            System.err.println("DETECTED " + e.getMessage() + "/" + this.errorHandler);
            if (this.errorHandler != null) try {
                this.errorHandler.warning(this.makeException(e));
            } catch (SAXException saxException) {
                throw new XNIException(saxException);
            } else {
                System.err.println("THROWING " + e.getMessage());
                throw e;
            }
        }

        public void fatalError(String domain, String key, XMLParseException e)
        throws XNIException {
            System.err.println("DETECTED " + e.getMessage() + "/" + this.errorHandler);
            if (this.errorHandler != null) try {
                this.errorHandler.warning(this.makeException(e));
            } catch (SAXException saxException) {
                throw new XNIException(saxException);
            } else {
                System.err.println("THROWING " + e.getMessage());
                throw e;
            }
        }
        
        private SAXParseException makeException(XMLParseException exception) {
            final SAXParseException saxParseException;
            saxParseException = new SAXParseException(exception.getMessage(),
                                                      exception.getPublicId(),
                                                      exception.getLiteralSystemId(),
                                                      exception.getLineNumber(),
                                                      exception.getColumnNumber(),
                                                      exception);
            return (SAXParseException) saxParseException.initCause(exception);
        }
    }
}
