/* ========================================================================== *
 *                                                                            *
 * Copyright 2004 The Apache Software Foundation.                             *
 *                                                                            *
 * Licensed  under the Apache License,  Version 2.0 (the "License");  you may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at                                                     *
 *                                                                            *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                            *
 * Unless  required  by  applicable law or  agreed  to in  writing,  software *
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.           *
 *                                                                            *
 * See  the  License for  the  specific language  governing  permissions  and *
 * limitations under the License.                                             *
 *                                                                            *
 * ========================================================================== */
package org.apache.cocoon.kernel.resolution;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/**
 * <p>A {@link LocalResolver} is an implementation of the {@link Resolver}
 * interface for resources accessible from files.</p>
 *
 * <p>This class supports two kinds of files: directories and regular files.
 * If the file is not a directory, it must (then) be a valid JAR archive.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class LocalResolver implements Resolver {

    /** <p>Our list of {@link File} sources.</p> */
    private List sources = new ArrayList();

    /** <p>A {@link Map} associating {@link File}s with {@link JarFile}s.</p> */
    private Map jars = new HashMap();

    /**
     * <p>Create a new {@link LocalResolver} instance accessing resources from
     * an array of specified {@link URL}s.</p>
     *
     * <p>All {@link URL}s specified in the array must be accessible using the
     * <code>file:</code> protocol.</p>
     *
     * @see #add(File)
     * @param urls an array of file {@link URL}s.
     * @throws IOException if one of the {@link URL}s cannot be accessed from a
     *                     {@link File}, or if an I/O error occurred.
     */
    public LocalResolver(URL urls[])
    throws IOException {
        /* Ignore if the list is null */
        if (urls == null) return;

        /* Process each url ensuring it's a file or directory */
        for (int x = 0; x < urls.length; x++) {
            /* Ignore if one of the urls is null */
            if (urls[x] == null) continue;

            /* Throw a security exception if the protocol is not file */
            if (! "file".equals(urls[x].getProtocol())) {
                throw new IOException("Unsupported URL \"" + urls[x] + "\"");
            }

            /* Locate the file on disk and add it */
            this.add(new File(urls[x].getPath()).getCanonicalFile());
        }
    }

    /**
     * <p>Add a new {@link File} to the list of sources managed by this
     * {@link LocalResolver}.</p>
     *
     * <p>If the specified {@link File} is a directory, it will be used as one
     * of the roots for resources resolution, if it represents a regular file,
     * it <b>must</b> be a valid JAR archive.</p>
     *
     * @param file the {@link File} to add to the list of our sources.
     * @throws IOException if one of the {@link File} is not a directory and is
     *                     not a valid JAR archive, or if an I/O error occurred.
     */
    protected void add(File file)
    throws IOException {
        if (file == null) throw new NullPointerException();
        if (this.sources.contains(file)) return;
        
        if (file.isDirectory()) {
            this.sources.add(file);
        } else if (file.isFile()) {
            JarFile jar = new JarFile(file);
            this.sources.add(file);
            this.jars.put(file, jar);
        } else {
            throw new IOException("Unable to access file \"" + file + "\"");
        }
    }
    
    /**
     * <p>Resolve a specified name into a {@link Resource}.</p>
     *
     * @param name a non null {@link String} identifying the resource name.
     * @return a {@link Resource} instance or <b>null</b> if not found.
     */
    public Resource resolve(String name) {
        Iterator iterator = this.sources.iterator();
        while (iterator.hasNext()) try {
            File cur = (File)iterator.next();
            JarFile jar = (JarFile)this.jars.get(cur);
            if (jar != null) {
                JarEntry ent = jar.getJarEntry(name);
                if (ent != null) return(new JarResource(jar, ent, cur.toURL()));
            } else {
                cur = new File(cur, name).getCanonicalFile();
                if (cur.exists()) return(new FileResource(cur));
            }
        } catch (IOException e) {
            /* Swallow any exception and continue with the next source */
        }
        return(null);
    }

    /**
     * <p>Return a list of all {@link File} instances used as sources by this
     * {@link LocalResolver}.</p>
     *
     * @return a <b>non null</b> array of {@link File} instances.
     */
    public File[] files() {
        return((File[])this.sources.toArray(new File[this.sources.size()]));
    }

    /**
     * <p>Return a list of all {@link File} instances used as sources by this
     * {@link LocalResolver} as an array of {@link URL}s.</p>
     *
     * @return a <b>non null</b> array of {@link URL} instances.
     * @throws MalformedURLException if one of the source files or directories
     *                               could not be converted to a {@link URL}.
     */
    public URL[] urls()
    throws MalformedURLException {
        File files[] = this.files();
        URL urls[] = new URL[files.length];
        for (int x = 0; x < files.length; x++) urls[x] = files[x].toURL();
        return(urls);
    }
}