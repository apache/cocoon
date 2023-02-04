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
package org.apache.cocoon.components.sax;

import org.apache.cocoon.components.sax.AbstractXMLByteStreamCompiler;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;


/**
 * This class compiles SAX events to an OutputStream.
 * If you want to reuse this instance, make sure to call {@link #recycle(OutputStream)} to set a new OutputStream
 * and reset the class inbetween two compilation tasks.
 *
 * @version $Id$
 */
public final class XMLOutputStreamCompiler
    extends AbstractXMLByteStreamCompiler {

    /** The buffer for the compile xml byte stream. */
    private OutputStream os;

    public XMLOutputStreamCompiler(final OutputStream out)
        throws IOException {
        super();
        this.recycle(out);
    }

    public void recycle(final OutputStream out) {
        this.os = out;
        super.recycle();
    }

    public void recycle() {
        this.recycle(this.os);
    }

    protected void write(final int b)
        throws SAXException {
        try {
            this.os.write((byte)b);
        } catch(final IOException e) {
            throw new SAXException(e);
        }
    }
}
