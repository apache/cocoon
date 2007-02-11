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
package org.apache.cocoon.environment.commandline;

import org.apache.cocoon.environment.Session;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * Command-line version of Http Session.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: CommandLineSession.java,v 1.2 2004/03/05 13:02:54 bdelacretaz Exp $
 */
public final class CommandLineSession
implements Session {

    private long creationTime = System.currentTimeMillis();

    private Hashtable attributes = new Hashtable();

    public CommandLineSession() {
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public String getId() {
        return "1";
    }

    public long getLastAccessedTime() {
        return this.creationTime;
    }

    public void setMaxInactiveInterval(int interval) {
        // ignored
    }

    public int getMaxInactiveInterval() {
        return -1;
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public Enumeration getAttributeNames() {
        return this.attributes.keys();
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public void invalidate() {
        this.attributes.clear();
        invalidateSession();
    }

    public boolean isNew() {
        return false;
    }

    protected static CommandLineSession session;

    /**
     * Get the current session object - if available
     */
    public static Session getSession(boolean create) {
        if (create && session == null) {
            session = new CommandLineSession();
        }
        return session;
    }

    /**
     * Invalidate the current session
     */
    public static void invalidateSession() {
        session = null;
    }

}

