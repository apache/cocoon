/*-- $Id: Manager.java,v 1.10 2000-12-03 19:54:48 greenrd Exp $ --

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

import java.util.*;
import java.io.*;

/**
 * This class is used to create and control software actors and resources.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.10 $ $Date: 2000-12-03 19:54:48 $
 */

public class Manager 
  extends Hashtable implements Actor, Factory, Director, DestroyListener {

    static {
        // Tomcat 3.1 classloader workaround
        try {
            Class.forName ("org.apache.cocoon.framework.DestroyListener");
        }
        catch (ClassNotFoundException ex) {
            ex.printStackTrace ();
        }
    }

    /**
     * Initialize the actor by indicating their director.
     */
    public void init(Director director) {
    }

    /**
     * Create the instance of a class given its name.
     */
    public Object create(String name) {
        return create(name, null);
    }

    /**
     * Create the instance of a class and, if configurable, use
     * the given configurations to configure it.
     */
    public Object create(String name, Configurations conf) throws RuntimeException {
        try {
            Object object = null;
            ClassLoader cl = this.getClass().getClassLoader();

            if (cl != null) {
                object = cl.loadClass(name).newInstance();
            } else {
                object = Class.forName(name).newInstance();
            }

            if (object instanceof Actor) {
                ((Actor) object).init((Director) this);
            }

            if ((object instanceof Configurable) && (conf != null)) {
                ((Configurable) object).init(conf);
            }

            return object;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error creating " + name + ": class is not found");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error creating " + name + ": does not have access");
        } catch (InstantiationException e) {
            throw new RuntimeException("Error creating " + name + ": could not instantiate " + e.getMessage());
        } catch (RuntimeException e) {
            throw e;
        } catch (NoClassDefFoundError e) {
            throw new RuntimeException("Error creating " + name + ": make sure the needed classes can be found in the classpath (" + e.getMessage() + ")");
        } catch (Throwable e) {
            throw new RuntimeException("Factory error:  unknown exception creating \" " + name + "\" : " + e);
        }
    }

    /**
     * Create a vector of instances.
     */
    public Vector create(Vector names) {
        return create(names, null);
    }

    /**
     * Create a vector of instances with given configurations.
     */
     public Vector create(Vector names, Configurations conf) {
         Vector v = new Vector(names.size());
         Enumeration e = names.elements();
         while (e.hasMoreElements()) {
             v.addElement(create((String) e.nextElement(), conf));
         }
         return v;
     }

    public void destroy () {
      destroyAll ();
    }

    /**
     * Calls destroy() on all components that are instances of DestroyListener
     */
    public void destroyAll () {
       Enumeration e = elements ();
       while (e.hasMoreElements ()) {
          Object x = e.nextElement ();
          if (x instanceof DestroyListener && x != this) {
            ((DestroyListener) x).destroy ();
          }
       }
    }

    /**
     * Get the actor currently playing the given role.
     */
    public Object getActor(String role) {
        return this.get(role);
    }

    /**
     * Set the actor for the role.
     */
    public void setRole(String role, Object actor) {
        this.put(role, actor);
    }

    /**
     * Get the roles currently set.
     */
    public Enumeration getRoles() {
        return this.keys();
    }
}
