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
package org.apache.butterfly.reading;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.butterfly.components.pipeline.PipelineProcessingException;
import org.apache.butterfly.environment.ObjectModelHelper;
import org.apache.butterfly.environment.Request;
import org.apache.butterfly.environment.Response;
import org.apache.butterfly.environment.http.HttpResponse;
import org.apache.butterfly.source.Source;
import org.apache.butterfly.source.SourceResolver;
import org.apache.butterfly.util.ByteRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Description of ResourceReader.
 * 
 * @version CVS $Id$
 */
public class ResourceReader implements Reader {
    protected static final Log logger = LogFactory.getLog(ResourceReader.class);
    private SourceResolver sourceResolver;
    protected String mimeType;
    protected long expires = -1;
    protected int bufferSize = 8192;
    protected boolean byteRanges = true;
    private Source inputSource;
    private OutputStream out;
    protected Response response;
    protected Request request;
    private Map objectModel;

    /**
     * 
     */
    public ResourceReader() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void setInputSource(String source) {
        this.inputSource = sourceResolver.resolveURI(source);
    }
    
    /**
     * @param sourceResolver The sourceResolver to set.
     */
    public void setSourceResolver(SourceResolver sourceResolver) {
        this.sourceResolver = sourceResolver;
    }

    /**
     * @param bufferSize The bufferSize to set.
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    /**
     * @param byteRanges The byteRanges to set.
     */
    public void setByteRanges(boolean byteRanges) {
        this.byteRanges = byteRanges;
    }
    
    /**
     * @param expires The expires to set.
     */
    public void setExpires(long expires) {
        this.expires = expires;
    }
    
    /**
     * @param request The request to set.
     */
    public void setRequest(Request request) {
        this.request = request;
    }
    /**
     * @param response The response to set.
     */
    public void setResponse(Response response) {
        this.response = response;
    }
    
    /**
     * @param objectModel The objectModel to set.
     */
    public void setObjectModel(Map objectModel) {
        this.objectModel = objectModel;
    }
    
    /* (non-Javadoc)
     * @see org.apache.butterfly.reading.Reader#generate()
     */
    public void generate() {
        this.request = ObjectModelHelper.getRequest(objectModel);
        this.response = ObjectModelHelper.getResponse(objectModel);

        InputStream inputStream = inputSource.getInputStream();
        try {
            processStream(inputStream);
        } catch (IOException e) {
            throw new PipelineProcessingException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new PipelineProcessingException(e);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.reading.Reader#getLastModified()
     */
    public long getLastModified() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.sitemap.SitemapOutputComponent#setOutputStream(java.io.OutputStream)
     */
    public void setOutputStream(OutputStream out) {
        if (out instanceof BufferedOutputStream 
                || out instanceof org.apache.butterfly.util.BufferedOutputStream) {
               this.out = out;
           } else {
               this.out = new BufferedOutputStream(out, 1536);
           }
    }

    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.sitemap.SitemapOutputComponent#shouldSetContentLength()
     */
    public boolean shouldSetContentLength() {
        // TODO Auto-generated method stub
        return false;
    }

    protected void processStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int length = -1;

        // tell the client whether we support byte range requests or not
        if(byteRanges) {
            response.setHeader("Accept-Ranges", "bytes");
        } else {
            response.setHeader("Accept-Ranges", "none");
        }

        String ranges = request.getHeader("Ranges");

        ByteRange byteRange;
        if (ranges != null && byteRanges) {
            try {
                ranges = ranges.substring(ranges.indexOf('=') + 1);
                byteRange = new ByteRange(ranges);
            } catch (NumberFormatException e) {
                byteRange = null;

                // TC: Hm.. why don't we have setStatus in the Response interface ?
                if (response instanceof HttpResponse) {
                    // Respond with status 416 (Request range not satisfiable)
                    ((HttpResponse)response).setStatus(416);
                    if (logger.isDebugEnabled()) {
                        logger.debug("malformed byte range header [" + String.valueOf(ranges) + "]");
                    }
                }
            }
        } else {
            byteRange = null;
        }

        long contentLength = inputSource.getContentLength();

        if (byteRange != null) {
            String entityLength;
            String entityRange;
            if (contentLength != -1) {
                entityLength = "" + contentLength;
                entityRange = byteRange.intersection(new ByteRange(0, contentLength)).toString();
            } else {
                entityLength = "*";
                entityRange = byteRange.toString();
            }

            response.setHeader("Content-Range", entityRange + "/" + entityLength);

            if (response instanceof HttpResponse) {
                // Response with status 206 (Partial content)
                ((HttpResponse)response).setStatus(206);
            }

            int pos = 0;
            int posEnd;
            while ((length = inputStream.read(buffer)) > -1) {
                posEnd = pos + length - 1;
                ByteRange intersection = byteRange.intersection(new ByteRange(pos, posEnd));
                if (intersection != null) {
                    out.write(buffer, (int) intersection.getStart() - pos, (int) intersection.length());
                }
                pos += length;
            }
        } else {
            if (contentLength != -1) {
                response.setHeader("Content-Length", Long.toString(contentLength));
            }

            while ((length = inputStream.read(buffer)) > -1) {
                out.write(buffer, 0, length);
            }
        }

        out.flush();
    }

}
