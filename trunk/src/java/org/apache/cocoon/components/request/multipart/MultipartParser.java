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
package org.apache.cocoon.components.request.multipart;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class is used to implement a multipart request wrapper.
 * It will parse the http post stream and and fill it's hashtable with values.
 *
 * The hashtable will contain:
 * Vector: inline part values
 * FilePart: file part
 *
 * @author <a href="mailto:j.tervoorde@home.nl">Jeroen ter Voorde</a>
 * @version CVS $Id: MultipartParser.java,v 1.1 2003/03/09 00:09:10 pier Exp $
 */
public class MultipartParser extends Hashtable {

    /** Field FILE_BUFFER_SIZE           */
    private final static int FILE_BUFFER_SIZE = 4096;

    /** Field INLINE_BUFFER_SIZE           */
    private final static int INLINE_BUFFER_SIZE = 256;

    /** Field MAX_BOUNDARY_SIZE           */
    private static final int MAX_BOUNDARY_SIZE = 128;

    /** Field saveUploadedFilesToDisk           */
    private boolean saveUploadedFilesToDisk;

    /** Field uploadDirectory           */
    private File uploadDirectory = null;

    /** Field allowOverwrite           */
    private boolean allowOverwrite;

    /** Field silentlyRename           */
    private boolean silentlyRename;

    /** Field characterEncoding       */
    private String characterEncoding;
    /**
     * Constructor, parses given request
     *
     * @param request The servlet request.
     * @param saveUploadedFilesToDisk Write fileparts to the uploadDirectory. If true the corresponding object
     *              in the hashtable will contain a FilePartFile, if false a FilePartArray
     * @param uploadDirectory The directory to write to if saveUploadedFilesToDisk is true.
     * @param saveUploadedFilesToDisk
     * @param uploadDirectory
     * @param allowOverwrite Allow existing files to be overwritten.
     * @param silentlyRename If file exists rename file (using filename+number).
     * @param maxUploadSize The maximum content length accepted.
     *
     * @throws IOException
     * @throws MultipartException
     */
    public MultipartParser(
            HttpServletRequest request, boolean saveUploadedFilesToDisk, File uploadDirectory, boolean allowOverwrite, boolean silentlyRename, int maxUploadSize)
            throws IOException, MultipartException {

        this.saveUploadedFilesToDisk = saveUploadedFilesToDisk;
        this.uploadDirectory = uploadDirectory;
        this.allowOverwrite = allowOverwrite;
        this.silentlyRename = silentlyRename;
        this.characterEncoding = request.getCharacterEncoding();
        if (this.characterEncoding == null) {
            this.characterEncoding = "ISO-8859-1";
        }

        if (request.getContentLength() > maxUploadSize) {
            throw new IOException("Content length exceeds maximum upload size");
        }

        parseMultiPart(
                new TokenStream(
                        new PushbackInputStream(
                                new BufferedInputStream(request.getInputStream()),
                                MAX_BOUNDARY_SIZE)), getBoundary(request.getContentType()));
    }

    /**
     * Parse a multipart block
     *
     * @param ts
     * @param boundary
     *
     * @throws IOException
     * @throws MultipartException
     */
    private void parseMultiPart(TokenStream ts, String boundary)
            throws IOException, MultipartException {

        ts.setBoundary(boundary.getBytes());
        ts.read();    // read first boundary away
        ts.setBoundary(("\r\n" + boundary).getBytes());

        while (ts.getState() == TokenStream.STATE_NEXTPART) {
            ts.nextPart();
            parsePart(ts);
        }

        if (ts.getState() != TokenStream.STATE_ENDMULTIPART) {    // sanity check
            throw new MultipartException("Malformed stream");
        }
    }

    /**
     * Parse a single part
     *
     * @param ts
     *
     * @throws IOException
     * @throws MultipartException
     */
    private void parsePart(TokenStream ts)
            throws IOException, MultipartException {

        Hashtable headers = new Hashtable();
        headers = readHeaders(ts);
        try {
            if (headers.containsKey("filename")) {
		        if (!"".equals(headers.get("filename"))) {
                	parseFilePart(ts, headers);
		        } else {
        			// IE6 sends an empty part with filename="" for
        			// empty upload fields. Just parse away the part
        			byte[] buf = new byte[32];
        			while(ts.getState() == TokenStream.STATE_READING)
        				ts.read(buf);  
        		}
            } else if (((String) headers.get("content-disposition"))
                    .toLowerCase().equals("form-data")) {
                parseInlinePart(ts, headers);
            }

            // FIXME: multipart/mixed parts are untested.
            else if (((String) headers.get("content-disposition")).toLowerCase()
                    .indexOf("multipart") > -1) {
                parseMultiPart(new TokenStream(ts, MAX_BOUNDARY_SIZE),
                        "--" + (String) headers.get("boundary"));
                ts.read();    // read past boundary
            } else {
                throw new MultipartException("Unknown part type");
            }
        } catch (IOException e) {
            throw new MultipartException("Malformed stream: " + e.getMessage());
        } catch (NullPointerException e) {
            throw new MultipartException("Malformed header");
        }
    }

