/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.acting;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

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
 *
 * <p>Default value takes place only when specified parameter is
 * nullable and really is null or empty. Long numbers may be specified
 * in decimal, hex or octal values as accepted by java.Lang.decode
 * (String s).</p>
 *
 * <h3>Constraints</h3>
 * <table border="1">
 * <tr>
 *     <td>matches-regex</td><td>POSIX regular expression</td>
 * </tr>
 * <tr>
 *     <td>min-len</td><td>positive integer</td>
 * </tr>
 * <tr>
 *     <td>max-len</td><td>positive integer</td>
 * </tr>
 * <tr>
 *     <td>min</td><td>Double / Long</td>
 * </tr>
 * <tr>
 *     <td>max</td><td>Double / Long</td>
 * </tr>
 * </table>
 *
 * <p>Constraints can be defined globally for a parameter and can be
 * overridden by redefinition in a constraint-set. Thus if e.g. a
 * database field can take at maximum 200 character, this property can
 * be set globally.</p>
 *
 * <p>Values in parameter arrays are validated individually and the
 * worst error is reported back.</p>
 *
 * <h3>The attributes recognized in "constraint-set"</h3>
 * <table>
 * <tr>
 *     <td>equals-to-param</td><td>parameter name</td>
 * </tr>
 * <tr>
 *     <td>equals-to</td><td>string constant</td>
 * </tr>
 * </table>
 * @author <a href="mailto:Martin.Man@seznam.cz">Martin Man</a>
 * @author <a href="mailto:haul@informatik.tu-darmstadt.de">Christian Haul</a>
 * @version CVS $Id: AbstractValidatorAction.java,v 1.1 2003/03/09 00:08:38 pier Exp $
 */
public abstract class AbstractValidatorAction
extends AbstractComplementaryConfigurableAction
implements Configurable
{
    /**
     * Try to validate given parameter.
     * @param name The name of the parameter to validate.
     * @param constraints Configuration of all constraints for this
     * parameter as taken from the description XML file.
     * @param conf Configuration of all parameters as taken from the
     * description XML file.
     * @param params The map of parameters.
     * @param isString Indicates wheter given param to validate is
     * string (as taken from HTTP request for example) or wheteher it
     * should be regular instance of java.lang.Double, java.lang.Long,
     * etc.
     * @return The validated parameter.
     */
    public ValidatorActionHelper validateParameter(String name, Configuration constraints,
            Configuration[] conf, Map params, boolean isString) {

        return validateParameter(name, name, constraints, conf, params, isString);
    }

    /**
     * Try to validate given parameter.
     * @param name The actual name of the parameter to validate.
     * @param rule The name of the parameter element that contains the
     * rule that should be used for validation.
     * @param constraints Configuration of all constraints for this
     * parameter as taken from the description XML file.
     * @param conf Configuration of all parameters as taken from the
     * description XML file.
     * @param params The map of parameters.
     * @param isString Indicates wheter given param to validate is
     * string (as taken from HTTP request for example) or wheteher it
     * should be regular instance of java.lang.Double, java.lang.Long,
     * etc.
     * @return The validated parameter.
     */
    public ValidatorActionHelper validateParameter(String name, String rule, Configuration constraints,
            Configuration[] conf, Map params, boolean isString) {
        String type = null;
        int i = 0;

        if (getLogger().isDebugEnabled())
            getLogger().debug ("Validating parameter: " + name + " using rule: "+rule);

        /* try to find matching param description in conf tree */
        try {
            boolean found = false;
            for (i = 0; i < conf.length; i ++) {
                if (rule.equals (conf[i].getAttribute ("name"))) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Description for parameter "
                                      + name + " / " + rule + " not found");
                return null;
            }

            /* check parameter's type */
            type = conf[i].getAttribute ("type");
        } catch (Exception e) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("No type specified for parameter " + name);
            return null;
        }

        /*
         * Validation phase
         */
        Object value = params.get(name);
        
        if (value!=null && value.getClass().isArray()) {
            Object[] values = (Object[]) value;
            ValidatorActionHelper vaH = null;
            ValidatorActionResult vaR = ValidatorActionResult.OK;
            for (int j=0; j<values.length; j++) {
                value = values[j];
                if ("string".equals (type)) {
                    vaH = validateString(name, constraints, conf[i], params, value);
                } else if ("long".equals (type)) {
                    vaH = validateLong(name, constraints, conf[i], params, isString, value);
                } else if ("double".equals (type)) {
                    vaH = validateDouble(name, constraints, conf[i], params, isString, value);
                } else {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug ("Unknown type " + type
                                           + " specified for parameter " + name);
                    return null;
                }
                vaR = (vaR.getPos() < vaH.getResult().getPos() ? vaH.getResult() : vaR );
            }
            return new ValidatorActionHelper(vaH.getObject(), vaR);
        } else {
            if ("string".equals (type)) {
                return validateString(name, constraints, conf[i], params, value);
            } else if ("long".equals (type)) {
                return validateLong(name, constraints, conf[i], params, isString, value);
            } else if ("double".equals (type)) {
                return validateDouble(name, constraints, conf[i], params, isString, value);
            } else {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("Unknown type " + type
                                       + " specified for parameter " + name);
            }
            return null;
        }
    }

    /**
     * Validates nullability and default value for given parameter. If given
     * constraints are not null they are validated as well.
     */
    private ValidatorActionHelper validateString(String name, Configuration constraints,
            Configuration conf, Map params, Object param) {

        String value = null;
        String dflt = getDefault(conf, constraints);
        boolean nullable = getNullable(conf, constraints);

        if (getLogger().isDebugEnabled())
            getLogger().debug ("Validating string parameter " + name);
        try {
            value = getStringValue(param);
        } catch (Exception e) {
            // ClassCastException
            return new ValidatorActionHelper(value, ValidatorActionResult.ERROR);
        }
        if (value == null) {
            if (getLogger().isDebugEnabled())
                getLogger().debug ("String parameter " + name + " is null");
            if (!nullable) {
                return new ValidatorActionHelper(value, ValidatorActionResult.ISNULL);
            } else {
                return new ValidatorActionHelper(dflt);
            }
        }
        if (constraints != null) {
            String eq = constraints.getAttribute ("equals-to", "");
            eq = conf.getAttribute ("equals-to", eq);

            String eqp = constraints.getAttribute ("equals-to-param", "");
            eqp = conf.getAttribute ("equals-to-param", eqp);

            String regex = conf.getAttribute ("matches-regex", "");
            regex = constraints.getAttribute ( "matches-regex", regex);

            String oneOf = conf.getAttribute ("one-of", "");
            oneOf = constraints.getAttribute ( "one-of", oneOf);

            Long minlen = getAttributeAsLong (conf, "min-len", null);
            minlen = getAttributeAsLong (constraints, "min-len", minlen);

            Long maxlen = getAttributeAsLong (conf, "max-len", null);
            maxlen = getAttributeAsLong (constraints, "max-len", maxlen);

            // Validate whether param is equal to constant
            if (!"".equals (eq)) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("String parameter "
                                       + name + " should be equal to " + eq);
                if (!value.equals (eq)) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug ("and it is not");
                    return new ValidatorActionHelper ( value, ValidatorActionResult.NOMATCH);
                }
            }

            // Validate whether param is equal to another param
            // FIXME: take default value of param being compared with into
            // account?
            if (!"".equals (eqp)) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("String parameter "
                                       + name + " should be equal to " + params.get (eqp));
                if (!value.equals (params.get (eqp))) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug ("and it is not");
                    return new ValidatorActionHelper ( value, ValidatorActionResult.NOMATCH);
                }
            }

            // Validate whether param length is at least of minimum length
            if (minlen != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("String parameter "
                                       + name + " should be at least " + minlen + " characters long");
                if ( value.length() < minlen.longValue() ) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug ("and it is shorter (" +
                                           value.length() + ")" );
                    return new ValidatorActionHelper ( value, ValidatorActionResult.TOOSMALL);
                }
            }

            // Validate whether param length is at most of maximum length
            if (maxlen != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("String parameter "
                                       + name + " should be at most " + maxlen + " characters long");

                if ( value.length() > maxlen.longValue() ) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug ("and it is longer (" +
                                           value.length() + ")" );
                    return new ValidatorActionHelper ( value, ValidatorActionResult.TOOLARGE);
                }
            }

            // Validate wheter param matches regular expression
            if (!"".equals (regex)) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("String parameter " + name +
                                       " should match regexp \"" + regex + "\"" );
                try {
                    RE r = new RE ( regex );
                    if ( !r.match(value) ) {
                        if (getLogger().isDebugEnabled())
                            getLogger().debug("and it does not match");
                        return new ValidatorActionHelper ( value, ValidatorActionResult.NOMATCH);
                    };
                } catch ( RESyntaxException rese ) {
                    if (getLogger().isDebugEnabled())
                        getLogger().error ("String parameter " + name +
                                           " regex error ", rese);
                    return new ValidatorActionHelper ( value, ValidatorActionResult.NOMATCH);
                }
            }
            
            // Validates against a set of possibilities
            if (!"".equals(oneOf)){
                if (getLogger().isDebugEnabled())
                    getLogger().debug("String parameter " + name + 
                                      " should be one of \"" + oneOf +"\"" );
                if (!oneOf.startsWith("|"))
                    oneOf="|"+oneOf;
                if (!oneOf.endsWith("|"))
                    oneOf=oneOf+"|";
                if (value.indexOf("|") != -1){
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("String parameter " + name + 
                                          "contains \"|\" - can't validate that." );
                    return new ValidatorActionHelper(value, ValidatorActionResult.ERROR);
                }
                if (oneOf.indexOf("|"+value+"|") == -1) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug ("and it is not" );
                    return new ValidatorActionHelper ( value, ValidatorActionResult.NOMATCH);
                }
                return new ValidatorActionHelper ( value, ValidatorActionResult.OK);

            }

        }
        return new ValidatorActionHelper(value);
    }

    /**
     * Validates nullability and default value for given parameter. If given
     * constraints are not null they are validated as well.
     */
    private ValidatorActionHelper validateLong(String name, Configuration constraints,
            Configuration conf, Map params, boolean is_string, Object param) {

        boolean nullable = getNullable (conf, constraints);
        Long value = null;
        Long dflt = getLongValue(getDefault(conf, constraints), true);

        if (getLogger().isDebugEnabled())
            getLogger().debug ("Validating long parameter "
                               + name + " (encoded in a string: " + is_string + ")");
        try {
            value = getLongValue(param, is_string);
        } catch (Exception e) {
            // Unable to parse long
            return new ValidatorActionHelper(value, ValidatorActionResult.ERROR);
        }
        if (value == null) {
            if (getLogger().isDebugEnabled())
                getLogger().debug ("Long parameter " + name + " is null");
            if (!nullable) {
                return new ValidatorActionHelper(value, ValidatorActionResult.ISNULL);
            } else {
                return new ValidatorActionHelper(dflt);
            }
        }
        if (constraints != null) {
            Long eq = getAttributeAsLong (constraints, "equals-to", null);
            String eqp = constraints.getAttribute ("equals-to-param", "");

            Long min = getAttributeAsLong (conf, "min", null);
            min = getAttributeAsLong ( constraints, "min", min);

            Long max = getAttributeAsLong (conf, "max",null);
            max = getAttributeAsLong (constraints, "max", max);

            // Validate whether param is equal to constant
            if (eq != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("Long parameter "
                                       + name + " should be equal to " + eq);

                if (!value.equals(eq)) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug ("and it is not");
                    return new ValidatorActionHelper ( value, ValidatorActionResult.NOMATCH);
                }
            }

            // Validate whether param is equal to another param
            // FIXME: take default value of param being compared with into
            // account?
            if (!"".equals (eqp)) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("Long parameter "
                                       + name + " should be equal to " + params.get (eqp));
                // Request parameter is stored as string.
                // Need to convert it beforehand.
                try {
                    Long _eqp = new Long ( Long.parseLong((String) params.get(eqp)) );
                    if (!value.equals (_eqp)) {
                        if (getLogger().isDebugEnabled())
                            getLogger().debug ("and it is not");
                        return new ValidatorActionHelper(value, ValidatorActionResult.NOMATCH);
                    }
                } catch ( NumberFormatException nfe ) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("Long parameter "+ name +": "+eqp+" is no long", nfe);
                    return new ValidatorActionHelper(value, ValidatorActionResult.NOMATCH);
                }
            }

            // Validate wheter param is at least min
            if (min != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("Long parameter "
                                       + name + " should be at least " + min);

                if (min.compareTo(value)>0) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug ("and it is not");
                    return new ValidatorActionHelper ( value, ValidatorActionResult.TOOSMALL);
                }
            }

            // Validate wheter param is at most max
            if (max != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("Long parameter "
                                       + name + " should be at most " + max);
                if (max.compareTo(value)<0) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug ("and it is not");
                    return new ValidatorActionHelper ( value, ValidatorActionResult.TOOLARGE);
                }
            }
        }
        return new ValidatorActionHelper(value);
    }

    /**
     * Validates nullability and default value for given parameter. If given
     * constraints are not null they are validated as well.
     */
    private ValidatorActionHelper validateDouble(String name, Configuration constraints,
            Configuration conf, Map params, boolean is_string, Object param) {

        boolean nullable = getNullable(conf, constraints);
        Double value = null;
        Double dflt = getDoubleValue(getDefault(conf, constraints), true);

        if (getLogger().isDebugEnabled())
            getLogger().debug ("Validating double parameter "
                               + name + " (encoded in a string: " + is_string + ")");
        try {
            value = getDoubleValue(param, is_string);
        } catch (Exception e) {
            // Unable to parse double
            return new ValidatorActionHelper(value, ValidatorActionResult.ERROR);
        }
        if (value == null) {
            if (getLogger().isDebugEnabled())
                getLogger().debug ("double parameter " + name + " is null");
            if (!nullable) {
                return new ValidatorActionHelper(value, ValidatorActionResult.ISNULL);
            } else {
                return new ValidatorActionHelper(dflt);
            }
        }
        if (constraints != null) {
            Double eq = getAttributeAsDouble (constraints, "equals-to", null);
            String eqp = constraints.getAttribute ("equals-to-param", "");

            Double min = getAttributeAsDouble (conf, "min", null);
            min = getAttributeAsDouble ( constraints, "min", min);

            Double max = getAttributeAsDouble (conf, "max", null);
            max = getAttributeAsDouble (constraints, "max", max);

            // Validate whether param is equal to constant
            if (eq != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("Double parameter "
                                       + name + " should be equal to " + eq);

                if (!value.equals (eq)) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug ("and it is not");
                    return new ValidatorActionHelper ( value, ValidatorActionResult.NOMATCH);
                }
            }

            // Validate whether param is equal to another param
            // FIXME: take default value of param being compared with into
            // account?
            if (!"".equals (eqp)) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("Double parameter "
                                       + name + " should be equal to " + params.get (eqp));
                // Request parameter is stored as string.
                // Need to convert it beforehand.
                try {
                    Double _eqp = new Double ( Double.parseDouble((String) params.get(eqp)));
                    if (!value.equals (_eqp)) {
                        if (getLogger().isDebugEnabled())
                            getLogger().debug ("and it is not");
                        return new ValidatorActionHelper ( value, ValidatorActionResult.NOMATCH);
                    }
                } catch ( NumberFormatException nfe ) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("Double parameter "+ name +": "+eqp+" is no double", nfe);
                    return new ValidatorActionHelper ( value, ValidatorActionResult.NOMATCH);
                }
            }

            // Validate wheter param is at least min
            if (min != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("Double parameter "
                                       + name + " should be at least " + min);
                if (0 > value.compareTo(min)) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug ("and it is not");
                    return new ValidatorActionHelper (value, ValidatorActionResult.TOOSMALL);
                }
            }

            // Validate wheter param is at most max
            if (max != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug ("Double parameter "
                                       + name + " should be at most " + max);
                if (0<value.compareTo(max)) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug ("and it is not");
                    return new ValidatorActionHelper (value, ValidatorActionResult.TOOLARGE);
                }
            }
        }
        return new ValidatorActionHelper(value);
    }

    /**
     * Returns the parsed Double value.
     */
    private Double getDoubleValue (Object param, boolean is_string)
            throws ClassCastException, NumberFormatException {

        /* convert param to double */
        if (is_string) {
            String tmp = getStringValue(param);
            if (tmp == null) {
                return null;
            }
            return new Double(tmp);
        } else {
            return (Double)param;
        }
    }

    /**
     * Returns the parsed Long value.
     */
    private Long getLongValue (Object param, boolean is_string)
            throws ClassCastException, NumberFormatException {

        /* convert param to long */
        if (is_string) {
            String tmp = getStringValue(param);
            if (tmp == null) {
                return null;
            }
            return Long.decode(tmp);
        } else {
            return (Long)param;
        }
    }

    /**
     * Returns string
     * @throws ClassCastException if param is not a String object
     */
    private String getStringValue(Object param) throws ClassCastException {

        /* convert param to string */
        String value = (String)param;
        if (value != null && "".equals(value.trim())) {
            value = null;
        }
        return value;
    }

    /**
     * Returns the value of 'nullable' attribute from given configuration or
     * from given constraints, value present in constraints takes precedence,
     * false when attribute is not present in either of them.
     */
    private boolean getNullable(Configuration conf, Configuration cons) {
        /* check nullability */
        try {
            String tmp = cons.getAttribute("nullable");
            return "yes".equals(tmp) || "true".equals(tmp);
        } catch (Exception e) {
            String tmp = "no";
            if (conf != null) tmp = conf.getAttribute("nullable", "no");
            return "yes".equals(tmp) || "true".equals(tmp);
        }
    }

    /**
     * Returns the default value from given configuration or constraints.
     * Value present in constraints takes precedence, null is returned when no
     * default attribute is present in eiher of them.
     */
    private String getDefault(Configuration conf, Configuration cons) {
        String dflt = "";
        try {
            dflt = cons.getAttribute("default");
        } catch (Exception e) {
            if (conf != null)
                dflt = conf.getAttribute("default", "");
        }
        if ("".equals(dflt.trim())) {
            dflt = null;
        }
        return dflt;
    }

    /**
     * Replacement for Avalon's Configuration.getAttributeAsLong
     * because that one doesn't take <code>Long</code> but long and
     * thus won't take <code>null</code> as parameter value for
     * default.
     *
     * @param conf Configuration
     * @param name Parameter's name
     * @param dflt Default value
     * @return Parameter's value in <code>configuration</code> or
     * <code>dflt</code> if parameter is not set or couldn't be
     * converted to a <code>Long</code>
     * @throws NumberFormatException if conversion fails
     */
    private Long getAttributeAsLong(Configuration conf, String name, Long dflt)
            throws NumberFormatException {
        try {
            return new Long(conf.getAttribute(name));
        } catch (ConfigurationException e) {
            return dflt;
        }
    }

    /**
     * Addition to Avalon's Configuration.getAttributeAsFloat
     * because that one does only deal with <code>float</code>.
     *
     * @param conf Configuration
     * @param name Parameter's name
     * @param dflt Default value
     * @return Parameter's value in <code>configuration</code> or
     * <code>dflt</code> if parameter is not set or couldn't be
     * converted to a <code>Double</code>
     * @throws NumberFormatException if conversion fails
     */
    private Double getAttributeAsDouble(Configuration conf, String name, Double dflt)
            throws NumberFormatException {
        try {
            return new Double(conf.getAttribute(name));
        } catch (ConfigurationException e) {
            return dflt;
        }
    }
}
