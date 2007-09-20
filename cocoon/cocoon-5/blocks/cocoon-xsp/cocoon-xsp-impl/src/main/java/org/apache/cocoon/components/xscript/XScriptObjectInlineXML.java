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
package org.apache.cocoon.components.xscript;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

/**
 * An <code>XScriptObject</code> created from an inline XML fragment.
 *
 * @version $Id$
 * @since July 7, 2001
 */
public class XScriptObjectInlineXML extends XScriptObject {

    StringBuffer stringBuffer;
    StringBufferContentHandler streamHandler;

    public XScriptObjectInlineXML(XScriptManager manager) {
        this(manager, new StringBuffer("<?xml version=\"1.0\"?>\n\n"));
    }

    public XScriptObjectInlineXML(XScriptManager manager, String string) {
        this(manager, new StringBuffer(string));
    }

    public XScriptObjectInlineXML(XScriptManager manager, StringBuffer stringBuffer) {
        super(manager);
        this.stringBuffer = stringBuffer;
        this.streamHandler = new StringBufferContentHandler(this.stringBuffer);
    }

    public InputStream getInputStream() throws IOException {
        // FIXME(VG): This method should never be used because it
        // always converts content into system encoding. This will
        // ruin i18n documents. Use getInputSource() instead.
        return new ByteArrayInputStream(this.stringBuffer.toString().getBytes());
    }

    public InputSource getInputSource() throws IOException {
        InputSource is = new InputSource(new StringReader(this.stringBuffer.toString()));
        is.setSystemId(getURI());
        return is;
    }

    public ContentHandler getContentHandler() {
        return this.streamHandler;
    }

    public String toString() {
        return this.stringBuffer.toString();
    }

    public long getContentLength() {
        return this.stringBuffer.length();
    }

    public String getContent() {
        return this.stringBuffer.toString();
    }

    public String getURI() {
        // FIXME: Implement a URI scheme to be able to refer to XScript
        // variables by URI
        return "xscript:inline:" + System.identityHashCode(this);
    }

}
