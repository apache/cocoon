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
package org.apache.cocoon.xml.dom;

import org.w3c.dom.Document;

/**
 * This interface identifies classes producing instances of DOM
 * <code>Document</code> objects.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @version CVS $Id: DOMFactory.java,v 1.2 2004/03/05 13:03:02 bdelacretaz Exp $
 */
public interface DOMFactory {
    /**
     * Create a new Document object.
     */
    Document newDocument();

    /**
     * Create a new Document object with a specified DOCTYPE.
     */
    Document newDocument(String name);

    /**
     * Create a new Document object with a specified DOCTYPE, public ID and
     * system ID.
     */
    Document newDocument(String name, String publicId, String systemId);
}
