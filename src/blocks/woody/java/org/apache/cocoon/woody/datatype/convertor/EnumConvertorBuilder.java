/*
 * $Id: EnumConvertorBuilder.java,v 1.1 2003/11/06 22:58:36 ugo Exp $
 */
package org.apache.cocoon.woody.datatype.convertor;

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Description of EnumConvertorBuilder.
 */
public class EnumConvertorBuilder implements ConvertorBuilder {

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.convertor.ConvertorBuilder#build(org.w3c.dom.Element)
     */
    public Convertor build(Element configElement) throws Exception {
        if (configElement == null) {
            return null;
        }
        Element enumEl = DomHelper.getChildElement(configElement,
                Constants.WD_NS, "enum", true);
        String clazz = enumEl.getFirstChild().getNodeValue();
        return new EnumConvertor(clazz);
    }

}
