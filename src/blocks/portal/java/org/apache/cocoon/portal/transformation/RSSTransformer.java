/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.portal.transformation;

import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.w3c.tidy.Tidy;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer records the content of all description elements
 * and tries to interpret them as valid XML.
 * It's actually a quick hack...
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: RSSTransformer.java,v 1.1 2003/07/11 14:17:02 cziegeler Exp $
 */
public final class RSSTransformer
extends AbstractSAXTransformer {

    /**
     *  receive notification of start element event.
     **/
    public void startElement(String uri, String name, String raw, Attributes attributes)
    throws SAXException {
        super.startElement(uri,name,raw,attributes);
        if ("description".equals(name)) {
            this.startTextRecording();
        }
    }

    /**
     * receive notification of end element event.
     */
    public void endElement(String uri,String name,String raw)
    throws SAXException  {
        if ("description".equals(name)) {
            final String text = this.endTextRecording();
            final String html = "<html><body>"+text+"</body></html>";

            try {
                final Tidy xhtmlconvert = new Tidy();
                xhtmlconvert.setXmlOut(true);
                xhtmlconvert.setXHTML(true);
                xhtmlconvert.setShowWarnings(false);
                org.w3c.dom.Document doc = xhtmlconvert.parseDOM(new java.io.ByteArrayInputStream(html.getBytes()), null);
                org.w3c.dom.NodeList node = org.apache.cocoon.xml.dom.DOMUtil.selectNodeList(doc, "/html/body/*");
                if (null != node) {
                    for(int i = 0; i < node.getLength(); i++) {
                        this.sendEvents(node.item(i));
                    }
                } else {
                    this.sendTextEvent(text);
                }
            } catch (Exception e) {
                this.sendTextEvent(text);
            }
        }
        super.endElement(uri,name,raw);
    }

    /**
     * Replace occurence of searchString in source with replacement. If
     * replacement is null remove the occurences.
     */
    public static String replace(String source,
                                 String searchString,
                                 String replacement) {
        if (source != null && searchString != null) {
            int pos;
            int l = searchString.length();
            if (replacement == null) replacement = "";
            do {
                pos = source.indexOf(searchString);
                if (pos != -1) {
                    source = source.substring(0, pos) + replacement + source.substring(pos + l);
                }
            } while (pos != -1);
        }
        return source;
    }

}
