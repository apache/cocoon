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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * <p>A {@link FileResource} is the implementation of the {@link Resource}
 * interface for resources accessible via {@link File}s.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class FileResource implements Resource {

    /* <p>Our root directory.</p> */
    private File file = null;

    /**
     * <p>Create a new {@link FileResource} associated with a {@link File}
     * resource.</p>
     *
     * @param file the {@link File} instance of the resource.
     */
    protected FileResource(File file) {
        if (file == null) throw new NullPointerException();
        this.file = file;
    }

    /**
     * <p>Checks whether this {@link Resource} is a container for other
     * resources.</p>
     *
     * <p>For this method to return <b>true</b>, the 
     * {@link File#isDirectory()} method of the associated file must
     * also return <b>true</b>.</p>
     *
     * @return <b>true</b> if the {@link #children()} method can return a
     *         <b>non null</b> array, <b>false</b> if the {@link #open()}
     *         method can return a <b>non null</b> {@link InputStream}.
     */
    public boolean isDirectory() {
        return(this.file.isDirectory());
    }

    /**
     * <p>Return an array of all child {@link Resource}s of this instance.</p>
     *
     * @return <b>null</b> if {@link #isDirectory()} returns <b>false</b>, a
     *         {@link Resource}s array otherwise.
     */
    public Resource[] children() {
        if (this.isDirectory()) return(null);
        File list[] = this.file.listFiles();
        if (list == null) return(new Resource[0]);
        FileResource resources[] = new FileResource[list.length];
        for (int x = 0; x < list.length; x++) {
            resources[x] = new FileResource(list[x]);
        }
        return(resources);
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
        return(new FileInputStream(this.file));
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
        return(this.file.toURL());
    }


}