    /**
     * Parse a file part
     *
     * @param in
     * @param headers
     *
     * @throws IOException
     * @throws MultipartException
     */
    private void parseFilePart(TokenStream in, Hashtable headers)
            throws IOException, MultipartException {

        byte[] buf = new byte[FILE_BUFFER_SIZE];
        OutputStream out = null;
        File file = null;

        if (!saveUploadedFilesToDisk) {
            out = new ByteArrayOutputStream();
        } else {
            String fileName = (String) headers.get("filename");
            if(File.separatorChar == '\\')
                fileName = fileName.replace('/','\\');
            else
                fileName = fileName.replace('\\','/');

            String filePath = uploadDirectory.getPath() + File.separator;
            fileName = new File(fileName).getName();
            file = new File(filePath + fileName);

            if (file.exists()) {
                if (!allowOverwrite) {
                    if (silentlyRename) {
                        int c = 0;

                        do {
                            file = new File(filePath + c++ + "_" + fileName);
                        } while (file.exists());
                    } else {
                        throw new MultipartException("Duplicate file "
                                + file.getName() + ".");
                    }
                }
            }

            out = new FileOutputStream(file);
        }

        int read = 0;
        while (in.getState() == TokenStream.STATE_READING) {    // read data
            read = in.read(buf);
            out.write(buf, 0, read);
        }

        out.close();
        if (file == null) {
            byte[] bytes = ((ByteArrayOutputStream) out).toByteArray();

            put(headers.get("name"),
                    new FilePartArray(headers, new ByteArrayInputStream(bytes),bytes.length));
        } else {
            put(headers.get("name"), new FilePartFile(headers, file));
        }
    }

    /**
     * Parse an inline part
     *
     * @param in
     * @param headers
     *
     * @throws IOException
     */
    private void parseInlinePart(TokenStream in, Hashtable headers)
            throws IOException {

        byte[] buf = new byte[INLINE_BUFFER_SIZE];
        StringBuffer value = new StringBuffer();

        while (in.getState() == TokenStream.STATE_READING) {
            int read = in.read(buf);
            value.append(new String(buf, 0, read, this.characterEncoding));
        }

        String field = (String) headers.get("name");
        Vector v = (Vector) get(field);

        if (v == null) {
            v = new Vector();
            put(field, v);
        }

        v.add(value.toString());
    }

    /**
     * Read part headers
     *
     * @param in
     *
     * @return
     *
     * @throws IOException
     */
    private Hashtable readHeaders(TokenStream in) throws IOException {

        Hashtable headers = new Hashtable();
        String hdrline = readln(in);

        while (!"".equals(hdrline)) {
            StringTokenizer tokenizer = new StringTokenizer(hdrline);

            headers.put(tokenizer.nextToken(" :").toLowerCase(),
                    tokenizer.nextToken(" :;"));

	        // The extra tokenizer.hasMoreTokens() in headers.put
	        // handles the filename="" case IE6 submits for an empty
	        // upload field.
            while (tokenizer.hasMoreTokens()) {
                headers.put(tokenizer.nextToken(" ;=\""),
                        tokenizer.hasMoreTokens()?tokenizer.nextToken("=\""):"");
            }

            hdrline = readln(in);
        }

        return headers;
    }

    /**
     * Get boundary from contentheader
     *
     * @param hdr
     *
     * @return
     */
    private String getBoundary(String hdr) {

        int start = hdr.toLowerCase().indexOf("boundary=");
        if (start > -1) {
            return "--" + hdr.substring(start + 9);
        } else {
            return null;
        }
    }

    /**
     * Read string until newline or end of stream
     *
     * @param in
     *
     * @return
     *
     * @throws IOException
     */
    private String readln(TokenStream in) throws IOException {

        StringBuffer out = new StringBuffer();
        int b = in.read();

        while ((b != -1) && (b != '\r')) {
            out.append((char) b);
            b = in.read();
        }

        if (b == '\r') {
            in.read();    // read '\n'
        }

        return out.toString();
    }
}
