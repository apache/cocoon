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
 * @version CVS $Id: TreeException.java,v 1.1 2003/06/21 21:11:53 pier Exp $
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
