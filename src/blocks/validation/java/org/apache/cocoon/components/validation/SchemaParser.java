/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.validation;

import java.io.IOException;

import org.apache.avalon.framework.thread.ThreadSafe;
import org.xml.sax.SAXException;

/**
 * <p>A component parsing schemas usable for XML validation and returning them as
 * {@link Schema} instances.</p>
 * 
 * <p>This interface does not imply any requirement in terms of the grammar used
 * to produce {@link Schema} instances. Normally multiple-grammar selection is
 * provided through the {@link Validator} class.</p>
 * 
 * <p>The only requirement imposed by this interface is that the final class
 * implementing this interface must be {@link ThreadSafe}.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public interface SchemaParser extends ThreadSafe {

    /** <p>Avalon Role name of this component.</p> */
    public static final String ROLE = SchemaParser.class.getName();

    /**
     * <p>Parse the specified URI and return a {@link Schema}.</p>
     * 
     * <p>Once parsed, the returned {@link Schema} can be used multiple time to
     * validate documents by sending their SAX events to the handler returned by
     * the {@link Schema#newValidator()}.</p>
     * 
     * @param uri the URI of the {@link Schema} to return.
     * @return a <b>non-null</b> {@link Schema} instance.
     * @throws SAXException if an error occurred parsing the schema.
     * @throws IOException if an I/O error occurred parsing the schema.
     */
    public Schema getSchema(String uri)
    throws SAXException, IOException;

    /**
     * <p>Return an array of {@link String}s containing all schema grammars
     * supported by this {@link SchemaParser}.</p>
     * 
     * <p>All {@link String}s in the array returned by this method should be
     * valid grammar names as defined in the {@link Validator} class.</p>
     *
     * @return a <b>non-null</b> array of {@link String}s.
     */
    public String[] getSupportedGrammars();

}
