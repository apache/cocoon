/*
 * Copyright 2001,2004 The Apache Software Foundation.
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
 * @version CVS $Id: AbstractStreamWriteableSource.java,v 1.3 2004/03/05 13:02:40 bdelacretaz Exp $
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
