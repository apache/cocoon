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
package org.apache.cocoon.servlet.multipart;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is used to implement a multipart request wrapper.
 * It will parse the http post stream and and fill it's hashtable with values.
 *
 * The hashtable will contain:
 * Vector: inline part values
 * FilePart: file part
 *
 * @author <a href="mailto:j.tervoorde@home.nl">Jeroen ter Voorde</a>
 * @version CVS $Id: MultipartParser.java,v 1.5 2004/03/08 14:03:30 cziegeler Exp $
 */
public class MultipartParser {

    private final static int FILE_BUFFER_SIZE = 4096;

    private final static int INLINE_BUFFER_SIZE = 256;

    private static final int MAX_BOUNDARY_SIZE = 128;

    private boolean saveUploadedFilesToDisk;

    private File uploadDirectory = null;

    private boolean allowOverwrite;

    private boolean silentlyRename;
    
    private int maxUploadSize;

    private String characterEncoding;
    
    private Hashtable parts;
    
    /**
     * Constructor, parses given request
     *
     * @param saveUploadedFilesToDisk Write fileparts to the uploadDirectory. If true the corresponding object
     *              in the hashtable will contain a FilePartFile, if false a FilePartArray
     * @param uploadDirectory The directory to write to if saveUploadedFilesToDisk is true.
     * @param allowOverwrite Allow existing files to be overwritten.
     * @param silentlyRename If file exists rename file (using filename+number).
     * @param maxUploadSize The maximum content length accepted.
     * @param characterEncoding The character encoding to be used.
     */
    public MultipartParser(boolean saveUploadedFilesToDisk,
                           File uploadDirectory,
                           boolean allowOverwrite,
                           boolean silentlyRename,
                           int maxUploadSize,
                           String characterEncoding)
    {
        this.saveUploadedFilesToDisk = saveUploadedFilesToDisk;
        this.uploadDirectory = uploadDirectory;
        this.allowOverwrite = allowOverwrite;
        this.silentlyRename = silentlyRename;
        this.maxUploadSize = maxUploadSize;
        this.characterEncoding = characterEncoding;
    }

    public Hashtable getParts(int contentLength, String contentType, InputStream requestStream)
    throws IOException, MultipartException {
        if (contentLength > this.maxUploadSize) {
            throw new IOException("Content length exceeds maximum upload size");
        }

        this.parts = new Hashtable();

        BufferedInputStream bufferedStream = new BufferedInputStream(requestStream);
        PushbackInputStream pushbackStream = new PushbackInputStream(bufferedStream, MAX_BOUNDARY_SIZE);
        TokenStream stream = new TokenStream(pushbackStream);

        parseMultiPart(stream, getBoundary(contentType));

        return this.parts;    
    }
    
    public Hashtable getParts(HttpServletRequest request) throws IOException, MultipartException {
        return getParts(request.getContentLength(), request.getContentType(), request.getInputStream());    
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
            e.printStackTrace();
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

            this.parts.put(headers.get("name"),
                    new PartInMemory(headers, new ByteArrayInputStream(bytes),bytes.length));
        } else {
            this.parts.put(headers.get("name"), new PartOnDisk(headers, file));
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
        Vector v = (Vector) this.parts.get(field);

        if (v == null) {
            v = new Vector();
            this.parts.put(field, v);
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
