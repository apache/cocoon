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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.cocoon.components.elementprocessor.ElementProcessor;
import org.apache.cocoon.components.elementprocessor.impl.poi.POIFSElementProcessor;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.xml.sax.SAXException;

/**
 *  An extension of ElementProcessorSerializer with extensions for dealing with
 *  the POIFS filesystem This is an abstract class. Concrete extensions need to
 *  implement the following methods:
 *  <ul>
 *    <li> String getMimeType()</li>
 *    <li> void doLocalPreEndDocument()</li>
 *    <li> void doLocalPostEndDocument()</li>
 *    <li> ElementProcessorFactory getElementProcessorFactory()</li>
 *    </li>
 *  </ul>
 *
 * @version $Id$
 */
public abstract class POIFSSerializer extends ElementProcessorSerializer {

    private final POIFSFileSystem _filesystem;

    /**
     * Constructor
     */
    public POIFSSerializer() {
        _filesystem = new POIFSFileSystem();
    }

    /**
     * Receive notification of the end of a document.
     *
     * @exception  SAXException  if there is an error writing the document to the
     *      output stream
     */
    public void endDocument() throws SAXException {
        doLocalPreEndDocument();

        OutputStream stream = getOutputStream();

        if ( stream != null ) {
            try {
                _filesystem.writeFilesystem( stream );
            } catch ( IOException e ) {
                throw SAXExceptionFactory("could not process endDocument event", e );
            }
        } else {
            throw SAXExceptionFactory("no outputstream for writing the document!!" );
        }
        doLocalPostEndDocument();
    }

    /**
     * Provide access to the filesystem for extending classes
     *
     * @return    the filesystem
     */
    protected POIFSFileSystem getFilesystem() {
        return _filesystem;
    }

    /**
     * Extending classes should do whatever they need to do prior to writing the
     * filesystem out
     */
    protected abstract void doLocalPreEndDocument();

    /**
     * Extending classes should do whatever they need to do after writing the
     * filesystem out
     */
    protected abstract void doLocalPostEndDocument();

    /**
     * perform pre-initialization on an element processor
     *
     * @param  processor         the element processor to be iniitialized
     * @exception  SAXException  on errors
    */
    protected void doPreInitialization(ElementProcessor processor)
    throws SAXException {
        try {
            ((POIFSElementProcessor)processor).setFilesystem(_filesystem);
        } catch (ClassCastException e) {
            throw SAXExceptionFactory( "could not pre-initialize processor", e );
        }
    }
}
