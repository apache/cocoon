/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.serializers;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.cocoon.Job;
import org.apache.cocoon.XMLConsumer;
import org.apache.cocoon.XMLConsumerImpl;
import org.apache.cocoon.framework.AbstractComponent;
import org.apache.cocoon.framework.Configurations;
import org.apache.cocoon.framework.ConfigurationException;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.xml.sax.AttributeList;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.DocumentHandler;

/**
 * The <code>FileProducer</code> produces XML data from source files.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-09 01:53:21 $
 */
public class XercesSerializer extends AbstractComponent implements Serializer {
    /** The Xerces serializer factory */
    private org.apache.xml.serialize.SerializerFactory fac=null;
    /** The factory configurations */
    private Configurations configurations=null;
    /** The content type */
    private String contentType=null;

    public XercesSerializer(Configurations conf) {
        super();
        this.configurations=conf;
    }

    public XMLConsumer getXMLConsumer(Job job, String src, OutputStream out)
    throws IOException {
        job.setContentType(this.contentType);
        OutputFormat fmt=new OutputFormat();
        DocumentHandler h=fac.makeSerializer(out,fmt).asDocumentHandler();
        return(new XMLConsumerImpl(h));
    }

    public void configure(Configurations conf)
    throws ConfigurationException {
        conf.merge(this.configurations);
        String m=conf.getParameter("outputMethod");
        if (m==null) throw new ConfigurationException("Parameter 'outputMethod"+
                                                      "' was not specified");
        if (m.equalsIgnoreCase("html")) {
            m=Method.HTML;
            this.contentType="text/html";
        } else if (m.equalsIgnoreCase("xhtml")) {
            m=Method.XHTML;
            this.contentType="text/xhtml";
        } else if (m.equalsIgnoreCase("xml")) {
            m=Method.XML;
            this.contentType="text/xml";
        } else if (m.equalsIgnoreCase("text")) {
            m=Method.TEXT;
            this.contentType="text/plain";
        } else throw new ConfigurationException("Invalid method '"+m+"'");
        fac=org.apache.xml.serialize.SerializerFactory.getSerializerFactory(m);
    }
    
    public boolean modifiedSince(long date) {
        return(false);
    }
}
