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
package org.apache.cocoon.xml.xlink;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class extends the XLink semantic capabilities to understand those
 * elements that are have default linking semantics associated.
 *
 * <p>This class reacts on 'href' and 'src' attributes and is able to understand
 * the semantics of XHTML/WML/SMIL/SVG and all the rest of the languages that
 * use either XLink of the above attributes.</p>
 *
 * <p>NOTE: this class is clearly a hack and is not future compatible, but
 * since many XML formats to date are not compatible with the XLink semantics
 * this is what we have to do to live in the bleeding edge. Once there will
 * be a way to remove this, that will be a happy day for XML and for Cocoon too.</p>
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:torstenknodt@datas-world.de">Torsten Knodt</a>
 * @version CVS $Id: ExtendedXLinkPipe.java,v 1.4 2003/09/24 21:26:51 cziegeler Exp $
 */
public abstract class ExtendedXLinkPipe extends XLinkPipe {

    protected static Set arrayToSet(Object[] array) {
        final Set set = new HashSet(array.length);

        for (int i = 0; i<array.length; i++)
            set.add(array[i]);
        return set;
    }

    private final Map MAP = new HashMap() {
        {
            put("", arrayToSet(new String[] {
                "about", "action", "background", "data", "discuri", "href",
                "longdesc", "src"
            }));
            put("http://www.w3.org/1999/xhtml", arrayToSet(new String[] {
                "action", "background", "data", "href", "longdesc", "src"
            }));
            put("http://www.w3.org/2002/01/P3Pv1",
                arrayToSet(new String[]{ "about",
                                         "discuri" }));
        }
    };

    private int attrIndex = -1;

    public void startElement(String uri, final String name, final String raw,
                             final Attributes attr) throws SAXException {
        final Set attrList = (Set) MAP.get((uri==null) ? "" : uri);

        if (attrList!=null) {
            for (int i = 0; i<attr.getLength(); i++)
                if (attr.getURI(i).equals("") &&
                    attrList.contains(attr.getLocalName(i))) {
                    final String att = attr.getValue(i);

                    if (att!=null) {
                        final String str = ": URI="+uri+" NAME="+name+" RAW="+
                                           raw+" ATT="+attr.getLocalName(i)+
                                           " NS="+uri+" VALUE="+att;

                        if (attrIndex!=-1) {
                            getLogger().warn("Possible internal error"+str);
                        }
                        getLogger().debug("Transforming to XLink"+str);
                        attrIndex = i;
                        simpleLink(att, null, null, null, null, null, uri,
                                   name, raw, attr);
                        return;
                    }
                }
        }

        super.startElement(uri, name, raw, attr);
    }

    public void simpleLink(final String href, final String role,
                           final String arcrole, final String title,
                           final String show, final String actuate,
                           final String uri, final String name,
                           final String raw,
                           final Attributes attr) throws SAXException {
        if (attrIndex!=-1) {
            AttributesImpl newattr = new AttributesImpl(attr);

            newattr.setValue(attrIndex, href);
            attrIndex = -1;
            super.startElement(uri, name, raw, newattr);
        } else {
            super.simpleLink(href, role, arcrole, title, show, actuate, uri,
                             name, raw, attr);
        }

    }
}
