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
package org.apache.cocoon.servlet.multipart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * This class represents a file part parsed from a http post stream.
 *
 * @version $Id$
 */
public class PartOnDisk extends Part {

    /** Field file */
    private File file = null;
    private int size;

    /**
     * Constructor PartOnDisk
     *
     * @param headers
     * @param file
     */
    public PartOnDisk(Map headers, File file) {
        super(headers);
        this.file = file;

        // Ensure the file will be deleted when we exit the JVM
        this.file.deleteOnExit();

        this.size = (int) file.length();
    }

    /**
     * Returns the file name
     */
    public String getFileName() {
        return file.getName();
    }

    /**
     * Returns the file size in bytes
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Returns the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns a (ByteArray)InputStream containing the file data
     *
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        if (this.file != null) {
            return new FileInputStream(file);
        }
        throw new IllegalStateException("This part has already been disposed.");
    }

    /**
     * Returns the filename
     */
    public String toString() {
        return file.getPath();
    }

    /**
     * Delete the underlying file.
     */
    public void dispose() {
        if (this.file != null) {
            this.file.delete();
            this.file = null;
        }
    }

    /**
     * Ensures the underlying file has been deleted
     */
    public void finalize() throws Throwable {
        // Ensure the file has been deleted
        dispose();
        super.finalize();
    }
}
