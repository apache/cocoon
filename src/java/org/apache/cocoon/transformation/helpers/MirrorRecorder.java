/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.transformation.helpers;

import java.util.ArrayList;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Consume elements start/end and characters events and reproduce them.
 * 
 * WARNING: THIS CLASS DOES NOT WORK PROPERLY WITH NAMESPACES
 *
 * @author <a href="mailto:mattam@netcourrier.com">Matthieu Sozeau</a>
 * @version CVS $Id: MirrorRecorder.java,v 1.3 2003/11/24 18:39:48 joerg Exp $
 */
public class MirrorRecorder
    extends NOPRecorder
    implements EventRecorder, Cloneable {

    private ArrayList events;

    // Used for indexing (parameters)
    static class NullEvent implements EventRecorder {
        private String s;

        public NullEvent(String s) {
            this.s = s;
        }

        public String name() {
            return s;
        }

        public void send(ContentHandler handler)
        throws SAXException {
        }

        public Object clone() {
            return new NullEvent(s);
        }

        public String toString() {
            return "{" + s + "}";
        }
    }

    static class StartEvent implements EventRecorder {
        private String uri, name, raw;
        private Attributes attr;

        public StartEvent(String namespace, String name, String raw,
                          Attributes attr) {
            this.uri = namespace;
            this.name = name;
            this.raw = raw;
            this.attr = new AttributesImpl(attr);
        }

        public void send(ContentHandler handler)
        throws SAXException {
            handler.startElement(uri, name, raw, attr);
        }

        public Object clone() {
            return new StartEvent(uri, name, raw, attr);
        }

        public String toString() {
            StringBuffer str = new StringBuffer("<" + raw);
            if (attr != null) {
                for(int i = 0; i < attr.getLength(); ++i) {
                    str.append(" " + attr.getQName(i) + "=\"" + attr.getValue(i) + "\"");
                }
            }

            return str.append(">").toString();
        }
    }

    static class EndEvent implements EventRecorder {
        private String uri, name, raw;

        public EndEvent(String namespace, String name, String raw) {
            this.uri = namespace;
            this.name = name;
            this.raw = raw;
        }

        public Object clone() {
            return new EndEvent(uri, name, raw);
        }

        public void send(ContentHandler handler)
        throws SAXException {
            handler.endElement(uri, name, raw);
        }

        public String toString() {
            return "</" + raw + ">";
        }
    }

    static class CharacterEvent implements EventRecorder {
        private String ch;

        public CharacterEvent(char ary[], int start, int length) {
            ch = new String(ary, start, length);
        }

        public CharacterEvent(String ch) {
            this.ch = ch;
        }

        public Object clone() {
            return new CharacterEvent(ch);
        }

        public void send(ContentHandler handler)
        throws SAXException {
            handler.characters(ch.toCharArray(), 0, ch.length());
        }

        public String toString() {
            return ch;
        }
    }

    
    public MirrorRecorder() {
        this.events = new ArrayList();
    }

    public MirrorRecorder(Node n) {
        this.events = new ArrayList();

        if(n != null) {
            NodeList childs = n.getChildNodes();
            for(int i = 0; i < childs.getLength(); ++i) {
                try {
                    nodeToEvents(childs.item(i));
                } catch (SAXException e) {
                    // FIXME: what to do?
                }
            }
        }
    }

    private void nodeToEvents(Node n) throws SAXException {
        switch(n.getNodeType()) {
            case Node.ELEMENT_NODE:
                Attributes attrs;
                if(n.getAttributes() instanceof Attributes) {
                    attrs = (Attributes) n.getAttributes();
                } else {
                    NamedNodeMap map = n.getAttributes();
                    attrs = new AttributesImpl();

                    for(int i = 0; i < map.getLength(); ++i) {
                        Node node = map.item(i);
                        final String ns = node.getNamespaceURI() == null? "" : node.getNamespaceURI();
                        final String ln = node.getLocalName() == null? node.getNodeName() : node.getLocalName();
                        ((AttributesImpl) attrs).addAttribute(ns,
                                                              ln,
                                                              node.getNodeName(),
                                                              "CDATA",
                                                              node.getNodeValue());
                    }
                }

                final String ns = n.getNamespaceURI() == null? "" : n.getNamespaceURI();
                final String ln = n.getLocalName() == null? n.getNodeName() : n.getLocalName();
                startElement(ns, ln, n.getNodeName(), attrs);
                if (n.hasChildNodes()) {
                    NodeList childs = n.getChildNodes();
                    for(int i = 0; i < childs.getLength(); ++i) {
                        nodeToEvents(childs.item(i));
                    }
                }

                endElement(ns, ln, n.getNodeName());
                break;
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                characters(n.getNodeValue());
                break;
        }
    }

    public MirrorRecorder(MirrorRecorder n) {
        this.events = new ArrayList();
        for(int i = 0; i < n.events.size(); ++i) {
            EventRecorder e = (EventRecorder) n.events.get(i);
            this.events.add(e.clone());
        }
    }

    public Object clone() {
        return new MirrorRecorder(this);
    }

    public void startElement(String namespace, String name, String raw,
                             Attributes attr)
            throws SAXException {
        events.add(new StartEvent(namespace, name, raw, attr));
    }

    public void endElement(String namespace, String name, String raw)
            throws SAXException {
        events.add(new EndEvent(namespace, name, raw));
    }

    public void characters(char ary[], int start, int length)
            throws SAXException {
        characters(new String(ary, start, length));
    }

    public void characters(String tmp) throws SAXException {
        int i = 0, j = 0;

        while(tmp.length() > 0) {
            i = tmp.indexOf('{', i);
            if(i == -1) {
                events.add(new CharacterEvent(tmp));
                return;
            } else {
                if(i >= 0) {
                    events.add(new CharacterEvent(tmp.substring(0, i)));
                }

                j = tmp.indexOf('}', i);
                if(j != -1) {
                    events.add(new NullEvent(tmp.substring(i + 1, j)));
                    tmp = tmp.substring(j + 1, tmp.length());
                    i = 0;
                }
            }
        }
    }

    public void send(ContentHandler handler) throws SAXException {
        for(int i = 0; i < events.size(); ++i) {
            ((EventRecorder) (events.get(i))).send(handler);
        }
    }

    public void send(ContentHandler handler, Map params) throws SAXException
    {
        EventRecorder param;

        for(int i = 0; i < events.size(); ++i) {
            if(events.get(i) instanceof NullEvent) {
                param = (EventRecorder) params.get(((NullEvent) events.get(i)).name());
                if(param != null)
                    param.send(handler);
            } else {
                ((EventRecorder) (events.get(i))).send(handler);
            }
        }
    }

    public String text() {
        StringBuffer s = new StringBuffer();

        for(int i = 0; i < events.size(); ++i) {
            s.append(events.get(i).toString());
        }
        return(s.toString());
    }

    public String toString() {
        StringBuffer s = new StringBuffer("MirrorRecorder: ");
        s.append(String.valueOf(events.size()) + " event(s)");
        s.append("\ntext: ");
        for(int i = 0; i < events.size(); ++i) {
            if(events.get(i) instanceof CharacterEvent) {
                s.append(events.get(i).toString());
            }
        }

        return s.toString();
    }

    public void recycle() {
        events.clear();
    }

    public boolean empty() {
        return events.size() == 0;
    }
}
