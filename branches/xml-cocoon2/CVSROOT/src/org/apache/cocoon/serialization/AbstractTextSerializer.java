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

import org.apache.cocoon.caching.Cacheable;
import org.apache.cocoon.caching.CacheValidity;
import org.apache.cocoon.caching.NOPCacheValidity;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.apache.cocoon.util.TraxErrorHandler;

/**
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.12 $ $Date: 2001-04-23 17:11:45 $
 */
public abstract class AbstractTextSerializer extends AbstractSerializer implements Configurable, Cacheable {

    /**
     * The trax <code>TransformerFactory</code> used by this serializer.
     */
    private SAXTransformerFactory tfactory = null;

    /**
     * The <code>Properties</code> used by this serializer.
     */
    protected Properties format = new Properties();

    /**
     * Helper for TransformerFactory.
     */
    protected synchronized SAXTransformerFactory getTransformerFactory()
    {
        if(tfactory == null)  {
            tfactory = (SAXTransformerFactory) TransformerFactory.newInstance();
            tfactory.setErrorListener(new TraxErrorHandler(getLogger()));
        }
        return tfactory;
    }

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
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     * This method must be invoked before the generateValidity() method.
     *
     * @return The generated key or <code>0</code> if the component
     *              is currently not cacheable.
     */
    public long generateKey() {
        return 1;
    }

    /**
     * Generate the validity object.
     * Before this method can be invoked the generateKey() method
     * must be invoked.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public CacheValidity generateValidity() {
        return new NOPCacheValidity();
    }

    /**
     * Recycle serializer by removing references
     */
    public void recycle() {
        super.recycle();
    }
}
