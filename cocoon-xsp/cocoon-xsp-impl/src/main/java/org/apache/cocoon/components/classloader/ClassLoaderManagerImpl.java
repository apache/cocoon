/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A singleton-like implementation of <code>ClassLoaderManager</code>
 *
 * @version $Id$
 */
public class ClassLoaderManagerImpl implements ClassLoaderManager, ThreadSafe, Disposable {

    /**
     * Set of class directories
     */
    protected final Set fileSet = Collections.synchronizedSet(new HashSet());

    /**
     * The class loader instance
     */
    private RepositoryClassLoader instance;

    /**
     * A constructor that ensures only a single class loader instance exists
     */
    public ClassLoaderManagerImpl() {
        reinstantiate();
    }

    public void dispose() {
        this.fileSet.clear();
        reinstantiate();
    }

    /**
     * Add a directory to the proxied class loader
     *
     * @param directoryName The repository name
     * @throws IOException If the directory is invalid
     */
    public void addDirectory(File directoryName) throws IOException {
        if (this.fileSet.add(directoryName)) {
            this.instance.addDirectory(directoryName);
        }
    }

    /**
     * Load a class through the proxied class loader
     *
     * @param className The name of the class to be loaded
     * @return The loaded class
     * @throws ClassNotFoundException If the class is not found
     */
    public Class loadClass(String className) throws ClassNotFoundException {
        return this.instance.loadClass(className);
    }

    /**
     * Reinstantiate the proxied class loader to allow for class reloading
     */
    public void reinstantiate() {
        if (this.fileSet.isEmpty()) {
            this.instance = new RepositoryClassLoader();
        } else {
            this.instance = new RepositoryClassLoader(this.fileSet);
        }
    }
}
