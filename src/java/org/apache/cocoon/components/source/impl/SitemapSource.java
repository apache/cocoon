/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.source.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Processor;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;
import org.apache.cocoon.environment.wrapper.MutableEnvironmentFacade;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Implementation of a {@link Source} that gets its content
 * by invoking a pipeline.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id$
 */
public final class SitemapSource
extends AbstractLogEnabled
implements Source, XMLizable {

    /** validities for the internal pipeline */
    private SitemapSourceValidity validity;

    /** The system id */
    private final String systemId;

    /** The system id used for caching */
    private String systemIdForCaching;

    /** The current ServiceManager */
    private final ServiceManager manager;

    /** The processor */
    private final Processor processor;

    /** The pipeline description */
    private Processor.InternalPipelineDescription pipelineDescription;

    /** The environment */
    private final MutableEnvironmentFacade environment;

    /** The redirect <code>Source</code> */
    private Source redirectSource;

    /** The <code>SAXException</code> if unable to get resource */
    private SAXException exception;

    /** Do I need a refresh ? */
    private boolean needsRefresh;

    /** Is start processing on the environment called? */
    private boolean processed;

    /** The used protocol */
    private final String protocol;

    /** SourceResolver (for the redirect source) */
    private SourceResolver sourceResolver;

    private String mimeType;

    /**
     * Construct a new object
     */
    public SitemapSource(ServiceManager manager,
                         String         uri,
                         Map            parameters,
                         Logger         logger)
    throws MalformedURLException {

        Environment env = EnvironmentHelper.getCurrentEnvironment();
        if ( env == null ) {
            throw new MalformedURLException("The cocoon protocol can not be used outside an environment.");
        }
        this.manager = manager;
        this.enableLogging(logger);

        SitemapSourceInfo info = SitemapSourceInfo.parseURI(env, uri);
        this.protocol = info.protocol;

        // does the uri point to this sitemap or to the root sitemap?
        if (info.processFromRoot) {
            this.processor = EnvironmentHelper.getCurrentProcessor().getRootProcessor();
        } else {
            this.processor = EnvironmentHelper.getCurrentProcessor();
        }

        // create environment...
        final EnvironmentWrapper wrapper = new EnvironmentWrapper(env, info, logger);

        // The environment is a facade whose delegate can be changed in case of internal redirects
        this.environment = new MutableEnvironmentFacade(wrapper);

        // ...and put information passed from the parent request to the internal request
        if ( null != parameters ) {
            this.environment.getObjectModel().put(ObjectModelHelper.PARENT_CONTEXT, parameters);
        } else {
            this.environment.getObjectModel().remove(ObjectModelHelper.PARENT_CONTEXT);
        }

        this.systemId = info.systemId;

        // create a new validity holder
        this.validity = new SitemapSourceValidity();

        // initialize
        this.init();
    }

    /**
     * Return the protocol identifier.
     */
    public String getScheme() {
        return this.protocol;
    }

    /**
     * Get the content length of the source or -1 if it
     * is not possible to determine the length.
     */
    public long getContentLength() {
        return -1;
    }

    /**
     * Get the last modification date.
     * @return The last modification in milliseconds since January 1, 1970 GMT
     *         or 0 if it is unknown
     */
    public long getLastModified() {
        return 0;
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream()
    throws IOException, SourceException {

        if (this.needsRefresh) {
            this.refresh();
        }
        // VG: Why exception is not thrown in constructor?
        if (this.exception != null) {
            throw new SourceException("Cannot get input stream for " + getURI(), this.exception);
        }

        if (this.redirectSource != null) {
            return this.redirectSource.getInputStream();
        }

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            this.environment.setOutputStream(os);
            EnvironmentHelper.enterProcessor(this.pipelineDescription.lastProcessor,
                                            this.manager,
                                            this.environment);
            try {

                this.pipelineDescription.processingPipeline.process(this.environment);
            } finally {
                EnvironmentHelper.leaveProcessor();
            }
            return new ByteArrayInputStream(os.toByteArray());

        } catch (ResourceNotFoundException e) {
            throw new SourceNotFoundException("Exception during processing of " + this.systemId, e);
        } catch (Exception e) {
            throw new SourceException("Exception during processing of " + this.systemId, e);
        } finally {
            // Unhide wrapped environment output stream
            this.environment.setOutputStream(null);
            this.needsRefresh = true;
        }
    }

    /**
     * Return the unique identifer for this source
     */
    public String getURI() {
        return this.systemIdForCaching;
    }

    /**
     *
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        return true;
    }

    /**
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     */
    public SourceValidity getValidity() {
        return this.validity;
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be null.
     */
     public String getMimeType() {
         return this.mimeType;
     }

    /**
     * Refresh this object and update the last modified date
     * and content length.
     */
    public void refresh() {
        this.reset();
        this.init();
    }

    /**
     * Initialize
     */
    protected void init() {
        this.systemIdForCaching = this.systemId;
        try {
            this.environment.startingProcessing();
            this.processed = true;
            this.pipelineDescription = this.processor.buildPipeline(this.environment);
            this.environment.setURI(this.pipelineDescription.prefix, this.pipelineDescription.uri);

            String redirectURL = this.environment.getRedirectURL();
            if (redirectURL == null) {

                EnvironmentHelper.enterProcessor(this.pipelineDescription.lastProcessor,
                                                 this.manager,
                                                 this.environment);
                try {
                    this.pipelineDescription.processingPipeline.prepareInternal(this.environment);
                    this.validity.set(this.pipelineDescription.processingPipeline.getValidityForEventPipeline());
                    final String eventPipelineKey = this.pipelineDescription.processingPipeline.getKeyForEventPipeline();
                    this.mimeType = this.environment.getContentType();

                    if (eventPipelineKey != null) {
                        StringBuffer buffer = new StringBuffer(this.systemId);
                        if (this.systemId.indexOf('?') == -1) {
                            buffer.append('?');
                        } else {
                            buffer.append('&');
                        }
                        buffer.append("pipelinehash=");
                        buffer.append(eventPipelineKey);
                        this.systemIdForCaching = buffer.toString();
                    } else {
                        this.systemIdForCaching = this.systemId;
                    }
                } finally {
                    EnvironmentHelper.leaveProcessor();
                }
            } else {
                if (redirectURL.indexOf(":") == -1) {
                    redirectURL = this.protocol + ":/" + redirectURL;
                }
                if ( this.sourceResolver == null ) {
                    this.sourceResolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
                }
                this.redirectSource = this.sourceResolver.resolveURI(redirectURL);
                this.validity.set(this.redirectSource.getValidity());
                this.mimeType = this.redirectSource.getMimeType();
            }
        } catch (SAXException e) {
            reset();
            this.exception = e;
        } catch (Exception e) {
            reset();
            this.exception = new SAXException("Could not get sitemap source "
                                                     + this.systemId, e);
        }
        this.needsRefresh = false;
    }

    /**
     * Stream content to the content handler
     */
    public void toSAX(ContentHandler contentHandler)
    throws SAXException {
        if (this.needsRefresh) {
            this.refresh();
        }
        if (this.exception != null) {
            throw this.exception;
        }
        try {
            if (this.redirectSource != null) {
                SourceUtil.parse(this.manager, this.redirectSource, contentHandler);
            } else {
                XMLConsumer consumer;
                if (contentHandler instanceof XMLConsumer) {
                    consumer = (XMLConsumer)contentHandler;
                } else if (contentHandler instanceof LexicalHandler) {
                    consumer = new ContentHandlerWrapper(contentHandler, (LexicalHandler)contentHandler);
                } else {
                    consumer = new ContentHandlerWrapper(contentHandler);
                }
                // We have to add an environment changer
                // for clean environment stack handling.
                EnvironmentHelper.enterProcessor(this.pipelineDescription.lastProcessor,
                                                 this.manager,
                                                 this.environment);
                try {
                    this.pipelineDescription.processingPipeline.process(this.environment,
                                 EnvironmentHelper.createEnvironmentAwareConsumer(consumer));
                } finally {
                    EnvironmentHelper.leaveProcessor();
                }
            }
        } catch (SAXException e) {
            // Preserve original exception
            throw e;
        } catch (Exception e) {
            throw new SAXException("Exception during processing of "
                                          + this.systemId, e);
        } finally {
            this.needsRefresh = true;
        }
    }

    /**
     * Reset everything
     */
    private void reset() {
        if (this.pipelineDescription != null) {
            this.pipelineDescription.release();
            this.pipelineDescription = null;
        }

        if (this.processed) {
            this.processed = false;
            this.environment.finishingProcessing();
        }

        if (this.redirectSource != null) {
            this.sourceResolver.release(this.redirectSource);
            this.redirectSource = null;
        }

        this.validity.set(null);

        this.environment.reset();
        this.exception = null;
        this.needsRefresh = true;
    }

    /**
     * Recyclable
     */
    public void recycle() {
        this.validity = new SitemapSourceValidity();
        this.reset();
        if ( this.sourceResolver != null ) {
            this.manager.release( this.sourceResolver );
            this.sourceResolver = null;
        }
    }

    /**
     * Get the value of a parameter.
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public String getParameter(String name) {
        return null;
    }

    /**
     * Get the value of a parameter.
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public long getParameterAsLong(String name) {
        return 0;
    }

    /**
     * Get parameter names
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public Iterator getParameterNames() {
        return java.util.Collections.EMPTY_LIST.iterator();
    }

    /**
     * A simple SourceValidity protecting callers from resets.
     */
    public static final class SitemapSourceValidity implements SourceValidity {

        private SourceValidity nested_validity = null;

        private SitemapSourceValidity() {
            super();
        }

        private void set(SourceValidity validity) {
            this.nested_validity = validity;
        }

        public int isValid() {
            return(this.nested_validity != null?
                   this.nested_validity.isValid():
                   SourceValidity.INVALID);
        }

        public int isValid(SourceValidity validity) {
            if (validity instanceof SitemapSourceValidity) {
                return(this.nested_validity != null?
                        this.nested_validity.isValid(((SitemapSourceValidity) validity).getNestedValidity()):
                        SourceValidity.INVALID);
            }
            return SourceValidity.INVALID;
        }
        
        public SourceValidity getNestedValidity() {
            return this.nested_validity;
        }
    }
}
