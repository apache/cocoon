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

import org.xml.sax.Locator;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: DataEvent.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public abstract class DataEvent extends LocatedEvent {

    /** Our private character array */
    private char data[] = null;

    /**
     * Create a new <code>DataEvent</code> instance.
     *
     * @param data An single character.
     */
    public DataEvent(char data) {
        this(null, data);
    }

    /**
     * Create a new <code>DataEvent</code> instance.
     *
     * @param data An array of characters.
     */
    public DataEvent(char data[]) {
        this(null, data, 0, data.length);
    }

    /**
     * Create a new <code>DataEvent</code> instance.
     *
     * @param data An array of characters.
     * @param start The position in the source array where the characters
     *              to be copied start from.
     * @param length The number of characters to copy.
     */
    public DataEvent(char data[], int start, int length) {
        this(null, data, start, length);
    }

    /**
     * Create a new <code>DataEvent</code> instance.
     *
     * @param data The source <code>String</code>.
     */
    public DataEvent(String data) {
        this(null, data);
    }

    /**
     * Create a new <code>DataEvent</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param data An single character.
     */
    public DataEvent(Locator locator, char data) {
        super(locator);
        this.data = new char[1];
        this.data[0] = data;
    }

    /**
     * Create a new <code>DataEvent</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param data An array of characters.
     */
    public DataEvent(Locator locator, char data[]) {
        this(locator, data, 0, data.length);
    }

    /**
     * Create a new <code>DataEvent</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param data An array of characters.
     * @param start The position in the source array where the characters
     *              to be copied start from.
     * @param length The number of characters to copy.
     */
    public DataEvent(Locator locator, char data[], int start, int length) {
        super(locator);

        try {
            this.data = new char[length];
            System.arraycopy(data, start, this.data, 0, length);
        } catch (Exception e) {
            throw new TreeException(locator, "Cannot create data event", e);
        }
    }

    /**
     * Create a new <code>DataEvent</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param data The source <code>String</code>.
     */
    public DataEvent(Locator locator, String data) {
        super(locator);
        if (data == null) {
            throw new TreeException(locator, "Cannot create data event");
        }
        this.data = data.toCharArray();
    }

    /**
     * Merge the data associated with this <code>DataEvent<code> to another
     * <code>DataEvent</code> instance and update its location..
     *
     * @param event The <code>DataEvent</code> to which this one should be merged.
     */
    protected void mergeData(DataEvent event) {
        char data[] = new char[this.data.length + event.data.length];
        System.arraycopy(event.data, 0, data, 0, event.data.length);
        System.arraycopy(this.data, 0, data, event.data.length, this.data.length);
        event.data = data;
        super.mergeLocation(event);
    }

    /**
     * Return the data contained in this <code>DataEvent</code>.
     * <br />
     * The returned character array is always a copy of the data held in this
     * instance.
     *
     * @return A characters array containing all data stored in this instance.
     */
    protected char[] getArrayValue() {
        char array[] = new char[this.data.length];
        System.arraycopy(this.data, 0, array, 0, this.data.length);
        return(array);
    }

    /**
     * Return the data contained in this <code>DataEvent</code> as a
     * <code>String</code>.
     *
     * @return A <code>String</code> containing all data stored in this instance.
     */
    public String getStringValue() {
        return(new String(this.data));
    }
}
