/*
 * $Id: EnumType.java,v 1.1 2003/11/06 22:58:37 ugo Exp $
 */
package org.apache.cocoon.woody.datatype.typeimpl;

/**
 * Description of EnumType.
 */
public class EnumType extends AbstractDatatype {
    
    public EnumType() {
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.Datatype#getTypeClass()
     */
    public Class getTypeClass() {
        return this.getConvertor().getTypeClass();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.Datatype#getDescriptiveName()
     */
    public String getDescriptiveName() {
        return this.getConvertor().getTypeClass().getName();
    }

}
