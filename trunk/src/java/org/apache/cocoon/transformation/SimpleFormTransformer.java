/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.transformation;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.avalon.excalibur.pool.Recyclable;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ValidatorActionResult;
import org.apache.cocoon.transformation.helpers.FormValidatorHelper;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.HashMap;
import org.apache.cocoon.xml.dom.DOMStreamer;

import org.w3c.dom.DocumentFragment;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** 
 * Eliminates the need for XSP to use FormValidatorAction or HTML forms. 
 * Caveat: Select options need a value attribute to work correctly.
 *
 * <p>This transformer fills all HTML 4 form elements with values from
 * an InputModule, e.g. request, with the same name. It handles select
 * boxes, textareas, checkboxes, radio buttons, password and text
 * fields, and buttons. Form elements and complete forms can be protected
 * from substitution by adding an attribute fixed="true" to them.</p>
 *
 * <p>In addition, it handles FormValidatorAction results by
 * selectively omitting &lt;error/&gt; elements. These elements need a
 * "name" attribute corresponding to the name of the form element, and
 * either a "when" or a "when-ge" attribute.</p>
 *
 * <p>An error element is send down the pipeline if validation results
 * are available and either the results equals the "when" attribute or
 * the result is greater or equal to the "when-ge" attribute.</p>
 *
 * <p>Names for validation results are "ok", "not-present", "error",
 * "is-null", "too-small", "too-large", and "no-match" for the similar
 * named values from ValidatorActionResult.</p>
 *
 * <p>There need not to be an "error" element for every form element,
 * multiple error elements for the same form element may be
 * present.</p>
 *
 * <p><em>Names of error elements are never augmented by prefix, suffix or
 * form name.</em></p>
 * 
 * <p>Page parts with multiple occurrences depending on the number of
 * actual parameters can be enclosed in &lt;repeat on="expr" using="var"/&gt;
 * elements. <em>expr</em> is used to determine the number of occurrences
 * and <em>var</em> will be expanded with the ordinary number. Repeat elements
 * can be nested.</p>
 * 
 * <p>Example:</p>
 * <pre>
 *  <repeat on="mult" using="i"><input type="text" name="mult[${i}]"/></repeat>
 * </pre>
 * <p>Will include as many input elements as mult parameters are present. Adding
 * the repeater variable to the elements name is necessary only with structured
 * parameters or when they should be numbered. See also the <em>strip-number</em>
 * configuration parameter.</p>
 *
 * <p>To use this transformer, add the following to your
 * transformation pipeline: <pre>
 *   &lt;map:transform type="simple-form"/&gt;
 * </pre></p>
 *
 * <p>Configuration elements:
 * <table>
 *   <tr><td>input-module</td><td>(String) InputModule configuration, 
 *           defaults to an empty configuration and the "request-param" module</td></tr>
 *   <tr><td>fixed-attribute</td><td>(String) Name of the attribute used to 
 *           indicate that this element should not be changed. ("fixed")</td></tr>
 *   <tr><td>use-form-name</td><td>(boolean) Add the name of the form to the
 *           name of form elements. Uses default Separator , if default separator is null
 *           or empty, separator is set to "/". ("false")</td></tr>
 *   <tr><td>use-form-name-twice</td><td>(boolean) Add the name of the form twice to the
 *           name of form elements. This is useful when the form instance has no
 *           all enclosing root tag and the form name is used instead <em>and</em> the
 *           form name needs to be used to find the form data. Uses default Separator , 
 *           if default separator is null or empty, separator is set to "/".("false")</td></tr>
 *   <tr><td>separator</td><td>(String) Separator between form name and element name ("/")
 *           </td></tr>
 *   <tr><td>prefix</td><td>(String) Prefix to add to element name for value lookup. No
 *           separator will be added between prefix and rest of the name. Default
 *           is "", when use-form-name is set, defaults to separator.</td></tr>
 *   <tr><td>suffix</td><td>(String) Added to the input element's name. No
 *           separator will be added between rest of the name and suffix. ("")</td></tr>
 *   <tr><td>ignore-validation</td><td>(boolean) If set to true, all error
 *           tags are copied as is regardless of the validation results.("false")</td></tr>
 *   <tr><td>decoration</td><td>(int) Length of decorations around repeat variable. Example:
 *           when using JXPath based module, decoration would be "[" and "]", hence 1. (1)</td></tr>
 *   <tr><td>strip-number</td><td>(boolean) If set to false, element names of repeated
 *           elements will contain the expanded repeater variable. ("true")</td></tr>
 * </table>
 * </p>
 *
 * <p>Sitemap parameters:
 * <table>
 *   <tr><td>fixed</td><td>(boolean) Do not change values</td></tr>
 *   <tr><td>prefix</td><td>(String) Added to the input element's name</td></tr>
 *   <tr><td>suffix</td><td>(String) Added to the input element's name</td></tr>
 *   <tr><td>input</td><td>(String) InputModule name</td></tr>
 *   <tr><td>decoration</td><td>(int) Length of decorations around repeat variable.</td></tr>
 *   <tr><td>strip-number</td><td>(boolean) Expanded repeater variable.</td></tr>
 * </table>
 * </p>
 *
 * <p>Example:<pre>
 *     &lt;input name="user.name" size="50" maxlength="60"/&gt;
 *     &lt;error name="user.name" when-ge="error"&gt;required&lt;/error&gt;
 * </pre></p>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SimpleFormTransformer.java,v 1.11 2004/03/10 17:58:05 unico Exp $
 */
