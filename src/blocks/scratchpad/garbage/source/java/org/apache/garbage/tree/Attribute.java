/* ============================================================================ *
 *                   The Apache Software License, Version 1.1                   *
 * ============================================================================ *
 *                                                                              *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved. *
 *                                                                              *
 * Redistribution and use in source and binary forms, with or without modifica- *
 * tion, are permitted provided that the following conditions are met:          *
 *                                                                              *
 * 1. Redistributions of  source code must  retain the above copyright  notice, *
 *    this list of conditions and the following disclaimer.                     *
 *                                                                              *
 * 2. Redistributions in binary form must reproduce the above copyright notice, *
 *    this list of conditions and the following disclaimer in the documentation *
 *    and/or other materials provided with the distribution.                    *
 *                                                                              *
 * 3. The end-user documentation included with the redistribution, if any, must *
 *    include  the following  acknowledgment:  "This product includes  software *
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)." *
 *    Alternately, this  acknowledgment may  appear in the software itself,  if *
 *    and wherever such third-party acknowledgments normally appear.            *
 *                                                                              *
 * 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be *
 *    used to  endorse or promote  products derived from  this software without *
 *    prior written permission. For written permission, please contact          *
 *    apache@apache.org.                                                        *
 *                                                                              *
 * 5. Products  derived from this software may not  be called "Apache", nor may *
 *    "Apache" appear  in their name,  without prior written permission  of the *
 *    Apache Software Foundation.                                               *
 *                                                                              *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND *
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE *
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT, *
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU- *
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS *
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON *
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT *
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF *
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
 *                                                                              *
 * This software  consists of voluntary contributions made  by many individuals *
 * on  behalf of the Apache Software  Foundation.  For more  information on the *
 * Apache Software Foundation, please see <http://www.apache.org/>.             *
 *                                                                              *
 * ============================================================================ */
package org.apache.garbage.tree;

import java.util.Iterator;

import org.apache.commons.jxpath.JXPathContext;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: Attribute.java,v 1.1 2003/09/04 12:42:32 cziegeler Exp $
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
