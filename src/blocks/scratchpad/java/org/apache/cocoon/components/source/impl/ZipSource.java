/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
  * @version CVS $Id: ZipSource.java,v 1.1 2003/12/29 16:24:44 reinhard Exp $
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
