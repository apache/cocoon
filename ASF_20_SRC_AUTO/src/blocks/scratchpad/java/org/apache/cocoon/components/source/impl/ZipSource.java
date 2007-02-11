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
package org.apache.cocoon.components.source.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.util.MIMEUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;

/**
  * @author <a href="http://apache.org/~reinhard">Reinhard Poetz</a>
  * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
  * @version CVS $Id: ZipSource.java,v 1.2 2004/03/05 10:07:25 bdelacretaz Exp $
  * @since 2.1.4 
  */
public class ZipSource extends AbstractLogEnabled implements Source {

    Source archive;
    String documentName;

    public ZipSource(Source archive, String fileName) {
        this.archive = archive;
        this.documentName = fileName;
    }

    public boolean exists() {
        if(!this.archive.exists()) {
            return false;
        }
        ZipInputStream zipStream = null;
        ZipEntry document = null;
        boolean found = false;
        try {
            zipStream = new ZipInputStream(this.archive.getInputStream());
            do {
                document = zipStream.getNextEntry();
                if (document != null) {
                    if (document.getName().equals(this.documentName)) {
                        found = true;
                    } else {
                        zipStream.closeEntry();
                    }
                }
            } while (document != null && found == false);
        } catch(IOException ioe) {
            return false;
        } finally {
            try {
                zipStream.close();
            } catch (IOException ioe) {
                this.getLogger().error("Error while closing ZipInputStream: " + this.documentName);
            }
        } 
        return found;
    }
    
    public InputStream getInputStream()
        throws IOException, SourceNotFoundException {

        ZipInputStream zipStream =
            new ZipInputStream(this.archive.getInputStream());
        ZipEntry document = null;
        boolean found = false;
        do {
            document = zipStream.getNextEntry();
            if (document != null) {
                if (document.getName().equals(this.documentName)) {
                    found = true;
                } else {
                    // go to next entry
                    zipStream.closeEntry();
                }
            }
        } while (document != null && found == false);

        if (document == null) {
            throw new SourceNotFoundException(
                "The document "
                    + documentName
                    + " is not in the archive "
                    + this.archive.getURI());
        }

        // now we will extract the document and write it into a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int length = -1;
        while (zipStream.available() > 0) {
            length = zipStream.read(buffer, 0, 8192);
            if (length > 0) {
                baos.write(buffer, 0, length);
            }
        }
        zipStream.close();
        baos.flush();

        // return an input stream
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public String getURI() {
        return this.archive.getURI() + "/" + this.documentName;
    }

    public String getScheme() {
        return ZipSourceFactory.ZIP_SOURCE_SCHEME;
    }

    public SourceValidity getValidity() {
        return this.archive.getValidity();
    }

    public void refresh() {
    }

    public String getMimeType() {
        String ext = this.documentName.substring( this.documentName.lastIndexOf(".") );
        return MIMEUtils.getMIMEType( ext );
    }

    public long getContentLength() {
        return -1;
    }

    public long getLastModified() {
        return this.archive.getLastModified();
    }

}
