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
package org.apache.cocoon.xml;

import org.apache.cocoon.xml.dom.DOMBuilder;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * Abstract implementation of {@link XMLFragment} for objects that are more
 * easily represented as SAX events.
 *
 * <p>The {@link #toDOM} method is implemented by piping in a {@link DOMBuilder}
 * the results of {@link #toSAX(ContentHandler)} that must be implemented by concrete
 * subclasses.</p>
 *
 * @version $Id$
 */
public abstract class AbstractSAXFragment implements XMLFragment {

    /**
     * Appends children representing the object's state to the given node
     * by using the results of <code>toSAX()</code>.
     */
    public void toDOM(Node node) throws Exception {
        toSAX(new DOMBuilder(node));
    }
}
