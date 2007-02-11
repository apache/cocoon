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
package org.apache.garbage.serializer;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: Serializer.java,v 1.2 2004/03/05 10:07:22 bdelacretaz Exp $
 */
public interface Serializer extends ContentHandler, LexicalHandler {

    /**
     * Reset this <code>Serializer</code>.
     */
    public void reset();

    /**
     * Set the <code>OutputStream</code> where this serializer will
     * write data to.
     *
     * @param out The <code>OutputStream</code> used for output.
     */
    public void setOutput(OutputStream out);

    /**
     * Set the <code>OutputStream</code> where this serializer will
     * write data to.
     *
     * @param out The <code>OutputStream</code> used for output.
     * @param encoding The character encoding to use.
     * @throws UnsupportedEncodingException If the specified encoding is not
     *                                      supported by this Java VM.
     */
    public void setOutput(OutputStream out, String encoding)
    throws UnsupportedEncodingException;

    /**
     * Set the <code>Writer</code> where this serializer will write data to.
     *
     * @param out The <code>Writer</code> used for output.
     */
    public void setOutput(Writer out);

    /**
     * Return the MIME Content-Type produced by this serializer.
     */
    public String getContentType();
}
