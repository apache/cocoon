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
package org.apache.cocoon.serialization;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.CascadingIOException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: XMLSerializer.java,v 1.3 2004/03/08 14:03:32 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=Serializer
 * @x-avalon.lifestyle type=pooled
 * @x-avalon.info name=xml-serializer
 */

public class XMLSerializer extends AbstractTextSerializer {

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        super.configure( conf );
        this.format.put(OutputKeys.METHOD,"xml");
    }

    /**
     * Set the {@link OutputStream} where the requested resource should
     * be serialized.
     */
    public void setOutputStream(OutputStream out) throws IOException {
        super.setOutputStream(out);
        try {
            TransformerHandler handler = this.getTransformerHandler();
            handler.getTransformer().setOutputProperties(this.format);
            handler.setResult(new StreamResult(this.output));
            this.setContentHandler(handler);
            this.setLexicalHandler(handler);
        } catch (Exception e) {
            final String message = "Cannot set XMLSerializer outputstream"; 
            throw new CascadingIOException(message, e);
        }
    }

}
