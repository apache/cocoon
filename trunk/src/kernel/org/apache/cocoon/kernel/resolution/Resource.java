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

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * <p>The {@link Resource} interface defines an abstract readable resource.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.3 $)
 */
public interface Resource {

    /**
     * <p>Checks whether this {@link Resource} is a container for other
     * resources.</p>
     *
     * @return <b>true</b> if the {@link #children()} method can return a
     *         <b>non null</b> array, <b>false</b> if the {@link #open()}
     *         method can return a <b>non null</b> {@link InputStream}.
     */
    public boolean isDirectory();

    
    /**
     * <p>Return an array of all child {@link Resource}s of this instance.</p>
     *
     * @return <b>null</b> if {@link #isDirectory()} returns <b>false</b>, a
     *         {@link Resource}s array otherwise.
     */
    public Resource[] children();

    /**
     * <p>Return an {@link InputStream} to read from this {@link Resource}.</p>
     *
     * @return <b>null</b> if {@link #isDirectory()} returns <b>true</b>, an
     *         {@link InputStream} instance otherwise.
     * @throws IOException if an I/O error occurred opening the stream.
     */
    public InputStream open()
    throws IOException;

    /**
     * <p>Convert this {@link Resource} into a {@link URL}.</p>
     *
     * @return a <b>non null</b> {@link URL}.
     * @throws MalformedURLException if this {@link Resource} could not be
     *                               represented as a {@link URL}.
     */
    public URL toURL()
    throws MalformedURLException;
}
