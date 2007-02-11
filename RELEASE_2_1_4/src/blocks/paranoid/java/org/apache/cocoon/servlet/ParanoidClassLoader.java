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
package org.apache.cocoon.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * The <code>ParanoidClassLoader</code> reverses the search order for
 * classes.  It checks this classloader before it checks its parent.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: ParanoidClassLoader.java,v 1.1 2003/12/11 18:22:14 sylvain Exp $
 */

public class ParanoidClassLoader extends URLClassLoader {

    /**
     * Default constructor has no parents or initial <code>URL</code>s.
     */
    public ParanoidClassLoader() {
        this(null, null, null);
    }

    /**
     * Alternate constructor to define a parent.
     */
    public ParanoidClassLoader(final ClassLoader parent) {
        this(new URL[0], parent, null);
    }

    /**
     * Alternate constructor to define initial <code>URL</code>s.
     */
    public ParanoidClassLoader(final URL[] urls) {
        this(urls, null, null);
    }

    /**
     * Alternate constructor to define a parent and initial
     * <code>URL</code>s.
     */
    public ParanoidClassLoader(final URL[] urls, final ClassLoader parent) {
        this(urls, parent, null);
    }

    /**
     * Alternate constructor to define a parent, initial
     * <code>URL</code>s, and a default
     * <code>URLStreamHandlerFactory</code>.
     */
    public ParanoidClassLoader(final URL[] urls, final ClassLoader parent, final URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    /**
     * Extends <code>URLClassLoader</code>'s initialization methods so
     * we return a <code>ParanoidClassLoad</code> instead.
     */
    public static final URLClassLoader newInstance(final URL[] urls) {
        return new ParanoidClassLoader(urls);
    }

    /**
     * Extends <code>URLClassLoader</code>'s initialization methods so
     * we return a <code>ParanoidClassLoad</code> instead.
     */
    public static final URLClassLoader newInstance(final URL[] urls, final ClassLoader parent) {
        return new ParanoidClassLoader(urls, parent);
    }

    /**
     * Loads the class from this <code>ClassLoader</class>.  If the
     * class does not exist in this one, we check the parent.  Please
     * note that this is the exact opposite of the
     * <code>ClassLoader</code> spec.  We use it to work around
     * inconsistent class loaders from third party vendors.
     *
     * @param     name the name of the class
     * @param     resolve if <code>true</code> then resolve the class
     * @return    the resulting <code>Class</code> object
     * @exception ClassNotFoundException if the class could not be found
     */
    public final Class loadClass(String name, boolean resolve)
      throws ClassNotFoundException
    {
        // First check if it's already loaded
        Class clazz = findLoadedClass(name);
        
        if (clazz == null) {
            
            try {
                clazz = findClass(name);
                //System.err.println("Paranoid load : " + name);
            } catch (ClassNotFoundException cnfe) {
                ClassLoader parent = getParent();
                if (parent != null) {
                     // Ask to parent ClassLoader (can also throw a CNFE).
                    clazz = parent.loadClass(name);
                } else {
                    // Propagate exception
                    throw cnfe;
                }
            }
        }
        
        if (resolve) {
            resolveClass(clazz);
        }
        
        return clazz;
    }
    
    /**
     * Gets a resource from this <code>ClassLoader</class>.  If the
     * resource does not exist in this one, we check the parent.
     * Please note that this is the exact opposite of the
     * <code>ClassLoader</code> spec.  We use it to work around
     * inconsistent class loaders from third party vendors.
     *
     * @param name of resource
     */
    public final URL getResource(final String name) {

        URL resource = findResource(name);
        ClassLoader parent = this.getParent();
        if (resource == null && parent != null) {
            resource = parent.getResource(name);
        }

        return resource;
    }

    /**
     * Adds a new directory of class files.
     *
     * @param file for jar or directory
     * @throws IOException
     */
    public final void addDirectory(File file) throws IOException {
        this.addURL(file.getCanonicalFile().toURL());
    }
    
    /**
     * Adds a new URL
     */
    
    public void addURL(URL url) {
    	super.addURL(url);
    }
}
