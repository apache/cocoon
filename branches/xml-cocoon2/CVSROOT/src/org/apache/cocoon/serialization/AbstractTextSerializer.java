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
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-11-30 21:42:00 $
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

        try {
            Configuration encoding = conf.getChild("encoding");
            format.setEncoding(encoding.getValue());
        } catch (ConfigurationException ce) {
            // TODO: how to handle non-existant encoding?
        }

        String doctypePublic = null;

        try {
            Configuration dtPublic = conf.getChild("doctype-public");
            doctypePublic = dtPublic.getValue();
        } catch (ConfigurationException ce) {
            doctypePublic = null;
        }

        try {
            Configuration doctypeSystem = conf.getChild("doctype-system");
            format.setDoctype(doctypePublic, doctypeSystem.getValue());
        } catch (ConfigurationException ce) {
            // TODO: how to handle non-existant doctype-system?
        }

        try {
            Configuration indent = conf.getChild("indent");
            format.setIndenting(true);
            format.setIndent(indent.getValueAsInt());
        } catch (ConfigurationException ce) {
            // TODO: how to handle non-existant indent?
        }

        try {
            Configuration preserveSpace = conf.getChild("preserve-space");
            format.setPreserveSpace(preserveSpace.getValueAsBoolean());
        } catch (ConfigurationException ce) {
          // TODO: how to handle non-existant preserve-space?
        }

        try {
            Configuration declaration = conf.getChild("xml-declaration");
            format.setOmitXMLDeclaration(!declaration.getValueAsBoolean());
        } catch (ConfigurationException ce) {
          // TODO: how to handle non-existant xml-declaration?
        }

        try {
            Configuration lineWidth = conf.getChild("line-width");
            format.setLineWidth(lineWidth.getValueAsInt());
        } catch (ConfigurationException ce) {
          // TODO: how to handle non-existant line-width?
        }
    }
}
