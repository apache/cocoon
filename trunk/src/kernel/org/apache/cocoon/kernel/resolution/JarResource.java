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
package org.apache.cocoon.kernel.resolution;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>A {@link JarResource} is the implementation of the {@link Resource}
 * interface for {@link JarEntry} resources in a {@link JarFile}.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.4 $)
 */
public class JarResource implements Resource {

    /** <p>Our {@link JarFile} where the resource is contained.</p> */
    private JarFile file = null;

    /** <p>The {@link JarEntry} identifying the resource entry.</p> */
    private JarEntry entry = null;

    /** <p>The {@link URL} of the jar file.</p> */
    private URL url = null;

    /**
     * <p>Create a new {@link JarResource} instance associated with a
     * {@link JarEntry} found in a {@link JarFile}.</p>
     *
     * @param file the {@link JarFile} containing the resource.
     * @param entry the {@link JarEntry} identifying the resource in the jar.
     * @param url the {@link URL} associated with the jar, if any.
     */
    protected JarResource(JarFile file, JarEntry entry, URL url) {
        this.file = file;
        this.entry = entry;
        this.url = url;
    }

    /**
     * <p>Checks whether this {@link Resource} is a container for other
     * resources.</p>
     *
     * <p>For this method to return <b>true</b>, the 
     * {@link JarEntry#isDirectory()} method of the associated entry must
     * also return <b>true</b>.</p>
     *
     * @return <b>true</b> if the {@link #children()} method can return a
     *         <b>non null</b> array, <b>false</b> if the {@link #open()}
     *         method can return a <b>non null</b> {@link InputStream}.
     */
    public boolean isDirectory() {
        return(this.entry.isDirectory());
    }

    /**
     * <p>Return an array of all child {@link Resource}s of this instance.</p>
     *
     * @return <b>null</b> if {@link #isDirectory()} returns <b>false</b>, a
     *         {@link Resource}s array otherwise.
     */
    public Resource[] children() {
        if (!this.isDirectory()) return(null);
        ArrayList list = new ArrayList();
        Enumeration entries = this.file.entries();
        String prefix = this.entry.getName();
        while (entries.hasMoreElements()) {
            JarEntry cur = (JarEntry) entries.nextElement();
            if (!cur.getName().startsWith(prefix)) continue;
            list.add(new JarResource(this.file, cur, this.url));
        }
        return((Resource[])list.toArray(new Resource[list.size()]));
    }

    /**
     * <p>Return an {@link InputStream} to read from this {@link Resource}.</p>
     *
     * @return <b>null</b> if {@link #isDirectory()} returns <b>true</b>, an
     *         {@link InputStream} instance otherwise.
     * @throws IOException if an I/O error occurred opening the stream.
     */
    public InputStream open()
    throws IOException {
        if (this.isDirectory()) return(null);
        return(this.file.getInputStream(this.entry));
    }

    /**
     * <p>Convert this {@link Resource} into a {@link URL}.</p>
     *
     * @return a <b>non null</b> {@link URL}.
     * @throws MalformedURLException if this {@link Resource} could not be
     *                               represented as a {@link URL}.
     */
    public URL toURL()
    throws MalformedURLException {
        if (this.url == null) throw new MalformedURLException("No jar URL");
        return(new URL("jar:" + this.url + "!" + this.entry.getName()));
    }
}
