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
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-10-19 14:44:13 $
 */
public abstract class AbstractTextSerializer extends AbstractSerializer implements Configurable {

    /**
     * The <code>OutputFormat</code> used by this serializer.
     */    
    protected OutputFormat format;
    
    /** 
     * Set the configurations for this serializer. 
     */
    public void configure(Configuration conf) 
      throws ConfigurationException {

        format = new OutputFormat();
        format.setPreserveSpace(true);

        Configuration encoding = conf.getChild("encoding");
        if (encoding != null) {
            format.setEncoding(encoding.getValue());
        }

        Configuration doctypePublic = conf.getChild("doctype-public");
        Configuration doctypeSystem = conf.getChild("doctype-system");
        if (doctypeSystem != null) {
            format.setDoctype((doctypePublic != null) ? doctypePublic.getValue() : null, doctypeSystem.getValue());
        }

        Configuration indent = conf.getChild("indent");
        if (indent != null) {
            format.setIndenting(true);
            format.setIndent(indent.getValueAsInt());
        }

        Configuration preserveSpace = conf.getChild("preserve-space");
        if (preserveSpace != null) {
            format.setPreserveSpace(preserveSpace.getValueAsBoolean());
        }

        Configuration declaration = conf.getChild("xml-declaration");
        if (declaration != null) {
            format.setOmitXMLDeclaration(!declaration.getValueAsBoolean());
        }
        
        Configuration lineWidth = conf.getChild("line-width");
        if (lineWidth != null) {
            format.setLineWidth(lineWidth.getValueAsInt());
        }        
    }
}
