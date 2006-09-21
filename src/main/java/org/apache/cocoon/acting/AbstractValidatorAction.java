/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.acting;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.sitemap.SitemapParameters;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Abstract implementation of action that needs to perform validation of
 * parameters (from session, from request, etc.). All `validator' actions
 * share the same description xml file. In such file every parameter is
 * described via its name, type and its constraints. One large description
 * file can be used among all validator actions, because each action should
 * explicitely specify which parameters to validate - through a sitemap
 * parameter.
 *
 * <h3>Variant 1</h3>
 * <pre>
 * &lt;map:act type="validator"&gt;
 *         &lt;parameter name="descriptor" value="context://descriptor.xml"&gt;
 *         &lt;parameter name="validate" value="username,password"&gt;
 * &lt;/map:act&gt;
 * </pre>
 *
 * <p>The list of parameters to be validated is specified as a comma
 * separated list of their names. descriptor.xml can therefore be used
 * among many various actions. If the list contains only of <code>*</code>,
 * all parameters in the file will be validated.</p>
 *
 * <h3>Variant 2</h3>
 * <pre>
 * &lt;map:act type="validator"&gt;
 *         &lt;parameter name="descriptor" value="context://descriptor.xml"&gt;
 *         &lt;parameter name="constraint-set" value="is-logged-in"&gt;
 * &lt;/map:act&gt;
 * </pre>
 *
 * <p>The parameter "constraint-set" tells to take a given
 * "constraint-set" from description file and test all parameters
 * against given criteria. This variant is more powerful, more aspect
 * oriented and more flexibile than the previous one, because it
 * allows comparsion constructs, etc. See AbstractValidatorAction
 * documentation.</p>
 *
 * <p>For even more powerful validation, constraints can be grouped
 * and used independently of the parameter name. If a validate element
 * has a <code>rule</code> attribute, it uses the parameter with that
 * name as a rule template and validates the parameter from the
 * <code>name</code> attribute with that rule.</p>
 *
 * <p>This action returns null when validation fails, otherwise it
 * provides all validated parameters to the sitemap via {name}
 * expression.</p>
 *
 * <p>In addition a request attribute
 * <code>org.apache.cocoon.acting.FormValidatorAction.results</code>
 * contains the validation results in both cases and make it available
 * to other components. The special parameter "*" contains either the validation
 * result "OK", if all parameters were validated successfully, or
 * "ERROR" otherwise. Mind you that redirections create new request
 * objects and thus the result is not available for the target
 * page.</p>
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
 *
 *         &lt;constraint-set name="all"&gt;
 *                 &lt;include name="is-logged-in"/&gt;
 *                 &lt;include name="is-in-admin-role"/&gt;
 *                 &lt;include name="new-passwords-match"/&gt;
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
 * @version $Id$
 */
public abstract class AbstractValidatorAction
    extends AbstractComplementaryConfigurableAction {

    /**
     * This is the name of the request attribute containing the result
     */
    public static final String FORMVALIDATOR_PATH = "org.apache.cocoon.acting.FormValidatorAction.results";

   /**
     * Reads parameter values for all parameters that are contained in the active
     * constraint list. If a parameter has multiple values, all are stored in the
     * resulting map.
     * 
     * @param objectModel the object model
     * @param set a collection of parameter names
     * @return HashMap
     */
    abstract protected HashMap createMapOfParameters(Map objectModel, Collection set);

    /**
     * Are parameters encoded as strings?
     */
    abstract boolean isStringEncoded();

    /*
     * main method
     */
    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String src,
                   Parameters parameters) throws Exception {

        Configuration conf = this.getDescriptor(resolver, objectModel, parameters);
        if (conf == null)
            return null;

        String valStr =
            parameters.getParameter("validate", (String)settings.get("validate", "")).trim();
        String valSetStr =
            parameters.getParameter("validate-set", (String)settings.get("validate-set", "")).trim();
        String constraintSetStr =
            parameters.getParameter("constraint-set", (String)settings.get("constraint-set", "")).trim();
        if (!"".equals(valSetStr)) {
            if (getLogger().isInfoEnabled()) {
                getLogger().info("Using sitemap parameter 'validate-set' for specifying "
                                 + "the constraint-set for the ValidatorActions is deprecated in "
                                 + "favor of 'constraint-set' due to consistency in the naming.");
            }
            if ("".equals(constraintSetStr)) {
                constraintSetStr = valSetStr;
            }
        }
        Map desc = this.indexConfiguration(conf.getChildren("parameter"));

        Map actionMap = new HashMap();
        Map resultMap = new HashMap();
        Collection params = null;
        boolean allOK = false;

        if (!"".equals(valStr)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Validating parameters as specified via 'validate' parameter");
            }
            params = this.getSetOfParameterNamesFromSitemap(valStr, desc);
        } else if (!"".equals(constraintSetStr)) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Validating parameters from given constraint-set " + constraintSetStr);
            }
            Map csets = this.indexConfiguration(conf.getChildren("constraint-set"));
            params = this.resolveConstraints(constraintSetStr, csets);
        }
        
        if (params == null) {
            throw new ProcessingException("Neither a constraint-set nor parameters in the sitemap "
                                          + "were specified for validating at "
                                          + SitemapParameters.getLocation(parameters));
        }
        HashMap values = this.createMapOfParameters(objectModel, params);
        allOK = this.validateSetOfParameters(desc, actionMap, resultMap, params, values, this.isStringEncoded());

        return this.setResult(objectModel, actionMap, resultMap, allOK);
    }

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
    public ValidatorActionHelper validateParameter(
        String name,
        Configuration constraints,
        Map conf,
        Map params,
        boolean isString) {

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
    public ValidatorActionHelper validateParameter(
        String name,
        String rule,
        Configuration constraints,
        Map conf,
        Map params,
        boolean isString) {
        String type = null;

        if (getLogger().isDebugEnabled())
            getLogger().debug("Validating parameter: " + name + " using rule: " + rule);

        /* try to find matching param description in conf tree */
        try {
            Configuration theConf = (Configuration) conf.get(rule);
            type = theConf.getAttribute("type");

            return validateValue(name, constraints, theConf, params, isString, type);

        } catch (Exception e) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("No type specified for parameter " + name);
            return null;
        }
    }

    /**
     * Validate a single parameter value.
     * 
     * @param name String holding the name of the parameter
     * @param constraints Configuration holding the constraint set configuration for the parameter
     * @param conf Configuration holding the parameter configuration
     * @param params Map of parameter values to be validated
     * @param isString boolean indicating if the value is string encoded
     * @param type string holding the name of the datatype to validate value
     * @return ValidatorActionHelper
     */
    protected ValidatorActionHelper validateValue(
        String name,
        Configuration constraints,
        Configuration conf,
        Map params,
        boolean isString,
        String type) {
        Object value = params.get(name);

        if (value != null && value.getClass().isArray()) {
            Object[] values = (Object[]) value;
            ValidatorActionHelper vaH = null;
            ValidatorActionResult vaR = ValidatorActionResult.OK;
            for (int j = 0; j < values.length; j++) {
                value = values[j];
                if ("string".equals(type)) {
                    vaH = validateString(name, constraints, conf, params, value);
                } else if ("long".equals(type)) {
                    vaH = validateLong(name, constraints, conf, params, isString, value);
                } else if ("double".equals(type)) {
                    vaH = validateDouble(name, constraints, conf, params, isString, value);
                } else {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug(
                            "Unknown type " + type + " specified for parameter " + name);
                    return null;
                }
                vaR = (vaR.getPos() < vaH.getResult().getPos() ? vaH.getResult() : vaR);
            }
            return new ValidatorActionHelper(vaH.getObject(), vaR);
        } else {
            if ("string".equals(type)) {
                return validateString(name, constraints, conf, params, value);
            } else if ("long".equals(type)) {
                return validateLong(name, constraints, conf, params, isString, value);
            } else if ("double".equals(type)) {
                return validateDouble(name, constraints, conf, params, isString, value);
            } else {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Unknown type " + type + " specified for parameter " + name);
            }
            return null;
        }
    }

    /**
     * Validates nullability and default value for given parameter. If given
     * constraints are not null they are validated as well.
     */
    private ValidatorActionHelper validateString(
        String name,
        Configuration constraints,
        Configuration conf,
        Map params,
        Object param) {

        String value = null;
        String dflt = getDefault(conf, constraints);
        boolean nullable = getNullable(conf, constraints);

        if (getLogger().isDebugEnabled())
            getLogger().debug("Validating string parameter " + name);
        try {
            value = getStringValue(param);
        } catch (Exception e) {
            // ClassCastException
            return new ValidatorActionHelper(value, ValidatorActionResult.ERROR);
        }
        if (value == null) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("String parameter " + name + " is null");
            if (!nullable) {
                return new ValidatorActionHelper(value, ValidatorActionResult.ISNULL);
            } else {
                return new ValidatorActionHelper(dflt);
            }
        }
        if (constraints != null) {
            String eq = constraints.getAttribute("equals-to", "");
            eq = conf.getAttribute("equals-to", eq);

            String eqp = constraints.getAttribute("equals-to-param", "");
            eqp = conf.getAttribute("equals-to-param", eqp);

            String regex = conf.getAttribute("matches-regex", "");
            regex = constraints.getAttribute("matches-regex", regex);

            String oneOf = conf.getAttribute("one-of", "");
            oneOf = constraints.getAttribute("one-of", oneOf);

            Long minlen = getAttributeAsLong(conf, "min-len", null);
            minlen = getAttributeAsLong(constraints, "min-len", minlen);

            Long maxlen = getAttributeAsLong(conf, "max-len", null);
            maxlen = getAttributeAsLong(constraints, "max-len", maxlen);

            // Validate whether param is equal to constant
            if (!"".equals(eq)) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("String parameter " + name + " should be equal to " + eq);
                if (!value.equals(eq)) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("and it is not");
                    return new ValidatorActionHelper(value, ValidatorActionResult.NOMATCH);
                }
            }

            // Validate whether param is equal to another param
            // FIXME: take default value of param being compared with into
            // account?
            if (!"".equals(eqp)) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug(
                        "String parameter " + name + " should be equal to " + params.get(eqp));
                if (!value.equals(params.get(eqp))) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("and it is not");
                    return new ValidatorActionHelper(value, ValidatorActionResult.NOMATCH);
                }
            }

            // Validate whether param length is at least of minimum length
            if (minlen != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug(
                        "String parameter "
                            + name
                            + " should be at least "
                            + minlen
                            + " characters long");
                if (value.length() < minlen.longValue()) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("and it is shorter (" + value.length() + ")");
                    return new ValidatorActionHelper(value, ValidatorActionResult.TOOSMALL);
                }
            }

            // Validate whether param length is at most of maximum length
            if (maxlen != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug(
                        "String parameter "
                            + name
                            + " should be at most "
                            + maxlen
                            + " characters long");

                if (value.length() > maxlen.longValue()) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("and it is longer (" + value.length() + ")");
                    return new ValidatorActionHelper(value, ValidatorActionResult.TOOLARGE);
                }
            }

            // Validate wheter param matches regular expression
            if (!"".equals(regex)) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug(
                        "String parameter " + name + " should match regexp \"" + regex + "\"");
                try {
                    RE r = new RE(regex);
                    if (!r.match(value)) {
                        if (getLogger().isDebugEnabled())
                            getLogger().debug("and it does not match");
                        return new ValidatorActionHelper(value, ValidatorActionResult.NOMATCH);
                    }
                } catch (RESyntaxException rese) {
                    if (getLogger().isDebugEnabled())
                        getLogger().error("String parameter " + name + " regex error ", rese);
                    return new ValidatorActionHelper(value, ValidatorActionResult.NOMATCH);
                }
            }

            // Validates against a set of possibilities
            if (!"".equals(oneOf)) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug(
                        "String parameter " + name + " should be one of \"" + oneOf + "\"");
                if (!oneOf.startsWith("|"))
                    oneOf = "|" + oneOf;
                if (!oneOf.endsWith("|"))
                    oneOf = oneOf + "|";
                if (value.indexOf("|") != -1) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug(
                            "String parameter " + name + "contains \"|\" - can't validate that.");
                    return new ValidatorActionHelper(value, ValidatorActionResult.ERROR);
                }
                if (oneOf.indexOf("|" + value + "|") == -1) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("and it is not");
                    return new ValidatorActionHelper(value, ValidatorActionResult.NOMATCH);
                }
                return new ValidatorActionHelper(value, ValidatorActionResult.OK);

            }

        }
        return new ValidatorActionHelper(value);
    }

    /**
     * Validates nullability and default value for given parameter. If given
     * constraints are not null they are validated as well.
     */
    private ValidatorActionHelper validateLong(
        String name,
        Configuration constraints,
        Configuration conf,
        Map params,
        boolean is_string,
        Object param) {

        boolean nullable = getNullable(conf, constraints);
        Long value = null;
        Long dflt = getLongValue(getDefault(conf, constraints), true);

        if (getLogger().isDebugEnabled())
            getLogger().debug(
                "Validating long parameter " + name + " (encoded in a string: " + is_string + ")");
        try {
            value = getLongValue(param, is_string);
        } catch (Exception e) {
            // Unable to parse long
            return new ValidatorActionHelper(value, ValidatorActionResult.ERROR);
        }
        if (value == null) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Long parameter " + name + " is null");
            if (!nullable) {
                return new ValidatorActionHelper(value, ValidatorActionResult.ISNULL);
            } else {
                return new ValidatorActionHelper(dflt);
            }
        }
        if (constraints != null) {
            Long eq = getAttributeAsLong(constraints, "equals-to", null);
            String eqp = constraints.getAttribute("equals-to-param", "");

            Long min = getAttributeAsLong(conf, "min", null);
            min = getAttributeAsLong(constraints, "min", min);

            Long max = getAttributeAsLong(conf, "max", null);
            max = getAttributeAsLong(constraints, "max", max);

            // Validate whether param is equal to constant
            if (eq != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Long parameter " + name + " should be equal to " + eq);

                if (!value.equals(eq)) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("and it is not");
                    return new ValidatorActionHelper(value, ValidatorActionResult.NOMATCH);
                }
            }

            // Validate whether param is equal to another param
            // FIXME: take default value of param being compared with into
            // account?
            if (!"".equals(eqp)) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug(
                        "Long parameter " + name + " should be equal to " + params.get(eqp));
                // Request parameter is stored as string.
                // Need to convert it beforehand.
                try {
                    Long _eqp = new Long(Long.parseLong((String) params.get(eqp)));
                    if (!value.equals(_eqp)) {
                        if (getLogger().isDebugEnabled())
                            getLogger().debug("and it is not");
                        return new ValidatorActionHelper(value, ValidatorActionResult.NOMATCH);
                    }
                } catch (NumberFormatException nfe) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug(
                            "Long parameter " + name + ": " + eqp + " is no long",
                            nfe);
                    return new ValidatorActionHelper(value, ValidatorActionResult.NOMATCH);
                }
            }

            // Validate wheter param is at least min
            if (min != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Long parameter " + name + " should be at least " + min);

                if (min.compareTo(value) > 0) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("and it is not");
                    return new ValidatorActionHelper(value, ValidatorActionResult.TOOSMALL);
                }
            }

            // Validate wheter param is at most max
            if (max != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Long parameter " + name + " should be at most " + max);
                if (max.compareTo(value) < 0) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("and it is not");
                    return new ValidatorActionHelper(value, ValidatorActionResult.TOOLARGE);
                }
            }
        }
        return new ValidatorActionHelper(value);
    }

    /**
     * Validates nullability and default value for given parameter. If given
     * constraints are not null they are validated as well.
     */
    private ValidatorActionHelper validateDouble(
        String name,
        Configuration constraints,
        Configuration conf,
        Map params,
        boolean is_string,
        Object param) {

        boolean nullable = getNullable(conf, constraints);
        Double value = null;
        Double dflt = getDoubleValue(getDefault(conf, constraints), true);

        if (getLogger().isDebugEnabled())
            getLogger().debug(
                "Validating double parameter "
                    + name
                    + " (encoded in a string: "
                    + is_string
                    + ")");
        try {
            value = getDoubleValue(param, is_string);
        } catch (Exception e) {
            // Unable to parse double
            return new ValidatorActionHelper(value, ValidatorActionResult.ERROR);
        }
        if (value == null) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("double parameter " + name + " is null");
            if (!nullable) {
                return new ValidatorActionHelper(value, ValidatorActionResult.ISNULL);
            } else {
                return new ValidatorActionHelper(dflt);
            }
        }
        if (constraints != null) {
            Double eq = getAttributeAsDouble(constraints, "equals-to", null);
            String eqp = constraints.getAttribute("equals-to-param", "");

            Double min = getAttributeAsDouble(conf, "min", null);
            min = getAttributeAsDouble(constraints, "min", min);

            Double max = getAttributeAsDouble(conf, "max", null);
            max = getAttributeAsDouble(constraints, "max", max);

            // Validate whether param is equal to constant
            if (eq != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Double parameter " + name + " should be equal to " + eq);

                if (!value.equals(eq)) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("and it is not");
                    return new ValidatorActionHelper(value, ValidatorActionResult.NOMATCH);
                }
            }

            // Validate whether param is equal to another param
            // FIXME: take default value of param being compared with into
            // account?
            if (!"".equals(eqp)) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug(
                        "Double parameter " + name + " should be equal to " + params.get(eqp));
                // Request parameter is stored as string.
                // Need to convert it beforehand.
                try {
                    Double _eqp = new Double(Double.parseDouble((String) params.get(eqp)));
                    if (!value.equals(_eqp)) {
                        if (getLogger().isDebugEnabled())
                            getLogger().debug("and it is not");
                        return new ValidatorActionHelper(value, ValidatorActionResult.NOMATCH);
                    }
                } catch (NumberFormatException nfe) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug(
                            "Double parameter " + name + ": " + eqp + " is no double",
                            nfe);
                    return new ValidatorActionHelper(value, ValidatorActionResult.NOMATCH);
                }
            }

            // Validate wheter param is at least min
            if (min != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Double parameter " + name + " should be at least " + min);
                if (0 > value.compareTo(min)) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("and it is not");
                    return new ValidatorActionHelper(value, ValidatorActionResult.TOOSMALL);
                }
            }

            // Validate wheter param is at most max
            if (max != null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Double parameter " + name + " should be at most " + max);
                if (0 < value.compareTo(max)) {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("and it is not");
                    return new ValidatorActionHelper(value, ValidatorActionResult.TOOLARGE);
                }
            }
        }
        return new ValidatorActionHelper(value);
    }

    /**
     * Returns the parsed Double value.
     */
    private Double getDoubleValue(Object param, boolean is_string)
        throws ClassCastException, NumberFormatException {

        /* convert param to double */
        if (is_string) {
            String tmp = getStringValue(param);
            if (tmp == null) {
                return null;
            }
            return new Double(tmp);
        } else {
            return (Double) param;
        }
    }

    /**
     * Returns the parsed Long value.
     */
    private Long getLongValue(Object param, boolean is_string)
        throws ClassCastException, NumberFormatException {

        /* convert param to long */
        if (is_string) {
            String tmp = getStringValue(param);
            if (tmp == null) {
                return null;
            }
            return Long.decode(tmp);
        } else {
            return (Long) param;
        }
    }

    /**
     * Returns string
     * @throws ClassCastException if param is not a String object
     */
    private String getStringValue(Object param) throws ClassCastException {

        /* convert param to string */
        String value = (String) param;
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
            return BooleanUtils.toBoolean(tmp);
        } catch (Exception e) {
            String tmp = "no";
            if (conf != null) {
                tmp = conf.getAttribute("nullable", "no");
            }
            return BooleanUtils.toBoolean(tmp);
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

    /**
     * Create an index map to an array of configurations by their name
     * attribute. An empty array results in an empty map.
     * 
     * @param descriptor
     * @return index map or empty map
     */
    protected Map indexConfiguration(Configuration[] descriptor) {
        if (descriptor == null)
            return new HashMap();
        Map result = new HashMap((descriptor.length > 0) ? descriptor.length * 2 : 5);
        for (int i = descriptor.length - 1; i >= 0; i--) {
            String name = descriptor[i].getAttribute("name", "");
            result.put(name, descriptor[i]);
        }
        return result;
    }

    /**
     * Recursively resolve constraint sets that may "include" other constraint
     * sets and return a collection of all parameters to validate.
     * 
     * @param valsetstr
     * @param consets
     * @return collection of all parameters to validate
     */
    protected Collection resolveConstraints(String valsetstr, Map consets) {
        /* get the list of params to be validated */
        Vector rules = new Vector();
        Configuration[] set = ((Configuration) consets.get(valsetstr)).getChildren("validate");
        for (int j = 0; j < set.length; j++) {
            rules.add(set[j]);
        }
        set = ((Configuration) consets.get(valsetstr)).getChildren("include");
        for (int j = 0; j < set.length; j++) {
            Collection tmp = resolveConstraints(set[j].getAttribute("name", ""), consets);
            rules.addAll(tmp);
        }
        return rules;
    }

    /**
     * Checks the default setting for reloading the descriptor file.
     * @return boolean
     */
    protected boolean isDescriptorReloadable() {
        // read global parameter settings
        boolean reloadable = DESCRIPTOR_RELOADABLE_DEFAULT;
        if (this.settings.containsKey("reloadable")) {
            reloadable = Boolean.valueOf((String) this.settings.get("reloadable")).booleanValue();
        }
        return reloadable;
    }

    /**
     * Get list of params to be validated from sitemap parameter and
     * isolates the parameter names from the comma separated list.
     * 
     */
    protected Collection getSetOfParameterNamesFromSitemap(String valstr, Map desc) {
        String[] rparams = null;
        Set set = new HashSet(20);
        if (!"*".equals(valstr.trim())) {
            rparams = StringUtils.split(valstr, ","); 
            if (rparams != null) {
                for (int i = rparams.length - 1; i >= 0; i--) {
                    set.add(desc.get(rparams[i]));
                }
            }
        } else {
            // validate _all_ parameters
            set = desc.entrySet();
        }
        return set;
    }

    /**
     * Validate all parameters in the set with the constraints contained in
     * desc and the values from params. Validation details are in resultMap and 
     * successful validated parameters in resultMap. 
     * 
     * @param desc
     * @param actionMap
     * @param resultMap
     * @param set
     * @param params
     * @param isString
     * @return boolean all parameters ok or not
     */
    protected boolean validateSetOfParameters(
        Map desc,
        Map actionMap,
        Map resultMap,
        Collection set,
        Map params,
        boolean isString) {

        boolean allOK = true;
        ValidatorActionHelper result;
        String name;
        String rule = null;
        for (Iterator i = set.iterator(); i.hasNext();) {
            Configuration constr = (Configuration) i.next();
            name = constr.getAttribute("name", null);
            rule = constr.getAttribute("rule", name);
            result = validateParameter(name, rule, constr, desc, params, isString);
            if (!result.isOK()) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Validation failed for parameter " + name);
                allOK = false;
            }
            actionMap.put(name, result.getObject());
            resultMap.put(name, result.getResult());
        }
        return allOK;
    }

    /**
     * Add success indicator to resulting maps and clear actionMap if unsuccessful.
     * Results are stored as request attributes.
     * 
     * @param objectModel the object model
     * @param actionMap a Map containing validated parameters
     * @param resultMap a Map containing validation results
     * @param allOK a boolean indicating if all validations were successful
     * @return actionMap if allOK or null otherwise
     */
    protected Map setResult(Map objectModel, Map actionMap, Map resultMap, boolean allOK) {
        if (!allOK) {
            // if any validation failed return an empty map
            actionMap = null;
            resultMap.put("*", ValidatorActionResult.ERROR);
            if (getLogger().isDebugEnabled())
                getLogger().debug("All form params validated. An error occurred.");
        } else {
            resultMap.put("*", ValidatorActionResult.OK);
            if (getLogger().isDebugEnabled())
                getLogger().debug("All form params successfully validated");
        }
        // store validation results in request attribute
        ObjectModelHelper.getRequest(objectModel).setAttribute(
            FORMVALIDATOR_PATH,
            resultMap);
        //return Collections.unmodifiableMap (actionMap);
        return actionMap;
    }

    /**
     * Load the descriptor containing the constraints.
     * @param resolver
     * @param parameters
     * @return a Configuration containing the constraints or null if a problem occurred.
     */
    protected Configuration getDescriptor(
        SourceResolver resolver,
        Map objectModel,
        Parameters parameters) {
        Configuration conf = null;
        try {
            conf =
                this.getConfiguration(
                    parameters.getParameter("descriptor", (String) this.settings.get("descriptor")),
                    resolver,
                    parameters.getParameterAsBoolean("reloadable", isDescriptorReloadable()));
        } catch (ConfigurationException e) {
            if (this.getLogger().isWarnEnabled())
                this.getLogger().warn("Exception reading descriptor: ", e);
        }
        return conf;
    }

}
