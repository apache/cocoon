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
package org.apache.cocoon.webapps.session.transformation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ValidatorActionResult;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.webapps.session.SessionConstants;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceParameters;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This is the session post transformer. It does all the setting and
 * destroying. Thus it should be the last transformer (before the xsl) in
 * the pipeline.
 * For performance and simplicity reasons this transformer inherits from
 * the SessionPreTransformer, although this is not needed (but then the
 * implementation of the SessionTransformer would be very unperformant).
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: SessionPostTransformer.java,v 1.8 2004/04/03 03:01:35 joerg Exp $
 */
public class SessionPostTransformer extends SessionPreTransformer {

    public static final String DELETECONTEXT_ELEMENT = "deletecontext";
    public static final String DELETECONTEXT_NAME_ATTRIBUTE = "name";

    public static final String SETXML_ELEMENT = "setxml";
    public static final String SETXML_CONTEXT_ATTRIBUTE = "context";
    public static final String SETXML_PATH_ATTRIBUTE = "path";

    public static final String APPENDXML_ELEMENT = "appendxml";
    public static final String APPENDXML_CONTEXT_ATTRIBUTE = "context";
    public static final String APPENDXML_PATH_ATTRIBUTE = "path";

    public static final String REMOVEXML_ELEMENT = "removexml";
    public static final String REMOVEXML_CONTEXT_ATTRIBUTE = "context";
    public static final String REMOVEXML_PATH_ATTRIBUTE = "path";

    public static final String MERGEXML_ELEMENT = "mergexml";
    public static final String MERGEXML_CONTEXT_ATTRIBUTE = "context";
    public static final String MERGEXML_PATH_ATTRIBUTE = "path";

    public static final String SAVECONTEXT_ELEMENT = "savexml";
    public static final String SAVECONTEXT_CONTEXT_ATTRIBUTE = "context";
    public static final String SAVECONTEXT_PATH_ATTRIBUTE = "path"; // optional

    public static final String INPUTXML_ELEMENT = "inputxml";
    public static final String INPUTXML_CONTEXT_ATTRIBUTE = "context";
    public static final String INPUTXML_PATH_ATTRIBUTE = "path";
    public static final String INPUTXML_NAME_ATTRIBUTE = "name";
    public static final String INPUTXML_TYPE_ATTRIBUTE = "type"; // optional
    public static final String INPUTXML_VALIDATIONRESULT_ATTRIBUTE = "valresult";

    /** The form element */
    public static final String FORM_ELEMENT = "form";

    /** The form action element */
    public static final String FORM_ACTION_ELEMENT = "action";

    /** The form content element */
    public static final String FORM_CONTENT_ELEMENT = "content";

    /** The form validation rules */
    public static final String FORM_VALIDATION_ELEMENT = "validate";
    public static final String FORM_VALIDATION_SOURCE_ATTRIBUTE = "src";
    public static final String FORM_VALIDATESET_ELEMENT = "constraint-set";

    /** State: no element parsed */
    private static final int STATE_OUTSIDE = 0;
    /** State: form element */
    private static final int STATE_FORM = 1;

    /** The current state */
    private int state;

    /** The current form name */
    private String formName;

    /** The validation results */
    private Map validationResultMap;

    public void setupTransforming() throws ProcessingException, SAXException, IOException {
        super.setupTransforming();
        this.state = STATE_OUTSIDE;
        this.formName = null;
    }

