/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.pipeline;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLoggable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Roles;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.sitemap.SitemapComponentSelector;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;
import org.xml.sax.EntityResolver;

/** A <CODE>ResourcePipeline</CODE> either
 * <UL>
 *  <LI>collects a <CODE>Reader</CODE> and let it process</LI>
 *  <LI>or connects a <CODE>EventPipeline</CODE> with a
 *  <CODE>Serializer</CODE> and let them produce the requested
 * resource
 * </UL>
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2001-04-30 14:17:12 $
 */
public abstract class AbstractStreamPipeline extends AbstractLoggable implements StreamPipeline, Disposable {
    protected EventPipeline eventPipeline;
    protected Reader reader;
    protected Parameters readerParam;
    protected String readerSource;
    protected String readerMimeType;
    protected String sitemapReaderMimeType;
    protected Serializer serializer;
    protected Parameters serializerParam;
    protected String serializerSource;
    protected String serializerMimeType;
    protected String sitemapSerializerMimeType;
    protected SitemapComponentSelector readerSelector;
    protected SitemapComponentSelector serializerSelector;

    /** the component manager */
    protected ComponentManager manager;

    /**
     * Pass the <code>ComponentManager</code> to the <code>Composable</code>.
     * The <code>Composable</code> implementation should use the specified
     * <code>ComponentManager</code> to acquire the components it needs for
     * execution.
     *
     * @param manager The <code>ComponentManager</code> which this
     *               <code>Composable</code> uses.
     * @throws ComponentException  */
    public void compose (ComponentManager manager) throws ComponentException {
        this.manager = manager;
        readerSelector = (SitemapComponentSelector) this.manager.lookup(Roles.READERS);
        serializerSelector = (SitemapComponentSelector) this.manager.lookup(Roles.SERIALIZERS);
    }

    public void setEventPipeline (EventPipeline eventPipeline) throws Exception {
        if (this.eventPipeline != null) {
            throw new ProcessingException ("EventPipeline already set. You can only select one EventPipeline ");
        }
        this.eventPipeline = eventPipeline;
    }

    public EventPipeline getEventPipeline () {
        return this.eventPipeline;
    }

    public void setReader (String role, String source, Parameters param)
    throws Exception {
        this.setReader (role, source, param, null);
    }

    public void setReader (String role, String source, Parameters param, String mimeType)
    throws Exception {
        if (this.reader != null) {
            throw new ProcessingException ("Reader already set. You can only select one Reader (" + role + ")");
        }
        this.reader = (Reader)readerSelector.select(role);
        this.readerSource = source;
        this.readerParam = param;
        this.readerMimeType = mimeType;
        this.sitemapReaderMimeType = readerSelector.getMimeTypeForRole(role);
    }

    public void setSerializer (String role, String source, Parameters param)
    throws Exception {
        this.setSerializer (role, source, param, null);
    }

    public void setSerializer (String role, String source, Parameters param, String mimeType)
    throws Exception {
        if (this.serializer != null) {
            throw new ProcessingException ("Serializer already set. You can only select one Serializer (" + role + ")");
        }
        this.serializer = (Serializer)serializerSelector.select(role);
        this.serializerSource = source;
        this.serializerParam = param;
        this.serializerMimeType = mimeType;
        this.sitemapSerializerMimeType = serializerSelector.getMimeTypeForRole(role);
    }

    public boolean process(Environment environment)
    throws ProcessingException {
        if ( this.reader != null ) {
            return processReader(environment);
        } else {
            if ( !checkPipeline() ) {
                throw new ProcessingException("Attempted to process incomplete pipeline.");
            }

            setupPipeline(environment);
            connectPipeline();

            // execute the pipeline:
            try {
                this.eventPipeline.process(environment);
            } catch ( Exception e ) {
                throw new ProcessingException(
                    "Failed to execute pipeline.",
                    e
                );
            }

            return true;
        }
    }

    /** Process the pipeline using a reader.
     * @throws ProcessingException if
     */
    protected boolean processReader(Environment environment)
    throws ProcessingException {
        String mimeType;
        try {
            this.reader.setup((EntityResolver) environment,environment.getObjectModel(),readerSource,readerParam);
            mimeType = this.reader.getMimeType();
            if ( mimeType != null ) {
                environment.setContentType(mimeType);
            } else if ( readerMimeType != null ) {
                environment.setContentType(this.readerMimeType);
            } else {
                environment.setContentType(this.sitemapReaderMimeType);
            }
            // has the read resource been modified?
            long lastModified = this.reader.getLastModified();
            if (lastModified != 0
                && environment.isResponseModified(lastModified) == false) {

                // environment supports this, so we are finished
                environment.setResponseIsNotModified();
                return true;
            }

            this.reader.setOutputStream(environment.getOutputStream());
            int length = this.reader.generate();
            if (length != 0) {
                environment.setContentLength(length);
            }
        } catch ( Exception e ) {
            throw new ProcessingException("Error reading resource",e);
        }
        return true;
    }

    /** Sanity check the non-reader pipeline.
     * @return true if the pipeline is 'sane', false otherwise.
     */
    protected boolean checkPipeline() {
        if ( this.eventPipeline == null ) {
            return false;
        }

        if ( this.serializer == null ) {
            return false;
        }

        return true;
    }

    /** Setup pipeline components.
     *  The mimeType is set for the serializer. The output stream
     *  must be set by the subclass.
     */
    protected void setupPipeline(Environment environment)
    throws ProcessingException {
        String mimeType = this.serializer.getMimeType();
        if (mimeType != null) {
            // we have a mimeType freom the component itself
            environment.setContentType (mimeType);
        } else if (serializerMimeType != null) {
            // there was a mimeType specified in the sitemap pipeline
            environment.setContentType (serializerMimeType);
        } else {
            // use the mimeType specified in the sitemap component declaration
            environment.setContentType (this.sitemapSerializerMimeType);
        }
    }

    /** Connect the pipeline.
     */
    protected void connectPipeline() throws ProcessingException {
        XMLProducer prev = (XMLProducer) this.eventPipeline;
        XMLConsumer next;

        // connect serializer.
        prev.setConsumer(this.serializer);
    }

    public void dispose() {
        if(readerSelector != null)
            manager.release((Component)readerSelector);
        if(serializerSelector != null)
            manager.release((Component)serializerSelector);
    }

    public void recycle() {
        try {
            // release reader.
            if ( this.readerSelector != null) {
                if ( this.reader != null ) {
                    readerSelector.release(this.reader);
                }
            }
            this.reader = null;

            // release eventPipeline
            this.eventPipeline = null;

            // release serializer
            if ( this.serializerSelector != null ) {
                if ( this.serializer != null ) {
                    serializerSelector.release(this.serializer);
                }
            }
            this.serializer = null;
        } catch ( Exception e ) {
            getLogger().warn(
                "Failed to release components from AbstractStreamPipeline.",
                e
            );
        } finally {
            this.reader = null;
            this.eventPipeline = null;
            this.serializer = null;
        }
    }
}
