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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * <p>The {@link ValidationReportTransformer} provides a {@link Transformer}
 * validating documents while being processed in a Cocoon pipeline, and preparing
 * a report of all detected inconsistancies according the specified schema.</p>
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
 * <p>The report prepared by this transformer will look like the following:</p>
 * 
 * <pre>
 *   &lt;report xmlns="http://apache.org/cocoon/validation/1.0"&gt;
 *     &lt;warning system="..." public="..." line="..." column="..."&gt;
 *       ... detailed message ...
 *     &lt;/warning&gt;
 *     &lt;error system="..." public="..." line="..." column="..."&gt;
 *       ... detailed message ...
 *     &lt;/error&gt;
 *     &lt;fatal system="..." public="..." line="..." column="..."&gt;
 *       ... message ...
 *     &lt;/fatal&gt;
 *   &lt;/report&gt;
 * </pre>
 * 
 * <p>The location attributes specified in the <code>&lt;warning/&gt;</code>,
 * <code>&lt;error/&gt;</code> and <code>&lt;fatal/&gt;</code> tags are all optional
 * and will be generated only if known.</p>
 *
 */
public class ValidationReportTransformer extends AbstractTransformer
implements Configurable, Serviceable, Disposable, CacheableProcessingComponent {
    
    /** <p>The configured {@link ServiceManager} instance.</p> */
    private ServiceManager serviceManager = null;
    /** <p>The configured {@link Validator} instance.</p> */
    private Validator validator = null;
    /** <p>The configured default grammar language.</p> */
    private String grammar = null;

    /** <p>The {@link Report} instance to be used while processing a request.</p> */
    private Report report = null;
    /** <p>The {@link ValidationHandler} to use in this transformation.</p> */
    private ValidationHandler handler = null;
    /** <p>The {@link XMLConsumer} to send the validation report to.</p> */
    private XMLConsumer consumer = null;
    /** <p>A unique key identifying the schema source for caching.</p> */
    private String key = null;

    /**
     * <p>Create a new {@link ValidationReportTransformer} instance.</p>
     */
    public ValidationReportTransformer() {
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
            Report r = new Report();
            String g = parameters.getParameter("grammar", this.grammar);
            s = resolver.resolveURI(source);
            if (g == null) {
                this.handler = this.validator.getValidationHandler(s, r);
            } else{
                this.handler = this.validator.getValidationHandler(s, g, r);
            }
            this.setContentHandler(this.handler);
            this.setLexicalHandler(this.handler);
            this.report = r;
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
        this.consumer = consumer;
    }

    /**
     * <p>Receive notification of the end of the document and produce the report
     * of the validation result.</p>
     *
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument()
    throws SAXException {
        super.endDocument();
        this.report.generateReport(this.consumer);
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
        this.consumer = null;
        this.handler = null;
        this.report = null;
        this.key = null;
        super.recycle();
    }
    
    /**
     * 
     */
    private static final class Report implements ErrorHandler {

        private static final String NS = "http://apache.org/cocoon/validation/1.0";
        private final List entries = new ArrayList();

        public void warning(SAXParseException exception)
        throws SAXException {
            this.entries.add(new ReportEntry("warning", exception));
        }

        public void error(SAXParseException exception)
        throws SAXException {
            this.entries.add(new ReportEntry("error", exception));
        }

        public void fatalError(SAXParseException exception)
        throws SAXException {
            this.entries.add(new ReportEntry("fatal", exception));
        }

        private void generateReport(ContentHandler handler)
        throws SAXException {
            /* Start the report */
            handler.startDocument();
            handler.startPrefixMapping("", NS);
            AttributesImpl attributes = new AttributesImpl();
            handler.startElement(NS, "report", "report", attributes);

            /* Each collected error will generate its own entry */
            Iterator iterator = this.entries.iterator();
            while(iterator.hasNext()) {
                ReportEntry entry = (ReportEntry) iterator.next();
                attributes.clear();

                if (entry.exception.getPublicId() != null) {
                    if (! "".equals(entry.exception.getPublicId())) {
                        attributes.addAttribute("", "public", "public", "CDATA",
                                                entry.exception.getPublicId());
                    }
                }

                if (entry.exception.getSystemId() != null) {
                    if (! "".equals(entry.exception.getSystemId())) {
                        attributes.addAttribute("", "system", "system", "CDATA",
                                                entry.exception.getSystemId());
                    }
                }

                if (entry.exception.getLineNumber() >= 0) {
                    String l = Integer.toString(entry.exception.getLineNumber());
                    attributes.addAttribute("", "line", "line", "CDATA", l);
                }

                if (entry.exception.getColumnNumber() >= 0) {
                    String c = Integer.toString(entry.exception.getColumnNumber());
                    attributes.addAttribute("", "column", "column", "CDATA", c);
                }
                
                String level = entry.level;
                handler.startElement(NS, level, level, attributes);
                char message[] = entry.exception.getMessage().toCharArray();
                handler.characters(message, 0, message.length);
                handler.endElement(NS, level, level);
            }

            /* After all the exceptions have been dumped, close the report */
            handler.endElement(NS, "report", "report");
            handler.endPrefixMapping("");
            handler.endDocument();
        }
    }

    private static final class ReportEntry {
        
        private final String level;
        private final SAXParseException exception;
        
        private ReportEntry(String level, SAXParseException exception) {
            this.level = level;
            this.exception = exception;
        }
    }
}
