/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.xml.xlink;

import org.xml.sax.SAXException;

/**
 * This interface indicates an XLinkHandler that uses the same 
 * event driven design patterns that SAX enforces.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-08-23 22:44:30 $
 */
public interface XLinkHandler  {
    
    public void simpleLink(String href, String role, String arcrole, String title, String show, String actuate) throws SAXException;
    
    public void startExtendedLink(String role, String title) throws SAXException;
    
    public void endExtendedLink() throws SAXException;
    
    public void startLocator(String href, String role, String title, String label) throws SAXException;

    public void endLocator() throws SAXException;
    
    public void startArc(String arcrole, String title, String show, String actuate, String from, String to) throws SAXException;
    
    public void endArc() throws SAXException;

    public void linkResource(String role, String title, String label) throws SAXException;
    
    public void linkTitle() throws SAXException;
    
}

