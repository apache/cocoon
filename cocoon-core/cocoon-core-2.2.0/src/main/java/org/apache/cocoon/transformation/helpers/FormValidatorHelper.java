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
package org.apache.cocoon.transformation.helpers;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.commons.logging.Log;
import org.apache.excalibur.source.Source;

import org.apache.cocoon.acting.AbstractValidatorAction;
import org.apache.cocoon.acting.ConfigurationHelper;
import org.apache.cocoon.acting.ValidatorActionResult;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

/**
 * The <code>ValidatorActionResult</code> object helper.
 * Used by XSP.
 *
 * @version $Id$
 */
public class FormValidatorHelper {

    private static Map configurations = new HashMap();

    /**
     * these make it easier for the xsl
     */

    String current_descriptor;
    boolean current_reloadable = true;
    Log current_logger;
    String current_constraint_set;
    String current_parameter;
    SourceResolver current_resolver;


    public FormValidatorHelper(String descriptor, boolean reloadable,
                               Log logger, SourceResolver resolver) {
        current_descriptor = descriptor;
        current_reloadable = reloadable;
        current_logger = logger;
        current_resolver = resolver;
    }

    public FormValidatorHelper(String descriptor, boolean reloadable,
                               Log logger, SourceResolver resolver,
                               String constraintset) {
        current_descriptor = descriptor;
        current_reloadable = reloadable;
        current_logger = logger;
        current_resolver = resolver;
        current_constraint_set = constraintset;
    }

    /**
     * keep track of current parameter context
     */
    public void setParameter(String parameter) {
        current_parameter = parameter;
    }

    /**
     * keep track of current constraint-set context
     * (probably this is not needed?)
     */
    public void setConstraintSet(String constraintset) {
        current_constraint_set = constraintset;
    }

