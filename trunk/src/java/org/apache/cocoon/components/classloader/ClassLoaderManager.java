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

/**
 * A class loader manager acting as a proxy for a <b>single</b>
 * <code>RepositoryClassLoader</code>.
 * This class guarantees that a single class loader instance exists so
 * that it can be safely reinstantiated for dynamic class reloading
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: ClassLoaderManager.java,v 1.4 2004/03/08 13:57:37 cziegeler Exp $
 */
public interface ClassLoaderManager {

    String ROLE = ClassLoaderManager.class.getName();

    /**
    * Add a directory to the proxied class loader
    *
    * @param directoryName The repository name
    * @exception IOException If the directory is invalid
    */
    void addDirectory(File directoryName) throws IOException;

    /**
    * Load a class through the proxied class loader
    *
    * @param className The name of the class to be loaded
    * @return The loaded class
    * @exception ClassNotFoundException If the class is not found
    */
    Class loadClass(String className) throws ClassNotFoundException;

    /**
    * Reinstantiate the proxied class loader to allow for class reloading
    *
    */
    void reinstantiate();
}
