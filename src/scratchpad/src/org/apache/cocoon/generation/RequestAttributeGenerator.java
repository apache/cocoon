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

import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.http.HttpEnvironment;
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
 * @version CVS $Id: RequestAttributeGenerator.java,v 1.2 2003/03/16 18:03:54 vgritsenko Exp $
 *
 * @cocoon:name                      req-attr
 * @cocoon:status                    scratchpad
 * @cocoon:parameter                 name="attribute-name"
 *   type="String"
 *   description="Specifies name of request attribute holding xml-data"
 *   required="no"
 *   default="org.apache.cocoon.xml-data"
 * @cocoon:http-request-attribute    name="org.apache.cocoon.xml-data"
 *   type="String xml-data"
 *   description="The xml-data of this request attribute is sent into the cocoon-pipeline."
 *   required="yes"
 *   default="none"
 */
public class RequestAttributeGenerator extends ComposerGenerator {
    /**
     * The name of this class
     *
     * @since    1.0
     */
    public final static String CLASS = RequestAttributeGenerator.class.getName();

    /**
     * The config parameter for specifying name of the request attribute,
     * holding the name associated with the xml data, ie <code>attribute-name
     * </code>.
     *
     * @since    1.0
     */
    public final static String REQUEST_ATTRIBUTE_NAME = "attribute-name";
    /**
     * The default name of the request attribute name, storing xml-data,
     *  ie. <code>org.apache.cocoon.xml-data</code>.
     *
     * @since    1.0
     */
    public final static String REQUEST_ATTRIBUTE_NAME_DEFAULT = "org.apache.cocoon.xml-data";


    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     *
     * @since    1.0
     */
    public void recycle() {
        super.recycle();
    }


    /**
     * Generate XML data out of request attribute, and send it into cocoon
     * pipeline.
     *
     * @exception  SAXException         Description of Exception
     * @exception  ProcessingException  Description of Exception
     * @since                           1.0
     */
    public void generate() throws SAXException, ProcessingException {
        SAXParser parser = null;
        String parameter = parameters.getParameter(REQUEST_ATTRIBUTE_NAME, REQUEST_ATTRIBUTE_NAME_DEFAULT);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Expecting xml data in request-attribute " + parameter);
        }

        String contentType = null;
        InputSource inputSource;

        HttpServletRequest request = (HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
        HttpServletResponse response = (HttpServletResponse) objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);

        byte[] xml_data = (byte[]) request.getAttribute(parameter);
        if (xml_data == null) {
            throw new ProcessingException("request-attribute " +
                    parameter + " is null, no xml-data for processing");
        }
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
            if (parser != null) {
                this.manager.release((Component)parser);
            }
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
     * @since 1.0
     */
    protected String getCharacterEncoding(HttpServletResponse res, String contentType) {
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
     * @since 1.0
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
