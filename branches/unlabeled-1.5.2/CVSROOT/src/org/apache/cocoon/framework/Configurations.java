/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.framework;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The <code>Configurations</code> object is a collection of parameters
 * required by a <code>Configurable</code> object for proper operation.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.5.2.1 $ $Date: 2000-02-07 15:35:38 $
 * @since Cocoon 2.0
 */
public class Configurations {
    private Hashtable parameters=null;

    /**
     * Create a new empty Configuration object.
     */
    public Configurations() {
        super();
        this.parameters=new Hashtable();
    }

    /**
     * Set the value of a specified parameter.
     * <br>
     * If the specified value is null the parameter is removed.
     *
     * @return The previous value of the parameter or null.
     */
    public String setParameter(String name, String value) {
        if (name==null) return(null);
        if (value==null) return((String)this.parameters.remove(name));
        return((String)this.parameters.put(name,value));
    }

    /**
     * Return an Enumeration view of all configuration parameter names.
     */
    public Enumeration getParameterNames() {
        return(this.parameters.keys());
    }
    
    /**
     * Check if the given parameter can be retrieved.
     */
    public boolean isParameter(String name) {
        return(this.parameters.containsKey(name));
    }

    /**
     * Retrieve the string value for a given parameter.
     * <br>
     * If the given parameter cannot be retrieved, null is returned.
     */
    public String getParameter(String name) {
        if(name==null) return(null);
        return((String)this.parameters.get(name));
    }

    /**
     * Retrieve the string value for a given parameter.
     * <br>
     * If the given parameter cannot be retrieved, defaultValue is returned.
     */
    public String getParameter(String name, String defaultValue) {
        String value=this.getParameter(name);
        return(value==null ? defaultValue : value);
    }
    
    /**
     * Retrieve the int value for a given parameter.
     *
     * @exception ConfigurationException If the parameter was not found or its
     *                                   value could not be parsed to int.
     */
    public int getParameterAsInteger(String name)
    throws ConfigurationException {
        String value=this.getParameter(name);
        if (value==null)
            throw new ConfigurationException("Parameter \""+name+
                                             "\" was not found");
        try {
            if (value.startsWith("0x"))
                return(Integer.parseInt(value.substring(2),16));
            else if (value.startsWith("0o"))
                return(Integer.parseInt(value.substring(2),8));
            else if (value.startsWith("0b"))
                return(Integer.parseInt(value.substring(2),2));
            else return(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Invalid integer value \""+value+
                                             "\" for parameter \""+name+"\"");
        }
    }
    
    /**
     * Retrieve the int value for a given parameter.
     * <br>
     * If the given parameter cannot be retrieved, or its value could not be
     * parsed to int, defaultValue is returned.
     */
    public int getParameterAsInteger(String name, int defaultValue) {
        try {
            return(this.getParameterAsInteger(name));
        } catch (ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /**
     * Retrieve the float value for a given parameter.
     *
     * @exception ConfigurationException If the parameter was not found or its
     *                                   value could not be parsed to float.
     */
    public float getParameterAsFloat(String name)
    throws ConfigurationException {
        String value=this.getParameter(name);
        if (value==null)
            throw new ConfigurationException("Parameter \""+name+
                                             "\" was not found");
        try {
            return(Float.parseFloat(value));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Invalid float value \""+value+
                                             "\" for parameter \""+name+"\"");
        }
    }
    
    /**
     * Retrieve the float value for a given parameter.
     * <br>
     * If the given parameter cannot be retrieved, or its value could not be
     * parsed to float, defaultValue is returned.
     */
    public float getParameterAsFloat(String name, float defaultValue) {
        try {
            return(this.getParameterAsFloat(name));
        } catch (ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /**
     * Retrieve the boolean value for a given parameter.
     *
     * @exception ConfigurationException If the parameter was not found or its
     *                                   value could not be parsed to boolean.
     */
    public boolean getParameterAsBoolean(String name)
    throws ConfigurationException {
        String value=this.getParameter(name);
        if (value==null)
            throw new ConfigurationException("Parameter \""+name+
                                             "\" was not found");
        if (value.equalsIgnoreCase("TRUE")) return(true);
        if (value.equalsIgnoreCase("FALSE")) return(false);
        else throw new ConfigurationException("Invalid boolean value \""+value+
                                              "\" for parameter \""+name+"\"");
    }

    /**
     * Retrieve the boolean value for a given parameter.
     * <br>
     * If the given parameter cannot be retrieved, or its value could not be
     * parsed to boolean, defaultValue is returned.
     */
    public boolean getParameterAsBoolean(String name, boolean defaultValue) {
        try {
            return(this.getParameterAsBoolean(name));
        } catch (ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /**
     * Get a new instance of this Configuration duplicating parameters one
     * by one.
     */
    public Configurations duplicate() {
        Configurations conf=new Configurations();
        Enumeration e=this.getParameterNames();
        while (e.hasMoreElements()) {
            String name=(String)e.nextElement();
            String value=this.getParameter(name);
            conf.setParameter(new String(name),new String(value));
        }
        return(conf);
    }
    
    /**
     * Merge parameters from another Configuration object into this one.
     *
     * @return This Configurations instance.
     */
    public Configurations merge(Configurations conf) {
        Enumeration e=conf.getParameterNames();
        while (e.hasMoreElements()) {
            String name=(String)e.nextElement();
            String value=conf.getParameter(name);
            this.setParameter(new String(name),new String(value));
        }
        return(this);
    }

    /**
     * Dump these configuration (for debugging purposes).
     */
    public void dump(PrintStream out) {
        Enumeration e=this.getParameterNames();
        out.println("# Dumping Configuration Parameters");
        while (e.hasMoreElements()) {
            String name=(String)e.nextElement();
            out.println("\""+name+"\"=\""+this.getParameter(name)+"\"");
        }
    }

    /**
     * Create a Configuration object from a DOM NodeList.
     * <br>
     * This method searches in a NodeList for elements like
     * &lt;parameter name=&quot;parameter_name&quot;
     *               value=&quot;parameter_value&quot;/&gt;
     * and creates a new Configuration object from them.
     */
    public static Configurations createFromNodeList(NodeList list) {
        Configurations conf=new Configurations();
        if (list!=null) {
            for (int x=0; x<list.getLength(); x++) {
                if (list.item(x).getNodeType()!=Node.ELEMENT_NODE) continue;
                Element elem=(Element)list.item(x);
                if (!elem.getTagName().equals("parameter")) continue;
                String name=elem.getAttribute("name");
                String value=elem.getAttribute("value");
                if ((name==null)||(value==null)) continue;
                conf.setParameter(name,value);
            }
        }
        return(conf);
    }
}
