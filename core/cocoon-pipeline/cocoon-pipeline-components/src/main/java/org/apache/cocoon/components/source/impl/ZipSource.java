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
package org.apache.cocoon.components.source.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;

import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.util.MIMEUtils;

/**
 * @version $Id$
 * @since 2.1.8
 */
public class ZipSource extends AbstractLogEnabled
                       implements Source {

    private String protocol;
    private Source archive;
    private String filePath;

    public ZipSource(String protocol, Source archive, String filePath) {
        this.protocol = protocol;
        this.archive = archive;
        this.filePath = filePath;
    }

    private ZipEntry findEntry(ZipInputStream zipStream)
    throws IOException {
        ZipEntry entry;
        while ((entry = zipStream.getNextEntry()) != null) {
            if (entry.getName().equals(this.filePath)) {
                return entry;
            }
            zipStream.closeEntry();
        }

        return null;
    }

    /* package access */
    void dispose(SourceResolver resolver) {
        resolver.release(this.archive);
        this.archive = null;
    }

    public boolean exists() {
        if(!this.archive.exists()) {
            return false;
        }

        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(this.archive.getInputStream());
            return findEntry(zipStream) != null;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (zipStream != null) {
                    zipStream.close();
                }
            } catch (IOException e) {
                getLogger().error("IOException while closing ZipInputStream: " + this.filePath);
            }
        }
    }

    public InputStream getInputStream() throws IOException {
        ZipInputStream zipStream = new ZipInputStream(this.archive.getInputStream());
        try {
            ZipEntry entry = findEntry(zipStream);
            if (entry == null) {
                throw new SourceNotFoundException("File " + this.filePath + " is not found in the archive " +
                                                  this.archive.getURI());
            }

            // Now we will extract the document and write it into a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int length;
            while (zipStream.available() > 0) {
                length = zipStream.read(buffer, 0, 8192);
                if (length > 0) {
                    baos.write(buffer, 0, length);
                }
            }

            // Return an input stream
            return new ByteArrayInputStream(baos.toByteArray());
        } finally {
            try {
                zipStream.close();
            } catch (IOException e) {
                getLogger().error("IOException while closing ZipInputStream: " + this.filePath);
            }
        }
    }

    public String getURI() {
        return this.protocol + ":" + this.archive.getURI() + "!/" + this.filePath;
    }

    public String getScheme() {
        return this.protocol;
    }

    public SourceValidity getValidity() {
        return this.archive.getValidity();
    }

    public void refresh() {
    }

    public String getMimeType() {
        String ext = this.filePath.substring(this.filePath.lastIndexOf("."));
        return MIMEUtils.getMIMEType(ext);
    }

    public long getContentLength() {
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(this.archive.getInputStream());
            ZipEntry entry = findEntry(zipStream);
            if (entry != null) {
                return entry.getSize();
            }

        } catch (IOException e) {
            // Ignored
        } finally {
            try {
                if (zipStream != null) {
                    zipStream.close();
                }
            } catch (IOException e) {
                getLogger().error("IOException while closing ZipInputStream: " + this.filePath);
            }
        }

        return -1;
    }

    public long getLastModified() {
        return this.archive.getLastModified();
    }
    
}
