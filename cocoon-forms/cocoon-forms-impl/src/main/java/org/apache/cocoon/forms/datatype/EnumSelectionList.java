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

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This type of selection list outputs a list of items
 * corresponding to the possible instances of an {@link org.apache.cocoon.forms.datatype.typeimpl.EnumType}.
 * <p>Example usage:</p>
 * <pre>
 * &lt;fd:selection-list type="enum" class="com.example.Sex"/&gt;
 * </pre>
 * <p>Produces the following output:</p>
 * <pre>
 * &lt;fi:selection-list&gt;
 *   &lt;fi:item value=""/&gt;
 *   &lt;fi:item value="com.example.Sex.MALE"&gt;
 *     &lt;fi:label&gt;
 *       &lt;i18n:text&gt;com.example.Sex.MALE&lt;/i18n:text&gt;
 *     &lt;/fi:label&gt;
 *   &lt;/fi:item&gt;
 *   &lt;fi:item value="com.example.Sex.FEMALE"&gt;
 *     &lt;fi:label&gt;
 *       &lt;i18n:text&gt;com.example.Sex.FEMALE&lt;/i18n:text&gt;
 *     &lt;/fi:label&gt;
 *   &lt;/fi:item&gt;
 * &lt;/fi:selection-list&gt;
 * </pre>
 *
 * <p>If you don't want an initial null value, add a
 * <code>nullable="false"</code> attribute to the
 * <code>fd:selection-list</code> element.</p>
 *
 * @version $Id$
 */
public class EnumSelectionList implements SelectionList {
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
    public void generateSaxFragment(ContentHandler contentHandler,
                                    Locale locale)
    throws SAXException {
        try {
            contentHandler.startElement(FormsConstants.INSTANCE_NS, SELECTION_LIST_EL, FormsConstants.INSTANCE_PREFIX_COLON + SELECTION_LIST_EL, XMLUtils.EMPTY_ATTRIBUTES);
            Field fields[] = clazz.getDeclaredFields();
            // Create void element
            if (nullable) {
                AttributesImpl voidAttrs = new AttributesImpl();
                voidAttrs.addCDATAAttribute("value", "");
                contentHandler.startElement(FormsConstants.INSTANCE_NS, ITEM_EL, FormsConstants.INSTANCE_PREFIX_COLON + ITEM_EL, voidAttrs);
                contentHandler.endElement(FormsConstants.INSTANCE_NS, ITEM_EL, FormsConstants.INSTANCE_PREFIX_COLON + ITEM_EL);
            }
            for (int i = 0 ; i < fields.length ; ++i) {
                int mods = fields[i].getModifiers();
                if (Modifier.isPublic(mods) && Modifier.isStatic(mods)
                        && Modifier.isFinal(mods) && fields[i].get(null).getClass().equals(clazz)) {
                    String stringValue = clazz.getName() + "." + fields[i].getName();
                    // Output this item
                    AttributesImpl itemAttrs = new AttributesImpl();
                    itemAttrs.addCDATAAttribute("value", stringValue);
                    contentHandler.startElement(FormsConstants.INSTANCE_NS, ITEM_EL, FormsConstants.INSTANCE_PREFIX_COLON + ITEM_EL, itemAttrs);
                    contentHandler.startElement(FormsConstants.INSTANCE_NS, LABEL_EL, FormsConstants.INSTANCE_PREFIX_COLON + LABEL_EL, XMLUtils.EMPTY_ATTRIBUTES);
                    // TODO: make i18n element optional
                    contentHandler.startElement(FormsConstants.I18N_NS, TEXT_EL, FormsConstants.I18N_PREFIX_COLON + TEXT_EL, XMLUtils.EMPTY_ATTRIBUTES);
                    contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
                    contentHandler.endElement(FormsConstants.I18N_NS, TEXT_EL, FormsConstants.I18N_PREFIX_COLON + TEXT_EL);
                    contentHandler.endElement(FormsConstants.INSTANCE_NS, LABEL_EL, FormsConstants.INSTANCE_PREFIX_COLON + LABEL_EL);
                    contentHandler.endElement(FormsConstants.INSTANCE_NS, ITEM_EL, FormsConstants.INSTANCE_PREFIX_COLON + ITEM_EL);
                }
            }
            // End the selection-list
            contentHandler.endElement(FormsConstants.INSTANCE_NS, SELECTION_LIST_EL, FormsConstants.INSTANCE_PREFIX_COLON + SELECTION_LIST_EL);
        } catch (Exception e) {
            throw new SAXException("Got exception trying to get enum's values", e);
        }
    }
}
