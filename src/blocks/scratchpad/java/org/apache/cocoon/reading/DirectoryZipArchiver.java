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
package org.apache.cocoon.reading;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
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
 * @version CVS $Id: DirectoryZipArchiver.java,v 1.3 2004/03/05 10:07:26 bdelacretaz Exp $
 */
public class DirectoryZipArchiver extends AbstractReader {

    private Source inputSource;

    private File directory;

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

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
