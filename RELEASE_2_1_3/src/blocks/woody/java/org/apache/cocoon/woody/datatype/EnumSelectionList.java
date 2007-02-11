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
package org.apache.cocoon.woody.datatype;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Builds a selection list with possible values for a class implementing
 * the {@link Enum} interface.
 * @version CVS $Id: EnumSelectionList.java,v 1.2 2003/11/09 09:21:19 ugo Exp $
 */
public class EnumSelectionList implements SelectionList {
    public static final String I18N_NS = "http://apache.org/cocoon/i18n/2.1";
    public static final String I18N_PREFIX_COLON = "i18n:";
    public static final String TEXT_EL = "text";
    
    private Datatype datatype;
    private Class clazz;

    /**
     * @param className
     * @param datatype
     */
    public EnumSelectionList(String className, Datatype datatype) throws ClassNotFoundException {
        this.datatype = datatype;
        // FIXME: use the correct class loader.
        this.clazz = Class.forName(className);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.SelectionList#getDatatype()
     */
    public Datatype getDatatype() {
        return datatype;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.SelectionList#generateSaxFragment(org.xml.sax.ContentHandler, java.util.Locale)
     */
    public void generateSaxFragment(
        ContentHandler contentHandler,
        Locale locale)
        throws SAXException {
        try {
            Field fields[] = clazz.getDeclaredFields();
            contentHandler.startElement(Constants.WI_NS, SELECTION_LIST_EL, Constants.WI_PREFIX_COLON + SELECTION_LIST_EL, Constants.EMPTY_ATTRS);
            for (int i = 0 ; i < fields.length ; ++i) {
                int mods = fields[i].getModifiers();
                if (Modifier.isPublic(mods) && Modifier.isStatic(mods)
                        && Modifier.isFinal(mods) && fields[i].get(null).getClass().equals(clazz)) {
                    String stringValue = clazz.getName() + "." + fields[i].getName();
                    // Output this item
                    AttributesImpl itemAttrs = new AttributesImpl();
                    itemAttrs.addCDATAAttribute("value", stringValue);
                    contentHandler.startElement(Constants.WI_NS, ITEM_EL, Constants.WI_PREFIX_COLON + ITEM_EL, itemAttrs);
                    contentHandler.startElement(Constants.WI_NS, LABEL_EL, Constants.WI_PREFIX_COLON + LABEL_EL, Constants.EMPTY_ATTRS);
                    // TODO: make i18n element optional
                    contentHandler.startElement(I18N_NS, TEXT_EL, I18N_PREFIX_COLON + TEXT_EL, Constants.EMPTY_ATTRS);
                    contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
                    contentHandler.endElement(I18N_NS, TEXT_EL, I18N_PREFIX_COLON + TEXT_EL);
                    contentHandler.endElement(Constants.WI_NS, LABEL_EL, Constants.WI_PREFIX_COLON + LABEL_EL);
                    contentHandler.endElement(Constants.WI_NS, ITEM_EL, Constants.WI_PREFIX_COLON + ITEM_EL);
                }
            }
            // End the selection-list
            contentHandler.endElement(Constants.WI_NS, SELECTION_LIST_EL, Constants.WI_PREFIX_COLON + SELECTION_LIST_EL);
        } catch (Exception e) {
            throw new SAXException("Got exception trying to get enum's values", e);
        }
    }

}
