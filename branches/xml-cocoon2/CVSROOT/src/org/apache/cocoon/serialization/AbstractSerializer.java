/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon.serialization;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

import org.apache.xml.serialize.SerializerFactory;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;

import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;

import org.apache.cocoon.sitemap.SitemapOutputComponent;
import org.apache.cocoon.xml.AbstractXMLConsumer;
import org.apache.cocoon.xml.util.XMLConsumerBridge;

/**
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2000-08-21 17:36:25 $
 */
public abstract class AbstractSerializer extends XMLConsumerBridge 
implements Serializer, Configurable {

    /**
     * The <code>OutputFormat</code> used by this serializer.
     */    
    protected OutputFormat format;
    
    /**
     * The <code>OutputStream</code> used by this serializer.
     */    
    protected OutputStream output;

    /** 
     * Set the configurations for this serializer. 
     */
    public void setConfiguration(Configuration conf) 
      throws ConfigurationException {

        format = new OutputFormat();
        format.setPreserveSpace(true);

        Configuration encoding = conf.getConfiguration("encoding");
        if (encoding != null) {
            format.setEncoding(encoding.getValue());
        }

        Configuration doctypePublic = conf.getConfiguration("doctype-public");
        Configuration doctypeSystem = conf.getConfiguration("doctype-system");
        if (doctypeSystem != null) {
            format.setDoctype((doctypePublic != null) ? doctypePublic.getValue() : null, doctypeSystem.getValue());
        }

        Configuration indent = conf.getConfiguration("indent");
        if (indent != null) {
            format.setIndenting(true);
            format.setIndent(indent.getValueAsInt());
        }

        Configuration preserveSpace = conf.getConfiguration("preserve-space");
        if (preserveSpace != null) {
            format.setPreserveSpace(preserveSpace.getValueAsBoolean());
        }

        Configuration declaration = conf.getConfiguration("xml-declaration");
        if (declaration != null) {
            format.setOmitXMLDeclaration(!declaration.getValueAsBoolean());
        }
        
        Configuration lineWidth = conf.getConfiguration("line-width");
        if (lineWidth != null) {
            format.setLineWidth(lineWidth.getValueAsInt());
        }        
    }
        
    /**
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        this.output = out;
    }

    /**
     * Get the mime-type of the output of this <code>Serializer</code>
     * This default implementation returns null to indicate that the 
     * mime-type specified in the sitemap is to be used
     */
    public String getMimeType() {
        return null;
    }
}
