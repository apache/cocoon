/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.arch.config;

import java.util.Enumeration;

/**
 * This is an abstract <code>Configuration</code> implementation that deals
 * with methods that can be abstracted away from underlying implementations.
 *
 * @author <a href="mailto:scoobie@betaversion.org">Federico Barbieri</a>
 *         (Betaversion Productions)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-02-27 01:33:03 $
 */
public abstract class AbstractConfiguration implements Configuration {
    /**
     * The location string containing information about this
     * <code>Configuration</code> location in the source file.
     */
    protected String location=null;
    
    /**
     * Construct a new <code>AbstractConfiguration</code> instance.
     */
    protected AbstractConfiguration() {
        this(null,-1);
    }

    /**
     * Construct a new <code>AbstractConfiguration</code> instance.
     */
    protected AbstractConfiguration(String source, int line) {
        super();
        this.location="";
        if (source!=null) this.location=source;
        if ((line>=0)&&(this.location.length()>0)) this.location+=" ";
        if (line>0) this.location+="line "+line;
        if (this.location.length()>0) this.location="("+this.location+")";
        else this.location=null;
    }

    /**
     * Returns the value of the configuration element as an <code>int</code>.
     */
    public int getValueAsInt()
	throws ConfigurationException {
        String value=this.getValue();
        try {
            if (value.startsWith("0x"))
                return(Integer.parseInt(value.substring(2),16));
            else if (value.startsWith("0o"))
                return(Integer.parseInt(value.substring(2),8));
            else if (value.startsWith("0b"))
                return(Integer.parseInt(value.substring(2),2));
            else return(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Cannot parse the value of the "+
                "configuration element \""+this.getName()+"\" as an integer",
                this);
        }
	}

    /**
     * Returns the value of the configuration element as a <code>long</code>.
     */
    public long getValueAsLong()
	throws ConfigurationException {
        String value=this.getValue();
        try {
            if (value.startsWith("0x"))
                return(Long.parseLong(value.substring(2),16));
            else if (value.startsWith("0o"))
                return(Long.parseLong(value.substring(2),8));
            else if (value.startsWith("0b"))
                return(Long.parseLong(value.substring(2),2));
            else return(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Cannot parse the value of the "+
                "configuration element \""+this.getName()+"\" as a long", this);
        }
	}

