// $Id: AbstractValidatorAction.java,v 1.1.2.2 2001-04-17 18:18:04 dims Exp $
package org.apache.cocoon.acting;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import org.apache.avalon.configuration.Parameters;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.apache.cocoon.*;
import org.apache.cocoon.util.Tokenizer;

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
 * 	&lt;parameter name="username" type="string" nullable="no"/&gt;
 * 	&lt;parameter name="id" type="long" nullable="no"/&gt;
 * 	&lt;parameter name="sallary" type="double" nullable="no"/&gt;
 * 	&lt;parameter name="theme" type="string" nullable="yes" default="dflt"/&gt;
 * &lt;/root&gt;
 * </pre>
 *
 * The types recognized by validator and its possible validation parameters
 * <table border="1">
 * 	<tr>
 * 		<td><b>string</b></td><td>nullable="yes|no" default="str"</td>
 * 	</tr>
 * 	<tr>
 * 		<td><b>long</b></td><td>nullable="yes|no" default="123123"</td>
 * 	</tr>
 * 	<tr>
 * 		<td><b>double</b></td><td>nullable="yes|no" default="0.5"</td>
 * 	</tr>
 * </table>
 * Default value takes place only when specified parameter is nullable and
 * really is null or empty. Long numbers may be specified in decimal, hex or
 * octal values as accepted by java.Lang.decode (String s).
 * 
 * @author Martin Man &lt;Martin.Man@seznam.cz&gt;
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-04-17 18:18:04 $
 */
public abstract class AbstractValidatorAction 
extends AbstractComplimentaryConfigurableAction
implements Configurable
{
    /**
     * Try to validate given parameter.
     * @param name The name of the parameter to validate.
     * @param conf Configuration of all parameters as taken from the
     * description XML file.
     * @param param The value of the parameter.
     * @param is_string Indicates wheter given param to validate is string
     * (as taken from HTTP request for example) or wheteher it should be
     * regular instance of java.lang.Double, java.lang.Long, etc.
     * @return The validated parameter.
     */
    public Object validateParameter (String name, Configuration[] conf,
            Object param, boolean is_string) {
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
            return null;
        }

        /*
         * Validation phase
         */
        if ("string".equals (type)) {
            return validateString (name, conf[i], param, is_string);
        } else if ("long".equals (type)) {
            return validateLong (name, conf[i], param, is_string);
        } else if ("double".equals (type)) {
            return validateDouble (name, conf[i], param, is_string);
        } else {
            getLogger().debug ("VALIDATOR: unknown type " + type
                    + " specified for parameter " + name);
        }
        return null;
    }

    private Object validateString (String name,
            Configuration conf, Object param, boolean is_string) {
        String value = null;
        value = getStringValue (name, conf, param, is_string);
        if (value == null) {
            return null;
        }
        /* FIXME: add other checks as-well */
        return value;
    }

    private Object validateLong (String name,
            Configuration conf, Object param, boolean is_string) {
        Long value = null;
        value = getLongValue (name, conf, param, is_string);
        if (value == null) {
            return null;
        }
        /* FIXME: add other checks as-well */
        return value;
    }

    private Object validateDouble (String name,
            Configuration conf, Object param, boolean is_string) {
        Double value = null;
        value = getDoubleValue (name, conf, param, is_string);
        if (value == null) {
            return null;
        }
        /* FIXME: add other checks as-well */
        return value;
    }

    /**
     * Checks whether param is nullable, if so checks for default value and
     * returns it, otherwise it returns the parsed Double value.
     */
    private Double getDoubleValue (String name,
            Configuration conf, Object param, boolean is_string) {
        boolean nullable = false;
        Double dflt = null;
        Double value = null;
        nullable = getNullable (conf);
        /* check for default value */
        try {
            String tmp = conf.getAttribute ("default");
            if (tmp != null && "".equals (tmp.trim ())) {
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
            } catch (NumberFormatException e) {
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
            return dflt;
        } else {
            return value;
        }
    }

    /**
     * Checks whether param is nullable, if so checks for default value and
     * returns it, otherwise it returns the parsed Double value.
     */
    private Long getLongValue (String name,
            Configuration conf, Object param, boolean is_string) {
        boolean nullable = false;
        Long dflt = null;
        Long value = null;
        nullable = getNullable (conf);
        /* check for default value */
        try {
            String tmp = conf.getAttribute ("default");
            if (tmp != null && "".equals (tmp.trim ())) {
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
            } catch (NumberFormatException e) {
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
            return dflt;
        } else {
            return value;
        }
    }

    /**
     * Checks whether param is nullable, if so checks for default value and
     * returns it, otherwise it returns the parsed Double value.
     */
    private String getStringValue (String name,
            Configuration conf, Object param, boolean is_string) {
        boolean nullable = false;
        String dflt = null;
        String value = null;
        nullable = getNullable (conf);
        /* check for default value */
        try {
            dflt = conf.getAttribute ("default");
            if (dflt != null && "".equals (dflt.trim ())) {
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
            return dflt;
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
            String tmp = conf.getAttribute ("nullable");
            if ("yes".equals (tmp) || "true".equals (tmp)) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
}

// $Id: AbstractValidatorAction.java,v 1.1.2.2 2001-04-17 18:18:04 dims Exp $
// vim: set et ts=4 sw=4:
