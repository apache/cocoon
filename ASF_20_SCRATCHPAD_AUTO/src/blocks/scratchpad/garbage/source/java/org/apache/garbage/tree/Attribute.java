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

import java.util.Iterator;

import org.apache.commons.jxpath.JXPathContext;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: Attribute.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public class Attribute extends LocatedEvents implements Evaluation {

    /** The name prefix of this attribute. */
    protected String qualified = "";

    /** The name prefix of this attribute. */
    protected String prefix = "";

    /** The local name of this attribute. */
    protected String local = "";

    /**
     * Create a new <code>Attribute</code> instance specifying its name.
     *
     * @param name The attribute name.
     * @throws TreeException If this instance cannot be created.
     */
    public Attribute(String name)
    throws TreeException {
        this(null, name);
    }

    /**
     * Create a new <code>Attribute</code> instance specifying its name and
     * its position.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param name The attribute name.
     * @throws TreeException If this instance cannot be created.
     */
    public Attribute(Locator locator, String name)
    throws TreeException {
        super(locator);

        if (name == null) {
            throw new TreeException(locator, "Attribute name cannot be null");
        }

        this.qualified = name;
        int k = name.indexOf(':');
        if (k >= 0 ) {
            this.prefix = name.substring(0, k);
            this.local = name.substring(k + 1);
            if ((this.prefix.length() == 0) || (this.local.length() == 0)) {
                throw new TreeException(locator, "Invalid \"prefix:name\"");
            }
        } else if ("xmlns".equals(name)) {
            this.prefix = name;
            this.local = "";
        } else {
            this.local = name;
        }
    }

    /**
     * Add a new event to this <code>Attribute</code>.
     *
     * @param event The <code>Event</code> instance to add.
     * @throws TreeException If the specified <code>Event</code> is null or
     *                       not an instance of <code>Characters</code> or
     *                       <code>Expression</code>.
     */
    public void append(Event event)
    throws TreeException {
        if (event == null) {
            throw new TreeException(this, "Cannot add null event");
        }

        if (event instanceof Evaluation) {
            super.append(event);
            return;
        }

        throw new TreeException(this, "Cannot add invalid event type: "
                                + event.getClass().getName());
    }

    /**
     * Evaluate the current event and return its <code>String</code> value to
     * be included as a part of an attribute value.
     *
     * @param runtime The <code>Runtime</code> receiving events notifications.
     * @throws SAXException In case of error processing this event.
     */
    public String evaluate(JXPathContext context)
    throws SAXException {
        StringBuffer buf = new StringBuffer();
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            buf.append(((Evaluation)iterator.next()).evaluate(context));
        }
        return(buf.toString());
    }

    /**
     * Compare this <code>Attribute</code> instance for equality.
     * <p>
     * This method will return <b>true</b> if the specified object is an
     * instance of <code>Attribute</code> and its qualified name equals
     * our qualified name.
     */
    public boolean equals(Object o) {
        if (!(o instanceof Attribute)) return(false);
        return(this.qualified.equals(((Attribute)o).qualified));
    }
}
