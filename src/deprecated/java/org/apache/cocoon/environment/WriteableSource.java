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
 * @version CVS $Id: WriteableSource.java,v 1.2 2003/04/27 15:16:15 cziegeler Exp $
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
