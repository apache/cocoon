/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sax;

import java.io.OutputStream;
import java.io.PrintStream;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * The <code>XMLDebugger</code> is a <code>XMLConsumer</code> implementation
 * used for debugging purposes.
 * <br>
 * This object dumps all SAX events received to an <code>OutputStream</code>
 * specified at construction time.
 * <br>
 * NOTE: (PF) Should we make a full blown serializer out of this one?
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-10 13:20:30 $
 * @since Cocoon 2.0
 */
public class XMLDebugger implements XMLConsumer {
    /** The PrintStream used for output */
    private PrintStream out=null;
    /** The current locator */
    private Locator loc=null;

    /**
     * Construct a new <code>XMLDebugger</code> instance.
     */
    public XMLDebugger(OutputStream out) {
        this.out=new PrintStream(out);
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     *
     * @param locator An object that can return the location of any SAX
     *                document event.
     */
    public void setDocumentLocator(Locator locator) {
        this.loc=locator;
        this.location();
        this.out.println("setDocumentLocator");
        this.out.flush();
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument() {
        this.location();
        this.out.println("startDocument");
        this.out.flush();
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument() {
        this.location();
        this.out.println("endDocument");
        this.out.flush();
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     */
    public void startPrefixMapping(String prefix, String uri) {
        this.location();
        String p=((prefix==null)?"(null)":"\""+prefix+"\"");
        String u=((uri==null)?"(null)":"\""+uri+"\"");
        this.out.println("startPrefixMapping prefix="+p+" uri="+u);
        this.out.flush();
    }

    /**
     * End the scope of a prefix-URI mapping.
     *
     * @param prefix The prefix that was being mapping.
     */
    public void endPrefixMapping(String prefix) {
        this.location();
        String p=((prefix==null)?"(null)":"\""+prefix+"\"");
        this.out.println("endPrefixMapping prefix="+p);
        this.out.flush();
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     * @param a The attributes attached to the element. If there are no
     *          attributes, it shall be an empty Attributes object.
     */
    public void startElement(String uri, String loc, String raw, Attributes a) {
        this.location();
        String u=((uri==null)?"(null)":"\""+uri+"\"");
        String l=((loc==null)?"(null)":"\""+loc+"\"");
        String r=((raw==null)?"(null)":"\""+raw+"\"");
        this.out.println("startElement uri="+u+" loc="+l+" raw="+r);
        this.out.flush();
        for(int x=0; x<a.getLength(); x++) {
            String au=a.getURI(x);
            String al=a.getLocalName(x);
            String ar=a.getRawName(x);
            String at=a.getType(x);
            String av=a.getValue(x);
            au=((au==null)?"(null)":"\""+au+"\"");
            al=((al==null)?"(null)":"\""+al+"\"");
            ar=((ar==null)?"(null)":"\""+ar+"\"");
            at=((at==null)?"(null)":"\""+at+"\"");
            av=((av==null)?"(null)":"\""+av+"\"");
            this.out.println("    attribute uri="+au+" loc="+al+" raw="+ar+
                                          " typ="+at+" val="+av);
        }
    }
        

    /**
     * Receive notification of the end of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     */
    public void endElement (String uri, String loc, String raw) {
        this.location();
        String u=((uri==null)?"(null)":"\""+uri+"\"");
        String l=((loc==null)?"(null)":"\""+loc+"\"");
        String r=((raw==null)?"(null)":"\""+raw+"\"");
        this.out.println("endElement uri="+u+" loc="+l+" raw="+r);
        this.out.flush();
    }

    /**
     * Receive notification of character data.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void characters (char ch[], int start, int len) {
        this.location();
        this.out.println("characters ["+new String(ch,start,len)+"]");
        this.out.flush();
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void ignorableWhitespace (char ch[], int start, int len) {
        this.location();
        this.out.println("ignorableWhitespace ["+new String(ch,start,len)+"]");
        this.out.flush();
    }

    /**
     * Receive notification of a processing instruction.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if none was
     *             supplied.
     */
    public void processingInstruction (String target, String data) {
        this.location();
        String t=((target==null)?"(null)":"\""+target+"\"");
        String d=((data==null)?"(null)":"\""+data+"\"");
        this.out.println("processingInstruction target="+t+" data="+d);
        this.out.flush();
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity.  If it is a  parameter
     *             entity, the name will begin with '%'.
     */
    public void skippedEntity (String name) {
        this.location();
        String n=((name==null)?"(null)":"\""+name+"\"");
        this.out.println("skippedEntity name="+n);
        this.out.flush();
    }        

    /**
     * Report the start of DTD declarations, if any.
     *
     * @param name The document type name.
     * @param publicId The declared public identifier for the external DTD
     *                 subset, or null if none was declared.
     * @param systemId The declared system identifier for the external DTD
     *                 subset, or null if none was declared.
     */
    public void startDTD (String name, String publicId, String systemId) {
        this.location();
        String n=((name==null)?"(null)":"\""+name+"\"");
        String p=((publicId==null)?"(null)":"\""+publicId+"\"");
        String s=((systemId==null)?"(null)":"\""+systemId+"\"");
        this.out.println("startDTD name="+n+" publicId="+p+" systemId="+s);
        this.out.flush();
    }        
    
    /**
     * Report the end of DTD declarations.
     */
    public void endDTD () {
        this.location();
        this.out.println("endDTD");
        this.out.flush();
    }

    /**
     * Report the beginning of an entity.
     *
     * @param name The name of the entity. If it is a parameter entity, the
     *             name will begin with '%'.
     */
    public void startEntity (String name) {
        this.location();
        String n=((name==null)?"(null)":"\""+name+"\"");
        this.out.println("startEntity name="+n);
        this.out.flush();
    }        

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     */
    public void endEntity (String name) {
        this.location();
        String n=((name==null)?"(null)":"\""+name+"\"");
        this.out.println("endEntity name="+n);
        this.out.flush();
    }        

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA () {
        this.location();
        this.out.println("startCDATA");
        this.out.flush();
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA () {
        this.location();
        this.out.println("endCDATA");
        this.out.flush();
    }
    

    /**
     * Report an XML comment anywhere in the document.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param len The number of characters to use from the array.
     */
    public void comment (char ch[], int start, int len) {
        this.location();
        this.out.println("comment ["+new String(ch,start,len)+"]");
        this.out.flush();
    }

    /** Report the current location */
    private void location() {
        /**
        if(this.loc==null) {
            this.out.print("[NO LOCATION INFORMATION] ");
        } else try {
            String sys=this.loc.getSystemId();
            String pub=this.loc.getPublicId();
            this.out.print("[SystemID="+((sys==null)?"(null)":"\""+sys+"\""));
            this.out.print(" PublicID="+((pub==null)?"(null)":"\""+sys+"\""));
            this.out.print(" Line="+this.loc.getLineNumber());
            this.out.print(" Col.="+this.loc.getColumnNumber()+"] ");
        } catch (Exception e) {
            this.out.print("[EXCEPTION RETRIEVING LOCATION]");
        }*/
    }
}
