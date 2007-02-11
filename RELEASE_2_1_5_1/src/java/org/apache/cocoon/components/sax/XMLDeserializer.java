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
package org.apache.cocoon.components.sax;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.xml.XMLProducer;
import org.xml.sax.SAXException;

/**
 * This interfaces identifies classes that deserialize XML data, sending SAX
 * events to the configured <code>XMLConsumer</code> (or SAX
 * <code>ContentHandler</code> and <code>LexicalHandler</code>).
 * <br>
 * The production of the xml data is started by passing an
 * the xml information to the <code>deserialize</code>
 * method.
 * It is beyond the scope of this interface to specify the format of
 * the serialized data.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: XMLDeserializer.java,v 1.3 2004/03/05 13:02:50 bdelacretaz Exp $
 */
public interface XMLDeserializer extends XMLProducer, Component {

    String ROLE = XMLDeserializer.class.getName();

    /**
     * Deserialize the xml data and stream it.
     *
     * @param saxFragment The xml data.
    */
    void deserialize(Object saxFragment)
    throws SAXException;
    
}
