/*-- $Id: Monitor.java,v 1.6 2000-03-06 11:46:00 stefano Exp $ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

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

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
package org.apache.cocoon.framework;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class watches over the changes of indicated resources.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.6 $ $Date: 2000-03-06 11:46:00 $
 */

public class Monitor {

    private Hashtable table;

    class Container {
        public Object resource;
        public long timestamp;

        public Container(Object resource, long timestamp) {
            this.resource = resource;
            this.timestamp = timestamp;
        }
    }

    class MultiContainer {
        private Vector v = new Vector();

        public MultiContainer(Object o1, Object o2) {
            this.add(o1);
            this.add(o2);
        }

        public void add(Object o) {
            if (!this.v.contains(o)) this.v.addElement(o);
        }

        public Enumeration elements() {
            return this.v.elements();
        }
    }

    public Monitor(int capacity) {
        this.table = new Hashtable(capacity);
    }

    /**
     * Tells the monitor to watch the given resource, timestamps it
     * and associate it to the given key.
     */
    public void watch(Object key, Object resource) {
        Object o = table.get(key);
        if (o == null) {
            this.table.put(key, new Container(resource, timestamp(resource)));
        } else {
            Container c = new Container(resource, timestamp(resource));
            if (o instanceof MultiContainer) {
                ((MultiContainer) o).add(c);
            } else {
                this.table.put(key, new MultiContainer(o, c));
            }
        }
    }

    /**
     * Queries the monitor for changes. For maximum reliability, this
     * method is synchronous, but less reliable for faster asynchronous
     * versions could be implemented. Returns true if the resource
     * associated to the given key has changed since the last call
     * to watch.
     *
     * WARNING: due to a stupid bug in "FileURLConnection", the
     * class that implements the "file:" protocol for the java.net.URL
     * framework, the getLastModified() method always returns 0.
     * For this reason, the use of the File resource is strongly
     * suggested over the "file:" type URL.
     * NOTE: this may not be (and should not be) the case in other
     * virtual machine implementations or if we rewrite the URL
     * handler ourselves (which I don't care to do at this point).
     */
    public boolean hasChanged(Object context) {
        Object o = this.table.get(context);
        if (o != null) {
            if (o instanceof Container) {
                Container c = (Container) o;
                return c.timestamp != timestamp(c.resource);
            } else {
                Enumeration e = ((MultiContainer) o).elements();
                while (e.hasMoreElements()) {
                    Container c = (Container) e.nextElement();
                    if (c.timestamp != timestamp(c.resource)) return true;
                }
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Invalidates the given context.
     */
    public void invalidate(Object context) {
        if (context != null) this.table.remove(context);
    }

    /**
     * Create a timestamp indicating the last modified time
     * of the given resource.
     */
    private long timestamp(Object resource) {
        long timestamp;
        if (resource instanceof File) {
            timestamp = ((File) resource).lastModified();
        } else if (resource instanceof URL) {
            try {
                timestamp = ((URL) resource).openConnection().getLastModified();
            } catch (IOException e) {
                timestamp = 0;
            }
        } else {
            throw new IllegalArgumentException("Resource not monitorizable.");
        }
        return timestamp;
    }
}