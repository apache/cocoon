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
package org.apache.cocoon.components.source;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.logger.Logger;

import org.apache.excalibur.source.SourceException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ModifiableSource;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;
import org.apache.cocoon.xml.AbstractXMLConsumer;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * Description of a source which is defined by a pipeline.
 *
 * @deprecated by the Avalon Excalibur Source Resolving
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SitemapSource.java,v 1.4 2003/07/06 11:48:48 sylvain Exp $
 */
public final class SitemapSource
extends AbstractXMLConsumer
implements ModifiableSource {

    /** The last modification date or 0 */
    private long lastModificationDate;

    /** The system id */
    private String systemId;

    /** The uri */
    private String uri;

    /** The current ComponentManager */
    private ComponentManager manager;

    /** The processor */
    private Processor processor;

    /** The pipeline processor */
    private Processor pipelineProcessor;

    /** The environment */
    private EnvironmentWrapper environment;

    /** The prefix for the processing */
    private String prefix;

    /** The <code>ProcessingPipeline</code> */
    private ProcessingPipeline processingPipeline;

    /** The redirect <code>Source</code> */
    private org.apache.excalibur.source.Source redirectSource;

    /** The <code>SAXException</code> if unable to get resource */
    private SAXException exception;

    /** Do I need a refresh ? */
    private boolean needsRefresh;

    /** The unique key for this processing */
    private Object processKey;

    /**
     * Construct a new object
     */
    public SitemapSource(ComponentManager manager,
                         String           uri,
                         Logger           logger)
    throws IOException, ProcessingException {

        Environment env = CocoonComponentManager.getCurrentEnvironment();
        if ( env == null ) {
            throw new MalformedURLException("The cocoon protocol can not be used outside an environment.");
        }

        this.manager = manager;
        this.enableLogging(logger);
        boolean rawMode = false;

        // remove the protocol
        int position = uri.indexOf(':') + 1;
        if (position != 0) {
            // check for subprotocol
            if (uri.startsWith("raw:", position)) {
                position += 4;
                rawMode = true;
            }
        }

        // does the uri point to this sitemap or to the root sitemap?
        if (uri.startsWith("//", position)) {
            position += 2;
            try {
                this.processor = (Processor)this.manager.lookup(Processor.ROLE);
            } catch (ComponentException e) {
                throw new ProcessingException("Cannot get Processor instance", e);
            }
            this.prefix = ""; // start at the root
        } else if (uri.startsWith("/", position)) {
            position ++;
            this.prefix = null;
            this.processor = CocoonComponentManager.getCurrentProcessor();
        } else {
            throw new ProcessingException("Malformed cocoon URI.");
        }

        // create the queryString (if available)
        String queryString = null;
        int queryStringPos = uri.indexOf('?', position);
        if (queryStringPos != -1) {
            queryString = uri.substring(queryStringPos + 1);
            uri = uri.substring(position, queryStringPos);
        } else if (position > 0) {
            uri = uri.substring(position);
        }

        // build the request uri which is relative to the context
        String requestURI = (this.prefix == null ? env.getURIPrefix() + uri : uri);

        // create system ID
        this.systemId = queryString == null ?
            "cocoon://" + requestURI :
            "cocoon://" + requestURI + "?" + queryString;

        this.environment = new EnvironmentWrapper(env, requestURI, queryString, logger, manager, rawMode);
        this.uri = uri;
        this.refresh();
    }

    /**
     * Get the last modification date of the source or 0 if it
     * is not possible to determine the date.
     */
    public long getLastModified() {
        if (this.needsRefresh) {
            this.refresh();
        }
        return this.lastModificationDate;
    }

    /**
     * Get the content length of the source or -1 if it
     * is not possible to determine the length.
     */
    public long getContentLength() {
        return -1;
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream()
      throws ProcessingException, IOException {

        if (this.needsRefresh) {
            this.refresh();
        }
        // VG: Why exception is not thrown in constructor?
        if (this.exception != null) {
            throw new ProcessingException(this.exception);
        }

        if (this.redirectSource != null) {
            try {
                return this.redirectSource.getInputStream();
            } catch (SourceException se) {
                throw SourceUtil.handle(se);
            }
        }

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            this.environment.setOutputStream(os);
            try {
                CocoonComponentManager.enterEnvironment(this.environment,
                                                        this.manager,
                                                        this.pipelineProcessor);
                this.processingPipeline.process(this.environment);
            } finally {
                CocoonComponentManager.leaveEnvironment();
            }
            return new ByteArrayInputStream(os.toByteArray());

        } catch (ProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new ProcessingException("Exception during processing of " + this.systemId, e);
        } finally {
            // Unhide wrapped environment output stream
            this.environment.setOutputStream(null);
            reset();
        }
    }

    /**
     * Return the unique identifer for this source
     */
    public String getSystemId() {
        return this.systemId;
    }

    /**
     * Refresh this object and update the last modified date
     * and content length.
     */
    public void refresh() {
        reset();
        try {
            this.processKey = CocoonComponentManager.startProcessing(this.environment);
            this.environment.setURI(this.prefix, this.uri);
            this.processingPipeline = this.processor.buildPipeline(this.environment);
            this.pipelineProcessor = CocoonComponentManager.getLastProcessor(this.environment); 
            this.environment.changeToLastContext();
            String redirectURL = this.environment.getRedirectURL();
            if (redirectURL != null) {
                if (redirectURL.indexOf(":") == -1) {
                    redirectURL = "cocoon:/" + redirectURL;
                }
                this.redirectSource = this.environment.resolveURI(redirectURL);
                this.lastModificationDate = this.redirectSource.getLastModified();
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
     * Return a new <code>InputSource</code> object
     */
    public InputSource getInputSource()
    throws ProcessingException, IOException {
        InputSource newObject = new InputSource(this.getInputStream());
        newObject.setSystemId(this.systemId);
        return newObject;
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
            XMLConsumer consumer;
            if (contentHandler instanceof XMLConsumer) {
                consumer = (XMLConsumer)contentHandler;
            } else if (contentHandler instanceof LexicalHandler) {
                consumer = new ContentHandlerWrapper(contentHandler, (LexicalHandler)contentHandler);
            } else {
                consumer = new ContentHandlerWrapper(contentHandler);
            }
            if (this.redirectSource != null) {
                SourceUtil.parse(this.manager, this.redirectSource, consumer);
            } else {
                // We have to buffer the result in order to get
                // clean environment stack handling.
                XMLSerializer xmls = (XMLSerializer) this.manager.lookup(XMLSerializer.ROLE);
                Object fragment;
                try {
                    CocoonComponentManager.enterEnvironment(this.environment,
                                                            this.manager,
                                                            this.pipelineProcessor);
                    this.processingPipeline.process(this.environment, xmls);
                    fragment = xmls.getSAXFragment();
                } finally {
                    this.manager.release(xmls);
                    CocoonComponentManager.leaveEnvironment();
                }
                XMLDeserializer xmld = (XMLDeserializer) this.manager.lookup(XMLDeserializer.ROLE);
                try {
                    xmld.setConsumer(consumer);
                    xmld.deserialize(fragment);
                } finally {
                    this.manager.release(xmld);
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

    private void reset() {
        if (this.processingPipeline != null) this.processingPipeline.release();
        if (this.processKey != null) {
            CocoonComponentManager.endProcessing(this.environment, this.processKey);
            this.processKey = null;
        }
        this.processingPipeline = null;
        this.lastModificationDate = 0;
        this.environment.release(this.redirectSource);
        this.environment.reset();
        this.redirectSource = null;
        this.exception = null;
        this.needsRefresh = true;
        this.pipelineProcessor = null;
    }

    public void recycle() {
        reset();
    }
}
