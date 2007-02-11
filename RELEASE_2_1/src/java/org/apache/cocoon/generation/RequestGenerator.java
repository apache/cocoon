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
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServletGenerator;
import org.apache.cocoon.transformation.helpers.NOPRecorder;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Generates an XML representation of the incoming request.
 * <p>
 * <b>Configuration options:</b>
 * <dl>
 * <dt> <i>container-encoding</i> (optional)
 * <dd> The encoding used by container. Default value is ISO-8859-1.
 * <dt> <i>form-encoding</i> (optional)
 * <dd> The supposed encoding of the request parameter. Default is null.
 * <dt> <i>generate-attributes</i> (optional)
 * <dd> If true, also generates request attributes. Default is false.
 * </dl>
 * These configuration options supported in both declaration and use time. 
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: RequestGenerator.java,v 1.3 2003/06/16 23:46:10 stefano Exp $
 */
public class RequestGenerator extends ServletGenerator implements Parameterizable {

    /** The URI of the namespace of this generator. */
    private String PREFIX = "h";
    private String URI = "http://apache.org/cocoon/request/2.0";
    private String global_container_encoding;
    private String global_form_encoding;
    private String container_encoding;
    private String form_encoding;
    private boolean global_generate_attributes;
    private boolean generate_attributes;

    public void parameterize(Parameters parameters)
    throws ParameterException {
        global_container_encoding = parameters.getParameter("container-encoding", "ISO-8859-1");
        global_form_encoding = parameters.getParameter("form-encoding", null);
        global_generate_attributes = parameters.getParameterAsBoolean("generate-attributes", false);
    }

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        container_encoding = par.getParameter("container-encoding", global_container_encoding);
        form_encoding = par.getParameter("form-encoding", global_form_encoding);
        generate_attributes = "false no off".indexOf(par.getParameter("generate-attributes",
								      "" + global_generate_attributes)) < 0;
    }

    /**
     * Generate XML data.
     */
    public void generate()
    throws SAXException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        this.contentHandler.startDocument();
        this.contentHandler.startPrefixMapping(PREFIX,URI);
        AttributesImpl attr = new AttributesImpl();

        this.attribute(attr,"target", request.getRequestURI());
        this.attribute(attr,"source", (this.source != null ? this.source : ""));
        this.start("request", attr);

        this.start("requestHeaders", attr);
        Enumeration headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = (String) headers.nextElement();
            this.attribute(attr,"name",header);
            this.start("header",attr);
            this.data(request.getHeader(header));
            this.end("header");
        }
        this.end("requestHeaders");

        this.start("requestParameters",attr);
        Enumeration parameters=request.getParameterNames();
        while (parameters.hasMoreElements()) {
            String parameter = (String) parameters.nextElement();
            this.attribute(attr,"name",parameter);
            this.start("parameter",attr);
            String values[] = request.getParameterValues(parameter);
            if (values != null) {
                for (int x = 0; x < values.length; x++) {
                    this.start("value",attr);
                    if (form_encoding != null) {
                        try {
                            this.data(values[x],container_encoding,form_encoding);
                        } catch (UnsupportedEncodingException uee) {
                            throw new CascadingRuntimeException("The suggested encoding is not supported.", uee);
                        }
                    } else if (parameter.startsWith("xml:")) {
                        try {
                            this.parse(values[x]);
                        } catch (Exception e) {
                            throw new CascadingRuntimeException("Could not parse the xml parameter", e);
                        }
                    } else {
                        this.data(values[x]);
                    }
                    this.end("value");
                }
            }
            this.end("parameter");
        }
        this.end("requestParameters");

        if (generate_attributes) {
            this.start("requestAttributes",attr);
            Enumeration attributes = request.getAttributeNames();
            while (attributes.hasMoreElements()) {
                String attribute=(String)attributes.nextElement();
                this.attribute(attr,"name",attribute);
                this.start("attribute",attr);
                Object value=request.getAttribute(attribute);
                if (value!=null) {
                    this.start("value",attr);
                    XMLUtils.valueOf(this.contentHandler, value);
                    this.end("value");
                }
                this.end("attribute");
            }
            this.end("requestAttributes");
        }

        this.start("configurationParameters",attr);
        String[] confparams=super.parameters.getNames();
        for (int i = 0; i < confparams.length; i++) {
            this.attribute(attr, "name", confparams[i]);
            this.start("parameter",attr);
            this.data(super.parameters.getParameter(confparams[i], ""));
            this.end("parameter");
        }
        this.end("configurationParameters");

        this.end("request");

        this.contentHandler.endPrefixMapping(PREFIX);
        this.contentHandler.endDocument();
    }

    private void attribute(AttributesImpl attr, String name, String value) {
        attr.addAttribute("",name,name,"CDATA",value);
    }

    private void start(String name, AttributesImpl attr)
    throws SAXException {
        super.contentHandler.startElement(URI,name,PREFIX + ":" + name,attr);
        attr.clear();
    }

    private void end(String name)
    throws SAXException {
        super.contentHandler.endElement(URI,name,PREFIX + ":" + name);
    }

    private void data(String data)
    throws SAXException {
        super.contentHandler.characters(data.toCharArray(),0,data.length());
    }
    
    private void data(String data, String container_encoding, String form_encoding) 
    throws SAXException, UnsupportedEncodingException {
        this.data(new String(data.getBytes(container_encoding), form_encoding));
    }
    
    private void parse(String data)
    throws Exception {
        SAXParser parser = null;

        try {
            parser = (SAXParser) manager.lookup(SAXParser.ROLE);
            StringReader inputStream = new StringReader(data);
            InputSource is = new InputSource(inputStream);
            parser.parse(is, new FilteringXMLConsumer(super.xmlConsumer));
        } catch (Exception e) {
            throw e;
        } finally {
            if (parser != null) manager.release((Component) parser);
        }
    }
    
    private class FilteringXMLConsumer extends NOPRecorder {
        XMLConsumer c;
        
        FilteringXMLConsumer(XMLConsumer c) {
            this.c = c;
        }
        
        public void startPrefixMapping(String prefix, String uri)
         throws SAXException {
             this.c.startPrefixMapping(prefix,uri);
         }
        
         public void endPrefixMapping(String prefix)
         throws SAXException {
             this.c.endPrefixMapping(prefix);
         }
        
         public void startElement(String namespace, String name, String raw, Attributes attr)
         throws SAXException {
             this.c.startElement(namespace,name,raw,attr);
         }
        
         public void endElement(String namespace, String name, String raw)
         throws SAXException {
             this.c.endElement(namespace,name,raw);
         }
        
         public void characters(char ary[], int start, int length)
         throws SAXException {
             this.c.characters(ary,start,length);
         }
    }

}
