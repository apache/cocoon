// $Id: AbstractValidatorAction.java,v 1.1.2.8 2001-05-10 14:14:13 mman Exp $
package org.apache.cocoon.acting;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.AbstractLoggable;
import org.apache.log.Logger;

import java.util.Map;

/**
 * Abstract implementation of action that needs to perform validation of
 * parameters (from session, from request, etc.). All `validator' actions
 * share the same description xml file. In such file every parameter is
 * described via its name, type and its constraints. One large description
 * file can be used among all validator actions, because each action should
 * explicitely specify which parameters to validate - through a sitemap
 * parameter.
 *
 * <pre>
 * &lt;root&gt;
 *         &lt;parameter name="username" type="string" nullable="no"/&gt;
 *         &lt;parameter name="role" type="string" nullable="no"/&gt;
 *         &lt;parameter name="oldpassword" type="string" nullable="no"/&gt;
 *         &lt;parameter name="newpassword" type="string" nullable="no"/&gt;
 *         &lt;parameter name="renewpassword" type="string" nullable="no"/&gt;
 *         &lt;parameter name="id" type="long" nullable="no"/&gt;
 *         &lt;parameter name="sallary" type="double" nullable="no"/&gt;
 *         &lt;parameter name="theme" type="string" nullable="yes" default="dflt"/&gt;
 *         &lt;constraint-set name="is-logged-in"&gt;
 *                 &lt;validate name="username"/&gt;
 *                 &lt;validate name="role"/&gt;
 *         &lt;/constraint-set&gt;
 *
 *         &lt;constraint-set name="is-in-admin-role"&gt;
 *                 &lt;validate name="username"/&gt;
 *                 &lt;validate name="role" equals-to="admin"/&gt;
 *         &lt;/constraint-set&gt;
 *
 *         &lt;constraint-set name="new-passwords-match"&gt;
 *                 &lt;validate name="oldpassword"/&gt;
 *                 &lt;validate name="newpassword"/&gt;
 *                 &lt;validate name="renewpassword" 
 *                         equals-to-param="newpass"/&gt;
 *         &lt;/constraint-set&gt;
 * &lt;/root&gt;
 * </pre>
 *
 * <h3>The types recognized by validator and their attributes</h3>
 * <table border="1">
 *         <tr>
 *                 <td><b>string</b></td><td>nullable="yes|no" default="str"</td>
 *         </tr>
 *         <tr>
 *                 <td><b>long</b></td><td>nullable="yes|no" default="123123"</td>
 *         </tr>
 *         <tr>
 *                 <td><b>double</b></td><td>nullable="yes|no" default="0.5"</td>
 *         </tr>
 * </table>
 * Default value takes place only when specified parameter is nullable and
 * really is null or empty. Long numbers may be specified in decimal, hex or
 * octal values as accepted by java.Lang.decode (String s).
 *
 * <h3>The attributes recognized in "constraint-set"</h3>
 * <strong>FIXME: this works only with strings for now</strong>
 * <table>
 * <tr>
 *     <td>equals-to-param</td><td>parameter name</td>
 *     <td>equals-to</td><td>string constant</td>
 * </tr>
 * </table>
 * @author Martin Man &lt;Martin.Man@seznam.cz&gt;
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2001-05-10 14:14:13 $
 */
