package org.apache.cocoon.woody.datatype.typeimpl;

import org.apache.cocoon.woody.datatype.Datatype;
import org.apache.cocoon.woody.datatype.DatatypeManager;
import org.w3c.dom.Element;

/**
 * Builds {@link BooleanType}s.
 */
public class BooleanTypeBuilder extends AbstractDatatypeBuilder {
    public Datatype build(Element datatypeElement, boolean arrayType, DatatypeManager datatypeManager) throws Exception {
        BooleanType type = new BooleanType();
        type.setArrayType(arrayType);
        type.setBuilder(this);

        buildValidationRules(datatypeElement, type, datatypeManager);
        buildConvertor(datatypeElement, type);

        return type;
    }
}
