/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-27 01:25:35 $
 */
public class Parameters {
    private Hashtable parameters=null;

    /**
     * Create a new <code>Parameters</code> instance.
     */
    public Parameters() {
        super();
        this.parameters=new Hashtable();
    }

    /**
     * Set the <code>String</code> value of a specified parameter.
     * <br>
     * If the specified value is <b>null</b> the parameter is removed.
     *
     * @return The previous value of the parameter or <b>null</b>.
     */
    public String setParameter(String name, String value) {
        if (name==null) return(null);
        if (value==null) return((String)this.parameters.remove(name));
        return((String)this.parameters.put(name,value));
    }

    /**
     * Return an <code>Enumeration</code> view of all parameter names.
     */
    public Enumeration getParameterNames() {
        return(this.parameters.keys());
    }
    
    /**
     * Check if the specified parameter can be retrieved.
     */
    public boolean isParameter(String name) {
        return(this.parameters.containsKey(name));
    }

    /**
     * Retrieve the <code>String</code> value of the specified parameter.
     * <br>
     * If the specified parameter cannot be found, <b>null</b> is returned.
     */
    private String getParameter(String name) {
        if(name==null) return(null);
        return((String)this.parameters.get(name));
    }

    /**
     * Retrieve the <code>String</code> value of the specified parameter.
     * <br>
     * If the specified parameter cannot be found, <code>defaultValue</code>
     * is returned.
     */
    public String getParameter(String name, String defaultValue) {
        String value=this.getParameter(name);
        return(value==null ? defaultValue : value);
    }
    
    /**
     * Retrieve the <code>int</code> value of the specified parameter.
     * <br>
     * If the specified parameter cannot be found, <code>defaultValue</code>
     * is returned.
     */
    public int getParameterAsInteger(String name, int defaultValue) {
        String value=this.getParameter(name);
        if (value==null) return(defaultValue);
        try {
            if (value.startsWith("0x"))
                return(Integer.parseInt(value.substring(2),16));
            else if (value.startsWith("0o"))
                return(Integer.parseInt(value.substring(2),8));
            else if (value.startsWith("0b"))
                return(Integer.parseInt(value.substring(2),2));
            else return(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return(defaultValue);
        }
    }
    
    /**
     * Retrieve the <code>long</code> value of the specified parameter.
     * <br>
     * If the specified parameter cannot be found, <code>defaultValue</code>
     * is returned.
     */
    public long getParameterAsLong(String name, long defaultValue) {
        String value=this.getParameter(name);
        if (value==null) return(defaultValue);
        try {
            if (value.startsWith("0x"))
                return(Long.parseLong(value.substring(2),16));
            else if (value.startsWith("0o"))
                return(Long.parseLong(value.substring(2),8));
            else if (value.startsWith("0b"))
                return(Long.parseLong(value.substring(2),2));
            else return(Long.parseLong(value));
        } catch (NumberFormatException e) {
            return(defaultValue);
        }
    }
    
    /**
     * Retrieve the <code>float</code> value of the specified parameter.
     * <br>
     * If the specified parameter cannot be found, <code>defaultValue</code>
     * is returned.
     */
    public float getParameterAsFloat(String name, float defaultValue) {
        String value=this.getParameter(name);
        if (value==null) return(defaultValue);
        try {
            return(Float.parseFloat(value));
        } catch (NumberFormatException e) {
            return(defaultValue);
        }
    }
    
    /**
     * Retrieve the <code>boolean</code> value of the specified parameter.
     * <br>
     * If the specified parameter cannot be found, <code>defaultValue</code>
     * is returned.
     */
    public boolean getParameterAsBoolean(String name, boolean defaultValue) {
        String value=this.getParameter(name);
        if (value==null) return(defaultValue);
        if (value.equalsIgnoreCase("TRUE")) return(true);
        if (value.equalsIgnoreCase("FALSE")) return(false);
        return(defaultValue);
    }
    
    /**
     * Merge parameters from another <code>Parameters</code> instance
     * into this.
     *
     * @return This <code>Parameters</code> instance.
     */
    public Parameters merge(Parameters conf) {
        Enumeration e=conf.getParameterNames();
        while (e.hasMoreElements()) {
            String name=(String)e.nextElement();
            String value=conf.getParameter(name);
            this.setParameter(new String(name),new String(value));
        }
        return(this);
    }
}
