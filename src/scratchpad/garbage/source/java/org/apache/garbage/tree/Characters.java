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
 * @version CVS $Id: Characters.java,v 1.1 2003/06/21 21:11:53 pier Exp $
 */
public class Characters extends DataEvent implements Evaluation {

    /**
     * Create a new <code>Characters</code> instance.
     *
     * @param data An single character.
     */
    public Characters(char data) {
        super(data);
    }

    /**
     * Create a new <code>Characters</code> instance.
     *
     * @param data An array of characters.
     */
    public Characters(char data[]) {
        super(data);
    }

    /**
     * Create a new <code>Characters</code> instance.
     *
     * @param data An array of characters.
     * @param start The position in the source array where the characters
     *              to be copied start from.
     * @param length The number of characters to copy.
     */
    public Characters(char data[], int start, int length) {
        super(data, start, length);
    }

    /**
     * Create a new <code>Characters</code> instance.
     *
     * @param data The source <code>String</code>.
     */
    public Characters(String data) {
        super(data);
    }

    /**
     * Create a new <code>Characters</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param data An single character.
     */
    public Characters(Locator locator, char data) {
        super(locator, data);
    }

    /**
     * Create a new <code>Characters</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param data An array of characters.
     */
    public Characters(Locator locator, char data[]) {
        super(locator, data);
    }

    /**
     * Create a new <code>Characters</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param data An array of characters.
     * @param start The position in the source array where the characters
     *              to be copied start from.
     * @param length The number of characters to copy.
     */
    public Characters(Locator locator, char data[], int start, int length) {
        super(locator, data, start, length);
    }

    /**
     * Create a new <code>Characters</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param data The source <code>String</code>.
     */
    public Characters(Locator locator, String data) {
        super(locator, data);
    }

    /**
     * If possible, merge this <code>Event</code> to another.
     *
     * @param event The <code>Event</code> to which this one should be merged.
     * @return <b>true</b> if the merging was successful, <b>false</b> in all
     *         other cases.
     */
    public boolean merge(Event event) {
        if (event instanceof Characters) {
            super.mergeData((DataEvent)event);
            return(true);
        }
        return(false);
    }

    /**
     * Evaluate the current event and return its <code>String</code> value to
     * be included as a part of an attribute value.
     *
     * @param runtime The <code>Runtime</code> receiving events notifications.
     */
    public String evaluate(JXPathContext context) {
        return(this.getStringValue());
    }

    /**
     * Process this event in the context of the specified <code>Runtime</code>.
     *
     * @param runtime The <code>Runtime</code> receiving events notifications.
     * @throws SAXException In case of error processing this event.
     */
    public void process(Runtime runtime, JXPathContext context)
    throws SAXException {
        runtime.characters(this.getArrayValue());
    }
}
