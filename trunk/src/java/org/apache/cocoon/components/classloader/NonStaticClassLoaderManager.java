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
package org.apache.cocoon.components.classloader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * A singleton-like implementation of <code>ClassLoaderManager</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:proyal@apache.org">Peter Royal</a>
 * @version CVS $Id: NonStaticClassLoaderManager.java,v 1.2 2003/12/29 13:27:36 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=ClassLoaderManager
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=non-static-classloader
 */
public class NonStaticClassLoaderManager implements ClassLoaderManager
{
    /**
     * The single class loader instance
     */
    private RepositoryClassLoader instance = new RepositoryClassLoader();

    private Set fileSet = Collections.synchronizedSet( new HashSet() );

    /**
     * Add a directory to the proxied class loader
     *
     * @param directoryName The repository name
     * @exception IOException If the directory is invalid
     */
    public void addDirectory( File directoryName ) throws IOException
    {
        if( !this.fileSet.contains( directoryName ) )
        {
            this.fileSet.add( directoryName );
            this.instance.addDirectory( directoryName );
        }
    }

    /**
     * Load a class through the proxied class loader
     *
     * @param className The name of the class to be loaded
     * @return The loaded class
     * @exception ClassNotFoundException If the class is not found
     */
    public Class loadClass( String className ) throws ClassNotFoundException
    {
        return this.instance.loadClass( className );
    }

    /**
     * Reinstantiate the proxied class loader to allow for class reloading
     *
     */
    public void reinstantiate()
    {
        if( this.fileSet.isEmpty() )
        {
            this.instance = new RepositoryClassLoader();
        }
        else
        {
            this.instance = new RepositoryClassLoader( new Vector( this.fileSet ) );
        }
    }
}