    /**
     * Get the specified attribute
     *
     * @param objectModel The Map objectModel
     * @param name The parameter name
     */
    public static Object getAttribute(Map objectModel, String name) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return request.getAttribute(name);
    }

    /**
     * Extracts the validation results from the request attribute
     *
     * @param objectModel The Map objectModel
     * @return Map with ValidatorActionResults
     * @see org.apache.cocoon.acting.ValidatorActionResult
     */
    public static Map getResults(Map objectModel) {
        Request request = ObjectModelHelper.getRequest(objectModel);
        return (Map) request.getAttribute(AbstractValidatorAction.FORMVALIDATOR_PATH);
    }


    /**
     * Extracts the validation results from the request attribute
     * for a specific request parameter
     *
     * @param objectModel The Map objectModel
     * @param name Request parameter's name
     * @see org.apache.cocoon.acting.ValidatorActionResult
     */
    public static ValidatorActionResult getParamResult(Map objectModel,
                                                       String name) {
        ValidatorActionResult result = ValidatorActionResult.NOTPRESENT;
        Map param_result = getResults(objectModel);
        if (param_result != null) {
            result = (ValidatorActionResult) param_result.get(name);
        }
        return (result != null? result : ValidatorActionResult.NOTPRESENT);
    }

    /**
     * Extracts the validation results from the request attribute
     * for the context's current request parameter
     *
     * @param objectModel The Map objectModel
     * @see org.apache.cocoon.acting.ValidatorActionResult
     */
    public ValidatorActionResult getParamResult(Map objectModel) {
        ValidatorActionResult result = ValidatorActionResult.NOTPRESENT;
        Map param_result = getResults(objectModel);
        if (param_result != null) {
            result = (ValidatorActionResult) param_result.get(current_parameter);
        }
        return (result != null? result : ValidatorActionResult.NOTPRESENT);
    }


    /**
     * Test whether the validation returned no error for this
     * parameter.
     *
     * @param objectModel The Map objectModel
     * @param name Request parameter's name
     * @return true only if the parameter was validated and the validation
     * did not return an error.
     */
    public static boolean isOK(Map objectModel, String name) {
        return getParamResult(objectModel, name).equals(ValidatorActionResult.OK);
    }

    /**
     * Test whether the validation returned no error for the
     * context's current parameter.
     *
     * @param objectModel The Map objectModel
     * @return true only if the parameter was validated and the validation
     * did not return an error.
     */
    public boolean isOK(Map objectModel) {
        return isOK(objectModel, current_parameter);
    }


    /**
     * Test whether the validation returned an error for this
     * parameter.
     *
     * @param objectModel The Map objectModel
     * @param name Request parameter's name
     * @return true if the parameter was either not validated or the validation
     * returned an error.
     */
    public static boolean isError(Map objectModel, String name) {
        return getParamResult(objectModel, name).ge(ValidatorActionResult.ERROR);
    }

    /**
     * Test whether the validation returned an error for the
     * context's current parameter.
     *
     * @param objectModel The Map objectModel
     * @return true if the parameter was either not validated or the validation
     * returned an error.
     */
    public boolean isError(Map objectModel) {
        return isError(objectModel, current_parameter);
    }


    /**
     * Test whether the validated parameter was null but wasn't allowed to.
     *
     * @param objectModel The Map objectModel
     * @param name Request parameter's name
     * @return true if the parameter was validated and the validation
     * returned an error because the parameter was null but wasn't allowd to.
     */
    public static boolean isNull(Map objectModel, String name) {
        return getParamResult(objectModel, name).equals(ValidatorActionResult.ISNULL);
    }

    /**
     * Test whether the context's current parameter as validated was null but
     * wasn't allowed to.
     *
     * @param objectModel The Map objectModel
     * @return true if the parameter was validated and the validation
     * returned an error because the parameter was null but wasn't allowd to.
     */
    public boolean isNull(Map objectModel) {
        return isNull(objectModel, current_parameter);
    }


    /**
     * Test whether the validated parameter was too small.
     *
     * @param objectModel The Map objectModel
     * @param name Request parameter's name
     * @return true if the parameter was validated and the validation
     * returned an error because either its value or its length was
     * too small.
     */
    public static boolean isTooSmall(Map objectModel, String name) {
        boolean ok = getParamResult(objectModel, name).equals(ValidatorActionResult.TOOSMALL);

        if (!ok) {
            ok = isNull(objectModel, name);
        }

        return ok;
    }

    /**
     * Test whether the context's current parameter was too small.
     *
     * @param objectModel The Map objectModel
     * @return true if the parameter was validated and the validation
     * returned an error because either its value or its length was
     * too small.
     */
    public boolean isTooSmall(Map objectModel) {
        return isTooSmall(objectModel, current_parameter);
    }


    /**
     * Test whether the validated parameter was too large.
     *
     * @param objectModel The Map objectModel
     * @param name Request parameter's name
     * @return true if the parameter was validated and the validation
     * returned an error because either its value or its length was
     * too large.
     */
    public static boolean isTooLarge(Map objectModel, String name) {
        return (getParamResult(objectModel, name) == ValidatorActionResult.TOOLARGE);
    }

    /**
     * Test whether the context's current parameter was too large.
     *
     * @param objectModel The Map objectModel
     * @return true if the parameter was validated and the validation
     * returned an error because either its value or its length was
     * too large.
     */
    public boolean isTooLarge(Map objectModel) {
        return isTooLarge(objectModel, current_parameter);
    }


    /**
     * Test whether the validated parameter wasn't matched by the requested
     * regular expression.
     *
     * @param objectModel The Map objectModel
     * @param name Request parameter's name
     * @return true if the parameter was validated and the validation
     * returned an error because its value wasn't matched by the requested
     * regular expression.
     */
    public static boolean isNoMatch(Map objectModel, String name) {
        return getParamResult(objectModel, name).equals(ValidatorActionResult.NOMATCH);
    }

    /**
     * Test whether the context's current parameter wasn't matched by the requested
     * regular expression.
     *
     * @param objectModel The Map objectModel
     * @return true if the parameter was validated and the validation
     * returned an error because its value wasn't matched by the requested
     * regular expression.
     */
    public boolean isNoMatch(Map objectModel) {
        return isNoMatch(objectModel, current_parameter);
    }


    /**
     * Test whether the validated parameter wasn't validated
     *
     * @param objectModel The Map objectModel
     * @param name Request parameter's name
     * @return true if the parameter was not validated.
     */
    public static boolean isNotPresent(Map objectModel, String name) {
        return getParamResult(objectModel, name).equals(ValidatorActionResult.NOTPRESENT);
    }

    /**
     * Test whether the context's current parameter wasn't validated
     *
     * @param objectModel The Map objectModel
     * @return true if the parameter was not validated.
     */
    public boolean isNotPresent(Map objectModel) {
        return isNotPresent(objectModel, current_parameter);
    }


    /**
     * Set up the complementary configuration file.  Please note that
     * multiple Actions can share the same configurations.  By using
     * this approach, we can limit the number of config files.
     * Also note that the configuration file does not have to be a file.
     *
     * This is based on the similar named functions in
     * org.apache.cocoon.acting.AbstractComplimentaryConfigurableAction
     * with the addition of reloadable configuration files, reloadable
     * flagg, manager, and logger  parameter.
     *
     * @param descriptor URL of descriptor.xml file @see org.apache.cocoon.acting.AbstractComplimentaryConfigurableAction
     * @param resolver
     * @param reloadable set to <code>true</code> if changes of
     * <code>descriptor</code> should trigger a reload. Note that this
     * only works if <code>Source</code> is able to determine the
     * modification time @see org.apache.cocoon.environment.Source
     * @param logger used to send debug and error messages to
     * @return up-to-date configuration, either (re)loaded or cached.
     */

    protected static Configuration getConfiguration(String descriptor, SourceResolver resolver,
                                                    boolean reloadable, Log logger)
            throws ConfigurationException {

        if (descriptor == null) {
            throw new ConfigurationException("The form descriptor is not set!");
        }

        ConfigurationHelper conf = null;
        synchronized (FormValidatorHelper.configurations) {
            Source source = null;
            try {
                source = resolver.resolveURI(descriptor);
                conf = (ConfigurationHelper) FormValidatorHelper.configurations.get(source.getURI());
                if (conf == null || (reloadable && conf.lastModified != source.getLastModified())) {
                    logger.debug("(Re)Loading " + descriptor);

                    if (conf == null) {
                        conf = new ConfigurationHelper();
                    }

                    SAXConfigurationHandler builder = new SAXConfigurationHandler();
                    SourceUtil.toSAX(source, builder);

                    conf.lastModified = source.getLastModified();
                    conf.configuration = builder.getConfiguration();

                    FormValidatorHelper.cacheConfiguration(source.getURI(), conf);
                } else {
                    logger.debug("Using cached configuration for " + descriptor);
                }
            } catch (Exception e) {
                logger.error("Could not configure Database mapping environment", e);
                throw new ConfigurationException("Error trying to load configurations for resource: " + source.getURI());
            } finally {
                resolver.release(source);
            }
        }

        return conf.configuration;
    }

    /**
     * Cache the configuration so that we can use it later.
     */
    private static void cacheConfiguration(String descriptor, ConfigurationHelper conf) {
        synchronized (FormValidatorHelper.configurations) {
            FormValidatorHelper.configurations.put(descriptor, conf);
        }
    }

    /**
     * Iterate over a set of configurations and return the one whose
     * name matches the given one.
     *
     * @param conf set of configurations
     * @param name name of configuration
     * @param logger
     * @return specified configuration or <code>null</code> if not found.
     */
    protected static Configuration getConfigurationByName(Configuration[] conf,
                                                          String name,
                                                          Log logger) {
        int j = 0;
        boolean found = false;
        String setname = null;
        for (j = 0; j < conf.length; j++) {
            setname = conf[j].getAttribute("name", "");
            if (name.trim().equals(setname.trim())) {
                found = true;
                break;
            }
        }
        if (!found) {
            logger.debug("FormValidatorHelper.getConfigurationByName: configuration " + name + " not found.");
            return null;
        }
        return conf[j];
    }

    /**
     * Get an attribute for a parameter as specified in
     * descriptor.xml.
     *
     * @param descriptor URL of descriptor.xml file @see org.apache.cocoon.acting.AbstractComplimentaryConfigurableAction
     * @param resolver
     * @param reloadable set to <code>true</code> if changes of
     * <code>descriptor</code> should trigger a reload. Note that this
     * only works if <code>Source</code> is able to determine the
     * modification time @see org.apache.cocoon.environment.Source
     * @param logger used to send debug and error messages to
     * @param attribute attribute name
     * @return attribute value or <code>null</code>
     */
    public static String getParameterAttributes(String descriptor,
                                                SourceResolver resolver,
                                                boolean reloadable,
                                                String constraintset,
                                                String parameter,
                                                String attribute,
                                                Log logger) {
        try {
            Configuration conf = getConfiguration(descriptor, resolver, reloadable, logger);
            Configuration[] desc = conf.getChildren("parameter");
            Configuration[] csets = conf.getChildren("constraint-set");

            Configuration cset = getConfigurationByName(csets, constraintset, logger);

            Configuration[] set = cset.getChildren("validate");
            Configuration constraints = getConfigurationByName(set, parameter, logger);
            Configuration descr = getConfigurationByName(desc, parameter, logger);
            return constraints.getAttribute(attribute, descr.getAttribute(attribute, ""));
        } catch (Exception e) {
            logger.debug("FormValidatorHelper.getParameterAttributes Exception " + e);
        }
        
        return "";
    }


    /**
     * Get an attribute for the context's current parameter as specified in
     * descriptor.xml.
     *
     * @param attribute attribute name
     * @return attribute value or <code>null</code>
     */
    public String getParameterAttribute(String attribute) {
        return FormValidatorHelper.getParameterAttributes(current_descriptor,
                current_resolver,
                current_reloadable,
                current_constraint_set,
                current_parameter,
                attribute,
                current_logger);
    }

    /**
     * Get an attribute for a parameter as specified in
     * descriptor.xml.
     *
     * @param attribute attribute name
     * @return attribute value or <code>null</code>
     */
    public String getParameterAttribute(String parameter, String attribute) {
        return FormValidatorHelper.getParameterAttributes(current_descriptor,
                current_resolver,
                current_reloadable,
                current_constraint_set,
                parameter,
                attribute,
                current_logger);
    }
}
