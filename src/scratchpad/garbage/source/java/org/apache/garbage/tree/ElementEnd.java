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

import org.apache.commons.jxpath.JXPathContext;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: ElementEnd.java,v 1.1 2003/06/21 21:11:53 pier Exp $
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
