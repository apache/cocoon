/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon.serialization;

import org.apache.xml.serialize.SerializerFactory;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;

import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;

/**
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-09-27 16:15:36 $
 */
public abstract class AbstractTextSerializer extends AbstractSerializer implements Configurable {

    /**
     * The <code>OutputFormat</code> used by this serializer.
     */    
    protected OutputFormat format;
    
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
}
