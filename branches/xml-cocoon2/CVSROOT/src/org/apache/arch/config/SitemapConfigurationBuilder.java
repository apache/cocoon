/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.arch.config;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.xml.sax.Attributes;
import org.xml.sax.AttributeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This utility class will create a <code>Configuration</code> tree from an
 * XML file parsed with a SAX version 1 or 2 parser.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-07-06 04:57:42 $
 */
public class SitemapConfigurationBuilder {

    /**
     * Return a new Configuration object.
     */
    public Configuration newConfiguration(String name) {
        return(new ConfigurationImpl (name));
    }
 
    /** 
     * Add an attribute to this configuration element, returning its old 
     * value or <b>null</b>. 
     */ 
    public void addAttribute(String name, String value, Configuration conf) { 
        ((ConfigurationImpl)conf).addAttribute(name,value); 
    } 
 
    /** 
     * Add an attribute to this configuration element, returning its old 
     * value or <b>null</b>. 
     */ 
    public void addConfiguration(Configuration child, Configuration conf) { 
        ((ConfigurationImpl)conf).addConfiguration(child); 
    } 
}
