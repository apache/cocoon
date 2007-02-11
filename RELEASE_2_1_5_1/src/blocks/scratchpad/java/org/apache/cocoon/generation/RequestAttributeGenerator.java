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

import java.io.StringReader;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The <code>RequestAttributeGenerator</code> is a class that reads XML from a
 * request attribute and generates SAX Events.
 * <p>
 * The response encoding is taken as the encoding of the xml-data.
 * </p>
 *
 * @author <a href="mailto:Kinga_Dziembowski@hp.com">Kinga Dziembowski</a>
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: RequestAttributeGenerator.java,v 1.5 2004/05/07 17:32:59 joerg Exp $
 *
 * @cocoon.sitemap.component.name       req-attr
 * @cocoon.sitemap.component.label      content
 * @cocoon.sitemap.component.logger     sitemap.generator.req-attr
 * @cocoon.sitemap.component.parameter  attribute-name
 *   type="String"
 *   description="Specifies name of request attribute holding xml data. This xml data will be sent into the cocoon pipeline."
 *   required="no"
 *   default="org.apache.cocoon.xml-data"
 */
public class RequestAttributeGenerator extends ServiceableGenerator {

    /**
     * The config parameter for specifying name of the request attribute,
     * holding the name associated with the xml data, ie <code>attribute-name
     * </code>.
     *
     */
    public final static String REQUEST_ATTRIBUTE_NAME = "attribute-name";
    /**
     * The default name of the request attribute name, storing xml-data,
     *  ie. <code>org.apache.cocoon.xml-data</code>.
     *
     */
    public final static String REQUEST_ATTRIBUTE_NAME_DEFAULT = "org.apache.cocoon.xml-data";


    /**
     * Generate XML data out of request attribute, and send it into cocoon
     * pipeline.
     *
     * @exception  SAXException         Description of Exception
     * @exception  ProcessingException  Description of Exception
     */
    public void generate() throws SAXException, ProcessingException {
        String parameter = parameters.getParameter(REQUEST_ATTRIBUTE_NAME, REQUEST_ATTRIBUTE_NAME_DEFAULT);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Expecting xml data in request-attribute " + parameter);
        }

        String contentType = null;
        InputSource inputSource;

        final Request request = ObjectModelHelper.getRequest(this.objectModel);
        final Response response = ObjectModelHelper.getResponse(this.objectModel);
        
        byte[] xml_data = (byte[]) request.getAttribute(parameter);
        if (xml_data == null) {
            throw new ProcessingException("request-attribute " +
                    parameter + " is null, no xml-data for processing");
        }

        SAXParser parser = null;
        try {
            String sXml = new String(xml_data);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("processing : " + sXml);
            }
            inputSource = new InputSource(new StringReader(sXml));

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("processing request attribute ");
            }
            String charset = getCharacterEncoding(response, contentType);
            if (charset != null) {
                inputSource.setEncoding(charset);
            }
            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            parser.parse(inputSource, super.xmlConsumer);
        } catch (Exception e) {
            getLogger().error("Could not get parser", e);
            throw new ProcessingException("Exception in RequestAttributeGenerator.generate()", e);
        } finally {
            this.manager.release(parser);
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
     * @param  contentType  value associated with Content-Type HTTP header.
     * @param  res          Description of Parameter
     * @return              The characterEncoding value
     */
    protected String getCharacterEncoding(Response res, String contentType) {
        String charencoding = null;
        String charset = "charset=";
        if (contentType == null) {
            return (null);
        }
        int idx = contentType.indexOf(charset);
        if (idx == -1) {
            return (null);
        }
        try {
            charencoding = res.getCharacterEncoding();

            if (charencoding != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("charset from container: " + charencoding);
                }
                charencoding = charencoding.trim();
                if ((charencoding.length() > 2) && (charencoding.startsWith("\"")) && (charencoding.endsWith("\""))) {
                    charencoding = charencoding.substring(1, charencoding.length() - 1);
                }
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("charset from container clean: " + charencoding);
                }
                return (charencoding);
            } else {

                return extractCharset(contentType, idx);
            }
        } catch (Throwable e) {
            // We will be there if the container do not implement getCharacterEncoding() method
            return extractCharset(contentType, idx);
        }
    }


    /**
     * Description of the Method
     *
     * @param  contentType  Description of Parameter
     * @param  idx          Description of Parameter
     * @return              Description of the Returned Value
     */
    protected String extractCharset(String contentType, int idx) {
        String charencoding = null;
        String charset = "charset=";
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("charset from extractCharset");
        }
        charencoding = contentType.substring(idx + charset.length());
        int idxEnd = charencoding.indexOf(";");
        if (idxEnd != -1) {
            charencoding = charencoding.substring(0, idxEnd);
        }
        charencoding = charencoding.trim();
        if ((charencoding.length() > 2) && (charencoding.startsWith("\"")) && (charencoding.endsWith("\""))) {
            charencoding = charencoding.substring(1, charencoding.length() - 1);
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("charset from extractCharset: " + charencoding);
        }
        return (charencoding.trim());
    }
}
