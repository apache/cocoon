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
package org.apache.cocoon.generation;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.servlet.multipart.Part;
import org.apache.cocoon.util.PostInputStream;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

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
 * @version CVS $Id: StreamGenerator.java,v 1.9 2004/03/06 14:24:51 joerg Exp $
 */
public class StreamGenerator extends ServiceableGenerator
{

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
                    getLogger().debug("no Content-Type header - using contentType parameter: " + contentType);
                }
                if (contentType == null) {
                    throw new IOException("both Content-Type header and defaultContentType parameter are not set");
                }
            }
            if (contentType.startsWith("application/x-www-form-urlencoded") ||
                    contentType.startsWith("multipart/form-data")) {
                String parameter = parameters.getParameter(FORM_NAME, null);
                if (parameter == null) {
                    throw new ProcessingException(
                        "StreamGenerator expects a sitemap parameter called '" +
                        FORM_NAME + "' for handling form data"
                    );
                }
                Object xmlObject = request.get(parameter);
                Reader xmlReader = null;
                if (xmlObject instanceof String) {
                    xmlReader  = new StringReader((String)xmlObject);
                } else if (xmlObject instanceof Part) {
                    xmlReader = new InputStreamReader(((Part)xmlObject).getInputStream());
                } else {
                    throw new ProcessingException("Unknown request object encountered named " + 
                                                  parameter + " : " + xmlObject);
                }                
                inputSource = new InputSource(xmlReader);
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

