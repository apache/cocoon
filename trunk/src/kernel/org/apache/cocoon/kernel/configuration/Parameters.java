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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>The {@link Parameters} class defines a simple {@link Map} that can
 * be used to configure components.</p>
 *
 * <p>A {@link Parameters} map differs from a regular {@link HashMap} in the
 * sense that no <b>null</b> keys or values are accepted, and that keys can
 * only be {@link String} instances.</p>
 *
 * <p>This class also provides simple methods for resolving values to some
 * of the Java&trade; primitive types: boolean, int, long, float, double and
 * {@link String}.</p>
 *
 * <p>Validation of {@link Parameters} instances can be performed using the
 * {@link Parameters} object.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class Parameters extends HashMap implements Map {

    /** <p>Whether this instance is locked or not.</p> */
    private boolean locked = false;

    /**
     * <p>Create a new empty {@link Parameters} instance.</p>
     */
    public Parameters() {
        super();
    }

    /**
     * <p>Create a new {@link Parameters} instance from a {@link Configuration}
     * instance.</p>
     *
     * @throws ConfigurationException if the instance cannot be created.
     */
    public Parameters(Configuration configuration)
    throws ConfigurationException {
        super();
        String v = "value";
        Iterator iterator = configuration.children("parameter");
        while (iterator.hasNext()) try {
            Configuration c = (Configuration)iterator.next();
            String n = c.getStringAttribute("name");
            String t = c.getStringAttribute("type", "string");
            if ("string".equalsIgnoreCase(t)) {
                this.put(n, c.getStringValue(c.getStringAttribute("value")));
            } else if ("boolean".equalsIgnoreCase(t)) {
                this.put(n, c.getBooleanValue(c.getBooleanAttribute("value")));
            } else if ("double".equalsIgnoreCase(t)) {
                this.put(n, c.getDoubleValue(c.getDoubleAttribute("value")));
            } else if ("float".equalsIgnoreCase(t)) {
                this.put(n, c.getFloatValue(c.getFloatAttribute("value")));
            } else if ("integer".equalsIgnoreCase(t)) {
                this.put(n, c.getIntegerValue(c.getIntegerAttribute("value")));
            } else if ("long".equalsIgnoreCase(t)) {
                this.put(n, c.getLongValue(c.getLongAttribute("value")));
            } else if ("configuration".equalsIgnoreCase(t)) {
                if (c.size() == 1) this.put(n, c.get(0));
                throw new ConfigurationException("Too many/few children for "
                                                 + "parameter \"" + n + "\" of"
                                                 + "type \"configuration\"", c);
            } else {
                this.put(n, c.getValue(c.getAttribute("value")));
            }
        } catch (ConfigurationException e) {
            throw new ConfigurationException(e.getMessage(), configuration, e);
        }
    }

    /**
     * <p>Lock this {@link Parameters} instance.</p>
     *
     * <p>After this method is called, no further modifications are allowed
     * into this {@link Parameters} instance, and any call to the
     * {@link #put(Object,Object)} or {@link #remove(Object)} methods will
     * throw an {@link UnsupportedOperationException}.</p>
     *
     * @return this {@link Parameters} instance.
     */
    public Parameters lock() {
        this.locked = true;
        return(this);
    }

    /* ====================================================================== */

    /**
     * <p>Return the parameter value.</p>
     *
     * @param name the name of the parameter to retrieve.
     * @return the value of the parameter or <b>null</b> if the parameter was
     *         not found.
     */
    public Object get(Object name) {
        if (name == null) return(null);
        return(super.get(name));
    }

    /**
     * <p>Return the parameter value.</p>
     *
     * @param name the name of the parameter.
     * @param defaultValue the default value to return.
     * @return the value of the parameter or the specified default.
     */
    public Object get(Object name, Object defaultValue) {
        Object value = this.get(name);
        return(value == null? defaultValue: value);
    }
    
    /**
     * <p>Put a new parameter in this {@link Parameters} map.</p>
     *
     * @param name the name of the parameter.
     * @param value the value of the parameter.
     * @throws NullPointerException if either name or value are <b>null</b>.
     * @throws ClassCastException if the name is not a {@link String}.
     * @throws UnsupportedOperationException if this instance is locked.
     */
    public Object put(Object name, Object value) {
        if (this.locked)  throw new UnsupportedOperationException("Locked");
        if (name == null) throw new NullPointerException("Null name");
        if (value == null) throw new NullPointerException("Null value");
        if (name instanceof String) return(super.put(name, value));
        throw new ClassCastException("Invalid parameter name");
    }
    
    /**
     * <p>Put a new boolean parameter in this {@link Parameters} map.</p>
     *
     * @param name the name of the parameter.
     * @param value the value of the parameter.
     * @throws NullPointerException if the name was <b>null</b>.
     * @throws UnsupportedOperationException if this instance is locked.
     */
    public Object put(Object name, boolean value) {
        return this.put(name, new Boolean(value));
    }

    /**
     * <p>Put a new double parameter in this {@link Parameters} map.</p>
     *
     * @param name the name of the parameter.
     * @param value the value of the parameter.
     * @throws NullPointerException if the name was <b>null</b>.
     * @throws UnsupportedOperationException if this instance is locked.
     */
    public Object put(Object name, double value) {
        return this.put(name, new Double(value));
    }

    /**
     * <p>Put a new float parameter in this {@link Parameters} map.</p>
     *
     * @param name the name of the parameter.
     * @param value the value of the parameter.
     * @throws NullPointerException if the name was <b>null</b>.
     * @throws UnsupportedOperationException if this instance is locked.
     */
    public Object put(Object name, float value) {
        return this.put(name, new Float(value));
    }

    /**
     * <p>Put a new int parameter in this {@link Parameters} map.</p>
     *
     * @param name the name of the parameter.
     * @param value the value of the parameter.
     * @throws NullPointerException if the name was <b>null</b>.
     * @throws UnsupportedOperationException if this instance is locked.
     */
    public Object put(Object name, int value) {
        return this.put(name, new Integer(value));
    }

    /**
     * <p>Put a new long parameter in this {@link Parameters} map.</p>
     *
     * @param name the name of the parameter.
     * @param value the value of the parameter.
     * @throws NullPointerException if the name was <b>null</b>.
     * @throws UnsupportedOperationException if this instance is locked.
     */
    public Object put(Object name, long value) {
        return this.put(name, new Long(value));
    }

    /**
     * <p>Remove a parameter in this {@link Parameters} map.</p>
     *
     * @param name the name of the parameter.
     * @return the value of the parameter or <b>null</b> if the parameter was
     *         not found.
     */
    public Object remove(Object name) {
        if (name == null) return(null);
        return(super.remove(name));
    }

    /**
     * <p>Returns an unlocked shallow copy of this {@link Parameters}
     * instance.</p>
     *
     * <p>The parameter names and  values themselves are not cloned, and
     * the returned instance will not be locked.</p>
     *
     * @return a new <b>non null</b> cloned {@link Parameters} instance.
     */
    public Object clone() {
        Parameters parameters = (Parameters) super.clone();
        parameters.locked = false;
        return(parameters);
    }

    /* ====================================================================== */

    /**
     * <p>Return the boolean representation of the specified parameter.</p>
     *
     * @param name the name of the parameter to retrieve as a boolean.
     * @return the boolean value of the parameter.
     * @throws ConfigurationException if the parameter was not found or could
     *                                not be converted to a boolean.
     */
    public boolean getBoolean(String name)
    throws ConfigurationException {
        Object object = this.check(name);
        if (object instanceof Boolean) return(((Boolean)object).booleanValue());
        String string = object.toString();
        if ("0".equals(string)) return(false);
        if ("1".equals(string)) return(true);
        if ("false".equalsIgnoreCase(string)) return(false);
        if ("true".equalsIgnoreCase(string)) return(true);
        throw this.error(name, "boolean", null);
    }

    /**
     * <p>Return the boolean representation of the specified parameter.</p>
     *
     * @param name the name of the parameter to retrieve as a boolean.
     * @param defaultValue the default value to assign if the parameter was
     *                     not found or could not be converted.
     * @return the boolean value of the parameter or the specified default.
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        try {
            return(this.getBoolean(name));
        } catch(ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the integer representation of the specified parameter.</p>
     *
     * @param name the name of the parameter to retrieve as an integer.
     * @return the integer value of the parameter.
     * @throws ConfigurationException if the parameter was not found or could
     *                                not be converted to an integer.
     */
    public int getInteger(String name)
    throws ConfigurationException {
        Object object = this.check(name);
        if (object instanceof Number) return(((Number)object).intValue());
        try {
            return(Integer.parseInt(object.toString()));
        } catch (NumberFormatException e) {
            throw this.error(name, "int", e);
        }
    }
    
    /**
     * <p>Return the integer representation of the specified parameter.</p>
     *
     * @param name the name of the parameter to retrieve as a integer.
     * @param defaultValue the default value to assign if the parameter was
     *                     not found or could not be converted.
     * @return the integer value of the parameter or the specified default.
     */
    public int getInteger(String name, int defaultValue) {
        try {
            return(this.getInteger(name));
        } catch(ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the long integer representation of the specified parameter.</p>
     *
     * @param name the name of the parameter to retrieve as a long integer.
     * @return the long integer value of the parameter.
     * @throws ConfigurationException if the parameter was not found or could
     *                                not be converted to a long integer.
     */
    public long getLong(String name)
    throws ConfigurationException {
        Object object = this.check(name);
        if (object instanceof Number) return(((Number)object).longValue());
        try {
            return(Long.parseLong(object.toString()));
        } catch (NumberFormatException e) {
            throw this.error(name, "long", e);
        }
    }
    
    /**
     * <p>Return the long integer representation of the specified parameter.</p>
     *
     * @param name the name of the parameter to retrieve as a long integer.
     * @param defaultValue the default value to assign if the parameter was
     *                     not found or could not be converted.
     * @return the long integer value of the parameter or the specified default.
     */
    public long getLong(String name, long defaultValue) {
        try {
            return(this.getLong(name));
        } catch(ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the float representation of the specified parameter.</p>
     *
     * @param name the name of the parameter to retrieve as a float.
     * @return the float value of the parameter.
     * @throws ConfigurationException if the parameter was not found or could
     *                                not be converted to a float.
     */
    public float getFloat(String name)
    throws ConfigurationException {
        Object object = this.check(name);
        if (object instanceof Number) return(((Number)object).floatValue());
        try {
            return(Float.parseFloat(object.toString()));
        } catch (NumberFormatException e) {
            throw this.error(name, "float", e);
        }
    }

    /**
     * <p>Return the float representation of the specified parameter.</p>
     *
     * @param name the name of the parameter to retrieve as a float.
     * @param defaultValue the default value to assign if the parameter was
     *                     not found or could not be converted.
     * @return the float value of the parameter or the specified default.
     */
    public float getFloat(String name, float defaultValue) {
        try {
            return(this.getFloat(name));
        } catch(ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the double representation of the specified parameter.</p>
     *
     * @param name the name of the parameter to retrieve as a double.
     * @return the double value of the parameter.
     * @throws ConfigurationException if the parameter was not found or could
     *                                not be converted to a doube.
     */
    public double getDouble(String name)
    throws ConfigurationException {
        Object object = this.check(name);
        if (object instanceof Number) return(((Number)object).doubleValue());
        try {
            return(Double.parseDouble(object.toString()));
        } catch (NumberFormatException e) {
            throw this.error(name, "double", e);
        }
    }

    /**
     * <p>Return the double representation of the specified parameter.</p>
     *
     * @param name the name of the parameter to retrieve as a double.
     * @param defaultValue the default value to assign if the parameter was
     *                     not found or could not be converted.
     * @return the double value of the parameter or the specified default.
     */
    public double getDouble(String name, double defaultValue) {
        try {
            return(this.getDouble(name));
        } catch(ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the {@link String} representation of the specified parameter.</p>
     *
     * <p>The returned {@link String} will be created invoking the
     * {@link Object#toString() toString()} method on the value instance.</p>
     *
     * @param name the name of the parameter to retrieve as a {@link String}.
     * @return the {@link String} value of the parameter.
     * @throws ConfigurationException if the parameter was not found.
     */
    public String getString(String name)
    throws ConfigurationException {
        return(this.check(name).toString());
   }
    
    /**
     * <p>Return the {@link String} representation of the specified parameter.</p>
     *
     * <p>The returned {@link String} will be created invoking the
     * {@link Object#toString() toString()} method on the value instance.</p>
     *
     * @param name the name of the parameter to retrieve as a {@link String}.
     * @param defaultValue the default value to assign if the parameter was
     *                     not found or could not be converted.
     * @return the {@link String} value of the parameter or the specified default.
     */
    public String getString(String name, String defaultValue) {
        try {
            return(this.getString(name));
        } catch(ConfigurationException e) {
            return(defaultValue);
        }
    }

    /* ====================================================================== */
    
    /**
     * <p>Return the {@link Configuration} (complex) value of the specified
     * parameter.</p>
     *
     * <p>The parameter value <b>must</b> be stored as a {@link Configuration}
     * object in this instance, otherwise this method will fail.</p>
     *
     * @param name the name of the {@link Configuration} parameter to retrieve.
     * @return the {@link Configuration} value of the parameter.
     * @throws ConfigurationException if the parameter was not found or its
     *                                value was not a {@link Configuration}.
     */
    public Configuration getConfiguration(String name)
    throws ConfigurationException {
        try {
            return((Configuration)this.check(name));
        } catch (ClassCastException e) {
            throw this.error(name, "configuration", e);
        }
    }
    
    /**
     * <p>Return the {@link Configuration} (complex) value of the specified
     * parameter.</p>
     *
     * <p>The parameter value <b>must</b> be stored as a {@link Configuration}
     * object in this instance, otherwise this method will return the default
     * value.</p>
     *
     * @param name the name of the {@link Configuration} parameter to retrieve.
     * @param defaultValue the default value to assign if the parameter was
     *                     not found or was not a {@link Configuration}.
     * @return the {@link Configuration} value of the parameter.
     * @throws ConfigurationException if the parameter was not found or its
     *                                value was not a {@link Configuration}.
     */
    public Configuration getConfiguration(String name,
                                          Configuration defaultValue) {
        try {
            return(this.getConfiguration(name));
        } catch(ConfigurationException e) {
            return(defaultValue);
        }
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Verify that the specified parameter is of the specified type.</p>
     *
     * @param name the parameter name.
     * @param type the parameter type.
     * @return <b>true</b> if the parameter exists and is of the specified
     *         type, <b>false</b> otherwise.
     * @throws UnsupportedOperationException if type was unrecognized.
     */
    public boolean verify(String name, String type) {
        try {
            if ("string".equalsIgnoreCase(type)) {
                this.getString(name);
            } else if ("boolean".equalsIgnoreCase(type)) {
                this.getBoolean(name);
            } else if ("double".equalsIgnoreCase(type)) {
                this.getDouble(name);
            } else if ("float".equalsIgnoreCase(type)) {
                this.getFloat(name);
            } else if ("integer".equalsIgnoreCase(type)) {
                this.getInteger(name);
            } else if ("long".equalsIgnoreCase(type)) {
                this.getLong(name);
            } else if ("configuration".equalsIgnoreCase(type)) {
                this.getConfiguration(name);
            } else {
                throw new UnsupportedOperationException("Unknown type " + type);
            }
            /* We got here without exceptions, then all is fine */
            return(true);
        } catch (ConfigurationException e) {
            /* Configuration exception? no value or can't convert */
            return(false);
        }
    }

    /* ====================================================================== */

    /**
     * <p>Simple method to check the value of a parameter.</p>
     */
    private Object check(String name)
    throws ConfigurationException {
        if (name == null) throw new ConfigurationException("Null name");
        Object value = this.get(name);
        if (value != null) return(value);
        throw new ConfigurationException("Parameter \"" + name
                                         + "\" not found");
    }

    /**
     * <p>Simple method to throw a nicely formatted exception.</p>
     */
    private ConfigurationException error(String n, String t, Throwable x) {
        return new ConfigurationException("Parameter \"" + n + "\" value \""
                                          + this.getString(n, "?") + "\" can "
                                          + "not be converted to a " + t, x);
    }
}
