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

import org.apache.cocoon.xml.XMLConsumer;

/**
 * This interfaces identifies classes that serialize XML data, receiving
 * notification of SAX events.
 * <br>
 * It's beyond the scope of this interface to specify the format for
 * the serialized data.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: XMLSerializer.java,v 1.3 2004/03/08 14:01:56 cziegeler Exp $
 */
public interface XMLSerializer extends XMLConsumer {

    String ROLE = XMLSerializer.class.getName();
    /**
     * Get the serialized xml data
     *
     * @return The serialized xml data.
     */
    Object getSAXFragment();
}
