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
package org.apache.cocoon.xml;

import org.apache.cocoon.xml.dom.DOMBuilder;
import org.w3c.dom.Node;

/**
 * Abstract implementation of {@link XMLFragment} for objects that are more easily represented
 * as SAX events.
 * <br/>
 * The toDOM() method is implemented by piping in a <code>DOMBuilder</code> the results
 * of <code>toSAX()</code> that must be implemented by concrete subclasses.
 *
 * @author <a href="mailto:sylvain.wallez@anyware-tech.com">Sylvain Wallez</a>
 * @version CVS $Id: AbstractSAXFragment.java,v 1.2 2004/03/08 14:04:00 cziegeler Exp $
 */

public abstract class AbstractSAXFragment implements XMLFragment {

    /**
     * Appends children representing the object's state to the given node by using
     * the results of <code>toSAX()</code>.
     */
    public void toDOM(Node node) throws Exception
    {
        DOMBuilder builder = new DOMBuilder(node);
        this.toSAX(builder);
    }
}
