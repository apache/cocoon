/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.serialization;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.NamespacesTable;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-07-22 20:41:52 $
 */
public class HTMLSerializer extends AbstractSerializer implements XMLConsumer {
    /** The namespaces table */
    private NamespacesTable ns=new NamespacesTable();
    /** The PrintStream used for output */
    private OutputStreamWriter out=null;
    /** The current locator */
    private Locator loc=null;
    /** A flag telling wether we're processing the DTD */
    private boolean dtd=false;
    /** A flag telling wether we're processing a CDATA section */
    private boolean cdata=false;

    /**
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        super.setOutputStream(out);
        this.out=new OutputStreamWriter(super.output);;
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
        String c=super.parameters.getParameter("contentType","text/html");
        ((HttpEnvironment)(super.environment)).getResponse().setContentType(c);
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
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        this.ns.addDeclaration(prefix,uri);
    }

    /**
     * End the scope of a prefix-URI mapping.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        this.ns.removeDeclaration(prefix);
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        this.print('<');

        this.print(this.ns.resolve(uri,raw,null,loc).getQName());
        for (int x=0; x<a.getLength(); x++) {
            String name=this.ns.resolve(a.getURI(x),a.getQName(x),null,
                                        a.getLocalName(x)).getQName();
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
                } else {
                    this.print(' ');
                    this.print(name);
                    this.print('=');
                    this.print('\"');
                    this.print(value);
                    this.print('\"');
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
        String name=this.ns.resolve(uri,raw,null,loc).getQName();
        if ((name.equalsIgnoreCase("AREA"))     || (name.equalsIgnoreCase("BASE"))     ||        
            (name.equalsIgnoreCase("BASEFONT")) || (name.equalsIgnoreCase("BR"))       ||          
            (name.equalsIgnoreCase("COL"))      || (name.equalsIgnoreCase("FRAME"))    ||       
            (name.equalsIgnoreCase("HR"))       || (name.equalsIgnoreCase("IMG"))      ||         
            (name.equalsIgnoreCase("INPUT"))    || (name.equalsIgnoreCase("ISINDEX"))  ||     
            (name.equalsIgnoreCase("LINK"))     || (name.equalsIgnoreCase("META"))     ||        
            (name.equalsIgnoreCase("PARA"))) return;
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
        if(this.cdata) this.print(ch,start,len);
        else this.printSafe(ch,start,len);
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
        if(this.cdata) this.print(ch,start,len);
        else this.printSafe(ch,start,len);
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
        this.print("<?");
        this.print(target);
        this.print(' ');
        this.print(data);
        this.print("?>");
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
        this.print("<!DOCTYPE ");
        this.print(name);
        if (publicId!=null) {
            this.print(" PUBLIC \"");
            this.print(publicId);
            this.print('\"');
            if (systemId!=null) {
                this.print(' ');
                this.print('\"');
                this.print(systemId);
                this.print('\"');
            }
        } else if (systemId!=null) {
            this.print(" SYSTEM \"");
            this.print(systemId);
            this.print('\"');
        }
        this.print('>');
        this.print('\n');
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
        this.print("<![CDATA[");
        this.cdata=true;
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA ()
    throws SAXException {
        this.cdata=false;
        this.print("]]>");
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
}
