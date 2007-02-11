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

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

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
 *         (Apache Software Foundation)
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: RequestGenerator.java,v 1.2 2003/05/19 10:40:39 stephan Exp $
 */
public class RequestGenerator extends ServletGenerator implements Parameterizable {

    /** The URI of the namespace of this generator. */
    private String URI="http://apache.org/cocoon/request/2.0";
    private String global_container_encoding;
    private String global_form_encoding;
    private String container_encoding;
    private String form_encoding;
    private boolean global_generate_attributes;
    private boolean generate_attributes;

    public void parameterize(Parameters parameters)
    throws ParameterException {
        // super.parameterize(parameters);

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
        this.contentHandler.startPrefixMapping("",URI);
        AttributesImpl attr=new AttributesImpl();

        this.attribute(attr,"target", request.getRequestURI());
        this.attribute(attr,"source", (this.source != null ? this.source : ""));
        this.start("request", attr);
        this.data("\n");
        this.data("\n");

        this.data("  ");
        this.start("requestHeaders", attr);
        this.data("\n");
        Enumeration headers=request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header=(String)headers.nextElement();
            this.attribute(attr,"name",header);
            this.data("    ");
            this.start("header",attr);
            this.data(request.getHeader(header));
            this.end("header");
            this.data("\n");
        }
        this.data("  ");
        this.end("requestHeaders");
        this.data("\n");
        this.data("\n");

        this.data("  ");
        this.start("requestParameters",attr);
        this.data("\n");
        Enumeration parameters=request.getParameterNames();
        while (parameters.hasMoreElements()) {
            String parameter=(String)parameters.nextElement();
            this.attribute(attr,"name",parameter);
            this.data("    ");
            this.start("parameter",attr);
            this.data("\n");
            String values[]=request.getParameterValues(parameter);
            if (values!=null) for (int x=0; x<values.length; x++) {
                this.data("      ");
                this.start("value",attr);
                if (form_encoding != null) {
                    try {
                        this.data(new String(values[x].getBytes(container_encoding),
                            form_encoding));
                    } catch(java.io.UnsupportedEncodingException uee) {
                        throw new CascadingRuntimeException("Unsupported Encoding Exception", uee);
                    }
                } else {
                    this.data(values[x]);
                }
                this.end("value");
                this.data("\n");
            }
            this.data("    ");
            this.end("parameter");
            this.data("\n");
        }
        this.data("  ");
        this.end("requestParameters");
        this.data("\n");
        this.data("\n");

        if(generate_attributes) {
            this.data("  ");
            this.start("requestAttributes",attr);
            this.data("\n");
            Enumeration attributes=request.getAttributeNames();
            while (attributes.hasMoreElements()) {
                String attribute=(String)attributes.nextElement();
                this.attribute(attr,"name",attribute);
                this.data("    ");
                this.start("attribute",attr);
                this.data("\n");
                Object value=request.getAttribute(attribute);
                if (value!=null) {
                    this.data("      ");
                    this.start("value",attr);
                    XMLUtils.valueOf(this.contentHandler, value );
                    this.end("value");
                    this.data("\n");
                }
                this.data("    ");
                this.end("attribute");
                this.data("\n");
            }
            this.data("  ");
            this.end("requestAttributes");
            this.data("\n");
            this.data("\n");
        }

        this.data("  ");
        this.start("configurationParameters",attr);
        this.data("\n");
        String[] confparams=super.parameters.getNames();
        for (int i=0; i<confparams.length; i++) {
            this.attribute(attr, "name", confparams[i]);
            this.data("    ");
            this.start("parameter",attr);
            this.data(super.parameters.getParameter(confparams[i], ""));
            this.end("parameter");
            this.data("\n");
        }
        this.data("  ");
        this.end("configurationParameters");
        this.data("\n");
        this.data("\n");

        this.end("request");

        // Finish
        this.contentHandler.endPrefixMapping("");
        this.contentHandler.endDocument();
    }

    private void attribute(AttributesImpl attr, String name, String value) {
        attr.addAttribute("",name,name,"CDATA",value);
    }

    private void start(String name, AttributesImpl attr)
    throws SAXException {
        super.contentHandler.startElement(URI,name,name,attr);
        attr.clear();
    }

    private void end(String name)
    throws SAXException {
        super.contentHandler.endElement(URI,name,name);
    }

    private void data(String data)
    throws SAXException {
        super.contentHandler.characters(data.toCharArray(),0,data.length());
    }
}
