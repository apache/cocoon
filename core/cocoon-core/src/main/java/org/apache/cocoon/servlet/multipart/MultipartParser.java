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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.cocoon.util.NullOutputStream;

/**
 * This class is used to implement a multipart request wrapper.
 * It will parse the http post stream and and fill it's hashtable with values.
 *
 * The hashtable will contain:
 * Vector: inline part values
 * FilePart: file part
 *
 * @version $Id$
 */
public class MultipartParser {

    public static final String UPLOAD_STATUS_SESSION_ATTR = "org.apache.cocoon.servlet.multipartparser.status";

    private final static int FILE_BUFFER_SIZE = 4096;

    private static final int MAX_BOUNDARY_SIZE = 128;

    private boolean saveUploadedFilesToDisk;

    private File uploadDirectory = null;

    private boolean allowOverwrite;

    private boolean silentlyRename;

    private int maxUploadSize;

    private String characterEncoding;

    private Hashtable parts;

    private boolean oversized = false;

    private int contentLength;

    private HttpSession session;

    private boolean hasSession;

    private Hashtable uploadStatus;

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

    private void parseParts(int contentLength, String contentType, InputStream requestStream)
    throws IOException, MultipartException {
        this.contentLength = contentLength;
        if (contentLength > this.maxUploadSize) {
            this.oversized = true;
        }

        BufferedInputStream bufferedStream = new BufferedInputStream(requestStream);
        PushbackInputStream pushbackStream = new PushbackInputStream(bufferedStream, MAX_BOUNDARY_SIZE);
        TokenStream stream = new TokenStream(pushbackStream);

        parseMultiPart(stream, getBoundary(contentType));
    }

    public Hashtable getParts(int contentLength, String contentType, InputStream requestStream)
    throws IOException, MultipartException {
        this.parts = new Hashtable();
        parseParts(contentLength, contentType, requestStream);
        return this.parts;
    }

    public Hashtable getParts(HttpServletRequest request) throws IOException, MultipartException {
        this.parts = new Hashtable();

        // Copy all parameters coming from the request URI to the parts table.
        // This happens when a form's action attribute has some parameters
        Enumeration names = request.getParameterNames();
        while(names.hasMoreElements()) {
            String name = (String)names.nextElement();
            String[] values = request.getParameterValues(name);
            Vector v = new Vector(values.length);
            for (int i = 0; i < values.length; i++) {
                v.add(values[i]);
            }
            this.parts.put(name, v);
        }

        // upload progress bar support
        this.session = request.getSession();
        this.hasSession = this.session != null;
        if (this.hasSession) {
            this.uploadStatus = new Hashtable();
            this.uploadStatus.put("started", Boolean.FALSE);
            this.uploadStatus.put("finished", Boolean.FALSE);
            this.uploadStatus.put("sent", new Integer(0));
            this.uploadStatus.put("total", new Integer(request.getContentLength()));
            this.uploadStatus.put("filename", "");
            this.uploadStatus.put("error", Boolean.FALSE);
            this.uploadStatus.put("uploadsdone", new Integer(0));
            this.session.setAttribute(UPLOAD_STATUS_SESSION_ATTR, this.uploadStatus);
        }

        parseParts(request.getContentLength(), request.getContentType(), request.getInputStream());

        if (this.hasSession) {
            this.uploadStatus.put("finished", Boolean.TRUE);
        }

        return this.parts;
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
        OutputStream out;
        File file = null;

        if (oversized) {
            out = new NullOutputStream();
        } else if (!saveUploadedFilesToDisk) {
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

            if (!allowOverwrite && !file.createNewFile()) {
                if (silentlyRename) {
                    int c = 0;
                    do {
                        file = new File(filePath + c++ + "_" + fileName);
                    } while (!file.createNewFile());
                } else {
                    throw new MultipartException("Duplicate file '" + file.getName()
                        + "' in '" + file.getParent() + "'");
                }
            }

            out = new FileOutputStream(file);
        }

        if (hasSession) { // upload widget support
            this.uploadStatus.put("finished", Boolean.FALSE);
            this.uploadStatus.put("started", Boolean.TRUE);
            this.uploadStatus.put("widget", headers.get("name"));
            this.uploadStatus.put("filename", headers.get("filename"));
        }

        int length = 0; // Track length for OversizedPart
        try {
            int read = 0;
            while (in.getState() == TokenStream.STATE_READING) {
                // read data
                read = in.read(buf);
                length += read;
                out.write(buf, 0, read);

                if (this.hasSession) {
                    this.uploadStatus.put("sent",
                        new Integer(((Integer)this.uploadStatus.get("sent")).intValue() + read)
                    );
                }
            }
            if (this.hasSession) { // upload widget support
                this.uploadStatus.put("uploadsdone",
                    new Integer(((Integer)this.uploadStatus.get("uploadsdone")).intValue() + 1)
                );
                this.uploadStatus.put("error", Boolean.FALSE);
            }
        } catch (IOException ioe) {
            // don't let incomplete file uploads pile up in the upload dir.
            // this usually happens with aborted form submits containing very large files.
            out.close();
            out = null;
            if ( file!=null ) file.delete();
            if (this.hasSession) { // upload widget support
                this.uploadStatus.put("error", Boolean.TRUE);
            }
            throw ioe;
        } finally {
            if ( out!=null ) out.close();
        }

        String field = (String)headers.get("name");
        Vector v = (Vector) this.parts.get(field);

        if (v == null) {
            v = new Vector();
            this.parts.put(field, v);
        }

        if (oversized) {
            v.add(new RejectedPart(headers, length, this.contentLength, this.maxUploadSize));
        } else if (file == null) {
            byte[] bytes = ((ByteArrayOutputStream) out).toByteArray();
            v.add(new PartInMemory(headers, bytes));
        } else {
            v.add(new PartOnDisk(headers, file));
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

        // Buffer incoming bytes for proper string decoding (there can be multibyte chars)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        while (in.getState() == TokenStream.STATE_READING) {
            int c = in.read();
            if (c != -1) bos.write(c);
        }

        String field = (String) headers.get("name");
        Vector v = (Vector) this.parts.get(field);

        if (v == null) {
            v = new Vector();
            this.parts.put(field, v);
        }

        v.add(new String(bos.toByteArray(), this.characterEncoding));
    }

    /**
     * Read part headers
     *
     * @param in
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
     */
    private String getBoundary(String hdr) {

        int start = hdr.toLowerCase().indexOf("boundary=");
        if (start > -1) {
            return "--" + hdr.substring(start + 9);
        }
        return null;
    }

    /**
     * Read string until newline or end of stream
     *
     * @param in
     *
     * @throws IOException
     */
    private String readln(TokenStream in) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int b = in.read();

        while ((b != -1) && (b != '\r')) {
            bos.write(b);
            b = in.read();
        }

        if (b == '\r') {
            in.read();    // read '\n'
        }

        return new String(bos.toByteArray(), this.characterEncoding);
    }

}
