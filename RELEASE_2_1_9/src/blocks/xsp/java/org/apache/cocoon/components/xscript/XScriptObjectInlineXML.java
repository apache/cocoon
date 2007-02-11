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
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: XScriptObjectInlineXML.java,v 1.1 2004/03/10 12:58:08 stephan Exp $
 * @since July 7, 2001
 */
public class XScriptObjectInlineXML extends XScriptObject {

    StringBuffer stringBuffer;
    StringBufferContentHandler streamHandler;

    public XScriptObjectInlineXML(XScriptManager manager) {
        super(manager);
        stringBuffer = new StringBuffer();
        stringBuffer.append("<?xml version=\"1.0\"?>\n\n");
        streamHandler = new StringBufferContentHandler(stringBuffer);
    }

    public XScriptObjectInlineXML(XScriptManager manager, StringBuffer stringBuffer) {
        super(manager);
        this.stringBuffer = stringBuffer;
        streamHandler = new StringBufferContentHandler(stringBuffer);
    }

    public XScriptObjectInlineXML(XScriptManager manager, String string) {
        super(manager);
        this.stringBuffer = new StringBuffer(string);
        streamHandler = new StringBufferContentHandler(stringBuffer);
    }

    public InputStream getInputStream() throws IOException {
        // FIXME(VG): This method should never be used because it
        // always converts content into system encoding. This will
        // ruin i18n documents. Use getInputSource() instead.
        return new ByteArrayInputStream(stringBuffer.toString().getBytes());
    }

    public InputSource getInputSource() throws IOException {
        InputSource is = new InputSource(new StringReader(stringBuffer.toString()));
        is.setSystemId(getURI());
        return is;
    }

    public ContentHandler getContentHandler() {
        return streamHandler;
    }

    public String toString() {
        return stringBuffer.toString();
    }

    public long getContentLength() {
        return stringBuffer.length();
    }

    public String getContent() {
        return stringBuffer.toString();
    }

    public String getURI() {
        // FIXME: Implement a URI scheme to be able to refer to XScript
        // variables by URI
        return "xscript:inline:" + System.identityHashCode(this);
    }
}
