/*
 * File LenientBaseBean.java 
 * created by mpo
 * on Dec 26, 2003 | 5:54:50 PM
 * 
 * (c) 2003 - Outerthought BVBA
 */
package org.apache.cocoon.woody.samples.bindings;

/**
 * LenientBaseBean
 */
public class LenientBaseBean {
    protected String breakingField;
    protected String surviveField;
    
    protected LenientBaseBean(String initVal) {
        this.breakingField = initVal;
        this.surviveField = initVal;
    }
    
    
    public String toString() {
        final String className = this.getClass().getName();
        final String state = "[breakingField=" +breakingField + "|surviveField="+ surviveField+"]";
        return className + state;
    }

}
