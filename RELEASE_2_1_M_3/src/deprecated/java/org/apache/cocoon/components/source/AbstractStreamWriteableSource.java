/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.cocoon.components.source;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This abstract class provides convenience methods to implement
 * a stream based <code>org.apache.cocoon.environment.WriteableSource</code>.
 * Implement getOutputStream() to obtain a valid implementation.
 * <p>
 * This base implementation creates a <code>ContentHandler</code> by using
 * the sitemap 'xml' serializer to write SAX events to the stream returned by
 * <code>getOutputStream()</code>.
 *
 * @deprecated Use the new Avalon Excalibur Source Resolving
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: AbstractStreamWriteableSource.java,v 1.2 2003/03/16 17:49:10 vgritsenko Exp $
 */
public abstract class AbstractStreamWriteableSource
    extends AbstractStreamSource
    implements org.apache.cocoon.environment.WriteableSource {

    protected AbstractStreamWriteableSource(ComponentManager manager) {
        super(manager);
    }

    /**
     * Checks if the <code>OutputStream</code> under <code>handler</code> can be cancelled.
     *
     * @see #canCancel(OutputStream)
     */
    public boolean canCancel(ContentHandler handler) {
        if (handler instanceof WritingPipe) {
            WritingPipe pipe = (WritingPipe)handler;
            if (pipe.getSource() == this) {
                return pipe.canCancel();
            }
        }

        // Not a valid handler for this source
        throw new IllegalArgumentException("The handler is not associated to this source");
    }

    /**
     * Always return <code>false</code>. To be redefined by implementations that support
     * <code>cancel()</code>.
     */
    public boolean canCancel(OutputStream stream) {
        return false;
    }

    /**
     * Cancels the <code>OutputStream</code> under <code>handler</code>.
     *
     * @see #cancel(OutputStream)
     */
    public void cancel(ContentHandler handler) throws Exception {
        if (handler instanceof WritingPipe) {
            WritingPipe pipe = (WritingPipe)handler;
            if (pipe.getSource() == this) {
                pipe.cancel();
                return;
            }
        }

        // Not a valid handler for this source
        throw new IllegalArgumentException("The handler is not associated to this source");
    }

    /**
     * Always throw <code>UnsupportedOperationException</code>. To be redefined by
     * implementations that support <code>cancel()</code>.
     */
    public void cancel(OutputStream stream) throws Exception {
        throw new UnsupportedOperationException("Cancel is not implemented on " +
            this.getClass().getName());
    }

    /**
     * Get a <code>ContentHandler</code> to write a SAX stream to this source. It
     * uses either the 'xml' or 'html' serializer depending on the result of
     * {@link #isHTMLContent()} to serialize events, and thus these serializers must
     * exist in this source's component manager.
     */
    public ContentHandler getContentHandler() throws SAXException, ProcessingException {

        Serializer serializer;
        ComponentSelector selector;

        String serializerName = this.isHTMLContent() ? "html" : "xml";

        // Get the serializer
        try {
            selector =
                (ComponentSelector)this.manager.lookup(Serializer.ROLE + "Selector");
            serializer = (Serializer)selector.select(serializerName);
        } catch(ComponentException ce) {
            throw new ProcessingException("Cannot get '" + serializerName + "' serializer");
        }

        try {
            return new WritingPipe(getOutputStream(), selector, serializer);
        } catch(IOException ioe) {
            selector.release(serializer);
            throw new ProcessingException("Cannot open stream for " + this.getSystemId(), ioe);
        }
    }

    /**
     * A pipe that closes the outputstream at the end of the document and handles cancel().
     */
    private class WritingPipe extends AbstractXMLPipe {

        // The output stream
        private OutputStream output;

        // Serialier and its selector for proper release
        private Serializer serializer;
        private ComponentSelector selector;

        public WritingPipe(OutputStream output, ComponentSelector selector, Serializer serializer)
          throws IOException {
            this.output = output;
            this.selector = selector;
            this.serializer = serializer;

            // Connect this pipe, the serializer and the output stream
            this.setConsumer(this.serializer);
            this.serializer.setOutputStream(this.output);
        }

        public org.apache.cocoon.environment.WriteableSource getSource() {
            return AbstractStreamWriteableSource.this;
        }

        /**
         * Close the underlying stream
         */
        public void endDocument() throws SAXException {
            super.endDocument();
            try {
                close();
            }
            catch(Exception e) {
                throw new SAXException("Error while closing output stream", e);
            }
        }

        public boolean canCancel() {
            return this.output != null;
        }

        /**
         * Cancel the wrapped output stream
         */
        public void cancel() throws Exception {
            AbstractStreamWriteableSource.this.cancel(output);
            close();
        }

        private void close() throws IOException {
            if (this.serializer != null) {
                // Disconnect serializer;
                this.recycle();
                // and release it
                this.selector.release(this.serializer);
                this.serializer = null;
            }

            if (this.output != null) {
                this.output.close();
                this.output = null;
            }
        }

        // Ensure all is closed properly
        protected void finalize() throws Throwable {
            close();
            super.finalize();
        }
    }
}
