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
 * @version CVS $Id: NonStaticClassLoaderManager.java,v 1.2 2004/03/05 13:02:46 bdelacretaz Exp $
 */
public class NonStaticClassLoaderManager implements ClassLoaderManager, ThreadSafe
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
