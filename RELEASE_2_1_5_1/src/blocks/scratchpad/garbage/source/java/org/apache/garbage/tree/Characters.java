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
 * @version CVS $Id: Characters.java,v 1.3 2004/03/24 18:54:23 joerg Exp $
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
     * @param context
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
