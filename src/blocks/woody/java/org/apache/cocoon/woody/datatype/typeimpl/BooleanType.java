package org.apache.cocoon.woody.datatype.typeimpl;

/**
 * A {@link org.apache.cocoon.woody.datatype.Datatype Datatype} implementation for
 * java.lang.Boolean's.
 */
public class BooleanType extends AbstractDatatype {
    public Class getTypeClass() {
        return Boolean.class;
    }

    public String getDescriptiveName() {
        return "boolean";
    }
}
