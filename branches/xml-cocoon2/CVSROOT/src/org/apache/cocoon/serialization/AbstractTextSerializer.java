/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.serialization;

import java.util.Properties;

import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.SAXTransformerFactory;

/**
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.9 $ $Date: 2001-03-15 22:08:50 $
 */
public abstract class AbstractTextSerializer extends AbstractSerializer implements Configurable {

    /**
     * The trax <code>TransformerFactory</code> used by this serializer.
     */
    protected SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();

    /**
     * The <code>Properties</code> used by this serializer.
     */
    protected Properties format = new Properties();

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf)
      throws ConfigurationException {

        Configuration cdataSectionElements = conf.getChild("cdata-section-elements");
        Configuration dtPublic = conf.getChild("doctype-public");
        Configuration dtSystem = conf.getChild("doctype-system");
        Configuration encoding = conf.getChild("encoding");
        Configuration indent = conf.getChild("indent");
        Configuration mediaType = conf.getChild("media-type");
        Configuration method = conf.getChild("method");
        Configuration omitXMLDeclaration = conf.getChild("omit-xml-declaration");
        Configuration standAlone = conf.getChild("standalone");
        Configuration version = conf.getChild("version");

        if (! cdataSectionElements.getLocation().equals("-")) {
            format.put(OutputKeys.CDATA_SECTION_ELEMENTS,cdataSectionElements.getValue());
        }
        if (! dtPublic.getLocation().equals("-")) {
            format.put(OutputKeys.DOCTYPE_PUBLIC,dtPublic.getValue());
        }
        if (! dtSystem.getLocation().equals("-")) {
            format.put(OutputKeys.DOCTYPE_SYSTEM,dtSystem.getValue());
        }
        if (! encoding.getLocation().equals("-")) {
            format.put(OutputKeys.ENCODING,encoding.getValue());
        }
        if (! indent.getLocation().equals("-")) {
            format.put(OutputKeys.INDENT,indent.getValue());
        }
        if (! mediaType.getLocation().equals("-")) {
            format.put(OutputKeys.MEDIA_TYPE,mediaType.getValue());
        }
        if (! method.getLocation().equals("-")) {
            format.put(OutputKeys.METHOD,method.getValue());
        }
        if (! omitXMLDeclaration.getLocation().equals("-")) {
            format.put(OutputKeys.OMIT_XML_DECLARATION,omitXMLDeclaration.getValue());
        }
        if (! standAlone.getLocation().equals("-")) {
            format.put(OutputKeys.STANDALONE,standAlone.getValue());
        }
        if (! version.getLocation().equals("-")) {
            format.put(OutputKeys.VERSION,version.getValue());
        }
    }

    /**
     * Recycle serializer by removing references
     */
    public void recycle() {
        this.format = new Properties();
        super.recycle();
    }
}