public class SimpleFormTransformer extends AbstractSAXTransformer implements Recyclable {

    /** strip numbers from repeated element name attributes */
    private boolean stripNumber = true;

    /** Symbolic names for elements */
    /** unknown element */
    private static final int ELEMENT_DEFAULT = 0;
    /** input element */
    private static final int ELEMENT_INPUT = 1;
    /** select element */
    private static final int ELEMENT_SELECT = 2;
    /** option element */
    private static final int ELEMENT_OPTION = 3;
    /** textarea element */
    private static final int ELEMENT_TXTAREA = 4;
    /** error element */
    private static final int ELEMENT_ERROR = 5;
    /** form element */
    private static final int ELEMENT_FORM = 6;
    /** repeat element */
    private static final int ELEMENT_REPEAT = 7;
    /** default element as Integer (needed as default in org.apache.cocoon.util.HashMap.get()) */
    private static final Integer defaultElement = new Integer(ELEMENT_DEFAULT);

    /** input type unknown */
    private static final int TYPE_DEFAULT = 0;
    /** input type checkbox */
    private static final int TYPE_CHECKBOX = 1;
    /** input type radio */
    private static final int TYPE_RADIO = 2;
    /** default input type as Integer (needed as default in org.apache.cocoon.util.HashMap.get()) */
    private static final Integer defaultType = new Integer(TYPE_DEFAULT);

    protected static final String INPUT_MODULE_ROLE = InputModule.ROLE;
    protected static final String INPUT_MODULE_SELECTOR = INPUT_MODULE_ROLE + "Selector";

    /** map element name string to symbolic name */
    private static final HashMap elementNames;
    /** map input type string to symbolic name */
    private static final HashMap inputTypes;
    /** map ValidatorActionResult to name string */
    private static final HashMap validatorResults;
    /** map name string to ValidatorActionResult */
    private static final HashMap validatorResultLabel;

    /** setup mapping tables */
    static {
        HashMap names = new HashMap();
        names.put("input", new Integer(ELEMENT_INPUT));
        names.put("select", new Integer(ELEMENT_SELECT));
        names.put("option", new Integer(ELEMENT_OPTION));
        names.put("textarea", new Integer(ELEMENT_TXTAREA));
        names.put("error", new Integer(ELEMENT_ERROR));
        names.put("form", new Integer(ELEMENT_FORM));
        names.put("repeat", new Integer(ELEMENT_REPEAT));
        elementNames = names;
        names = null;

        names = new HashMap();
        names.put("checkbox", new Integer(TYPE_CHECKBOX));
        names.put("radio", new Integer(TYPE_RADIO));
        inputTypes = names;
        names = null;

        names = new HashMap();
        names.put("ok", ValidatorActionResult.OK);
        names.put("not-present", ValidatorActionResult.NOTPRESENT);
        names.put("error", ValidatorActionResult.ERROR);
        names.put("is-null", ValidatorActionResult.ISNULL);
        names.put("too-small", ValidatorActionResult.TOOSMALL);
        names.put("too-large", ValidatorActionResult.TOOLARGE);
        names.put("no-match", ValidatorActionResult.NOMATCH);
        validatorResultLabel = names;

        names = new HashMap();
        names.put(ValidatorActionResult.OK, "ok");
        names.put(ValidatorActionResult.NOTPRESENT, "not-present");
        names.put(ValidatorActionResult.ERROR, "error");
        names.put(ValidatorActionResult.ISNULL, "is-null");
        names.put(ValidatorActionResult.TOOSMALL, "too-small");
        names.put(ValidatorActionResult.TOOLARGE, "too-large");
        names.put(ValidatorActionResult.NOMATCH, "no-match");
        validatorResults = names;
        names = null;
    }

    /** current element's request parameter values */
    protected Object[] values = null;

    /** current request's validation results (all validated elements) */
    protected Map validationResults = null;

    /** Should we skip inserting values? */
    private boolean fixed = false;
    /** Is the complete document protected? */
    private boolean documentFixed = false;

