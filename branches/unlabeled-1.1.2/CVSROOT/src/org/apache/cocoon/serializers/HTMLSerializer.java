/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.serializers;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import org.apache.cocoon.framework.ConfigurationException;
import org.apache.cocoon.framework.Configurations;
import org.apache.cocoon.sitemap.Request;
import org.apache.cocoon.sitemap.Response;
import org.apache.cocoon.sax.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-11 15:02:03 $
 * @since Cocoon 2.0
 */
public class HTMLSerializer extends AbstractSerializer implements XMLConsumer {
    /** The PrintStream used for output */
    private OutputStreamWriter out=null;
    /** The current locator */
    private Locator loc=null;
    /** The namespaces URI-PREFIX table */
    private Hashtable namespacesUriPrefix=null;
    /** The namespaces PREFIX-URI reversed table */
    private Hashtable namespacesPrefixUri=null;
    /** A flag representing an open element (for "/>" or ">") */
    private boolean openElement=false;
    /** A flag telling wether we're processing the DTD */
    private boolean dtd=false;

    /**
     * Return a new instance of this <code>XMLSerializer</code>.
     */
    public XMLConsumer getXMLConsumer(Request req, Response res, OutputStream out)
    throws IOException {
        String c=this.configurations.getParameter("contentType","text/html");
        String e=this.configurations.getParameter("encoding","UTF-8");
        res.setContentType(c);
        HTMLSerializer s=new HTMLSerializer();
        s.out=new OutputStreamWriter(new BufferedOutputStream(out),e);
        s.namespacesUriPrefix=new Hashtable();
        s.namespacesPrefixUri=new Hashtable();
        return(s);
    }

