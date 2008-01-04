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

import org.apache.cocoon.components.sax.AbstractXMLByteStreamInterpreter;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;


/**
 * This class interpreter compiled SAX event from an InputStream.
 * If you want to reuse this interpreter make sure to call first {@link #recycle()}
 * and then set the new consumer for the sax events before restarting with the {@link #deserialize(InputStream)} method.
 *
 * @version $Id: XMLInputStreamInterpreter.java 587751 2007-10-24 02:41:36Z vgritsenko $
 */
public final class XMLInputStreamInterpreter
    extends AbstractXMLByteStreamInterpreter {

    private InputStream is;

    public void deserialize(final InputStream inputStream)
        throws SAXException {
        this.is = inputStream;
        super.parse();
    }

    public void recycle() {
        this.is = null;
        super.recycle();
    }

    protected int read()
        throws SAXException {
        try {
            return is.read();
        } catch(final IOException e) {
            throw new SAXException(e);
        }
    }

    protected int read(final byte[] b)
        throws SAXException {
        try {
            return is.read(b);
        } catch(final IOException e) {
            throw new SAXException(e);
        }
    }
}
