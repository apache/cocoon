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
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-02-12 14:17:39 $
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

        Configuration encoding = conf.getChild("encoding");
        Configuration dtPublic = conf.getChild("doctype-public");
        Configuration dtSystem = conf.getChild("doctype-system");
        Configuration indent = conf.getChild("indent");
        Configuration preserveSpace = conf.getChild("preserve-space");
        Configuration declaration = conf.getChild("xml-declaration");
        Configuration lineWidth = conf.getChild("line-width");

        String doctypePublic = null;

        format = new OutputFormat();
        format.setPreserveSpace(true);

        if (! encoding.getLocation().equals("-")) {
            try {
                format.setEncoding(encoding.getValue());
            } catch (ConfigurationException ce) {
                getLogger().debug("No value for encoding--but expected", ce);
            }
        }

        if (! dtPublic.getLocation().equals("-")) {
            try {
                doctypePublic = dtPublic.getValue();
            } catch (ConfigurationException ce) {
                getLogger().debug("No Public Doctype--but expected", ce);
            }
        }

        if (! dtSystem.getLocation().equals("-")) {
            try {
                format.setDoctype(doctypePublic, dtSystem.getValue());
            } catch (ConfigurationException ce) {
                getLogger().debug("No System Doctype--but expected", ce);
            }
        }

        if (! indent.getLocation().equals("-")) {
            format.setIndenting(true);
            try {
                format.setIndent(indent.getValueAsInt());
            } catch (ConfigurationException ce) {
                getLogger().debug("No indent value or invalid value--but expected", ce);
            }
        }

        if (! preserveSpace.getLocation().equals("-")) {
            try {
                format.setPreserveSpace(preserveSpace.getValueAsBoolean());
            } catch (ConfigurationException ce) {
                getLogger().debug("No preserve-space value--but expected", ce);
            }
        }

        if (! declaration.getLocation().equals("-")) {
            try {
                format.setOmitXMLDeclaration(!declaration.getValueAsBoolean());
            } catch (ConfigurationException ce) {
                getLogger().debug("No declaration value or invalid value--but expected", ce);
            }
        }


        if (! lineWidth.getLocation().equals("-")) {
            try {
                format.setLineWidth(lineWidth.getValueAsInt());
            } catch (ConfigurationException ce) {
                getLogger().debug("No line-width value or invalid value--but expected", ce);
            }
        }
    }
}
