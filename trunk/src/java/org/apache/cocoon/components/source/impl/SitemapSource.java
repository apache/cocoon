/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.EnvironmentHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
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
 * @version CVS $Id: SitemapSource.java,v 1.23 2004/01/09 08:39:54 cziegeler Exp $
 */
public final class SitemapSource
extends AbstractLogEnabled
implements Source, XMLizable {

    /** validities for the internal pipeline */
    private SourceValidity sourceValidity;

    /** The system id */
    private String systemId;

    /** The system id used for caching */
    private String systemIdForCaching;
    
    /** The current ServiceManager */
    private ServiceManager manager;

    /** The processor */
    private Processor processor;

    /** The pipeline processor */
    private Processor pipelineProcessor;

    /** The environment */
    private MutableEnvironmentFacade environment;

    /** The <code>ProcessingPipeline</code> */
    private ProcessingPipeline processingPipeline;

    /** The redirect <code>Source</code> */
    private Source redirectSource;

    /** Redirect validity */
    private SourceValidity redirectValidity;

    /** The <code>SAXException</code> if unable to get resource */
    private SAXException exception;

    /** Do I need a refresh ? */
    private boolean needsRefresh;

    /** The unique key for this processing */
    private Object processKey;
    
    /** The used protocol */
    private String protocol;

    /** SourceResolver (for the redirect source) */
    private SourceResolver sourceResolver;
    
    /**
     * Construct a new object
     */
    public SitemapSource(ServiceManager manager,
                         String         uri,
                         Map            parameters,
                         Logger         logger)
    throws MalformedURLException {

        Environment env = EnvironmentHelper.getCurrentEnvironmentContext().getEnvironment();
        if ( env == null ) {
            throw new MalformedURLException("The cocoon protocol can not be used outside an environment.");
        }

        this.manager = manager;
        this.enableLogging(logger);

        SitemapSourceInfo info = SitemapSourceInfo.parseURI(env, uri);

        // does the uri point to this sitemap or to the root sitemap?
        String prefix;
        if (info.prefix.length() == 0) {
            this.processor = EnvironmentHelper.getCurrentProcessor().getRootProcessor();
        } else {
            this.processor = EnvironmentHelper.getCurrentProcessor();
        }

        // create environment...
        EnvironmentWrapper wrapper = new EnvironmentWrapper(env, info.requestURI, 
                                                   info.queryString, logger, manager, info.rawMode, info.view);
        wrapper.setURI(info.prefix, uri);
        
        // The environment is a facade whose delegate can be changed in case of internal redirects
        this.environment = new MutableEnvironmentFacade(wrapper);

        // ...and put information passed from the parent request to the internal request
        if ( null != parameters ) {
            this.environment.getObjectModel().put(ObjectModelHelper.PARENT_CONTEXT, parameters);
        } else {
            this.environment.getObjectModel().remove(ObjectModelHelper.PARENT_CONTEXT);
        }
        
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
            throw new IOException("SAXException: " + this.exception);
        }

        if (this.redirectSource != null) {
            return this.redirectSource.getInputStream();
        }

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            this.environment.setOutputStream(os);
            EnvironmentHelper.enterProcessor(this.pipelineProcessor, 
                                            this.manager,
                                            this.environment);
            try {
                
                this.processingPipeline.process(this.environment);
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
            reset();
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
        if (this.needsRefresh) {
            this.refresh();
        }
        if (this.redirectSource != null) {
            return this.redirectValidity;
        }
        return this.sourceValidity;
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be null.
     */
     public String getMimeType() {
        return "text/xml";
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
            this.processKey = EnvironmentHelper.startProcessing(this.environment);
            this.processingPipeline = this.processor.buildPipeline(this.environment);
            this.pipelineProcessor = EnvironmentHelper.getLastProcessor(this.environment);
            this.pipelineProcessor.getEnvironmentHelper().setContext(this.environment);

            String redirectURL = this.environment.getRedirectURL();
            if (redirectURL == null) {

                EnvironmentHelper.enterProcessor(this.pipelineProcessor,
                                                 this.manager,
                                                 this.environment);
                try {
                    this.processingPipeline.prepareInternal(this.environment);
                    this.sourceValidity = this.processingPipeline.getValidityForEventPipeline();
                    final String eventPipelineKey = this.processingPipeline.getKeyForEventPipeline();
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
                this.redirectValidity = this.redirectSource.getValidity();
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
    throws SAXException
    {
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
                EnvironmentHelper.enterProcessor(this.pipelineProcessor,
                                                 this.manager,
                                                 this.environment);
                try {
                    this.processingPipeline.process(this.environment,
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
            reset();
        }
    }

    /**
     * Reset everything
     */
    private void reset() {
        if (this.processingPipeline != null) {
            this.processor.releasePipeline(this.environment, this.processingPipeline);
            this.processingPipeline = null;
        }
        if (this.processKey != null) {
            EnvironmentHelper.endProcessing(this.environment, this.processKey);
            this.processKey = null;
        }
        this.sourceValidity = null;
        if (this.redirectSource != null) {
            this.sourceResolver.release(this.redirectSource);
        }
        this.environment.reset();
        this.redirectSource = null;
        this.redirectValidity = null;
        this.exception = null;
        this.needsRefresh = true;
        this.pipelineProcessor = null;
    }

    /**
     * Recyclable
     */
    public void recycle() {
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

}
