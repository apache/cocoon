/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.garbage;

import java.util.Iterator;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.garbage.serializer.Serializer;
import org.apache.garbage.tree.Event;
import org.apache.garbage.tree.Events;
import org.apache.garbage.tree.Runtime;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: Processor.java,v 1.2 2004/03/05 10:07:22 bdelacretaz Exp $
 */
public class Processor implements Runtime {

    private ContentHandler content = null;
    private LexicalHandler lexical = null;
    private Stack prefixes = null;
    private Stack elements = null;


    public Processor(Serializer serializer) {
      this(serializer, serializer);
    }

    public Processor(ContentHandler content, LexicalHandler lexical) {
        super();
        this.content = content;
        this.lexical = lexical;
        if (content == null) {
            throw new NullPointerException("No ContentHandler specifed");
        }
    }

    public void process(Events events, JXPathContext context)
    throws SAXException {
        this.prefixes = new Stack();
        this.elements = new Stack();
        String k[][] = { { "", ""} };
        this.prefixes.push(k);

        Iterator iterator = events.iterator();
        content.startDocument();
        while(iterator.hasNext()) ((Event)iterator.next()).process(this, context);
        content.endDocument();
    }

    /**
     * Receive notification of a <code>DocType</code> event.
     *
     * @see org.apache.garbage.tree.DocType
     */
    public void doctype(String name, String public_id, String system_id)
    throws SAXException {
        if (lexical == null) return;
        lexical.startDTD(name, public_id, system_id);
        lexical.endDTD();
    }

    /**
     * Receive notification of a <code>CData</code> event.
     *
     * @see org.apache.garbage.tree.CData
     */
    public void cdata(char data[])
    throws SAXException {
        if (lexical != null) {
            lexical.startCDATA();
            content.characters(data, 0, data.length);
            lexical.endCDATA();
            return;
        }
        content.characters(data, 0, data.length);
    }

    /**
     * Receive notification of a <code>Characters</code> event.
     *
     * @see org.apache.garbage.tree.Characters
     */
    public void characters(char data[])
    throws SAXException {
        content.characters(data, 0, data.length);
    }

    /**
     * Receive notification of a <code>Comment</code> event.
     *
     * @see org.apache.garbage.tree.Comment
     */
    public void comment(char data[])
    throws SAXException {
        if (lexical != null) lexical.comment(data, 0, data.length);
    }

    /**
     * Receive notification of a <code>ProcessingInstruction</code> event.
     *
     * @see org.apache.garbage.tree.ProcessingInstruction
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        content.processingInstruction(target, data);
    }

    /**
     * Receive notification of a <code>ElementStart</code> event.
     *
     * @param prefix The namespace prefix of the of the element.
     * @param local The local name (without prefix) of the element.
     * @param attributes All attributes associated with this element.
     * @param namespaces All namespaces declared by this element.
     * @see org.apache.garbage.tree.ElementStart
     */
    public void startElement(String prefix, String local, String qualified,
                             String attributes[][], String namespaces[][])
    throws SAXException {
        this.prefixes.push(namespaces);
        String uri = this.resolve(prefix);

        AttributesImpl a = new AttributesImpl();
        for (int x = 0; x < attributes.length; x++) {
            String apfx = attributes[x][0];
            String aloc = attributes[x][1];
            String anam = attributes[x][2];
            String aval = attributes[x][3];
            String auri = (apfx.length() > 0? resolve(apfx): "");

            if (a.getValue(auri, aloc) != null) {
                throw new SAXException("Duplicate attribute \""
                                       + prefix + "\"");
            }
            a.addAttribute(auri, aloc, anam, "CDATA", aval);
        }

        for (int x = 0; x < namespaces.length; x++) {
            content.startPrefixMapping(namespaces[x][0], namespaces[x][1]);
        }
        content.startElement(uri, local, qualified, a);
        this.elements.push(uri + '\0' + local);
    }

    /**
     * Receive notification of a <code>ElementEnd</code> event.
     *
     * @param prefix The namespace prefix of the of the element.
     * @param local The local name (without prefix) of the element.
     * @see org.apache.garbage.tree.ElementEnd
     */
    public void endElement(String prefix, String local, String qualified)
    throws SAXException {
        String uri = this.resolve(prefix);
        String name = (uri + '\0' + local);
        if (! name.equals(this.elements.pop())) {
            throw new SAXException("Expecting another element");
        }
        content.endElement(uri, local, qualified);
        String namespaces[][] = (String [][])this.prefixes.pop();
        for (int x = 0; x < namespaces.length; x++) {
            content.endPrefixMapping(namespaces[x][0]);
        }
    }

    private String resolve(String prefix)
    throws SAXException {
        for (int x = this.prefixes.size() - 1; x >= 0; x--) {
            String nsdecl[][] = (String [][])this.prefixes.get(x);
            for (int y = 0; y < nsdecl.length; y++) {
                if (prefix.equals(nsdecl[y][0])) return(nsdecl[y][1]);
            }
        }
        throw new SAXException("Cannot resolve namespace URI for prefix \""
                               + prefix + "\"");
    }

    public static final class Stack extends java.util.ArrayList {
        private Stack() {
            super();
        }

        private void push(Object o) {
            if (o == null) throw new NullPointerException();
            this.add(o);
        }

        private Object pop() {
            if (this.size() == 0) return(null);
            return(this.remove(this.size() - 1));
        }
    }
}
