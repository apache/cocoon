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

package org.apache.cocoon.environment;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.cocoon.ProcessingException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A {@link Source} that can be written to. It provides two methods that
 * allow for SAX-based and byte-based output.
 * <p>
 * Callers will use the most appropriate method for their use and
 * it's up to the implementation to handle both sources. For example,
 * an XML-based implementation can use a parser to convert bytes written
 * to the <code>OutputStream</code> to SAX events, and a byte-based
 * implementation (such as file), can use a serializer to convert
 * SAX events to a byte stream.
 *
 * @deprecated Use the {@link org.apache.excalibur.source.ModifiableSource} interface instead
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: WriteableSource.java,v 1.3 2004/03/05 13:02:41 bdelacretaz Exp $
 */
public interface WriteableSource extends ModifiableSource {

    /**
     * Does this source actually exist ?
     *
     * @return true if the resource exists.
     */
    boolean exists();

    /**
     * Get a <code>ContentHandler</code> where an XML document can
     * be written using SAX events.
     * <p>
     * Care should be taken that the returned handler can actually
     * be a {@link org.apache.cocoon.xml.XMLConsumer} supporting also
     * lexical events such as comments.
     *
     * @return a handler for SAX events
     */
    ContentHandler getContentHandler() throws SAXException, ProcessingException;

    /**
     * Get an <code>InputStream</code> where raw bytes can be written to.
     * The signification of these bytes is implementation-dependent and
     * is not restricted to a serialized XML document.
     *
     * @return a stream to write to
     */
    OutputStream getOutputStream() throws IOException, ProcessingException;

    /**
     * Can the data sent to a <code>ContentHandler</code> returned by
     * {@link #getContentHandler()} be cancelled ?
     *
     * @return true if the handler can be cancelled
     */
    boolean canCancel(ContentHandler handler);

    /**
     * Can the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()} be cancelled ?
     *
     * @return true if the stream can be cancelled
     */
    boolean canCancel(OutputStream stream);

    /**
     * Cancel the data sent to a <code>ContentHandler</code> returned by
     * {@link #getContentHandler()}.
     * <p>
     * After cancel, the handler should no more be used.
     */
    void cancel(ContentHandler handler) throws Exception;

    /**
     * Cancel the data sent to an <code>OutputStream</code> returned by
     * {@link #getOutputStream()}.
     * <p>
     * After cancel, the stream should no more be used.
     */
    void cancel(OutputStream stream) throws Exception;
}
