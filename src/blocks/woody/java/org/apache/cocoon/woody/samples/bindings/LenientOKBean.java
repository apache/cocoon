/*
 * File LenientOKBean.java 
 * created by mpo
 * on Dec 26, 2003 | 4:01:29 PM
 * 
 * (c) 2003 - Outerthought BVBA
 */
package org.apache.cocoon.woody.samples.bindings;

/**
 * LenientOKBean
 */
public class LenientOKBean extends LenientBaseBean{
   
    public LenientOKBean(String initVal) {
        super(initVal);
    }

    
    /**
     * @return Returns the breakingField.
     */
    public String getBreakingField() {
        return breakingField;
    }

    /**
     * @param breakingField The breakingField to set.
     */
    public void setBreakingField(String breakingField) {
        this.breakingField = breakingField;
    }

}