    private String fixedName = "fixed";
    private String prefix = null;
    private String suffix = null;
    private String defaultPrefix = null;
    private String defaultSuffix = null;
    private String separator = null;
    private String formName = null;
    private boolean useFormName = false;
    private boolean useFormNameTwice = false;
    private boolean ignoreValidation = false;
    private int decorationSize = 1;

    private String defaultInput = "request-param";
    private Configuration defaultInputConf = null;
    private Configuration inputConf = null;
    private InputModule input = null;
    private ServiceSelector inputSelector = null;
    private String inputName = null;

    /** Skip element's content only. Otherwise skip also surrounding element. */
    protected boolean skipChildrenOnly = false;

    /** Count nested repeat elements. */
    protected int recordingCount = 0;

    /** List of {@link RepeaterStatus} elements keeping track of nested repeat blocks. */
    protected List repeater = null;

    /** Map of {@link ValueList} to track multiple parameters. */
    protected Map formValues = null;

    /**
     * Keep track of repeater status. 
     */
    protected class RepeaterStatus {
        public String var = null;
        public String expr = null;
        public int count = 0;

        public RepeaterStatus(String var, int count, String expr) {
            this.var = var;
            this.count = count;
            this.expr = expr;
        }

        public String toString() {
            return "[" + this.var + "," + this.expr + "," + this.count + "]";
        }
    }

    /**
     * Keep track of multiple values. 
     */
    protected class ValueList {
        private int current = -1;
        private Object[] values = null;

        public ValueList(Object[] values) {
            this.values = values;
            this.current = (values != null && values.length > 0 ? 0 : -1);
        }

        public Object getNext() {
            Object result = null;
            if (this.values != null) {
                if (this.current < this.values.length) {
                    result = this.values[this.current++];
                }
            }
            return result;
        }
    }

    public SimpleFormTransformer() {
        this.defaultNamespaceURI = "";
        this.namespaceURI = "";
    }

    /** set per instance variables to defaults */
    private void reset() {
        this.skipChildrenOnly = false;
        this.values = null;
        this.validationResults = null;
        this.documentFixed = false;
        this.fixed = false;
        this.formName = null;
        this.recordingCount = 0;
        this.repeater = new LinkedList();
        this.formValues = new HashMap();

        if (this.inputSelector != null) {
            if (this.input != null)
                this.inputSelector.release(this.input);
            this.manager.release(this.inputSelector);
        }
    }

    /**
     * Avalon Configurable Interface
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.defaultInputConf = config.getChild("input-module");
        this.defaultInput = this.defaultInputConf.getAttribute("name", this.defaultInput);
        this.separator = config.getChild("separator").getValue(this.separator);
        this.defaultPrefix = config.getChild("prefix").getValue(this.defaultPrefix);
        this.defaultSuffix = config.getChild("suffix").getValue(this.defaultSuffix);
        this.fixedName = config.getChild("fixed-attribute").getValue(this.fixedName);
        this.useFormName = config.getChild("use-form-name").getValueAsBoolean(this.useFormName);
        this.useFormNameTwice =
            config.getChild("use-form-name-twice").getValueAsBoolean(this.useFormNameTwice);
        this.useFormName = this.useFormName || this.useFormNameTwice;
        if (this.useFormName) {
            this.separator =
                (this.separator == null || this.separator.equals("") ? "/" : this.separator);
            this.defaultPrefix = this.separator;
        }
        this.ignoreValidation =
            config.getChild("ignore-validation").getValueAsBoolean(this.ignoreValidation);
        this.decorationSize = config.getChild("decoration").getValueAsInteger(this.decorationSize);
        this.stripNumber = config.getChild("strip-number").getValueAsBoolean(this.stripNumber);
    }

    /**
     * Read sitemap parameters and set properties accordingly.
     */
    private void evaluateParameters() {
        this.documentFixed = this.parameters.getParameterAsBoolean("fixed", false);
        this.fixed = this.documentFixed;
        this.prefix = this.parameters.getParameter("prefix", this.defaultPrefix);
        this.suffix = this.parameters.getParameter("suffix", this.defaultSuffix);
        this.inputName = this.parameters.getParameter("input", null);
        this.decorationSize =
            this.parameters.getParameterAsInteger("decoration", this.decorationSize);
        this.stripNumber = this.parameters.getParameterAsBoolean("strip-number", this.stripNumber);
    }

    /**
     * Setup the next round.
     * The instance variables are initialised.
     * @param resolver The current SourceResolver
     * @param objectModel The objectModel of the environment.
     * @param src The value of the src attribute in the sitemap.
     * @param par The parameters from the sitemap.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {

        this.reset();

        super.setup(resolver, objectModel, src, par);

        if (request == null) {
            getLogger().debug("no request object");
            throw new ProcessingException("no request object");
        }
        this.evaluateParameters();
        this.setupInputModule();

    }

    /**
     * Setup and obtain reference to the input module.
     */
    private void setupInputModule() {
        this.inputConf = null;
        if (this.ignoreValidation) {
            this.validationResults = null;
        } else {
            this.validationResults = FormValidatorHelper.getResults(this.objectModel);
        }

        if (this.inputName == null) {
            this.inputName = this.defaultInput;
            this.inputConf = this.defaultInputConf;
        }

        try {
            // obtain input module
            this.inputSelector = (ServiceSelector) this.manager.lookup(INPUT_MODULE_SELECTOR);
            if (this.inputName != null
                && this.inputSelector != null
                && this.inputSelector.isSelectable(this.inputName)) {
                this.input = (InputModule) this.inputSelector.select(this.inputName);
                if (!(this.input instanceof ThreadSafe
                    && this.inputSelector instanceof ThreadSafe)) {
                    this.inputSelector.release(this.input);
                    this.manager.release(this.inputSelector);
                    this.input = null;
                    this.inputSelector = null;
                }
            } else {
                if (this.inputName != null)
                    if (getLogger().isErrorEnabled())
                        getLogger().error(
                            "A problem occurred setting up '"
                                + this.inputName
                                + "': Selector is "
                                + (this.inputSelector != null ? "not " : "")
                                + "null, Component is "
                                + (this.inputSelector != null
                                    && this.inputSelector.isSelectable(this.inputName)
                                        ? "known"
                                        : "unknown"));
            }
        } catch (Exception e) {
            if (getLogger().isWarnEnabled())
                getLogger().warn(
                    "A problem occurred setting up '" + this.inputName + "': " + e.getMessage());
        }
    }

    /**
     *  Recycle this component.
     */
    public void recycle() {
        super.recycle();
        this.reset();
    }

    /** 
     * Generate string representation of attributes. For debug only.
     */
    protected String printAttributes(Attributes attr) {
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        for (int i = 0; i < attr.getLength(); i++) {
            sb.append('@').append(attr.getLocalName(i)).append("='").append(
                attr.getValue(i)).append(
                "' ");
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Handle input elements that may have a "checked" attributes,
     * i.e. checkbox and radio.
     */
    protected void startCheckableElement(
        String aName,
        String uri,
        String name,
        String raw,
        AttributesImpl attributes)
        throws SAXException {

        // @fixed and this.fixed already considered in startInputElement
        this.values = this.getValues(aName);
        String checked = attributes.getValue("checked");
        String value = attributes.getValue("value");
        boolean found = false;

        if (getLogger().isDebugEnabled())
            getLogger().debug(
                "startCheckableElement "
                    + name
                    + " attributes "
                    + this.printAttributes(attributes));
        if (this.values != null) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("replacing");
            for (int i = 0; i < this.values.length; i++) {
                if (this.values[i].equals(value)) {
                    found = true;
                    if (checked == null) {
                        attributes.addAttribute("", "checked", "checked", "CDATA", "");
                    }
                    break;
                }
            }
            if (!found && checked != null) {
                attributes.removeAttribute(attributes.getIndex("checked"));
            }
        }
        this.relayStartElement(uri, name, raw, attributes);
    }

    /**
     * Handle input elements that may don't have a "checked"
     * attributes, e.g. text, password, button.
     */
    protected void startNonCheckableElement(
        String aName,
        String uri,
        String name,
        String raw,
        AttributesImpl attributes)
        throws SAXException {

        // @fixed and this.fixed already considered in startInputElement
        Object fValue = this.getNextValue(aName);
        String value = attributes.getValue("value");
        if (getLogger().isDebugEnabled())
            getLogger().debug(
                "startNonCheckableElement "
                    + name
                    + " attributes "
                    + this.printAttributes(attributes));
        if (fValue != null) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("replacing");
            if (value != null) {
                attributes.setValue(attributes.getIndex("value"), String.valueOf(fValue));
            } else {
                attributes.addAttribute("", "value", "value", "CDATA", String.valueOf(fValue));
            }
        }
        this.relayStartElement(uri, name, raw, attributes);
    }

    /**
     * Handle input elements. Calls startCheckableElement or
     * startNonCheckableElement.
     */
    protected void startInputElement(String uri, String name, String raw, Attributes attr)
        throws SAXException {

        // @value = request.getParameterValues(@name)
        String aName = getName(attr.getValue("name"));
        String fixed = attr.getValue(this.fixedName);

        if (getLogger().isDebugEnabled())
            getLogger().debug(
                "startInputElement " + name + " attributes " + this.printAttributes(attr));
        if (aName == null || this.fixed || (fixed != null && parseBoolean(fixed))) {
            this.relayStartElement(uri, name, raw, attr);

        } else {
            if (getLogger().isDebugEnabled())
                getLogger().debug("replacing");

            attr = this.normalizeAttributes(attr);

            AttributesImpl attributes = null;
            if (attr instanceof AttributesImpl) {
                attributes = (AttributesImpl) attr;
            } else {
                attributes = new AttributesImpl(attr);
            }
            String type = attributes.getValue("type");
            switch (((Integer) inputTypes.get(type, defaultType)).intValue()) {
                case TYPE_CHECKBOX :
                case TYPE_RADIO :
                    this.startCheckableElement(aName, uri, name, raw, attributes);
                    break;

                case TYPE_DEFAULT :
                    this.startNonCheckableElement(aName, uri, name, raw, attributes);
                    break;
            }
            this.values = null;
        }
    }

    /**
     * Handle select elements. Sets up some instance variables for
     * following option elements.
     */
    protected void startSelectElement(String uri, String name, String raw, Attributes attr)
        throws SAXException {

        // this.values = request.getParameterValues(@name)
        String aName = getName(attr.getValue("name"));
        String fixed = attr.getValue(this.fixedName);
        this.values = null;
        if (getLogger().isDebugEnabled())
            getLogger().debug(
                "startSelectElement " + name + " attributes " + this.printAttributes(attr));
        if (aName != null && !(this.fixed || (fixed != null && parseBoolean(fixed)))) {
            if (attr.getIndex("multiple") > -1) {
                this.values = this.getValues(aName);
            } else {
                Object val = this.getNextValue(aName);
                if (val != null) {
                    this.values = new Object[1];
                    this.values[0] = val;
                } else {
                    this.values = null;
                }
            }
            attr = this.normalizeAttributes(attr);
        }
        this.relayStartElement(uri, name, raw, attr);
    }

    /**
     * Handle option elements. Uses instance variables set up by
     * startSelectElement. Relies on option having a "value"
     * attribute, i.e. does not check following characters if "value"
     * is not present.
     */
    protected void startOptionElement(String uri, String name, String raw, Attributes attr)
        throws SAXException {

        // add @selected if @value in request.getParameterValues(@name)
        if (getLogger().isDebugEnabled())
            getLogger().debug(
                "startOptionElement " + name + " attributes " + this.printAttributes(attr));
        if (this.values == null || this.fixed) {
            this.relayStartElement(uri, name, raw, attr);
        } else {
            if (getLogger().isDebugEnabled())
                getLogger().debug("replacing");
            AttributesImpl attributes = null;
            if (attr instanceof AttributesImpl) {
                attributes = (AttributesImpl) attr;
            } else {
                attributes = new AttributesImpl(attr);
            }
            String selected = attributes.getValue("selected");
            String value = attributes.getValue("value");
            boolean found = false;

            for (int i = 0; i < this.values.length; i++) {
                if (this.values[i].equals(value)) {
                    found = true;
                    if (selected == null) {
                        attributes.addAttribute("", "selected", "selected", "CDATA", "");
                    }
                    break;
                }
            }
            if (!found && selected != null) {
                attributes.removeAttribute(attributes.getIndex("selected"));
            }

            this.relayStartElement(uri, name, raw, attributes);
        }
    }

    /**
     * Handles textarea elements. Skips nested events if request
     * parameter with same name exists.
     */
    protected void startTextareaElement(String uri, String name, String raw, Attributes attributes)
        throws SAXException {

        String aName = getName(attributes.getValue("name"));
        String fixed = attributes.getValue(this.fixedName);
        Object value = null;
        if (getLogger().isDebugEnabled())
            getLogger().debug(
                "startTextareaElement " + name + " attributes " + this.printAttributes(attributes));
        if (aName != null) {
            value = this.getNextValue(aName);
        }
        if (value == null || this.fixed || (fixed != null && parseBoolean(fixed))) {
            this.relayStartElement(uri, name, raw, attributes);
        } else {
            if (getLogger().isDebugEnabled())
                getLogger().debug("replacing");
            this.relayStartElement(uri, name, raw, this.normalizeAttributes(attributes));
            String valString = String.valueOf(value);
            this.characters(valString.toCharArray(), 0, valString.length());
            // well, this doesn't really work out nicely. do it the hard way.
            if (this.ignoreEventsCount == 0)
                this.skipChildrenOnly = true;
            this.ignoreEventsCount++;
        }
    }

    /**
     * Handle error elements. If validation results are available,
     * compares validation result for parameter with the same name as
     * the "name" attribute with the result names is "when" and
     * "when-ge". Drops element and all nested events when error
     * condition is not met.
     */
    protected void startErrorElement(String uri, String name, String raw, Attributes attr)
        throws SAXException {

        if (getLogger().isDebugEnabled())
            getLogger().debug(
                "startErrorElement " + name + " attributes " + this.printAttributes(attr));
        if (this.ignoreValidation) {
            this.relayStartElement(uri, name, raw, attr);
        } else if (this.validationResults == null || this.fixed) {
            this.relayStartElement(true, false, uri, name, raw, attr);
        } else {
            String aName = attr.getValue("name");
            if (aName == null) {
                this.relayStartElement(uri, name, raw, attr);
            } else {
                ValidatorActionResult validation =
                    FormValidatorHelper.getParamResult(this.objectModel, aName);
                String when = attr.getValue("when");
                String when_ge = attr.getValue("when-ge");

                if ((when != null && when.equals(validatorResults.get(validation)))
                    || (when_ge != null
                        && validation.ge(
                            (ValidatorActionResult) validatorResultLabel.get(
                                when_ge,
                                ValidatorActionResult.MAXERROR)))) {
                    AttributesImpl attributes = null;
                    if (attr instanceof AttributesImpl) {
                        attributes = (AttributesImpl) attr;
                    } else {
                        attributes = new AttributesImpl(attr);
                    }
                    // remove attributes not meant for client
                    attributes.removeAttribute(attributes.getIndex("name"));
                    if (when != null)
                        attributes.removeAttribute(attributes.getIndex("when"));
                    if (when_ge != null)
                        attributes.removeAttribute(attributes.getIndex("when-ge"));
                    this.relayStartElement(uri, name, raw, this.normalizeAttributes(attributes));
                } else {
                    this.relayStartElement(true, true, uri, name, raw, attr);
                }
            }
        }
    }

    /**
     * Start processing a form element. Sets protection indicator if attribute
     * "fixed" is present and either "true" or "yes". Removes attribute "fixed" 
     * if present. 
     * @param uri The namespace of the element.
     * @param name The local name of the element.
     * @param raw The qualified name of the element.
     * @param attr The attributes of the element.
     */
    protected void startFormElement(String uri, String name, String raw, Attributes attr)
        throws SAXException {

        String fixed = attr.getValue(this.fixedName);
        if (this.useFormName) {
            this.formName = attr.getValue("name");
        }
        if (fixed == null) {
            this.relayStartElement(uri, name, raw, attr);
        } else {
            if (!this.fixed && parseBoolean(fixed)) {
                this.fixed = true;
            }
            // remove attributes not meant for client
            AttributesImpl attributes = null;
            if (attr instanceof AttributesImpl) {
                attributes = (AttributesImpl) attr;
            } else {
                attributes = new AttributesImpl(attr);
            }
            attributes.removeAttribute(attributes.getIndex(this.fixedName));
            this.relayStartElement(uri, name, raw, this.normalizeAttributes(attributes));
        }
    }

    /**
     * Start recording repeat element contents and push repeat expression and
     * variable to repeater stack. Only start recording, if no other recorder is
     * currently running.
     * 
     * @param uri
     * @param name
     * @param raw
     * @param attr
     * @throws SAXException
     */
    protected void startRepeatElement(String uri, String name, String raw, Attributes attr)
        throws SAXException {

        if (this.recordingCount == 0) {
            if (!(this.fixed || parseBoolean(attr.getValue(this.fixedName)))) {
                RepeaterStatus status =
                    new RepeaterStatus("${" + attr.getValue("using") + "}", 0, attr.getValue("on"));
                this.repeater.add(status);
                this.startRecording();
                this.recordingCount++;
            } else {
                this.relayStartElement(uri, name, raw, attr);
            }
        } else {
            this.relayStartElement(uri, name, raw, attr);
            this.recordingCount++;
        }
    }

    /**
     * Stop recording repeat contents and replay required number of times.
     * Stop only if outmost repeat element is ending.
     * 
     * @param uri
     * @param name
     * @param raw
     * @throws SAXException
     */
    protected void endRepeatElement(String uri, String name, String raw) throws SAXException {
        this.recordingCount--;
        if (this.recordingCount == 0) {
            DocumentFragment fragment = this.endRecording();
            RepeaterStatus status = (RepeaterStatus) this.repeater.get(this.repeater.size() - 1);
            Object[] vals = this.getValues(this.getName(status.expr));
            int count = (vals != null ? vals.length : 0);
            for (status.count = 1; status.count <= count; status.count++) {
                DOMStreamer streamer = new DOMStreamer(this, this);
                streamer.stream(fragment);
            }
            this.repeater.remove(this.repeater.size() - 1);
        } else {
            this.relayEndElement(uri, name, raw);
            if (this.recordingCount < 0) {
                this.recordingCount = 0;
            }
        }
    }

    /**
     * Start processing elements of our namespace.
     * This hook is invoked for each sax event with our namespace.
     * @param uri The namespace of the element.
     * @param name The local name of the element.
     * @param raw The qualified name of the element.
     * @param attr The attributes of the element.
     */
    public void startTransformingElement(String uri, String name, String raw, Attributes attr)
        throws SAXException {

        if (this.ignoreEventsCount == 0 && this.recordingCount == 0) {
            switch (((Integer) elementNames.get(name, defaultElement)).intValue()) {
                case ELEMENT_INPUT :
                    this.startInputElement(uri, name, raw, attr);
                    break;

                case ELEMENT_SELECT :
                    this.startSelectElement(uri, name, raw, attr);
                    break;

                case ELEMENT_OPTION :
                    this.startOptionElement(uri, name, raw, attr);
                    break;

                case ELEMENT_TXTAREA :
                    this.startTextareaElement(uri, name, raw, attr);
                    break;

                case ELEMENT_ERROR :
                    this.startErrorElement(uri, name, raw, attr);
                    break;

                case ELEMENT_FORM :
                    this.startFormElement(uri, name, raw, attr);
                    break;

                case ELEMENT_REPEAT :
                    this.startRepeatElement(uri, name, raw, attr);
                    break;

                default :
                    this.relayStartElement(uri, name, raw, attr);
            }

        } else if (this.recordingCount > 0) {
            switch (((Integer) elementNames.get(name, defaultElement)).intValue()) {
                case ELEMENT_REPEAT :
                    this.startRepeatElement(uri, name, raw, attr);
                    break;

                default :
                    this.relayStartElement(uri, name, raw, attr);
            }
        } else {
            this.relayStartElement(uri, name, raw, attr);
        }
    }

    /**
     * Start processing elements of our namespace.
     * This hook is invoked for each sax event with our namespace.
     * @param uri The namespace of the element.
     * @param name The local name of the element.
     * @param raw The qualified name of the element.
     */
    public void endTransformingElement(String uri, String name, String raw) throws SAXException {

        if (this.ignoreEventsCount > 0) {
            this.relayEndElement(uri, name, raw);
        } else if (this.recordingCount > 0) {
            switch (((Integer) elementNames.get(name, defaultElement)).intValue()) {
                case ELEMENT_REPEAT :
                    this.endRepeatElement(uri, name, raw);
                    break;

                default :
                    this.relayEndElement(uri, name, raw);
            }
        } else {
            switch (((Integer) elementNames.get(name, defaultElement)).intValue()) {
                case ELEMENT_SELECT :
                    this.values = null;
                    this.relayEndElement(uri, name, raw);
                    break;
                case ELEMENT_INPUT :
                case ELEMENT_OPTION :
                case ELEMENT_TXTAREA :
                case ELEMENT_ERROR :
                    this.relayEndElement(uri, name, raw);
                    break;
                case ELEMENT_FORM :
                    this.fixed = this.documentFixed;
                    this.formName = null;
                    this.relayEndElement(uri, name, raw);
                    break;

                case ELEMENT_REPEAT :
                    this.endRepeatElement(uri, name, raw);
                    break;

                default :
                    this.relayEndElement(uri, name, raw);
            }
        }
    }

    /**
     * Check if a string is one of "yes", "true" ignoring case.
     * @param aBoolean
     * @return true if string is one of "yes", true"
     */
    private static boolean parseBoolean(String aBoolean) {
        return "true".equalsIgnoreCase(aBoolean) || "yes".equalsIgnoreCase(aBoolean);
    }

    /**
     * Remove extra information from element's attributes. Currently only removes
     * the repeater variable from the element's name attribute if present.
     * 
     * @param attr
     * @return modified attributes
     */
    private Attributes normalizeAttributes(Attributes attr) {
        Attributes result = attr;
        if (this.stripNumber && this.repeater.size() > 0) {
            String name = attr.getValue("name");
            if (name != null) {
                for (Iterator i = this.repeater.iterator(); i.hasNext();) {
                    RepeaterStatus status = (RepeaterStatus) i.next();
                    int pos = name.indexOf(status.var);
                    if (pos >= 0) {
                        AttributesImpl attributes;
                        if (result instanceof AttributesImpl) {
                            attributes = (AttributesImpl) result;
                        } else {
                            attributes = new AttributesImpl(result);
                        }
                        name =
                            name.substring(0, pos - this.decorationSize)
                                + name.substring(pos + status.var.length() + this.decorationSize);
                        attributes.setValue(attributes.getIndex("name"), name);
                        result = attributes;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Generate the "real" name of an element for value lookup.
     * @param name
     * @return "real" name.
     */
    private String getName(String name) {
        String result = name;
        if (this.useFormName && this.formName != null) {
            if (this.separator != null) {
                if (this.useFormNameTwice) {
                    result =
                        this.formName + this.separator + this.formName + this.separator + result;
                } else {
                    result = this.formName + this.separator + result;
                }
            } else {
                if (this.useFormNameTwice) {
                    result = this.formName + result;
                } else {
                    // does this make sense ?
                    result = this.formName + this.formName + result;
                }
            }
        }
        if (this.prefix != null) {
            result = this.prefix + result;
        }
        if (this.suffix != null) {
            result = result + this.prefix;
        }
        if (this.repeater.size() > 0) {
            for (Iterator i = this.repeater.iterator(); i.hasNext();) {
                RepeaterStatus status = (RepeaterStatus) i.next();
                int pos = result.indexOf(status.var);
                if (pos != -1) {
                    result =
                        result.substring(0, pos)
                            + status.count
                            + result.substring(pos + status.var.length());
                }
            }
        }
        return result;
    }

    /**
     * Obtain values from used InputModule if not done already and return the
     * next value. If no more values exist, returns null.
     * 
     * @param name
     * @return
     */
    private Object getNextValue(String name) {
        Object result = null;
        if (this.formValues.containsKey(name)) {
            ValueList vList = (ValueList) this.formValues.get(name);
            result = vList.getNext();
        } else {
            ValueList vList = new ValueList(this.getValues(name));
            result = vList.getNext();
            this.formValues.put(name, vList);
        }
        return result;
    }

    /**
     * Obtain values from the used InputModule.
     */
    private Object[] getValues(String name) {
        Object[] values = null;
        ServiceSelector iputSelector = null;
        InputModule iput = null;
        try {
            if (this.input != null) {
                // input module is thread safe
                // thus we still have a reference to it
                values = input.getAttributeValues(name, this.inputConf, objectModel);
                if (getLogger().isDebugEnabled())
                    getLogger().debug("cached module " + this.input
                                      + " attribute " + name
                                      + " returns " + values);
            } else {
                // input was not thread safe
                // so acquire it again
                iputSelector = (ServiceSelector)this.manager.lookup(INPUT_MODULE_SELECTOR);
                if (this.inputName != null
                    && iputSelector != null
                    && iputSelector.isSelectable(this.inputName)) {

                    iput = (InputModule) iputSelector.select(this.inputName);
                }
                if (iput != null) {
                    values = iput.getAttributeValues(name, this.inputConf, objectModel);
                }
                if (getLogger().isDebugEnabled())
                    getLogger().debug(
                        "fresh module " + iput + " attribute " + name + " returns " + values);
            }
        } catch (Exception e) {
            if (getLogger().isWarnEnabled())
                getLogger().warn(
                    "A problem occurred acquiring a value from '"
                        + this.inputName
                        + "' for '"
                        + name
                        + "': "
                        + e.getMessage());
        } finally {
            // release components if necessary
            if (iputSelector != null) {
                if (iput != null)
                    iputSelector.release(iput);
                this.manager.release(iputSelector);
            }
        }

        return values;
    }

    /**
     * Calls the super's method startTransformingElement.
     * 
     * @param uri
     * @param name
     * @param raw
     * @param attr
     * @throws SAXException
     */
    protected void relayStartElement(String uri, String name, String raw, Attributes attr)
        throws SAXException {
        this.relayStartElement(false, false, uri, name, raw, attr);
    }

    /**
     * Calls the super's method startTransformingElement and increments the
     * ignoreEventsCount if skip is true. Increment can be done either before
     * invoking super's method, so that the element itself is skipped, or afterwards,
     * so that only the children are skipped.
     * 
     * @param skip
     * @param skipChildrenOnly 
     * @param uri
     * @param name
     * @param raw
     * @param attr
     * @throws SAXException
     */
    protected void relayStartElement(
        boolean skip,
        boolean skipChildrenOnly,
        String uri,
        String name,
        String raw,
        Attributes attr)
        throws SAXException {

        try {
            if (this.ignoreEventsCount > 0) {
                this.ignoreEventsCount++;
                super.startTransformingElement(uri, name, raw, attr);
            } else {
                if (skip)
                    this.skipChildrenOnly = skipChildrenOnly;
                if (skip && !skipChildrenOnly)
                    this.ignoreEventsCount++;
                super.startTransformingElement(uri, name, raw, attr);
                if (skip && skipChildrenOnly)
                    this.ignoreEventsCount++;
            }
        } catch (ProcessingException e) {
            throw new SAXException(e);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Calls the super's method endTransformingElement and decrements the
     * ignoreEventsCount if larger than zero.
     * 
     * @param uri
     * @param name
     * @param raw
     * @throws SAXException
     */
    protected void relayEndElement(String uri, String name, String raw) throws SAXException {

        if (this.ignoreEventsCount == 1 && this.skipChildrenOnly)
            this.ignoreEventsCount--;
        try {
            super.endTransformingElement(uri, name, raw);
        } catch (ProcessingException e) {
            throw new SAXException(e);
        } catch (IOException e) {
            throw new SAXException(e);
        } catch (Exception e) {
            getLogger().error("exception", e);
        }

        if (this.ignoreEventsCount > 0)
            this.ignoreEventsCount--;
    }

}
