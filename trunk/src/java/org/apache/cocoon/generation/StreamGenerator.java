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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.util.PostInputStream;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * The <code>StreamGenerator</code> is a class that reads XML from a
 * request InputStream and generates SAX Events.
 *
 * For the POST requests with a mimetype of application/x-www-form-urlencoded,
 * or multipart/form-data the xml data is expected to be associated with the
 * sitemap parameter 'form-name'.
 *
 * For the POST requests with mimetypes: text/plain, text/xml,
 * application/xml the xml data is in the body of the POST request and
 * its length is specified by the value returned by getContentLength()
 * method.  The StreamGenerator uses helper
 * org.apache.cocoon.util.PostInputStream class for InputStream
 * reading operations.  At the time that Parser is reading the data
 * out of InputStream - Parser has no knowledge about the length of
 * data to be read.  The only way to signal to the Parser that all
 * data was read from the InputStream is to control reading operation-
 * PostInputStream--and to return to the requestor '-1' when the
 * number of bytes read is equal to the getContentLength() value.
 *
 * @author <a href="mailto:Kinga_Dziembowski@hp.com">Kinga Dziembowski</a>
 * @version CVS $Id: StreamGenerator.java,v 1.8 2003/12/06 21:22:08 cziegeler Exp $
 */
public class StreamGenerator extends ServiceableGenerator
{
    public static final String CLASS = StreamGenerator.class.getName();

    /** The parameter holding the name associated with the xml data  **/
    public static final String FORM_NAME = "form-name";

    /** The input source */
    private InputSource inputSource;

    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        super.recycle();
        this.inputSource = null;
    }

    /**
     * Generate XML data out of request InputStream.
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        SAXParser parser = null;
        int len = 0;
        String contentType = null;

        Request request = ObjectModelHelper.getRequest(this.objectModel);
        try {
            contentType = request.getContentType();
            if (contentType == null) {
                contentType = parameters.getParameter("defaultContentType", null);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("no Content-Type header - using contentType parameter");
                }
            }
            if (contentType == null) {
                throw new IOException("both Content-Type header and defaultContentType parameter are not set");
            } else if (contentType.startsWith("application/x-www-form-urlencoded") ||
                    contentType.startsWith("multipart/form-data")) {
                String parameter = parameters.getParameter(FORM_NAME, null);
                if (parameter == null) {
                    throw new ProcessingException(
                        "StreamGenerator expects a sitemap parameter called '" +
                        FORM_NAME + "' for handling form data"
                    );
                }
                String sXml = request.getParameter(parameter);
                inputSource = new InputSource(new StringReader(sXml));
            } else if (contentType.startsWith("text/plain") ||
                    contentType.startsWith("text/xml") ||
                    contentType.startsWith("application/xml")) {

                HttpServletRequest httpRequest = (HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
                if ( httpRequest == null ) {
                    throw new ProcessingException("This feature is only available in an http environment.");
                }
                len = request.getContentLength();
                if (len > 0) {
                        PostInputStream anStream = new PostInputStream(httpRequest.getInputStream(), len);
                        inputSource = new InputSource(anStream);
                } else {
                    throw new IOException("getContentLen() == 0");
                }
            } else {
                throw new IOException("Unexpected getContentType(): " + request.getContentType());
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("processing stream ContentType=" + contentType + " ContentLen=" + len);
            }
            String charset =  getCharacterEncoding(request, contentType) ;
            if( charset != null) {
                this.inputSource.setEncoding(charset);
            }
            parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);
            parser.parse(this.inputSource, super.xmlConsumer);
        } catch (IOException e) {
            getLogger().error("StreamGenerator.generate()", e);
            throw new ResourceNotFoundException("StreamGenerator could not find resource", e);
        } catch (SAXException e) {
            getLogger().error("StreamGenerator.generate()", e);
            throw(e);
        } catch (Exception e) {
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in StreamGenerator.generate()", e);
        } finally {
            this.manager.release( parser);
        }
    }

    /**
    * Content type HTTP header can contains character encodinf info
    * for ex. Content-Type: text/xml; charset=UTF-8
    * If the servlet is following spec 2.3 and higher the servlet API can be used to retrieve character encoding part of
    * Content-Type header. Some containers can choose to not unpack charset info - the spec is not strong about it.
    * in any case this method can be used as a latest resource to retrieve the passed charset value.
    * <code>null</code> is returned.
    * It is very common mistake to send : Content-Type: text/xml; charset="UTF-8".
    * Some containers are not filtering this mistake and the processing results in exception..
    * The getCharacterEncoding() compensates for above mistake.
    *
    * @param contentType value associated with Content-Type HTTP header.
    */
    public String getCharacterEncoding(Request req, String contentType) {
        String charencoding = null;
        String charset = "charset=";
        if (contentType == null) {
            return null;
        }
        int idx = contentType.indexOf(charset);
        if (idx == -1) {
            return null;
        }
        try {
            charencoding = req.getCharacterEncoding();

            if ( charencoding != null) {
                getLogger().debug("charset from container: " + charencoding);
                charencoding = charencoding.trim();
                if ((charencoding.length() > 2) && (charencoding.startsWith("\""))&& (charencoding.endsWith("\""))) {
                    charencoding = charencoding.substring(1, charencoding.length() - 1);
                }
                getLogger().debug("charset from container clean: " + charencoding);
                return charencoding;
            } else {
                return extractCharset( contentType, idx );
            }
        } catch(Throwable e) {
            // We will be there if the container do not implement getCharacterEncoding() method
             return extractCharset( contentType, idx );
        }
    }


    protected String extractCharset(String contentType, int idx) {
        String charencoding = null;
        String charset = "charset=";

        getLogger().debug("charset from extractCharset");
        charencoding = contentType.substring(idx + charset.length());
        int idxEnd = charencoding.indexOf(";");
        if (idxEnd != -1) {
            charencoding = charencoding.substring(0, idxEnd);
        }
        charencoding = charencoding.trim();
        if ((charencoding.length() > 2) && (charencoding.startsWith("\""))&& (charencoding.endsWith("\""))) {
            charencoding = charencoding.substring(1, charencoding.length() - 1);
        }
        getLogger().debug("charset from extractCharset: " + charencoding);
        return charencoding.trim();

    }
}

