/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.arch.config;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.NoSuchElementException;

/**
 * This is the default <code>Configuration</code> implementation.
 *
 * @author <a href="mailto:scoobie@betaversion.org">Federico Barbieri</a>
 *         (Betaversion Productions)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-27 01:19:57 $
 */
public class ConfigurationImpl extends AbstractConfiguration {

    /** The configuration attributes table. */
    private Hashtable attributes=new Hashtable();
    /** The configuration children list grouped by name. */
    private Hashtable children=new Hashtable();
    /** The configuration element name. */
    private String name=null;
    /** The configuration element value. */
    private String value=null;
    
    /**
     * Create a new <code>ConfigurationImpl</code> instance.
     */
    protected ConfigurationImpl(String name) {
        super();
        this.name=name;
    }

    /**
     * Create a new <code>ConfigurationImpl</code> instance.
     */
    protected ConfigurationImpl(String name, String source, int line) {
        super(source,line);
        this.name=name;
    }

    /**
     * Returns the name of this configuration element.
     */
	public String getName() {
	    return(this.name);
	}

    /**
     * Returns the value of the configuration element as a <code>String</code>.
     *
     * @exception ConfigurationException If the value is not present.
     */
	public String getValue()
	throws ConfigurationException {
	    if (this.value!=null) return(this.value);
        throw new ConfigurationException("No value is associated with the "+
            "configuration element \""+this.getName()+"\"", this);
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>String</code>.
     *
     * @exception ConfigurationException If the attribute is not present.
     */
	public String getAttribute(String name)
	throws ConfigurationException {
	    String value=(String)this.attributes.get(name);
	    if (value!=null) return(value);
        throw new ConfigurationException("No attribute named \""+name+"\" is "+
            "associated with the configuration element \""+this.getName()+"\"",
            this);
    }

    /**
     * Return the first <code>Configuration</code> object child of this
     * associated with the given name or <b>null</b>.
     *
     * @param name The name of the required child <code>Configuration</code>.
     */
    public Configuration getConfiguration(String name) {
	    Vector v=(Vector)this.children.get(name);
        if ((v!=null) && (v.size()>0)) return((Configuration)v.firstElement());
        return(null);
	}

    /**
     * Return an <code>Enumeration</code> of <code>Configuration</code> objects
     * children of this associated with the given name.
     * <br>
     * The returned <code>Enumeration</code> may be empty.
     *
     * @param name The name of the required children <code>Configuration</code>.
     */
	public Enumeration getConfigurations(String name) {
	    Vector v=(Vector)this.children.get(name);
	    if (v==null) return(new EmptyEnumerationImpl());
	    else return(v.elements());
    }
    
    /**
     * Append data to the value of this configuration element.
     */
    protected void appendValueData(String value) {
        if (this.value==null) this.value=value;
        else this.value=this.value+value;
    }

    /**
     * Add an attribute to this configuration element, returning its old
     * value or <b>null</b>.
     */
    protected String addAttribute(String name, String value) {
        return((String)this.attributes.put(name,value));
    }

    /**
     * Add a child <code>Configuration</code> to this configuration element.
     */
    protected void addConfiguration(Configuration conf) {
        String name=conf.getName();
        Vector v=(Vector)this.children.get(name);
        if (v==null) {
            v=new Vector();
            this.children.put(name,v);
        }
        v.addElement(conf);
    }

    /** An empty <code>Enumeration</code> implementation. */
    private class EmptyEnumerationImpl implements Enumeration {
        /** Tests if this enumeration contains more elements. */
        public boolean hasMoreElements() {
            return(false);
        }
        
        /** Returns the next element of this enumeration. */
        public Object nextElement() {
            throw new NoSuchElementException();
        }
    }
}