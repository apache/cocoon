/* ========================================================================== *
 *                                                                            *
 * Copyright 2004 The Apache Software Foundation.                             *
 *                                                                            *
 * Licensed  under the Apache License,  Version 2.0 (the "License");  you may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at                                                     *
 *                                                                            *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                            *
 * Unless  required  by  applicable law or  agreed  to in  writing,  software *
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.           *
 *                                                                            *
 * See  the  License for  the  specific language  governing  permissions  and *
 * limitations under the License.                                             *
 *                                                                            *
 * ========================================================================== */
package org.apache.cocoon.kernel.configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p>The {@link Configuration} class provides an XML/DOM-like view over a
 * set of configurable {@link Parameters}.</p>
 *
 * <p>In the context of a {@link Configuration}, {@link Parameters} are seen
 * as &quot;attributes&quot; of the configuration element itself.</p>
 *
 * <p>Furthermore a number of methods have been added to retrieve the node
 * namespace, name, and value, effectively allowing an XML-like structure
 * to be mapped into a {@link Configuration}.</p>
 *
 * <p>This is <b>not</b> a complete XML representation of a document, as the
 * {@link Configuration} class does no allow text nodes to be intermixed with
 * children {@link Configuration} nodes, does not support comments and/or
 * processing instructions and most important <b>does not support attribute
 * name spaces</b>.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class Configuration extends ArrayList {

    /** <p>A empty configuration returned for unavailable children.</p> */
    private static Configuration EMPTY_CONFIGURATION = new Configuration();
    
    /** <p>Whether this instance is locked or not.</p> */
    private boolean locked = false;
    
    /** <p>The name of this {@link Configuration} element. */
    private String name = null;

    /** <p>The namespace associated with this {@link Configuration} element. */
    private String namespace = null;

    /** <p>The value of {@link Configuration} element. */
    private Object value = null;

    /** <p>The {@link Parameters} instance to use as attributes. */
    private Parameters attributes = null;
    
    /** <p>The configuration location (if any).</p> */
    private String location = null;

    /** <p>The configuration location URL (if any).</p> */
    private URL url = null;
    
    /** <p>The hash code of this instance.</p> */
    private int hash = 0;
    
    /* ====================================================================== */
    
    /**
     * <p>Create a new empty {@link Configuration} element to be used only
     * when returning unavailable children.</p>
     */
    private Configuration() {
        super();
        this.name = "";
        this.namespace = null;
        this.attributes = new Parameters();
        this.lock();
        this.hash = 0;
    }

    /**
     * <p>Create a new {@link Configuration} element with a specified name.</p>
     *
     * @param name this {@link Configuration} element name.
     * @throws NullPointerException if the specified name was <b>null</b>.
     * @throws IllegalArgumentException if the specified name was empty.
     */
    public Configuration(String name) {
        this(null, name, null);
    }

    /**
     * <p>Create a new {@link Configuration} element with a specified name and
     * an optional namespace associated with it.</p>
     *
     * @param name this {@link Configuration} element name.
     * @param namespace the name space associated with the element name.
     * @throws NullPointerException if the specified name was <b>null</b>.
     * @throws IllegalArgumentException if the specified name was empty.
     */
    public Configuration(String namespace, String name) {
        this(namespace, name, null);
    }

    /**
     * <p>Create a new {@link Configuration} element with a specified name and
     * a {@link Parameters} instance to use as element attributes.</p>
     *
     * @param name this {@link Configuration} element name.
     * @param attributes the {@link Parameters} instance to use as attributes.
     * @throws NullPointerException if the specified name was <b>null</b>.
     * @throws IllegalArgumentException if the specified name was empty.
     */
    public Configuration(String name, Parameters attributes) {
        this(null, name, attributes);
    }
    
    /**
     * <p>Create a new {@link Configuration} element with a specified name, an
     * optional namespace associated with it and a {@link Parameters} instance
     * to use as element attributes.</p>
     *
     * @param name this {@link Configuration} element name.
     * @param namespace the name space associated with the element name.
     * @param attributes the {@link Parameters} instance to use as attributes.
     * @throws NullPointerException if the specified name was <b>null</b>.
     * @throws IllegalArgumentException if the specified name was empty.
     */
    public Configuration(String namespace, String name, Parameters attributes) {
        super();
        if (name == null) throw new NullPointerException("Null name");
        if (name.equals("")) throw new IllegalArgumentException("Empty name");
        this.name = name;
        this.namespace = ("".equals(namespace)? null: namespace);
        this.attributes = (attributes == null? new Parameters(): attributes);
        this.hash = new String(this.namespace + "|" + this.name).hashCode();
    }

    /* ====================================================================== */
    
    /**
     * <p>Lock this {@link Configuration} instance.</p>
     *
     * <p>After this method is called, no further modifications are allowed
     * into this {@link Configuration} instance, and any modification operation
     * will throw a {@link ConfigurationException}.</p>
     *
     * @return this {@link Configuration} instance.
     */
    public Configuration lock() {
        this.locked = true;
        return(this);
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the name of this {@link Configuration} element.</p>
     */
    public String name() {
        return(this.name);
    }

    /**
     * <p>Return the namespace declaration (if any) associated with this
     * {@link Configuration} element.</p>
     */
    public String namespace() {
        return(this.namespace);
    }

    /**
     * <p>Return the {@link Parameters} instance backing up attribute names
     * and values.</p>
     */
    public Parameters attributes() {
        return(this.attributes);
    }
    
    /* ====================================================================== */

    /**
     * <p>Add the specified {@link Configuration} instance as a child of this
     * instance at the end of this list.</p>
     *
     * @param child the child {@link Configuration}
     * @return <b>true</b> if this list was modified, <b>false</b> otherwise.
     * @throws NullPointerException if the child was <b>null</b>.
     * @throws ClassCastException if the child was not a {@link Configuration}.
     */
    public boolean add(Object child) {
        this.check(child);
        return(super.add(child));
    }

    /**
     * <p>Add the specified {@link Configuration} instance as a child of this
     * instance at the specified offset.</p>
     *
     * @param index the offset where the child must be added.
     * @param child the child {@link Configuration}
     * @return <b>true</b> if this list was modified, <b>false</b> otherwise.
     * @throws NullPointerException if the child was <b>null</b>.
     * @throws ClassCastException if the child was not a {@link Configuration}.
     */
     public void add(int index, Object child) {
         this.check(child);
         super.add(index, child);
     }
     
    /**
     * <p>Add the specified collection of {@link Configuration} instances as
     * children of this instance at the end of this list.</p>
     *
     * @param children the collection of children {@link Configuration}
     * @return <b>true</b> if this list was modified, <b>false</b> otherwise.
     * @throws NullPointerException if any of the children was <b>null</b>.
     * @throws ClassCastException if any of the children was not a
     *                            {@link Configuration} instance.
     */
    public boolean addAll(Collection children) {
        if (children == null) return(false);
        Iterator iterator = children.iterator();
        while (iterator.hasNext()) this.check(iterator.next());
        return(super.addAll(children));
    }

    /**
     * <p>Add the specified collection of {@link Configuration} instances as
     * children of this instance at the specified offset.</p>
     *
     * @param index the offset where the children must be added.
     * @param children the collection of children {@link Configuration}
     * @return <b>true</b> if this list was modified, <b>false</b> otherwise.
     * @throws NullPointerException if any of the children was <b>null</b>.
     * @throws ClassCastException if any of the children was not a
     *                            {@link Configuration} instance.
     */
    public boolean addAll(int index,  Collection children) {
        if (children == null) return(false);
        Iterator iterator = children.iterator();
        while (iterator.hasNext()) this.check(iterator.next());
        return(super.addAll(index, children));
    }

    /**
     * <p>Returns an unlocked deep copy of this {@link Configuration}
     * instance.</p>
     *
     * <p>The value and attribute names and values themselves are not cloned,
     * but the entire structure of all nested {@link Configuration} and
     * {@link Parameters} instances will be cloned.</p>
     *
     * <p>The returned instance will not be locked.</p>
     *
     * @return a new <b>non null</b> cloned {@link Configuration} instance.
     */
    public Object clone() {
        Configuration configuration = (Configuration) super.clone();
        configuration.locked = false;

        /* Clone attributes and children */
        configuration.clear();
        configuration.attributes = (Parameters)this.attributes.clone();
        Configuration array[] = new Configuration[this.size()];
        array = (Configuration [])this.toArray(array);
        for (int x = 0; x < array.length; x ++) {
            configuration.add(array[x].clone());
        }
        return(configuration);
    }
    
    
    
    /* ====================================================================== */

    /**
     * <p>Return an {@link Iterator} over all children of this
     * {@link Configuration} with a specified name.</p>
     *
     * @param name the name of all returned configuration children.
     * @return a <b>non null</b> {@link Iterator}.
     */
    public Iterator children(String name) {
        return(new ConfigurationIterator(this, null, name));
    }

    /**
     * <p>Return an {@link Iterator} over all children of this
     * {@link Configuration} with a specified name and namespace.</p>
     *
     * @param name the name of all returned configuration children.
     * @param namespace the namespace of all returned configuration children.
     * @return a <b>non null</b> {@link Iterator}.
     */
    public Iterator children(String namespace, String name) {
        return(new ConfigurationIterator(this, namespace, name));
    }
    
    /**
     * <p>Return the first child {@link Configuration} instance with the
     * specified name.</p>
     *
     * <p>If a child cannot be found an empty {@link Configuration} instance
     * is returned. The returned {@link Configuration} will have an empty (and
     * therefore illegal) name. To check if the element returned was the one
     * requested, the suggested method is to proceed in the following way:</p>
     *
     * <pre>
     * Configuration conf = configuration.child(name);
     * if (!configuration.name().equals(name)) {
     *   // There is no child with this name.
     * }</pre>
     *
     * <p>The empty {@link Configuration} element is returned to allow
     * constructs such as:</p>
     *
     * <pre>
     * Configuration conf = configuration.child(name).child(name2).child(namex);
     * </pre>
     * 
     * @param name the name of the returned configuration child.
     * @return a <b>non null</b> {@link Configuration} element.
     */
    public Configuration child(String name) {
        Iterator iterator = this.children(name);
        if (iterator.hasNext()) return((Configuration)iterator.next());
        return(Configuration.EMPTY_CONFIGURATION);
    }
    
    /**
     * <p>Return the first child {@link Configuration} instance with the
     * specified namespace and name.</p>
     *
     * <p>If a child cannot be found an empty {@link Configuration} instance
     * is returned. The returned {@link Configuration} will have an empty (and
     * therefore illegal) name. To check if the element returned was the one
     * requested, the suggested method is to proceed in the following way:</p>
     *
     * <pre>
     * Configuration conf = configuration.child(namespace, name);
     * if (!configuration.name().equals(name)) {
     *   // There is no child with this name.
     * }</pre>
     *
     * <p>The empty {@link Configuration} element is returned to allow
     * constructs such as:</p>
     *
     * <pre>
     * Configuration conf = configuration.child(name).child(name2).child(namex);
     * </pre>
     * 
     * @param name the name of the returned configuration child.
     * @param namespace the namespace of the returned configuration child.
     * @return a <b>non null</b> {@link Iterator}.
     */
    public Configuration child(String namespace, String name) {
        Iterator iterator = this.children(namespace, name);
        if (iterator.hasNext()) return((Configuration)iterator.next());
        return(Configuration.EMPTY_CONFIGURATION);
    }

    /* ====================================================================== */

    /**
     * <p>Create and return a {@link String} describing the location of this
     * {@link Configuration} element, if known.</p>
     *
     * <p>The returned string will assume the following format:
     * <code>file@line,column</code>.</p>
     *
     * @return a <b>non null</b> {@link String} describing the location.
     */
    public String location() {
        return(this.location);
    }

    /**
     * <p>Return (if possible) a URL absolutely locating this
     * {@link Configuration} as a file or resource.</p>
     *
     * @return a {@link URL} instance or <b>null</b> if unknown.
     */
    public URL locationURL() {
        return(this.url);
    }
    
    /**
     * Specify the location details for this {@link Configuration} item.
     *
     * @param location the path or URL of the source file.
     * @param line the line number in the source file.
     * @param column the column number in the source file.
     */
    public String locate(String location, int line, int column) {
        try {
            this.url = new URL(location);
        } catch (MalformedURLException e) {
            // Forget about this exception
        }

        /* Prepare the location string now */
        String old = this.location;
        this.location = location;
        if (line < 0) return(old);
        this.location += "@line=" + line;
        if (column < 0) return(old);
        this.location += ",column=" + column;
        return(old);
    }

    /* ====================================================================== */

    /**
     * <p>Return the value of this {@link Configuration} element.</p>
     *
     * @return the value of this configuration.
     * @throws ConfigurationException if the value was not set.
     */
    public Object getValue()
    throws ConfigurationException {
        if (this.value == null)
            throw new ConfigurationException("Value not set", this);
        return(this.value);
    }

    /**
     * <p>Return the value of this {@link Configuration} element.</p>
     *
     * @return the value of this configuration or the specified default.
     */
    public Object getValue(Object defaultValue) {
        return(this.value == null? defaultValue: value);
    }
    
    /**
     * <p>Set the value of this {@link Configuration} element.</p>
     *
     * @param value the value of this configuration element.
     * @return the value of this configuration.
     * @throws NullPointerException if the specified value was <b>null</b>.
     */
    public Object setValue(Object value) {
        if (value == null) throw new NullPointerException("Null value");
        Object old = this.value;
        this.value = value;
        return(old);
    }

    /**
     * <p>Set the boolean value of this {@link Configuration} element.</p>
     *
     * @param value the value of this configuration element.
     * @return the previous value of this configuration.
     */
    public Object setValue(boolean value) {
        return this.setValue(new Boolean(value));
    }
    
    /**
     * <p>Set the double value of this {@link Configuration} element.</p>
     *
     * @param value the value of this configuration element.
     * @return the previous value of this configuration.
     */
    public Object setValue(double value) {
        return this.setValue(new Double(value));
    }
    
    /**
     * <p>Set the float value of this {@link Configuration} element.</p>
     *
     * @param value the value of this configuration element.
     * @return the previous value of this configuration.
     */
    public Object setValue(float value) {
        return this.setValue(new Float(value));
    }
    
    /**
     * <p>Set the int value of this {@link Configuration} element.</p>
     *
     * @param value the value of this configuration element.
     * @return the previous value of this configuration.
     */
    public Object setValue(int value) {
        return this.setValue(new Integer(value));
    }
    
    /**
     * <p>Set the long value of this {@link Configuration} element.</p>
     *
     * @param value the value of this configuration element.
     * @return the previous value of this configuration.
     */
    public Object setValue(long value) {
        return this.setValue(new Long(value));
    }
    
    /**
     * <p>Remove the value of this {@link Configuration} element.</p>
     *
     * @return the value of this configuration or <b>null</b>.
     */
    public Object removeValue() {
        Object old = this.value;
        this.value = null;
        return(old);
    }
    
    /* ====================================================================== */

    /**
     * <p>Return the boolean representation of this element value.</p>
     *
     * @return the boolean value of the element value.
     * @throws ConfigurationException if the value was not found or could
     *                                not be converted to a boolean.
     */
    public boolean getBooleanValue()
    throws ConfigurationException {
        Object object = this.getValue();
        if (object instanceof Boolean) return(((Boolean)object).booleanValue());
        String string = object.toString();
        if ("0".equals(string)) return(false);
        if ("1".equals(string)) return(true);
        if ("false".equalsIgnoreCase(string)) return(false);
        if ("true".equalsIgnoreCase(string)) return(true);
        throw this.error("boolean", null);
    }

    /**
     * <p>Return the boolean representation of this element value.</p>
     *
     * @param defaultValue the default value to assign if the value was
     *                     not found or could not be converted.
     * @return the boolean value of the element value or the specified default.
     */
    public boolean getBooleanValue(boolean defaultValue) {
        try {
            return(this.getBooleanValue());
        } catch(ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the integer representation of this element value.</p>
     *
     * @return the integer value of the element value.
     * @throws ConfigurationException if the value was not found or could
     *                                not be converted to an integer.
     */
    public int getIntegerValue()
    throws ConfigurationException {
        Object object = this.getValue();
        if (object instanceof Number) return(((Number)object).intValue());
        try {
            return(Integer.parseInt(object.toString()));
        } catch (NumberFormatException e) {
            throw this.error("int", e);
        }
    }
    
    /**
     * <p>Return the integer representation of this element value.</p>
     *
     * @param defaultValue the default value to assign if the element value was
     *                     not found or could not be converted.
     * @return the integer value of the element value or the specified default.
     */
    public int getIntegerValue(int defaultValue) {
        try {
            return(this.getIntegerValue());
        } catch(ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the long integer representation of this element value.</p>
     *
     * @throws ConfigurationException if the value was not found or could
     *                                not be converted to a long integer.
     */
    public long getLongValue()
    throws ConfigurationException {
        Object object = this.getValue();
        if (object instanceof Number) return(((Number)object).longValue());
        try {
            return(Long.parseLong(object.toString()));
        } catch (NumberFormatException e) {
            throw this.error("long", e);
        }
    }
    
    /**
     * <p>Return the long integer representation of this element value.</p>
     *
     * @param defaultValue the default value to assign if the element value was
     *                     not found or could not be converted.
     * @return the long integer value of the element value or the specified default.
     */
    public long getLongValue(long defaultValue) {
        try {
            return(this.getLongValue());
        } catch(ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the float representation of this element value.</p>
     *
     * @return the float value of the value.
     * @throws ConfigurationException if the value was not found or could
     *                                not be converted to a float.
     */
    public float getFloatValue()
    throws ConfigurationException {
        Object object = this.getValue();
        if (object instanceof Number) return(((Number)object).floatValue());
        try {
            return(Float.parseFloat(object.toString()));
        } catch (NumberFormatException e) {
            throw this.error("float", e);
        }
    }

    /**
     * <p>Return the float representation of this element value.</p>
     *
     * @param defaultValue the default value to assign if the element value was
     *                     not found or could not be converted.
     * @return the float value of the element value or the specified default.
     */
    public float getFloatValue(float defaultValue) {
        try {
            return(this.getFloatValue());
        } catch(ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the double representation of this element value.</p>
     *
     * @return the double value of the value.
     * @throws ConfigurationException if the value was not found or could
     *                                not be converted to a doube.
     */
    public double getDoubleValue()
    throws ConfigurationException {
        Object object = this.getValue();
        if (object instanceof Number) return(((Number)object).doubleValue());
        try {
            return(Double.parseDouble(object.toString()));
        } catch (NumberFormatException e) {
            throw this.error("double", e);
        }
    }

    /**
     * <p>Return the double representation of this element value.</p>
     *
     * @param defaultValue the default value to assign if the element value was
     *                     not found or could not be converted.
     * @return the double value of the element value or the specified default.
     */
    public double getDoubleValue(double defaultValue) {
        try {
            return(this.getDoubleValue());
        } catch(ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the {@link String} representation of this element value.</p>
     *
     * <p>The returned {@link String} will be created invoking the
     * {@link Object#toString() toString()} method on the value instance.</p>
     *
     * @return the {@link String} value of the element value.
     * @throws ConfigurationException if the value was not found.
     */
    public String getStringValue()
    throws ConfigurationException {
        return(this.getValue().toString());
   }
    
    /**
     * <p>Return the {@link String} representation of this element value.</p>
     *
     * <p>The returned {@link String} will be created invoking the
     * {@link Object#toString() toString()} method on the value instance.</p>
     *
     * @param defaultValue the default value to assign if the element value was
     *                     not found or could not be converted.
     * @return the {@link String} value of the element value or the specified default.
     */
    public String getStringValue(String defaultValue) {
        try {
            return(this.getStringValue());
        } catch(ConfigurationException e) {
            return(defaultValue);
        }
    }

    /* ====================================================================== */
    
    /**
     * <p>Return the attribute value.</p>
     *
     * @param name the name of the attribute to retrieve.
     * @return the value of the attribute or <b>null</b> if the attribute was
     *         not found.
     */
    public Object getAttribute(Object name) {
        return(this.attributes.get(name));
    }
    
    /**
     * <p>Return the attribute value.</p>
     *
     * @param name the name of the attribute.
     * @param defaultValue the default value to return.
     * @return the value of the attribute or the specified default.
     */
    public Object getAttribute(Object name, Object defaultValue) {
        return(this.attributes.get(name, defaultValue));
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the boolean representation of the specified attribute.</p>
     *
     * @param name the name of the attribute to retrieve as a boolean.
     * @return the boolean value of the attribute.
     * @throws ConfigurationException if the attribute was not found or could
     *                                not be converted to a boolean.
     */
    public boolean getBooleanAttribute(String name)
    throws ConfigurationException {
        try {
            return(this.attributes.getBoolean(name));
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Attribute \"" + name +  "\" "
                                             + "invalid or not found", this);
        }
    }
    
    /**
     * <p>Return the boolean representation of the specified attribute.</p>
     *
     * @param name the name of the attribute to retrieve as a boolean.
     * @param defaultValue the default value to assign if the attribute was
     *                     not found or could not be converted.
     * @return the boolean value of the attribute or the specified default.
     */
    public boolean getBooleanAttribute(String name, boolean defaultValue) {
        return(this.attributes.getBoolean(name, defaultValue));
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the integer representation of the specified attribute.</p>
     *
     * @param name the name of the attribute to retrieve as an integer.
     * @return the integer value of the attribute.
     * @throws ConfigurationException if the attribute was not found or could
     *                                not be converted to an integer.
     */
    public int getIntegerAttribute(String name)
    throws ConfigurationException {
        try {
            return(this.attributes.getInteger(name));
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Attribute \"" + name +  "\" "
                                             + "invalid or not found", this);
        }
    }
    
    /**
     * <p>Return the integer representation of the specified attribute.</p>
     *
     * @param name the name of the attribute to retrieve as a integer.
     * @param defaultValue the default value to assign if the attribute was
     *                     not found or could not be converted.
     * @return the integer value of the attribute or the specified default.
     */
    public int getIntegerAttribute(String name, int defaultValue) {
        return(this.attributes.getInteger(name, defaultValue));
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the long integer representation of the specified attribute.</p>
     *
     * @param name the name of the attribute to retrieve as a long integer.
     * @return the long integer value of the attribute.
     * @throws ConfigurationException if the attribute was not found or could
     *                                not be converted to a long integer.
     */
    public long getLongAttribute(String name)
    throws ConfigurationException {
        try {
            return(this.attributes.getLong(name));
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Attribute \"" + name +  "\" "
                                             + "invalid or not found", this);
        }
    }
    
    /**
     * <p>Return the long integer representation of the specified attribute.</p>
     *
     * @param name the name of the attribute to retrieve as a long integer.
     * @param defaultValue the default value to assign if the attribute was
     *                     not found or could not be converted.
     * @return the long integer value of the attribute or the specified default.
     */
    public long getLongAttribute(String name, long defaultValue) {
        return(this.attributes.getLong(name, defaultValue));
    }
    
    /* ====================================================================== */
    
    /**
        * <p>Return the float representation of the specified attribute.</p>
     *
     * @param name the name of the attribute to retrieve as a float.
     * @return the float value of the attribute.
     * @throws ConfigurationException if the attribute was not found or could
     *                                not be converted to a float.
     */
    public float getFloatAttribute(String name)
    throws ConfigurationException {
        try {
            return(this.attributes.getFloat(name));
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Attribute \"" + name +  "\" "
                                             + "invalid or not found", this);
        }
    }
    
    /**
     * <p>Return the float representation of the specified attribute.</p>
     *
     * @param name the name of the attribute to retrieve as a float.
     * @param defaultValue the default value to assign if the attribute was
     *                     not found or could not be converted.
     * @return the float value of the attribute or the specified default.
     */
    public float getFloatAttribute(String name, float defaultValue) {
        return(this.attributes.getFloat(name, defaultValue));
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the double representation of the specified attribute.</p>
     *
     * @param name the name of the attribute to retrieve as a double.
     * @return the double value of the attribute.
     * @throws ConfigurationException if the attribute was not found or could
     *                                not be converted to a doube.
     */
    public double getDoubleAttribute(String name)
    throws ConfigurationException {
        try {
            return(this.attributes.getDouble(name));
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Attribute \"" + name +  "\" "
                                             + "invalid or not found", this);
        }
    }
    
    /**
     * <p>Return the double representation of the specified attribute.</p>
     *
     * @param name the name of the attribute to retrieve as a double.
     * @param defaultValue the default value to assign if the attribute was
     *                     not found or could not be converted.
     * @return the double value of the attribute or the specified default.
     */
    public double getDoubleAttribute(String name, double defaultValue) {
        return(this.attributes.getDouble(name, defaultValue));
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the {@link String} representation of the specified attribute.</p>
     *
     * <p>The returned {@link String} will be created invoking the
     * {@link Object#toString() toString()} method on the value instance.</p>
     *
     * @param name the name of the attribute to retrieve as a {@link String}.
     * @return the {@link String} value of the attribute.
     * @throws ConfigurationException if the attribute was not found.
     */
    public String getStringAttribute(String name)
    throws ConfigurationException {
        try {
            return(this.attributes.getString(name));
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Attribute \"" + name +  "\" "
                                             + "invalid or not found", this);
        }
    }
    
    /**
     * <p>Return the {@link String} representation of the specified attribute.</p>
     *
     * <p>The returned {@link String} will be created invoking the
     * {@link Object#toString() toString()} method on the value instance.</p>
     *
     * @param name the name of the attribute to retrieve as a {@link String}.
     * @param defaultValue the default value to assign if the attribute was
     *                     not found or could not be converted.
     * @return the {@link String} value of the attribute or the specified default.
     */
    public String getStringAttribute(String name, String defaultValue) {
        return(this.attributes.getString(name, defaultValue));
    }
    
    /* ====================================================================== */

    /**
     * <p>Return a human readable {@link String} representation of this
     * {@link Configuration} instance.</p>
     *
     * @return a <b>non null</b> {@link String}.
     */
    public String toString() {
        String value = getClass().getName() + "[" + name() + "]@" + hashCode();
        return(value);
    }

    /**
     * <p>Return the hash code of this {@link Configuration} instance.</p>
     *
     * @return the hash code.
     */
    public int hashCode() {
        return(this.hash);
    }

    /**
     * <p>Compare an {@link Object} for equality.</p>
     *
     * @param o an {@link Object} to compare for equality.
     * @return <b>true</b> if the object equals this, <b>false</b> otherwise.
     */
    public boolean equals(Object o) {
        /* Simple check */
        if (o == null) return (false);
        if (o == this) return (true);
        return(false);
    }
    
    /* ====================================================================== */

    /**
     * <p>Simple method to check the value of a child.</p>
     */
    private void check(Object child) {
        if (child == null) throw new NullPointerException("Can't add null");
        if (child instanceof Configuration) return;
        throw new ClassCastException("Unable to add non Configuration child");
    }

    /**
     * <p>Simple method to throw a nicely formatted exception.</p>
     */
    private ConfigurationException error(String type, Throwable throwable) {
        return new ConfigurationException("Value \""+ this.getStringValue("?")
                                          + "\" can "+ "not be converted to a "
                                          + type, this, throwable);
    }

    /**
     * <p>A simple iterator over children configurations.</p>
     */
    private static class ConfigurationIterator implements Iterator {
        /** <p>The next configuration element.</p> */
        private Configuration configuration = null;

        /** <p>The sub-iterator from where to fetch elements.</p> */
        private Iterator iterator = null;

        /** <p>The namespace of the {@link Configuration}s to return.</p> */
        private String namespace = null;

        /** <p>The name of the {@link Configuration}s to return.</p> */
        private String name = null;

        /**
         * <p>Create a new {@link ConfigurationIterator} returning a selected
         * part of the children of a specified {@link Configuration}.</p>
         *
         * @param c the {@link Configuration} to iterate for children.
         * @param ns the namespace of the {@link Configuration}s to return.
         * @param n the name of the {@link Configuration}s to return.
         */
        private ConfigurationIterator(Configuration c, String ns, String n) {
            this.iterator = c.iterator();
            this.namespace = ns;
            this.name = n;
        }

        /**
         * <p>Check if this {@link Iterator} can return the next element.</p>
         *
         * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
         * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
         * @version 1.0 (CVS $Revision: 1.1 $)
         */
        public boolean hasNext() {
            /* No name, no children. Bye! */
            if (this.name == null) return(false);

            /* Run on the nested iterator */
            while(this.iterator.hasNext()) {

                /* Return the next child configuration, whatever it is */
                Configuration child = (Configuration)iterator.next();

                /* If we process namespaces check both name and namespace */
                if (this.namespace != null) {
                    if ((!this.namespace.equals(child.namespace()))
                        || (!this.name.equals(child.name()))) continue;

                /* If we process only names, check only the name */
                } else if (!this.name.equals(child.name())) continue;

                /* If we didn't "continue" before, we have a match */
                this.configuration = child;
                return(true);
            }
            /* The nested iterator doesn't have any more children */
            return(false);
        }

        /**
         * <p>Return the next {@link Configuration} element available to this
         * {@link ConfigurationIterator}.</p>
         */
        public Object next() {
            /* If the "hasNext()" method didn't produce anything, try it */
            if ((this.configuration == null) && (!this.hasNext())) {
                throw new NoSuchElementException("Next element not found");
            }
            
            /* Return and invalidate the "next" object */
            Configuration current = this.configuration;
            this.configuration = null;
            return(current);
        }

        /**
         * <p>Block anyone attempting to remove only children.</p>
         */
        public void remove() {
            throw new UnsupportedOperationException("Use a normal iterator");
        }
    }
}
