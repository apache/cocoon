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

import org.xml.sax.Locator;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: DataEvent.java,v 1.1 2003/09/04 12:42:32 cziegeler Exp $
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