    /**
     * Configure this <code>XMLSerializer</code>.
     * <br>
     * By default this method only store configurations.
     */
    public void configure(Configurations conf)
    throws ConfigurationException {
        super.configure(conf);
        String c=this.configurations.getParameter("contentType","text/html");
        if((!c.equals("text/html"))&&(!c.equals("text/plain")))
            throw new ConfigurationException("Unsupported contentType '"+c+"'");
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     *
     * @param locator An object that can return the location of any SAX
     *                document event.
     */
    public void setDocumentLocator(Locator locator) {
        this.loc=locator;
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument()
    throws SAXException {
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument()
    throws SAXException {
        try {
            this.out.flush();
        } catch (IOException e) {
            throw new SAXException("IOException flushing stream",e);
        }
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     *
     * @param p The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     */
    public void startPrefixMapping(String p, String uri)
    throws SAXException {
        if (this.namespacesUriPrefix.put(uri,p)!=null)
            throw new SAXException("Namespace URI '"+uri+"' already declared");
        if (this.namespacesPrefixUri.put(p,uri)!=null)
            throw new SAXException("Namespace prefix '"+p+"' already declared");
    }

    /**
     * End the scope of a prefix-URI mapping.
     *
     * @param p The prefix that was being mapping.
     */
    public void endPrefixMapping(String p)
    throws SAXException {
        String uri=(String)this.namespacesPrefixUri.remove(p);
        if (uri==null)
            throw new SAXException("Namespace prefix '"+p+"' never declared");
        if (this.namespacesUriPrefix.remove(uri)==null)
            throw new SAXException("Namespace URI '"+uri+"' never declared");
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
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        this.print('<');

        this.print(this.qualify(uri,loc,raw).toUpperCase());
        for (int x=0; x<a.getLength(); x++) {
            String name=this.qualify(a.getURI(x),a.getLocalName(x),
                                     a.getRawName(x)).toLowerCase();
            String value=a.getValue(x);
            if ((name.equals("checked"))  || (name.equals("compact"))  ||
                (name.equals("declare"))  || (name.equals("defer"))    ||
                (name.equals("disabled")) || (name.equals("ismap"))    ||
                (name.equals("multiple")) || (name.equals("nohref"))   ||
                (name.equals("noresize")) || (name.equals("noshade"))  ||
                (name.equals("nowrap"))   || (name.equals("readonly")) ||
                (name.equals("selected"))) {
                if(name.equals(value)) {
                    this.print(' ');
                    this.print(name);
                }
            } else {
                this.print(' ');
                this.print(name);
                this.print('=');
                this.print('\"');
                this.print(value);
                this.print('\"');
            }
        }
        this.print('>');
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
    public void endElement (String uri, String loc, String raw)
    throws SAXException {
        String name=this.qualify(uri,loc,raw).toUpperCase();
        if ((name.equals("AREA"))     || (name.equals("BASE"))     ||        
            (name.equals("BASEFONT")) || (name.equals("BR"))       ||          
            (name.equals("COL"))      || (name.equals("FRAME"))    ||       
            (name.equals("HR"))       || (name.equals("IMG"))      ||         
            (name.equals("INPUT"))    || (name.equals("ISINDEX"))  ||     
            (name.equals("LINK"))     || (name.equals("META"))     ||        
            (name.equals("PARA"))) return;
        this.print('<');
        this.print('/');
        this.print(name);
        this.print('>');
    }

    /**
     * Receive notification of character data.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void characters (char ch[], int start, int len)
    throws SAXException {
        this.printSafe(ch,start,len);
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void ignorableWhitespace (char ch[], int start, int len)
    throws SAXException {
        this.printSafe(ch,start,len);
    }

    /**
     * Receive notification of a processing instruction.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if none was
     *             supplied.
     */
    public void processingInstruction (String target, String data)
    throws SAXException {
        this.print("<!-- Processing Instruction: Target=\"");
        this.print(target);
        this.print("\" Data=\"");
        this.print(data);
        this.print("\" -->");
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity.  If it is a  parameter
     *             entity, the name will begin with '%'.
     */
    public void skippedEntity (String name)
    throws SAXException {
        this.print('&');
        this.print(name);
        this.print(';');
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
    public void startDTD (String name, String publicId, String systemId)
    throws SAXException {
        //<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN"
        //                      "http://www.w3.org/TR/REC-html40/loose.dtd">
        // Set the DTD flag now, to avoid output
        this.dtd=true;
    }        
    
    /**
     * Report the end of DTD declarations.
     */
    public void endDTD ()
    throws SAXException {
        this.dtd=false;
    }

    /**
     * Report the beginning of an entity.
     *
     * @param name The name of the entity. If it is a parameter entity, the
     *             name will begin with '%'.
     */
    public void startEntity (String name)
    throws SAXException {
    }        

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     */
    public void endEntity (String name)
    throws SAXException {
    }        

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA ()
    throws SAXException {
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA ()
    throws SAXException {
    }
    

    /**
     * Report an XML comment anywhere in the document.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param len The number of characters to use from the array.
     */
    public void comment (char ch[], int start, int len)
    throws SAXException {
        this.print("<!--");
        this.print(ch,start,len);
        this.print("-->");
    }

    /** Print a string */
    private void print(String s)
    throws SAXException {
        char data[]=s.toCharArray();
        this.print(data,0,data.length);
    }
    
    /** Print data from a character array */
    private void print(char data[], int start, int len)
    throws SAXException {
        try {
            if(!this.dtd) this.out.write(data,start,len);
        } catch (IOException e) {
            throw new SAXException("IOException printing data",e);
        }
    }

    /** Print data from a character array */
    private void print(char c)
    throws SAXException {
        try {
            if(!this.dtd) this.out.write(c);
        } catch (IOException e) {
            throw new SAXException("IOException printing data",e);
        }
    }

    /** Print a string */
    private void printSafe(String s)
    throws SAXException {
        char data[]=s.toCharArray();
        this.printSafe(data,0,data.length);
    }
    
    /** Print data from a character array */
    private void printSafe(char data[], int start, int len)
    throws SAXException {
        int end=start+len;
        if(!this.dtd) for(int x=0; x<end; x++) this.printSafe(data[x]);
    }

    /** Print data from a character array */
    private void printSafe(char c)
    throws SAXException {
        try {
            if(this.dtd) return;
            if(c=='&') this.print("&amp;");
            else if(c=='<') this.print("&lt;");
            else if(c=='>') this.print("&gt;");
            else this.out.write(c);
        } catch (IOException e) {
            throw new SAXException("IOException printing data",e);
        }
    }

    /** Return the fully namespace qualified name */
    private String qualify(String uri, String loc, String raw)
    throws SAXException {
        if(uri.length()>0) {
            String pre=(String)this.namespacesUriPrefix.get(uri);
            if (pre==null) throw new SAXException("No prefix declared for "+
                                                  "namespace uri '"+uri+"'");
            if (pre.length()>0) return(pre+":"+loc);
            else return(loc);
        } else if(raw.length()>0) {
            return(raw);
        } else if(loc.length()>0) {
            return(loc);
        }
        throw new SAXException("Cannot qualify namespaced name");
    }
}
