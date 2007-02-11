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
package org.apache.cocoon.transformation;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ValidatorActionResult;
import org.apache.cocoon.components.language.markup.xsp.XSPFormValidatorHelper;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;

/** 
 * Eliminates the need for XSP to use FormValidatorAction. 
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
 * </table>
 * </p>
 *
 * <p>Sitemap parameters:
 * <table>
 *   <tr><td>fixed</td><td>(boolean) Do not change values</td></tr>
 *   <tr><td>prefix</td><td>(String) Added to the input element's name</td></tr>
 *   <tr><td>suffix</td><td>(String) Added to the input element's name</td></tr>
 *   <tr><td>input</td><td>(String) InputModule name</td></tr>
 * </table>
 * </p>
 *
 * <p>Example:<pre>
 *     &lt;input name="user.name" size="50" maxlength="60"/&gt;
 *     &lt;error name="user.name" when-ge="error"&gt;required&lt;/error&gt;
 * </pre></p>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SimpleFormTransformer.java,v 1.4 2003/09/24 21:41:12 cziegeler Exp $
 */
public class SimpleFormTransformer
    extends AbstractTransformer
    implements Composable, Configurable, Recyclable {

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

    /** nesting level of ignored elements */
    protected int ignoreCount;
    /** ignored element needs closing tag */
    protected boolean ignoreThis = false;

    /** stack of ignored element names */
    protected Stack stack = new Stack();

    /** current element's request parameter values */
    protected Object[] values = null;

    /** current request's validation results (all validated elements) */
    protected Map validationResults = null;

    /** The current Request object */
    protected Request request;
    /** The current objectModel of the environment */
    protected Map objectModel;
    /** The parameters specified in the sitemap */
    protected Parameters parameters;
    /** The Avalon ComponentManager for getting Components */
    protected ComponentManager manager;

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

    private String defaultInput = "request-param";
    private Configuration defaultInputConf = null;
    private Configuration inputConf = null;
    private InputModule input = null;
    private ComponentSelector inputSelector = null;
    private String inputName = null;

    /** Empty attributes (for performance). This can be used
     *  do create own attributes, but make sure to clean them
     *  afterwords.
     */
    protected AttributesImpl emptyAttributes = new AttributesImpl();

    /** set per instance variables to defaults */
    private void reset() {
        this.objectModel = null;
        this.request = null;
        this.parameters = null;
        this.stack.clear();
        this.ignoreCount = 0;
        this.values = null;
        this.validationResults = null;
        this.documentFixed = false;
        this.fixed = false;
        this.formName = null;

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
        this.objectModel = objectModel;

        this.request = ObjectModelHelper.getRequest(objectModel);
        if (request == null) {
            getLogger().debug("no request object");
            throw new ProcessingException("no request object");
        }
        this.parameters = par;
        this.documentFixed = par.getParameterAsBoolean("fixed", false);
        this.fixed = this.documentFixed;
        this.prefix = par.getParameter("prefix", this.defaultPrefix);
        this.suffix = par.getParameter("suffix", this.defaultSuffix);
        this.inputName = par.getParameter("input", null);
        this.inputConf = null;
        if (this.ignoreValidation) {
            this.validationResults = null;
        } else {
            this.validationResults = XSPFormValidatorHelper.getResults(objectModel);
        }

        if (this.inputName == null) {
            this.inputName = this.defaultInput;
            this.inputConf = this.defaultInputConf;
        }

        try {
            // obtain input module
            this.inputSelector = (ComponentSelector) this.manager.lookup(INPUT_MODULE_SELECTOR);
            if (this.inputName != null
                && this.inputSelector != null
                && this.inputSelector.hasComponent(this.inputName)) {
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
                                    && this.inputSelector.hasComponent(this.inputName)
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
     * Avalon Composable Interface
     * @param manager The Avalon Component Manager
     */
    public void compose(ComponentManager manager) {
        this.manager = manager;
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
        String uri,
        String name,
        String raw,
        AttributesImpl attributes)
        throws SAXException {

        // @fixed and this.fixed already considered in startInputElement
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
        super.startElement(uri, name, raw, attributes);
    }

    /**
     * Handle input elements that may don't have a "checked"
     * attributes, e.g. text, password, button.
     */
    protected void startNonCheckableElement(
        String uri,
        String name,
        String raw,
        AttributesImpl attributes)
        throws SAXException {

        // @fixed and this.fixed already considered in startInputElement
        String value = attributes.getValue("value");
        if (getLogger().isDebugEnabled())
            getLogger().debug(
                "startNonCheckableElement "
                    + name
                    + " attributes "
                    + this.printAttributes(attributes));
        if (this.values != null) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("replacing");
            if (value != null) {
                attributes.setValue(attributes.getIndex("value"), String.valueOf(this.values[0]));
            } else {
                attributes.addAttribute(
                    "",
                    "value",
                    "value",
                    "CDATA",
                    String.valueOf(this.values[0]));
            }
        }
        super.startElement(uri, name, raw, attributes);
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
            super.startElement(uri, name, raw, attr);

        } else {
            if (getLogger().isDebugEnabled())
                getLogger().debug("replacing");

            this.values = this.getValues(aName);

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
                    this.startCheckableElement(uri, name, raw, attributes);
                    break;

                case TYPE_DEFAULT :
                    this.startNonCheckableElement(uri, name, raw, attributes);
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
            this.values = this.getValues(aName);
        }
        super.startElement(uri, name, raw, attr);
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
            super.startElement(uri, name, raw, attr);
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

            super.startElement(uri, name, raw, attributes);
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
        Object[] value = null;
        if (getLogger().isDebugEnabled())
            getLogger().debug(
                "startTextareaElement " + name + " attributes " + this.printAttributes(attributes));
        if (aName != null) {
            value = this.getValues(aName);
        }
        if (value == null || this.fixed || (fixed != null && parseBoolean(fixed))) {
            super.startElement(uri, name, raw, attributes);
        } else {
            if (getLogger().isDebugEnabled())
                getLogger().debug("replacing");
            this.ignoreCount++;
            this.stack.push(name);
            this.ignoreThis = false;
            super.startElement(uri, name, raw, attributes);
            String valString = String.valueOf(value[0]);
            super.characters(valString.toCharArray(), 0, valString.length());
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
            super.startElement(uri, name, raw, attr);
        } else if (this.validationResults == null || this.fixed) {
            this.ignoreCount++;
            this.stack.push(name);
            this.ignoreThis = true;
        } else {
            String aName = attr.getValue("name");
            if (aName == null) {
                super.startElement(uri, name, raw, attr);
            } else {
                ValidatorActionResult validation =
                    XSPFormValidatorHelper.getParamResult(this.objectModel, aName);
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
                    super.startElement(uri, name, raw, attributes);
                } else {
                    this.ignoreCount++;
                    this.stack.push(name);
                    this.ignoreThis = true;
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
            super.startElement(uri, name, raw, attr);
        } else {
            if (!this.fixed && ("true".equals(fixed) || "yes".equals(fixed))) {
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
            super.startElement(uri, name, raw, attributes);
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
    public void startElement(String uri, String name, String raw, Attributes attr)
        throws SAXException {

        if (this.ignoreCount == 0) {
            if (uri != "") {
                super.startElement(uri, name, raw, attr);
            } else {
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

                    default :
                        super.startElement(uri, name, raw, attr);
                }
            }
        } else {
            this.ignoreCount++;
            this.stack.push(name);
            if (((Integer) elementNames.get(name, defaultElement)).intValue() == ELEMENT_ERROR) {
            }
        }
    }

    /**
     * Start processing elements of our namespace.
     * This hook is invoked for each sax event with our namespace.
     * @param uri The namespace of the element.
     * @param name The local name of the element.
     * @param raw The qualified name of the element.
     */
    public void endElement(String uri, String name, String raw) throws SAXException {

        if (uri != "") {
            if (this.ignoreCount == 0)
                super.endElement(uri, name, raw);
        } else {
            if (this.ignoreCount > 0) {
                if (((String) this.stack.peek()).equals(name)) {
                    this.stack.pop();
                    this.ignoreCount--;
                }
            }
            if (this.ignoreCount == 0 && this.ignoreThis) {
                this.ignoreThis = false;
                // skip event 
            } else if (this.ignoreCount > 0) {
                // skip event 
            } else {
                switch (((Integer) elementNames.get(name, defaultElement)).intValue()) {
                    case ELEMENT_INPUT :
                        super.endElement(uri, name, raw);
                        break;
                    case ELEMENT_SELECT :
                        this.values = null;
                        super.endElement(uri, name, raw);
                        break;
                    case ELEMENT_OPTION :
                        super.endElement(uri, name, raw);
                        break;
                    case ELEMENT_TXTAREA :
                        super.endElement(uri, name, raw);
                        break;
                    case ELEMENT_ERROR :
                        super.endElement(uri, name, raw);
                        break;
                    case ELEMENT_FORM :
                        this.fixed = this.documentFixed;
                        this.formName = null;
                        super.endElement(uri, name, raw);
                        break;

                    default :
                        super.endElement(uri, name, raw);
                }
            }
        }
    }

    /**
     * Receive notification of character data.
     *
     * @param c The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void characters(char c[], int start, int len) throws SAXException {
        if (this.ignoreCount == 0)
            super.characters(c, start, len);
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * @param c The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void ignorableWhitespace(char c[], int start, int len) throws SAXException {
        if (this.ignoreCount == 0)
            super.ignorableWhitespace(c, start, len);
    }

    /**
     * Receive notification of a processing instruction.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if none was
     *             supplied.
     */
    public void processingInstruction(String target, String data) throws SAXException {
        if (this.ignoreCount == 0)
            super.processingInstruction(target, data);
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity.  If it is a  parameter
     *             entity, the name will begin with '%'.
     */
    public void skippedEntity(String name) throws SAXException {
        if (this.ignoreCount == 0)
            super.skippedEntity(name);
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
        return result;
    }

    /**
     * Obtain values from the used InputModule.
     */
    private Object[] getValues(String name) {
        Object[] values = null;
        ComponentSelector iputSelector = null;
        InputModule iput = null;
        try {
            if (this.input != null) {
                // input module is thread safe
                // thus we still have a reference to it
                values = input.getAttributeValues(name, this.inputConf, objectModel);
                if (getLogger().isDebugEnabled())
                    getLogger().debug(
                        "cached module "
                            + this.input
                            + " attribute "
                            + name
                            + " returns "
                            + values);
            } else {
                // input was not thread safe
                // so acquire it again
                iputSelector = (ComponentSelector) this.manager.lookup(INPUT_MODULE_SELECTOR);
                if (this.inputName != null
                    && iputSelector != null
                    && iputSelector.hasComponent(this.inputName)) {

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

}