    /**
     * Returns the value of the configuration element as a <code>float</code>.
     */
    public float getValueAsFloat()
	throws ConfigurationException {
        String value=this.getValue();
        try {
            return(Float.parseFloat(value));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Cannot parse the value of the "+
                "configuration element \""+this.getName()+"\" as a float",
                this);
        }
    }

    /**
     * Returns the value of the configuration element as a <code>boolean</code>.
     */
    public boolean getValueAsBoolean()
	throws ConfigurationException {
        String value=this.getValue();
        if (value.equals("true")) return(true);
        if (value.equals("false")) return(false);
        throw new ConfigurationException("Cannot parse the value of the "+
            "configuration element \""+this.getName()+"\" as a boolean",
            this);
    }
    
    /**
     * Returns the value of the configuration element as a <code>String</code>.
     */
    public String getValue(String defaultValue) {
        try {
            return(this.getValue());
        } catch (ConfigurationException e) {
            return(defaultValue);
        }
    }

    /**
     * Returns the value of the configuration element as an <code>int</code>.
     */
    public int getValueAsInt(int defaultValue) {
        try {
            return(this.getValueAsInt());
        } catch (ConfigurationException e) {
            return(defaultValue);
        }
    }

    /**
     * Returns the value of the configuration element as a <code>long</code>.
     */
    public long getValueAsLong(long defaultValue) {
        try {
            return(this.getValueAsLong());
        } catch (ConfigurationException e) {
            return(defaultValue);
        }
    }

    /**
     * Returns the value of the configuration element as a <code>float</code>.
     */
    public float getValueAsFloat(float defaultValue) {
        try {
            return(this.getValueAsFloat());
        } catch (ConfigurationException e) {
            return(defaultValue);
        }
    }

    /**
     * Returns the value of the configuration element as a <code>boolean</code>.
     */
    public boolean getValueAsBoolean(boolean defaultValue) {
        try {
            return(this.getValueAsBoolean());
        } catch (ConfigurationException e) {
            return(defaultValue);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as an
     * <code>int</code>.
     */
    public int getAttributeAsInt(String name)
	throws ConfigurationException {
        String value=this.getAttribute(name);
        try {
            if (value.startsWith("0x"))
                return(Integer.parseInt(value.substring(2),16));
            else if (value.startsWith("0o"))
                return(Integer.parseInt(value.substring(2),8));
            else if (value.startsWith("0b"))
                return(Integer.parseInt(value.substring(2),2));
            else return(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Cannot parse the value of the "+
                "attribute \""+name+"\" of the configuration element \""+
                this.getName()+"\" as an integer",this);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>long</code>.
     */
    public long getAttributeAsLong(String name)
	throws ConfigurationException {
        String value=this.getAttribute(name);
        try {
            if (value.startsWith("0x"))
                return(Long.parseLong(value.substring(2),16));
            else if (value.startsWith("0o"))
                return(Long.parseLong(value.substring(2),8));
            else if (value.startsWith("0b"))
                return(Long.parseLong(value.substring(2),2));
            else return(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Cannot parse the value of the "+
                "attribute \""+name+"\" of the configuration element \""+
                this.getName()+"\" as a long", this);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>float</code>.
     */
    public float getAttributeAsFloat(String name)
	throws ConfigurationException {
        String value=this.getAttribute(name);
        try {
            return(Float.parseFloat(value));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Cannot parse the value of the "+
                "attribute \""+name+"\" of the configuration element \""+
                this.getName()+"\" as a float", this);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>boolean</code>.
     */
    public boolean getAttributeAsBoolean(String name)
	throws ConfigurationException {
        String value=this.getAttribute(name);
        if (value.equals("true")) return(true);
        if (value.equals("false")) return(false);
        throw new ConfigurationException("Cannot parse the value of the "+
            "attribute \""+name+"\" of the configuration element \""+
            this.getName()+"\" as a boolean", this);
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>String</code>.
     */
    public String getAttribute(String name, String defaultValue) {
        try {
            return(this.getAttribute(name));
        } catch (ConfigurationException e) {
            return(defaultValue);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as an
     * <code>int</code>.
     */
    public int getAttributeAsInt(String name, int defaultValue) {
        try {
            return(this.getAttributeAsInt(name));
        } catch (ConfigurationException e) {
            return(defaultValue);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>long</code>.
     */
    public long getAttributeAsLong(String name, long defaultValue) {
        try {
            return(this.getAttributeAsLong(name));
        } catch (ConfigurationException e) {
            return(defaultValue);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>float</code>.
     */
    public float getAttributeAsFloat(String name, float defaultValue) {
        try {
            return(this.getAttributeAsFloat(name));
        } catch (ConfigurationException e) {
            return(defaultValue);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>boolean</code>.
     */
    public boolean getAttributeAsBoolean(String name, boolean defaultValue) {
        try {
            return(this.getAttributeAsBoolean(name));
        } catch (ConfigurationException e) {
            return(defaultValue);
        }
    }

    /**
     * Return the first <code>Configuration</code> object child of this
     * associated with the given name.
     */
    public Configuration getConfiguration(String name) {
	    Enumeration e=this.getConfigurations(name);
	    if (e.hasMoreElements()) return((Configuration)e.nextElement());
        return(null);
	}

	/**
	 * Return a <code>String</code> indicating the position of this
	 * configuration element in a source file or URI or <b>null</b>.
	 */
	public String getLocation() {
	    return(this.location);
	}
}