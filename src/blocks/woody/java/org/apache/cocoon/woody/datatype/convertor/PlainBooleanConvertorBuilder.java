package org.apache.cocoon.woody.datatype.convertor;

import org.w3c.dom.Element;

/**
 * Builds {PlainBooleanConvertor}s.
 */
public class PlainBooleanConvertorBuilder implements ConvertorBuilder {
    public Convertor build(Element configElement) throws Exception {
        return new PlainBooleanConvertor();
    }
}
