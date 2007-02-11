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
 * @version CVS $Id: LocatedEvent.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public abstract class LocatedEvent extends AbstractEvent implements Locator {

    /** The column number of this event. */
    private int column = -1;

    /** The line number of this event. */
    private int line = -1;

    /** The system ID of this event. */
    private String system_id = null;

    /** The public ID of this event. */
    private String public_id = null;

    /**
     * Create a new <code>LocatedEvent</code> instance with full location
     * information.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     */
    public LocatedEvent(Locator locator) {
        super();
        this.locate(locator);
    }

    /**
     * Return the public identifier for the current document event.
     *
     * @return A <code>String</code> containing the public identifier,
     *         or <b>null</b> if none is available.
     */
    public String getPublicId() {
        return(this.public_id);
    }

    /**
     * Return the system identifier for the current document event.
     *
     * @return A <code>String</code> containing the system identifier,
     *         or <b>null</b> if none is available.
     */
    public String getSystemId() {
        return(this.system_id);
    }
    
    /**
     * Return the line number where the current document event ends.
     *
     * @return The line number, or -1 if none is available.
     */
    public int getLineNumber() {
        return(this.line);
    }

    /**
     * Return the column number where the current document event ends.
     *
     * @return The column number, or -1 if none is available.
     */
    public int getColumnNumber() {
        return(this.column);
    }

    /**
     * Update the location of the specified <code>LocatedEvent</code> with
     * the location information of this instance.
     * <br />
     * This does exactly the opposite of the <code>locate(...)</code> methods.
     *
     * @param event The <code>LocatedEvent</code> to be updated.
     * @see AbstractEvent#merge(Event)
     */
    protected void mergeLocation(LocatedEvent event) {
        event.locate(event);
    }

    /**
     * Set the location of this event.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     */
    private void locate(Locator locator) {
        if (locator != null) {
            this.public_id = locator.getPublicId();
            this.system_id = locator.getSystemId();
            this.line = locator.getLineNumber();
            this.column = locator.getColumnNumber();
        }
    }
}
