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
package org.apache.cocoon.serialization;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.jfor.jfor.converter.Converter;

/**
 * This class uses the <a href="http://www.jfor.org">jfor</a> library
 * to serialize XSL:FO documents to RTF streams.
 *
 * @author <a href="mailto:gianugo@rabellino.it">Gianugo Rabellino</a>
 * @version CVS $Id$
 */

public class RTFSerializer extends AbstractTextSerializer {

    private Writer rtfWriter;
    private Converter handler;


    /**
     * Set the OutputStream where the serializer will write to.
     *
     * @param out the OutputStream
     */
    public void setOutputStream(OutputStream out) {
        try {
            rtfWriter =
            new BufferedWriter(new OutputStreamWriter(out, "ISO-8859-1"));

            // FIXME Find a way to work with the org.apache.avalon.framework.logger.Logger
            handler = new Converter(rtfWriter,
               Converter.createConverterOption(System.out));
            super.setContentHandler(handler);

        } catch (Exception e) {
            getLogger().error("RTFSerializer.setOutputStream()", e);
            throw new CascadingRuntimeException("RTFSerializer.setOutputStream()", e);
        }
    }

    /**
     * Recycle the serializer. GC instance variables
     */
    public void recycle() {
        super.recycle();
        this.rtfWriter = null;
        this.handler = null;
    }
}
