/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.generation;

import java.util.Enumeration;
import java.util.Iterator;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Request;
import org.apache.avalon.excalibur.pool.Poolable;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.14 $ $Date: 2001-04-30 14:17:23 $
 */
public class RequestGenerator extends ServletGenerator implements Poolable {

    /** The URI of the namespace of this generator. */
    private String URI="http://xml.apache.org/cocoon/2.0/RequestGenerator";

    /**
     * Generate XML data.
     */
    public void generate()
    throws SAXException {

        Request request = (Request) objectModel.get(Constants.REQUEST_OBJECT);
        this.contentHandler.startDocument();
        this.contentHandler.startPrefixMapping("",URI);
        AttributesImpl attr=new AttributesImpl();

        this.attribute(attr,"target",request.getRequestURI());
        this.attribute(attr,"source",this.source);
        this.start("request",attr);
        this.data("\n");
        this.data("\n");

        this.data("  ");
        this.start("requestHeaders",attr);
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
                this.data(values[x]);
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

        this.data("  ");
        this.start("configurationParameters",attr);
        this.data("\n");
        Iterator confparams=super.parameters.getParameterNames();
        while (confparams.hasNext()) {
            String parameter=(String)confparams.next();
            this.attribute(attr,"name",parameter);
            this.data("    ");
            this.start("parameter",attr);
            this.data(super.parameters.getParameter(parameter,""));
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
