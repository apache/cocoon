/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: CommandLineSession.java,v 1.1 2003/03/09 00:09:29 pier Exp $
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