public abstract class AbstractValidatorAction
extends AbstractComplementaryConfigurableAction
implements Configurable
{
    /**
     * Try to validate given parameter.
     * @param name The name of the parameter to validate.
     * @param constraints Configuration of all constraints for this parameter
     * as taken from the description XML file.
     * @param conf Configuration of all parameters as taken from the
     * description XML file.
     * @param params The map of parameters.
     * @param is_string Indicates wheter given param to validate is string
     * (as taken from HTTP request for example) or wheteher it should be
     * regular instance of java.lang.Double, java.lang.Long, etc.
     * @return The validated parameter.
     * FIXME: should be reworked so that validation returns something
     * meaningfull in case the parameter is nullable and really is null (which
     * is o.k.) but returns null. Workaround for now is that empty string is
     * returned for nullable strings and -1 for numbers.
     */
    public Object validateParameter (String name, Configuration constraints, 
            Configuration[] conf, Map params, boolean is_string) {
        String type = null;
        int i = 0;

        getLogger().debug ("VALIDATOR: validating parameter: " + name);

        /* try to find matching param description in conf tree */
        try {
            boolean found = false;
            for (i = 0; i < conf.length; i ++) {
                if (name.equals (conf[i].getAttribute ("name"))) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                getLogger ().debug ("VALIDATOR: description for parameter "
                        + name + " not found");
                return null;
            }

            /* check parameter's type */
            type = conf[i].getAttribute ("type");
        } catch (Exception e) {
            getLogger ().debug ("VALIDATOR: no type specified for parameter "
                    + name);
            return null;
        }

        /*
         * Validation phase
         */
        if ("string".equals (type)) {
            return validateString (name, constraints, 
                    conf[i],params, is_string);
        } else if ("long".equals (type)) {
            return validateLong (name, constraints, 
                    conf[i], params, is_string);
        } else if ("double".equals (type)) {
            return validateDouble (name, constraints, 
                    conf[i], params, is_string);
        } else {
            getLogger().debug ("VALIDATOR: unknown type " + type
                    + " specified for parameter " + name);
        }
        return null;
    }
    
    /**
     * Validates nullability and default value for given parameter. If given
     * constraints are not null they are validated as well.
     */
    private Object validateString (String name, Configuration constraints,
            Configuration conf, Map params, boolean is_string) {
        Object param = params.get (name);
        String value = null;
        getLogger().debug ("VALIDATOR: validating string parameter "
                + name + " (encoded in a string: " + is_string + ")");
        value = getStringValue (name, conf, param, is_string);
        if (value == null) {
            getLogger().debug ("VALIDATOR: string parameter "
                    + name + " is null");
            return null;
        }
        if (constraints != null) {
            String eq = constraints.getAttribute ("equals-to", "");
            String eqp = constraints.getAttribute ("equals-to-param", "");

            // Validate whether param is equal to constant
            if (!"".equals (eq)) {
                getLogger().debug ("VALIDATOR: string parameter "
                        + name + "should be equal to " + eq);
                if (!value.equals (eq)) {
                    getLogger().debug ("VALIDATOR: and it is not");
                    return null;
                }
            }

            // Validate whether param is equal to another param
            // FIXME: take default value of param being compared with into
            // account?
            if (!"".equals (eqp)) {
                getLogger().debug ("VALIDATOR: string parameter "
                        + name + "should be equal to " + params.get (eqp));
                if (!value.equals (params.get (eqp))) {
                    getLogger().debug ("VALIDATOR: and it is not");
                    return null;
                }
            }
        }
        return value;
    }

    /**
     * Validates nullability and default value for given parameter. If given
     * constraints are not null they are validated as well.
     */
    private Object validateLong (String name, Configuration constraints,
            Configuration conf, Map params, boolean is_string) {
        Object param = params.get (name);
        Long value = null;
        getLogger().debug ("VALIDATOR: validating long parameter "
                + name + " (encoded in a string: " + is_string + ")");
        value = getLongValue (name, conf, param, is_string);
        if (value == null) {
            getLogger().debug ("VALIDATOR: long parameter "
                    + name + " is null");
            return null;
        }
        if (constraints != null) {
            /* FIXME: add other checks as-well */
        }
        return value;
    }

    /**
     * Validates nullability and default value for given parameter. If given
     * constraints are not null they are validated as well.
     */
    private Object validateDouble (String name, Configuration constraints,
            Configuration conf, Map params, boolean is_string) {
        Object param = params.get (name);
        Double value = null;
        getLogger().debug ("VALIDATOR: validating double parameter "
                + name + " (encoded in a string: " + is_string + ")");
        value = getDoubleValue (name, conf, param, is_string);
        if (value == null) {
            getLogger().debug ("VALIDATOR: double parameter "
                    + name + " is null");
            return null;
        }
        if (constraints != null) {
            /* FIXME: add other checks as-well */
        }
        return value;
    }

    /**
     * Checks whether param is nullable, if so checks for default value and
     * returns it, otherwise it returns the parsed Double value.
     * If the parameter is nullable and really is null and no default value
     * was specified, return -1 so that the validation succeeds.
     */
    private Double getDoubleValue (String name,
            Configuration conf, Object param, boolean is_string) {
        boolean nullable = false;
        Double dflt = null;
        Double value = null;
        nullable = getNullable (conf);
        /* check for default value */
        try {
            String tmp = conf.getAttribute ("default", "");
            if ("".equals (tmp.trim ())) {
                tmp = null;
            }
            dflt = Double.valueOf (tmp);
        } catch (Exception e) {
            dflt = null;
        }
        /* convert param to double */
        if (is_string) {
            String tmp = (String)param;
            if (tmp != null && "".equals (tmp.trim ())) {
                tmp = null;
            }
            try {
                value = Double.valueOf (tmp);
            } catch (Exception e) {
                value = null;
            }
        } else {
            try {
                value = (Double)param;
            } catch (Exception e) {
                value = null;
            }
        }
        /* return appropriate value */
        if (nullable && value == null) {
            return dflt != null ? dflt : new Double ("-1");
        } else {
            return value;
        }
    }

    /**
     * Checks whether param is nullable, if so checks for default value and
     * returns it, otherwise it returns the parsed Double value.
     * If the parameter is nullable and really is null and no default value
     * was specified, return -1 so that the validation succeeds.
     */
    private Long getLongValue (String name,
            Configuration conf, Object param, boolean is_string) {
        boolean nullable = false;
        Long dflt = null;
        Long value = null;
        nullable = getNullable (conf);
        /* check for default value */
        try {
            String tmp = conf.getAttribute ("default", "");
            if ("".equals (tmp.trim ())) {
                tmp = null;
            }
            dflt = Long.decode (tmp);
        } catch (Exception e) {
            dflt = null;
        }
        /* convert param to long */
        if (is_string) {
            String tmp = (String)param;
            if (tmp != null && "".equals (tmp.trim ())) {
                tmp = null;
            }
            try {
                value = Long.decode (tmp);
            } catch (Exception e) {
                value = null;
            }
        } else {
            try {
                value = (Long)param;
            } catch (Exception e) {
                value = null;
            }
        }
        /* return appropriate value */
        if (nullable && value == null) {
            return dflt != null ? dflt : new Long ("-1");
        } else {
            return value;
        }
    }

    /**
     * Checks whether param is nullable, if so checks for default value and
     * returns it, otherwise it returns the parsed Double value.
     * If the parameter is nullable and really is null and no default value
     * was specified, return "" so that the validation succeeds.
     */
    private String getStringValue (String name,
            Configuration conf, Object param, boolean is_string) {
        boolean nullable = false;
        String dflt = null;
        String value = null;
        nullable = getNullable (conf);
        /* check for default value */
        try {
            dflt = conf.getAttribute ("default", "");
            if ("".equals (dflt.trim ())) {
                dflt = null;
            }
        } catch (Exception e) {
            dflt = null;
        }
        /* convert param to string */
        try {
            value = (String)param;
            if (value != null && "".equals (value.trim ())) {
                value = null;
            }
        } catch (Exception e) {
            value = null;
        }
        /* return appropriate value */
        if (nullable && value == null) {
            return dflt != null ? dflt : "";
        } else {
            return value;
        }
    }

    /**
     * Returns the value of 'nullable' attribute from given configuration.
     */
    private boolean getNullable (Configuration conf) {
        /* check nullability */
        try {
            String tmp = conf.getAttribute ("nullable", "no");
            if ("yes".equals (tmp) || "true".equals (tmp)) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
}

// $Id: AbstractValidatorAction.java,v 1.1.2.8 2001-05-10 14:14:13 mman Exp $
// vim: set et ts=4 sw=4:
