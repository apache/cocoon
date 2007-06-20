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
package org.apache.cocoon.forms.transformation;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.util.Deprecation;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Variables;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @version $Id$
 */
public class FormsPipelineConfig {

    /**
     * Default key under which the Cocoon Forms form instance is stored in the JXPath context.
     */
    public static final String CFORMSKEY = "CocoonFormsInstance";

    /**
     * Name of the request attribute under which the Cocoon Form is stored (optional). */
    private final String attributeName;

    /**
     * Pointer to the current request object.
     */
    private final Request request;

    /**
     * Initialized jxpathcontext to evaluate passed expressions with.
     */
    private final JXPathContext jxpathContext;

    /**
     * Containts locale specified as a parameter to the transformer, if any.
     */
    private final Locale localeParameter;

    /**
     * The locale currently used by the transformer.
     */
    private Locale locale;

    /**
     * Value for the action attribute of the form.
     */
    private String formAction;

    /**
     * Value for the method attribute of the form.
     */
    private String formMethod;


    private FormsPipelineConfig(JXPathContext jxpc, Request req, Locale localeParam,
                                String attName, String actionExpression, String method) {
        this.attributeName = attName;
        this.request = req;
        this.jxpathContext =jxpc;
        this.localeParameter = localeParam;
        this.formAction = translateText(actionExpression);
        this.formMethod = method;
    }

    /**
     * Creates and initializes a FormsPipelineConfig object based on the passed
     * arguments of the setup() of the specific Pipeline-component.
     *
     * @param objectModel the objectmodel as passed in the setup()
     * @param parameters the parameters as passed in the setup()
     * @return an instance of FormsPipelineConfig initialized according to the
     * settings in the sitemap.
     */
    public static FormsPipelineConfig createConfig(Map objectModel, Parameters parameters) {
        // create and set the jxpathContext...
        Object flowContext = FlowHelper.getContextObject(objectModel);
        WebContinuation wk = FlowHelper.getWebContinuation(objectModel);
        JXPathContext jxpc = JXPathContext.newContext(flowContext);
        // We manually create a cocoon object here to provide the same way
        // of accessing things as in the jxtg
        // as soon as we have our unified om, we should use that
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(false);
        final Map cocoonOM = new HashMap();
        cocoonOM.put("continuation", wk);
        cocoonOM.put("request", request);
        if ( session != null ) {
            cocoonOM.put("session", session);
        }
        cocoonOM.put("parameters", parameters);

        FormsVariables vars = new FormsVariables();
        vars.declareVariable("cocoon", cocoonOM);
        // These four are deprecated!
        vars.declareVariable("continuation", wk);
        vars.declareVariable("request", request);
        vars.declareVariable("session", session);
        vars.declareVariable("parameters", parameters);
        vars.addDeprecatedVariable("continuation");
        vars.addDeprecatedVariable("request");
        vars.addDeprecatedVariable("session");
        vars.addDeprecatedVariable("parameters");
        jxpc.setVariables(vars);

        Locale localeParameter = null;
        String localeStr = parameters.getParameter("locale", null);
        if (localeStr != null) {
            localeParameter = I18nUtils.parseLocale(localeStr);
        }

        String attributeName = parameters.getParameter("attribute-name", null);
        String actionExpression = parameters.getParameter("form-action", null);
        String formMethod = parameters.getParameter("form-method", null);
        //TODO (20031223 mpo)think about adding form-encoding for the Generator.
        // Note generator will also need some text to go on the submit-button?
        // Alternative to adding more here is to apply xinclude ?

        return new FormsPipelineConfig(jxpc, request, localeParameter,
                attributeName, actionExpression, formMethod);
    }

    /**
     * Overloads {@link #findForm(String)} by setting the jxpath-expression to null
     */
    public Form findForm() throws SAXException {
        return this.findForm(null);
    }

    /**
     * Finds the form from the current request-context based on the settings of
     * this configuration object.  The fall-back search-procedure is as follows:
     * <ol><li>Use the provided jxpathExpression (if not null)</li>
     * <li>Use the setting of the 'attribute-name' parameter on the request</li>
     * <li>Obtain the form from it's default location in the flow context</li>
     * </ol>
     *
     * @param jxpathExpression that should be pointing to the form
     * @return the found form if found
     * @throws SAXException in any of the following cases:
     * <ul><li>The provided jxpathExpression (if not null) not point to a
     * {@link Form} instance.</li>
     * <li>The request is not holding a {@link Form} instance under the key
     * specified by 'attribute-name' (if specified)</li>
     * <li>Both jxpathExpression and 'attribute-name' were not specified AND
     * also the default location was not holding a valid {@link Form} instance.</li>
     * </ol>
     */
    public Form findForm(String jxpathExpression) throws SAXException {
        Object form = null;
        if (jxpathExpression != null) {
            form = this.jxpathContext.getValue(jxpathExpression);
            if (form == null) {
                throw new SAXException("No Cocoon Form found at location \"" + jxpathExpression + "\".");
            } else if (!(form instanceof Form)) {
                throw new SAXException("Object returned by expression \"" + jxpathExpression + "\" is not a Cocoon Form.");
            }
        } else if (this.attributeName != null) { // then see if an attribute-name was specified
            form = this.request.getAttribute(this.attributeName);
            if (form == null) {
                throw new SAXException("No Cocoon Form found in request attribute with name \"" + this.attributeName + "\"");
            } else if (!(form instanceof Form)) {
                throw new SAXException("Object found in request (attribute = '" + this.attributeName + "') is not a Cocoon Form.");
            }
        } else { // and then see if we got a form from the flow
            jxpathExpression = "/" + FormsPipelineConfig.CFORMSKEY;
            try {
                form = this.jxpathContext.getValue(jxpathExpression);
            } catch (JXPathException e) { /* do nothing */ }
            if (form == null) {
                throw new SAXException("No Cocoon Form found.");
            }
        }
        return (Form)form;
    }

    /**
     * Replaces JXPath expressions embedded inside #{ and } by their value.
     * This will parse the passed String looking for #{} occurences and then
     * uses the {@link #evaluateExpression(String)} to evaluate the found expression.
     *
     * @return the original String with it's #{}-parts replaced by the evaulated results.
     */
    public String translateText(String original) {
        if (original==null) {
            return null;
        }

        StringBuffer expression;
        StringBuffer translated = new StringBuffer();
        StringReader in = new StringReader(original);
        int chr;
        try {
            while ((chr = in.read()) != -1) {
                char c = (char) chr;
                if (c == '#') {
                    chr = in.read();
                    if (chr != -1) {
                        c = (char) chr;
                        if (c == '{') {
                            expression = new StringBuffer();
                            boolean more = true;
                            while ( more ) {
                                more = false;
                                if ((chr = in.read()) != -1) {
                                    c = (char)chr;
                                    if (c != '}') {
                                        expression.append(c);
                                        more = true;
                                    } else {
                                        translated.append(evaluateExpression(expression.toString()).toString());
                                    }
                                } else {
                                    translated.append('#').append('{').append(expression);
                                }
                            }
                        }
                    } else {
                        translated.append((char) chr);
                    }
                } else {
                    translated.append(c);
                }
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        return translated.toString();
    }

    /**
     * Evaluates the passed xpath expression using the internal jxpath context
     * holding the declared variables:
     * <ol><li>continuation: as made available by flowscript</li>
     * <li>request: as present in the cocoon processing environment</li>
     * <li>session: as present in the cocoon processing environment</li>
     * <li>parameters: as present in the cocoon sitemap node of the pipeline component</li></ol>
     *
     * @param expression
     * @return the object-value resulting the expression evaluation.
     */
    public Object evaluateExpression(String expression) {
        return this.jxpathContext.getValue(expression);
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocaleParameter() {
        return localeParameter;
    }

    /**
     * The value for the wi:form-generated/@action.
     * Note: wi:form-template copies this from its wt:form-template counterpart.
     *
     * @return the {@link #translateText(String)} result of the 'form-action' sitemap
     * parameter to the pipeline component, or null if that parameter was not set.
     */
    public String getFormAction() {
        return formAction;
    }

    /**
     * The value for the wi:form-generated/@method.
     * Note: wi:form-template copies this from its wt:form-template counterpart.
     *
     * @return the value of the 'form-method' sitemap parameter to the pipeline
     * component. (or 'null' if it was not set.)
     */
    public String getFormMethod() {
        return formMethod;
    }


    /**
     * Sets the form method to use in the generator/transformer that uses this.
     *
     * @param method to use in the generated form should be "POST", "GET" or null
     */
    public void setFormMethod(String method) {
        this.formMethod = method;
    }

    /**
     * The grouped attributes to set on the wi:form-generated element.
     * Note: wi:form-template copies this from its wt:form-template counterpart.
     *
     * @see #getFormAction()
     * @see #getFormMethod()
     */
    public Attributes getFormAttributes() {
        AttributesImpl attrs = new org.apache.cocoon.xml.AttributesImpl();
        addFormAttributes(attrs);
        return attrs;
    }

    public void addFormAttributes(AttributesImpl attrs) {
        if (getFormAction() != null) {
            attrs.addAttribute("", "action", "action", "CDATA", getFormAction());
        }
        if (getFormMethod() != null){
            attrs.addAttribute("", "method", "method", "CDATA", getFormMethod());
        }
    }

    public static final class FormsVariables implements Variables {

        final Map vars = new HashMap();
        final List deprecatedNames = new ArrayList();

        public void addDeprecatedVariable(String name) {
            this.deprecatedNames.add(name);
        }

        /* (non-Javadoc)
         * @see org.apache.commons.jxpath.Variables#declareVariable(java.lang.String, java.lang.Object)
         */
        public void declareVariable(String name, Object value) {
            this.vars.put(name, value);
        }

        /* (non-Javadoc)
         * @see org.apache.commons.jxpath.Variables#getVariable(java.lang.String)
         */
        public Object getVariable(String name) {
            Object value = this.vars.get(name);
            if ( deprecatedNames.contains(name) ) {
                Deprecation.logger.warn("CForms: usage of the variable '" + name + "' is deprecated."+
                                        "Please use 'cocoon/" + name + "' instead. The usage of just '"+
                                        name+"' will be removed in Cocoon 2.2.");
            }
            return value;
        }

        /* (non-Javadoc)
         * @see org.apache.commons.jxpath.Variables#isDeclaredVariable(java.lang.String)
         */
        public boolean isDeclaredVariable(String name) {
            return this.vars.containsKey(name);
        }

        /* (non-Javadoc)
         * @see org.apache.commons.jxpath.Variables#undeclareVariable(java.lang.String)
         */
        public void undeclareVariable(String name) {
            this.vars.remove(name);
        }
    }
}
