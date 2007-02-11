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
package org.apache.cocoon.reading;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The <code>DirectoryZipArchiver</code> component creates a compressed zip
 * archive of the files contained in the directory passed with 'src'.
 *
 * NOTE (SM): no content-length information is passed to the user since we
 * can't estimate it before actually performing the compression.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: DirectoryZipArchiver.java,v 1.1 2003/09/04 12:42:44 cziegeler Exp $
 */
public class DirectoryZipArchiver extends AbstractReader {

    private Source inputSource;
    private Response response;
    private Request request;

    private File directory;

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        this.request = ObjectModelHelper.getRequest(objectModel);
        this.response = ObjectModelHelper.getResponse(objectModel);

        try {
            this.inputSource = this.resolver.resolveURI(super.source);
        } catch (SourceException se) {
            throw new ProcessingException("Could not retrieve source '"+super.source+"'", se);
        }

        String systemId = inputSource.getURI();
        if (!systemId.startsWith("file:")) {
          throw new ResourceNotFoundException(systemId + " does not denote a directory");
        }

        // This relies on systemId being of the form "file://..."
        this.directory = new File(new URL(systemId).getFile());
        if (!directory.isDirectory()) {
            throw new ResourceNotFoundException(directory + " is not a directory.");
        }
    }

    /**
     * Generates the requested resource.
     */
    public void generate() throws IOException, ProcessingException {

        File[] files = this.directory.listFiles();

        ZipOutputStream zip = new ZipOutputStream(out);
        zip.setLevel(0); //FIXME (sm) this should be a configurable parameter

        for (int i = 0; i < files.length; i++) {
            if (!files[i].isDirectory()) {
                ZipEntry entry = new ZipEntry(files[i].getName());
                zip.putNextEntry(entry);
                read(files[i],zip);
                zip.closeEntry();
            }
        }

        zip.finish();
        zip.flush();
    }

    /**
     * Returns the mime-type of the resource in process.
     */
    public String getMimeType() {
        return "application/zip";
    }

    /**
     * Reads the given file in the given output stream.
     */
    protected void read(File file, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(file);
        byte[] buffer = new byte[8192];
        int length = -1;
        while ((length = in.read(buffer)) > -1) {
            out.write(buffer, 0, length);
        }
        in.close();
    }
}
