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
package org.apache.garbage.tree;

import org.apache.commons.jxpath.JXPathContext;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: ElementEnd.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public class ElementEnd extends LocatedEvent {

    /** The qualified name of this element. */
    private String qualified = "";

    /** The name prefix of this element. */
    private String prefix = "";

    /** The local name of this element. */
    private String local = "";

    /**
     * Create a new <code>ElementEnd</code> instance.
     *
     * @param name The name of this element.
     */
    public ElementEnd(String name) {
        this(null, name);
    }

    /**
     * Create a new <code>ElementEnd</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param name The name of this element.
     */
    public ElementEnd(Locator locator, String name) {
        super(locator);
        if (name == null) {
            throw new TreeException(locator, "No name supplied");
        }

        this.qualified = name;
        int k = name.indexOf(':');
        if (k >= 0 ) {
            this.prefix = name.substring(0, k);
            this.local = name.substring(k + 1);
            if ((this.prefix.length() == 0) || (this.local.length() == 0)) {
                throw new TreeException(locator, "Invalid \"prefix:name\"");
            }
        } else {
            this.local = name;
        }
    }

    /**
     * Process this event in the context of the specified <code>Runtime</code>.
     *
     * @param runtime The <code>Runtime</code> receiving events notifications.
     * @throws SAXException In case of error processing this event.
     */
    public void process(Runtime runtime, JXPathContext context)
    throws SAXException {
        runtime.endElement(this.prefix, this.local, this.qualified);
    }
}
