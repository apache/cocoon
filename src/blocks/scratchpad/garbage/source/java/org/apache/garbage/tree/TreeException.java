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
 * @version CVS $Id: TreeException.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public class TreeException extends RuntimeException implements Locator {

    /** The column number of this event. */
    private int column = -1;

    /** The line number of this event. */
    private int line = -1;

    /** The system ID of this event. */
    private String system_id = null;

    /** The public ID of this event. */
    private String public_id = null;

    /** The nested exception. */
    private Exception exception = null;

    /**
     * Create a new <code>TreeException</code> instance with a specified
     * detail message and location information.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param message The detail message of this exception.
     */
    public TreeException(Locator locator, String message) {
        this(locator, message, null);
    }

    /**
     * Create a new <code>TreeException</code> instance with a specified
     * nested exception and location information.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param e The nested exception.
     */
    public TreeException(Locator locator, Exception e) {
        this(locator, null, e);
    }

    /**
     * Create a new <code>TreeException</code> instance with a specified
     * detail message and location information.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param message The detail message of this exception.
     * @param e The nested exception.
     */
    public TreeException(Locator locator, String message, Exception e) {
        super(message == null? e.getMessage(): message);
        this.exception = e;
        if (locator != null) {
            this.public_id = locator.getPublicId();
            this.system_id = locator.getSystemId();
            this.line = locator.getLineNumber();
            this.column = locator.getColumnNumber();
        }
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
     * Return the <code>Exception</code> nested by this instance.
     *
     * @return An <code>Exception</code> or null if none is available.
     */
    public Exception getException() {
        return(this.exception);
    }

    /**
     * Return the message associated with this <code>Exception</code>.
     *
     * @return The message with (if possible) location information.
     */
    public String getMessage() {
        StringBuffer buf = new StringBuffer(super.getMessage());
        if (this.system_id != null) {
            buf.append(" in file \"");
            buf.append(this.system_id);
            buf.append('"');
            if (this.public_id != null) {
                buf.append(" with public ID \"");
                buf.append(this.public_id);
                buf.append('"');
            }
        }

        if (this.line >= 0) {
            buf.append(" at line ");
            buf.append(this.line);
            if (this.column >= 0) {
                buf.append(" column ");
                buf.append(this.column);
            }
        }
        buf.append('.');
        return(buf.toString());
    }
}
