package org.apache.cocoon.woody.datatype.convertor;

import org.w3c.dom.Element;

public class PlainDecimalConvertorBuilder implements ConvertorBuilder {
    public Convertor build(Element configElement) throws Exception {
        return new PlainDecimalConvertor();
    }
}
