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
package org.apache.cocoon.components.source.helpers;

import java.util.Date;

/**
 * This interface for lock of a source
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @version CVS $Id: SourceLock.java,v 1.2 2004/03/05 13:02:21 bdelacretaz Exp $
 */
public class SourceLock {

    private String  subject;
    private String  type;
    private Date    expiration;
    private boolean inheritable;
    private boolean exclusive;

    /**
     * Creates a new lock for a source
     *
     * @param subject Which user should be locked
     * @param type Type of lock
     * @param expiration When the lock expires
     * @param inheritable If the lock is inheritable
     * @param exclusive If the lock is exclusive
     */
    public SourceLock(String subject, String type, Date expiration,
                      boolean inheritable, boolean exclusive) {

        this.subject     = subject;
        this.type        = type;
        this.expiration  = expiration;
        this.inheritable = inheritable;
        this.exclusive   = exclusive;
    }

    /**
     *  Sets the subject for this lock
     *
     * @param subject Which user should be locked
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * return the subject of the lock
     * 
     * @return Which user should be locked
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Sets the type of the lock
     *
     * @param type Type of lock
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Return ths type of the lock
     * 
     * @return Type of lock
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the expiration date
     *
     * @param expiration Expiration date
     */
    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    /**
     * Returns the expiration date
     * 
     * @return Expiration date
     */
    public Date getExpiration() {
        return this.expiration;
    }

    /**
     * Sets the inheritable flag
     *
     * @param inheritable If the lock is inheritable
     */
    public void setInheritable(boolean inheritable) {
        this.inheritable = inheritable;
    }

    /**
     * Returns the inheritable flag
     * 
     * @return If the lock is inheritable
     */
    public boolean isInheritable() {
        return this.inheritable;
    }

    /**
     * Sets the exclusive flag
     *
     * @param exclusive If the lock is exclusive
     */
    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    /**
     * Returns the exclusive flag
     * 
     * @return If the lock is exclusive
     */
    public boolean isExclusive() {
        return this.exclusive;
    }
}