    /**
     * This is the real implementation of the startElement event
     * for the transformer
     * The event is checked for a valid element and the corresponding command
     * is executed.
     */
    public void startTransformingElement(String uri, String name, String raw, Attributes attr)
            throws ProcessingException, IOException, SAXException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN startTransformingElement uri=" + uri + ", name=" + name
                                                              + ", raw=" + raw + ", attr=" + attr);
        }
        if (name.equals(DELETECONTEXT_ELEMENT)) {
            this.getContextManager().deleteContext(attr.getValue(DELETECONTEXT_NAME_ATTRIBUTE));

        } else if (name.equals(SETXML_ELEMENT)) {
            this.startRecording();
            stack.push(attr.getValue(SETXML_CONTEXT_ATTRIBUTE));
            stack.push(attr.getValue(SETXML_PATH_ATTRIBUTE));

            // Element: mergexml
        } else if (name.equals(MERGEXML_ELEMENT)) {
            this.startRecording();
            stack.push(attr.getValue(MERGEXML_CONTEXT_ATTRIBUTE));
            stack.push(attr.getValue(MERGEXML_PATH_ATTRIBUTE));

            // Element: appendxml
        } else if (name.equals(APPENDXML_ELEMENT)) {
            this.startRecording();
            stack.push(attr.getValue(APPENDXML_CONTEXT_ATTRIBUTE));
            stack.push(attr.getValue(APPENDXML_PATH_ATTRIBUTE));

            // Element: removexml
        } else if (name.equals(REMOVEXML_ELEMENT)) {
            this.startTextRecording();
            stack.push(attr.getValue(REMOVEXML_CONTEXT_ATTRIBUTE));
            stack.push(attr.getValue(REMOVEXML_PATH_ATTRIBUTE));

        } else if (name.equals(SAVECONTEXT_ELEMENT)) {
            this.startParametersRecording();
            stack.push(attr.getValue(SAVECONTEXT_CONTEXT_ATTRIBUTE));
            if (attr.getValue(SAVECONTEXT_PATH_ATTRIBUTE) != null) {
                stack.push(attr.getValue(SAVECONTEXT_PATH_ATTRIBUTE));
            } else {
                stack.push("/");
            }

            // Element: inputxml
        } else if (name.equals(INPUTXML_ELEMENT)) {
            stack.push(attr.getValue(INPUTXML_CONTEXT_ATTRIBUTE));
            String fieldname = attr.getValue(INPUTXML_NAME_ATTRIBUTE);
            stack.push(fieldname);
            stack.push(attr.getValue(INPUTXML_PATH_ATTRIBUTE));

            AttributesImpl newattr = new AttributesImpl();
            newattr.addAttribute("", INPUTXML_NAME_ATTRIBUTE, INPUTXML_NAME_ATTRIBUTE, "CDATA", fieldname);
            if (attr.getValue(INPUTXML_TYPE_ATTRIBUTE) != null) {
                newattr.addAttribute("", INPUTXML_TYPE_ATTRIBUTE, INPUTXML_TYPE_ATTRIBUTE, "CDATA",
                                     attr.getValue(INPUTXML_TYPE_ATTRIBUTE));
            }

            ValidatorActionResult validationResult = null;
            if (validationResultMap != null && validationResultMap.get(fieldname) != null) {
                validationResult = (ValidatorActionResult)validationResultMap.get(fieldname);
                newattr.addAttribute("", INPUTXML_VALIDATIONRESULT_ATTRIBUTE,
                                     INPUTXML_VALIDATIONRESULT_ATTRIBUTE,"CDATA", validationResult.toString());
            }

            super.startTransformingElement("", name, name, newattr); // remove namespace
            this.startRecording();

            // Element form
        } else if (name.equals(FORM_ELEMENT) && this.state == STATE_OUTSIDE) {
            String formName = attr.getValue("name");
            if (formName == null) {
                throw new ProcessingException("The name attribute of the form element is required.");
            }
            this.stack.push(new Integer(this.state));
            this.state = STATE_FORM;
            this.stack.push(new AttributesImpl(attr));

            // Element form action
        } else if (name.equals(FORM_ACTION_ELEMENT) && this.state == STATE_FORM) {
            this.startTextRecording();

            // Element form content
        } else if (name.equals(FORM_CONTENT_ELEMENT) && this.state == STATE_FORM) {
            // get validation results to be used for inputxml elements
            validationResultMap = (Map)this.getSessionManager().getSession(true)
                                                 .getAttribute(this.formName + "validation-result");

            // Element form validation rules
        } else if (name.equals(FORM_VALIDATION_ELEMENT) && this.state == STATE_FORM) {
            this.startRecording();
            if (attr.getValue(FORM_VALIDATION_SOURCE_ATTRIBUTE) != null) {
                stack.push(attr.getValue(FORM_VALIDATION_SOURCE_ATTRIBUTE));
            } else {
                stack.push("EMPTY");
            }

        } else {
            super.startTransformingElement(uri, name, raw, attr);
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END startTransformingElement");
        }
    }

    public void endTransformingElement(String uri, String name, String raw)
            throws ProcessingException, IOException, SAXException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN endTransformingElement uri=" + uri + ", name=" + name + ", raw=" + raw);
        }
        if (name.equals(DELETECONTEXT_ELEMENT)) {
            // do nothing, the context was destroyed on the startElement event

            // Element: setxml
        } else if (name.equals(SETXML_ELEMENT)) {
            String path = (String)stack.pop();
            String contextName = (String)stack.pop();
            this.getSessionManager().setContextFragment(contextName, path, this.endRecording());

            // Element: mergexml
        } else if (name.equals(MERGEXML_ELEMENT)) {
            String path = (String)stack.pop();
            String contextName = (String)stack.pop();
            this.getSessionManager().mergeContextFragment(contextName, path, this.endRecording());

            // Element: appendxml
        } else if (name.equals(APPENDXML_ELEMENT)) {
            String path = (String)stack.pop();
            String contextName = (String)stack.pop();
            this.getSessionManager().appendContextFragment(contextName, path, this.endRecording());

            // Element: removexml
        } else if (name.equals(REMOVEXML_ELEMENT)) {
            String path = (String)stack.pop();
            String contextName = (String)stack.pop();
            // result is ignored
            endTextRecording();
            this.getSessionManager().removeContextFragment(contextName, path);

            // Element: savexml
        } else if (name.equals(SAVECONTEXT_ELEMENT)) {
            String path = (String)stack.pop();
            String contextName = (String)stack.pop();
            SourceParameters pars = this.endParametersRecording((SourceParameters)null);
            pars.setSingleParameterValue("contextname", contextName);
            pars.setSingleParameterValue("path", path);

            this.getContextManager().getContext(contextName).saveXML(path, pars);

            // Element: inputxml
        } else if (name.equals(INPUTXML_ELEMENT)) {
            String path = (String)this.stack.pop();
            String fieldname = (String)this.stack.pop();
            String contextname = (String)this.stack.pop();
            DocumentFragment defaultFragment = this.endRecording();

            if (this.formName == null) {
                throw new ProcessingException("The inputxml must be contained inside a form.");
            }
            DocumentFragment value = this.getFormManager().registerInputField(contextname, path,
                                                                              fieldname, formName);
            if (value == null) {
                value = defaultFragment;
            }
            this.sendEvents(value);
            super.endTransformingElement("", name, name);

            // Element form
        } else if (name.equals(FORM_ELEMENT) && this.state == STATE_FORM) {
            this.state = ((Integer)this.stack.pop()).intValue();
            this.sendEndElementEvent("form");
            this.formName = null;

            // Element form action
        } else if (name.equals(FORM_ACTION_ELEMENT) && this.state == STATE_FORM) {
            String action = this.endTextRecording();
            AttributesImpl a = (AttributesImpl)this.stack.pop();
            this.formName = a.getValue("name");
            boolean hasPars = (action.indexOf("?") != -1);
            action = this.response.encodeURL(action + (hasPars ? '&' : '?')
                                             + SessionConstants.SESSION_FORM_PARAMETER + '=' + this.formName);
            a.addAttribute("", "action", "action", "CDATA", action);
            if (a.getValue("method") == null) {
                a.addAttribute("", "method", "method", "CDATA", "POST");
            }
            this.sendStartElementEvent("form", a);

            // Element form content
        } else if (name.equals(FORM_CONTENT_ELEMENT) && this.state == STATE_FORM) {
            // ignore this

            // Element form validation rules
        } else if (name.equals(FORM_VALIDATION_ELEMENT) && this.state == STATE_FORM) {
            if (this.formName == null) {
                throw new ProcessingException("The validate element must be contained inside a form.");
            }
            DocumentFragment validationDoc = this.endRecording();
            String source = (String)stack.pop();
            if (!source.equals("EMPTY")) {
                // get configuration from external file
                // referenced by "src" attribute of "validate" element
                try {
                    Source resource = this.resolver.resolveURI(source);
                    SAXConfigurationHandler saxBuilder = new SAXConfigurationHandler();
                    SourceUtil.parse(this.manager, resource, saxBuilder);

                    Configuration conf = saxBuilder.getConfiguration();
                    Session session = this.getSessionManager().getSession(true);
                    session.setAttribute(this.formName, conf);

                    if (validationDoc != null) {
                        //validationDoc contains "constraint-set" element
                        validationDoc.normalize();
                        Node validationNode = validationDoc.getFirstChild();
                        while (validationNode.getNodeType() != Node.ELEMENT_NODE) {
                            validationNode = validationNode.getNextSibling();
                            if (validationNode == null) {
                                break;
                            }
                        }
                        if (validationNode != null && validationNode.getNodeType() == Node.ELEMENT_NODE
                                && validationNode.getNodeName().equals(FORM_VALIDATESET_ELEMENT)) {
                            Properties props = XMLUtils.createPropertiesForXML(false);
                            props.put(OutputKeys.ENCODING, "ISO-8859-1");
                            String validationXML = XMLUtils.serializeNode(validationNode, props);
                            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
                            conf = builder.build(new ByteArrayInputStream(validationXML.getBytes()));
                            session.setAttribute(this.formName + "constraint-set", conf);
                        }
                    }

                } catch (SourceException se) {
                    throw new ProcessingException("Cannot resolve" + source, se);
                } catch (ConfigurationException ce) {
                    throw new ProcessingException("Error building Configuration out of constraint-set element", ce);
                }

            } else if (validationDoc != null) {
                //validationDoc contains the validation rules inline
                try {
                    validationDoc.normalize();
                    Node validationNode = validationDoc.getFirstChild();
                    while (validationNode.getNodeType() != Node.ELEMENT_NODE) {
                        validationNode = validationNode.getNextSibling();
                        if (validationNode == null) {
                            break;
                        }
                    }
                    if (validationNode != null && validationNode.getNodeType() == Node.ELEMENT_NODE
                            && validationNode.getNodeName().equals("root")) {
                        Properties props = XMLUtils.createPropertiesForXML(false);
                        props.put(OutputKeys.ENCODING, "ISO-8859-1");
                        String validationXML = XMLUtils.serializeNode(validationNode, props);
                        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
                        Configuration conf = builder.build(new ByteArrayInputStream(validationXML.getBytes()));
                        Session session = this.getSessionManager().getSession(true);
                        session.setAttribute(this.formName, conf);
                        //the constraint-set to validate is the first and single one
                        session.setAttribute(this.formName + "constraint-set", conf.getChildren("constraint-set")[0]);

                    }
                } catch (ConfigurationException ce) {
                    throw new ProcessingException("Error building Configuration out of validation XML", ce);
                }
            }

        } else {
            super.endTransformingElement(uri, name, raw);
        }
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END endTransformingElement");
        }
    }
}
