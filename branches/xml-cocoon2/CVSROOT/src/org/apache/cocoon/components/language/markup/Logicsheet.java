/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.DTDHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Templates;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.TransformerConfigurationException;
import org.apache.xalan.transformer.TrAXFilter;

/**
 * A code-generation logicsheet. This class is actually a wrapper for
 * a "standard" XSLT stylesheet stored as <code>trax.Templates</code> object.
 * Though this will change shortly: a new markup language will be used
 * for logicsheet authoring; logicsheets written in this language will be
 * transformed into an equivalent XSLT stylesheet anyway...
 * This class should probably be based on an interface...
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2000-11-08 20:35:07 $
 */
public class Logicsheet {
    /**
    * The trax templates
    */
    protected Templates templates;

    /**
    * The constructor. It does preserve the namespace from the stylesheet.
    *
    * @param inputSource The stylesheet's input source
    * @exception IOException IOError processing input source
    * @exception SAXException Input source parse error
    */
    public void setInputSource(InputSource inputSource)
        throws SAXException, IOException
    {
        try {
            TransformerFactory tfactory = TransformerFactory.newInstance();
            templates = tfactory.newTemplates(new SAXSource(inputSource));
        } catch (TransformerConfigurationException e){
            e.printStackTrace();
        }
    }

    /**
    * Get the XMLFilter that performs the stylesheet transformation.
    *
    * @return The XMLFilter for the associated stylesheet.
    */
    public XMLFilter getXMLFilter() 
    {
        XMLFilter filter = null;
        try {
            filter = new TrAXFilter(templates);
        } catch (TransformerConfigurationException e){
            e.printStackTrace();
        }
        return filter;
    }
}

