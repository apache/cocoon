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

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.util.ByteRange;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.http.HttpResponse;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * The <code>ResourceReader</code> component is used to serve binary data
 * in a sitemap pipeline. It makes use of HTTP Headers to determine if
 * the requested resource should be written to the <code>OutputStream</code>
 * or if it can signal that it hasn't changed.
 *
 * Parameters:
 *   <dl>
 *     <dt>&lt;expires&gt;</dt>
 *       <dd>This parameter is optional. When specified it determines how long
 *           in miliseconds the resources can be cached by any proxy or browser
 *           between Cocoon2 and the requesting visitor.
 *       </dd>
 *     <dt>&lt;quick-modified-test&gt;</dt>
 *       <dd>This parameter is optional. This boolean parameter controlls the
 *           last modified test. If set to true (default is false), only the
 *           last modified of the current source is tested, but not if the
 *           same source is used as last time. (see http://marc.theaimsgroup.com/?l=xml-cocoon-dev&m=102921894301915&w=2 )
 *       </dd>
 *   </dl>
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ResourceReader.java,v 1.4 2004/02/04 12:18:54 cziegeler Exp $
 */
public class ResourceReader 
extends AbstractReader 
implements CacheableProcessingComponent, Parameterizable {

    /** The list of generated documents */
    private static final Map documents = new HashMap();

    protected Source inputSource;
    protected InputStream inputStream;

    protected boolean quickTest;
    protected boolean byteRanges;

    protected Response response;
    protected Request request;
    protected long expires;
    protected int bufferSize;

    protected long configuredExpires;
    protected boolean configuredQuickTest;
    protected int configuredBufferSize;
    protected boolean configuredByteRanges;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        this.configuredExpires = parameters.getParameterAsLong("expires", -1);
        this.configuredQuickTest = parameters.getParameterAsBoolean("quick-modified-test", false);
        this.configuredBufferSize = parameters.getParameterAsInteger("buffer-size", 8192);
        this.configuredByteRanges = parameters.getParameterAsBoolean("byte-ranges", true);
    }

    /**
     * Setup the reader.
     * The resource is opened to get an <code>InputStream</code>,
     * the length and the last modification date
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        request = ObjectModelHelper.getRequest(objectModel);
        response = ObjectModelHelper.getResponse(objectModel);

        expires = par.getParameterAsLong("expires", this.configuredExpires);
        bufferSize = par.getParameterAsInteger("buffer-size", this.configuredBufferSize);

        byteRanges = par.getParameterAsBoolean("byte-ranges", this.configuredByteRanges);
        quickTest = par.getParameterAsBoolean("quick-modified-test", this.configuredQuickTest);

        try {
            inputSource = resolver.resolveURI(src);
        }
        catch (SourceException se) {
            throw SourceUtil.handle("Error during resolving of '" + src + "'.", se);
        }
    }

    /**
     * Recyclable
     */
    public void recycle() {
        if (inputSource != null) {
            super.resolver.release(inputSource);
            inputSource = null;
        }
        super.recycle();
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public java.io.Serializable getKey() {
        return inputSource.getURI();
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return inputSource.getValidity();
    }

    /**
     * @return the time the read source was last modified or 0 if it is not
     *         possible to detect
     */
    public long getLastModified() {
        if (quickTest) {
            return inputSource.getLastModified();
        }
        final String systemId = (String) documents.get(request.getRequestURI());
        if (systemId == null || inputSource.getURI().equals(systemId)) {
            return inputSource.getLastModified();
        }
        else {
            documents.remove(request.getRequestURI());
            return 0;
        }
    }

    protected void processStream() throws IOException, ProcessingException {
        byte[] buffer = new byte[bufferSize];
        int length = -1;

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
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("malformed byte range header [" + String.valueOf(ranges) + "]");
                    }
                }
            }
        }
        else {
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

            response.setHeader("Accept-Ranges", "bytes");

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
        }
        else {
            if (contentLength != -1) {
                response.setHeader("Content-Length", Long.toString(contentLength));
            }

            // Bug #9539: This resource reader does not support ranges
            response.setHeader("Accept-Ranges", "none");

            while ((length = inputStream.read(buffer)) > -1) {
                out.write(buffer, 0, length);
            }
        }

        out.flush();
    }

    /**
     * Generates the requested resource.
     */
    public void generate() throws IOException, ProcessingException {
        try {
            if (expires > 0) {
                response.setDateHeader("Expires", System.currentTimeMillis() + expires);
            }
            else {
                response.addHeader("Vary", "Host");
            }
            
            long lastModified = getLastModified();
            if (lastModified > 0) {
                response.setDateHeader("Last-Modified", lastModified);
            }
            
            try {
                inputStream = inputSource.getInputStream();
            }
            catch (SourceException se) {
                throw SourceUtil.handle("Error during resolving of the input stream", se);
            }

            // Bugzilla Bug 25069, close inputStream in finally block
            // this will close inputStream even if processStream throws
            // an exception
            try {
                processStream();
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            if (!quickTest) {
                // if everything is ok, add this to the list of generated documents
                // (see http://marc.theaimsgroup.com/?l=xml-cocoon-dev&m=102921894301915&w=2 )
                documents.put(request.getRequestURI(), inputSource.getURI());
            }
        }
        catch (IOException e) {
            getLogger().debug("Received an IOException, assuming client severed connection on purpose");
        }
    }

    /**
     * Returns the mime-type of the resource in process.
     */
    public String getMimeType() {
        Context ctx = ObjectModelHelper.getContext(objectModel);

        if (ctx != null) {
            if (ctx.getMimeType(source) != null) {
                return ctx.getMimeType(source);
            }
            else {
                return inputSource.getMimeType();
            }
        }
        else {
            return inputSource.getMimeType();
        }
    }

}
