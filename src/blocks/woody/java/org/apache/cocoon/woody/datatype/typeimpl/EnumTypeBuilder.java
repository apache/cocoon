/*
 * $Id: EnumTypeBuilder.java,v 1.1 2003/11/06 22:58:37 ugo Exp $
 */
package org.apache.cocoon.woody.datatype.typeimpl;

import org.apache.cocoon.woody.datatype.Datatype;
import org.apache.cocoon.woody.datatype.DatatypeManager;
import org.w3c.dom.Element;

/**
 * Description of EnumTypeBuilder.
 */
public class EnumTypeBuilder extends AbstractDatatypeBuilder {

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.DatatypeBuilder#build(org.w3c.dom.Element, boolean, org.apache.cocoon.woody.datatype.DatatypeManager)
     */
    public Datatype build(Element datatypeElement,
						  boolean arrayType,
						  DatatypeManager datatypeManager) throws Exception {
        EnumType type = new EnumType();
        type.setArrayType(arrayType);
        type.setBuilder(this);

        buildValidationRules(datatypeElement, type, datatypeManager);
        buildConvertor(datatypeElement, type);

        return type;
    }

}
