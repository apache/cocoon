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
package org.apache.cocoon.forms.datatype;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This type of selection list outputs a list of items
 * corresponding to the possible instances of an {@link org.apache.cocoon.forms.datatype.typeimpl.EnumType}.
 * <p>Example usage:</p>
 * <pre>
 * &lt;wd:selection-list type="enum" class="com.example.Sex"/>
 * </pre>
 * <p>Produces the following output:</p>
 * <pre>
 * &lt;wi:selection-list>
 *   &lt;wi:item value=""/>
 *   &lt;wi:item value="com.example.Sex.MALE">
 *     &lt;wi:label>
 *       &lt;i18n:text>com.example.Sex.MALE</i18n:text>
 *     &lt;/wi:label>
 *   &lt;/wi:item>
 *   &lt;wi:item value="com.example.Sex.FEMALE">
 *     &lt;wi:label>
 *       &lt;i18n:text>com.example.Sex.FEMALE</i18n:text>
 *     &lt;/wi:label>
 *   &lt;/wi:item>
 * &lt;/wi:selection-list>
 * </pre>
 * <p>If you don't want an initial null value, add a nullable="false"
 * attribute to the wd:selection-list element.
 * 
 * @version CVS $Id: EnumSelectionList.java,v 1.2 2004/03/09 11:31:11 joerg Exp $
 */
public class EnumSelectionList implements SelectionList {
    public static final String I18N_NS = "http://apache.org/cocoon/i18n/2.1";
    public static final String I18N_PREFIX_COLON = "i18n:";
    public static final String TEXT_EL = "text";
    
    private Datatype datatype;
    private Class clazz;
    private boolean nullable;

    /**
     * @param className
     * @param datatype
     */
    public EnumSelectionList(String className, Datatype datatype, boolean nullable) throws ClassNotFoundException {
        this.datatype = datatype;
        this.nullable = nullable;
        this.clazz = Class.forName(className);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.forms.datatype.SelectionList#getDatatype()
     */
    public Datatype getDatatype() {
        return datatype;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.forms.datatype.SelectionList#generateSaxFragment(org.xml.sax.ContentHandler, java.util.Locale)
     */
    public void generateSaxFragment(
        ContentHandler contentHandler,
        Locale locale)
        throws SAXException {
        try {
            contentHandler.startElement(Constants.FI_NS, SELECTION_LIST_EL, Constants.FI_PREFIX_COLON + SELECTION_LIST_EL, Constants.EMPTY_ATTRS);
            Field fields[] = clazz.getDeclaredFields();
            // Create void element
            if (nullable) {
                AttributesImpl voidAttrs = new AttributesImpl();
                voidAttrs.addCDATAAttribute("value", "");
                contentHandler.startElement(Constants.FI_NS, ITEM_EL, Constants.FI_PREFIX_COLON + ITEM_EL, voidAttrs);
                contentHandler.endElement(Constants.FI_NS, ITEM_EL, Constants.FI_PREFIX_COLON + ITEM_EL);
            }            
            for (int i = 0 ; i < fields.length ; ++i) {
                int mods = fields[i].getModifiers();
                if (Modifier.isPublic(mods) && Modifier.isStatic(mods)
                        && Modifier.isFinal(mods) && fields[i].get(null).getClass().equals(clazz)) {
                    String stringValue = clazz.getName() + "." + fields[i].getName();
                    // Output this item
                    AttributesImpl itemAttrs = new AttributesImpl();
                    itemAttrs.addCDATAAttribute("value", stringValue);
                    contentHandler.startElement(Constants.FI_NS, ITEM_EL, Constants.FI_PREFIX_COLON + ITEM_EL, itemAttrs);
                    contentHandler.startElement(Constants.FI_NS, LABEL_EL, Constants.FI_PREFIX_COLON + LABEL_EL, Constants.EMPTY_ATTRS);
                    // TODO: make i18n element optional
                    contentHandler.startElement(I18N_NS, TEXT_EL, I18N_PREFIX_COLON + TEXT_EL, Constants.EMPTY_ATTRS);
                    contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
                    contentHandler.endElement(I18N_NS, TEXT_EL, I18N_PREFIX_COLON + TEXT_EL);
                    contentHandler.endElement(Constants.FI_NS, LABEL_EL, Constants.FI_PREFIX_COLON + LABEL_EL);
                    contentHandler.endElement(Constants.FI_NS, ITEM_EL, Constants.FI_PREFIX_COLON + ITEM_EL);
                }
            }
            // End the selection-list
            contentHandler.endElement(Constants.FI_NS, SELECTION_LIST_EL, Constants.FI_PREFIX_COLON + SELECTION_LIST_EL);
        } catch (Exception e) {
            throw new SAXException("Got exception trying to get enum's values", e);
        }
    }

}
