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
package org.apache.cocoon.xml.xlink;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

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
 * @author <a href="mailto:tk-cocoon@datas-world.de">Torsten Knodt</a>
 * @version CVS $Id: ExtendedXLinkPipe.java,v 1.6 2004/03/05 13:03:02 bdelacretaz Exp $
 */
public abstract class ExtendedXLinkPipe extends XLinkPipe {

    protected static Set arrayToSet(Object[] array) {
        final Set set = new HashSet(array.length);

        for (int i = 0; i < array.length; i++)
            set.add(array[i]);
        return set;
    }
    
    private final Map MAP = new HashMap() {
        {
            put(
                "",
                arrayToSet(
                    new String[] {
                        "about",
                        "action",
                        "background",
                        "data",
                        "discuri",
                        "href",
                        "longdesc",
                        "onenterforward",
                        "onenterbackward",
                        "ontimer",
                        "onpick",
                        "src" }));
            put(
                "http://www.w3.org/1999/xhtml",
                arrayToSet(
                    new String[] {
                        "action",
                        "background",
                        "data",
                        "href",
                        "longdesc",
                        "src" }));
            put(
                "http://www.w3.org/2001/XInclude",
                arrayToSet(new String[] { "href" }));
            put(
                "http://www.wapforum.org/2001/wml",
                arrayToSet(
                    new String[] {
                        "onenterforward",
                        "onenterbackward",
                        "ontimer",
                        "href",
                        "onpick",
                        "src" }));
            put(
                "http://www.w3.org/2002/01/P3Pv1",
                arrayToSet(
                    new String[] { "about", "discuri", "src", "service" }));            
        }
    };

    private int attrIndex = -1;

    public void startElement(
        String uri,
        final String name,
        final String raw,
        final Attributes attr)
        throws SAXException {
        final Set attrList = (Set) MAP.get((uri == null) ? "" : uri);

        if (attrList != null) {
            for (int i = attrIndex + 1; i < attr.getLength(); i++)
                if (attr.getURI(i).equals("")
                    && attrList.contains(attr.getLocalName(i))) {

                    final String att = attr.getValue(i);

                    if (att != null) {
                        final String str =
                            ": URI="
                                + uri
                                + " NAME="
                                + name
                                + " RAW="
                                + raw
                                + " ATT="
                                + attr.getLocalName(i)
                                + " NS="
                                + uri
                                + " VALUE="
                                + att;

                        if (getLogger().isDebugEnabled())
                           getLogger().debug("Transforming to XLink" + str);
                           
                        attrIndex = i;
                        
                        simpleLink(
                            att,
                            null,
                            null,
                            null,
                            null,
                            null,
                            uri,
                            name,
                            raw,
                            attr);
                        
                        return;
                    }
                }
            attrIndex = -1;
        }

        super.startElement(uri, name, raw, attr);
    }

    public void simpleLink(
        final String href,
        final String role,
        final String arcrole,
        final String title,
        final String show,
        final String actuate,
        final String uri,
        final String name,
        final String raw,
        final Attributes attr)
        throws SAXException {
        if (attrIndex != -1) {
            final AttributesImpl newattr = new AttributesImpl(attr);
            newattr.setValue(attrIndex, href);
            startElement(uri, name, raw, newattr);
        } else {
            super.simpleLink(
                href,
                role,
                arcrole,
                title,
                show,
                actuate,
                uri,
                name,
                raw,
                attr);
        }
    }
}
