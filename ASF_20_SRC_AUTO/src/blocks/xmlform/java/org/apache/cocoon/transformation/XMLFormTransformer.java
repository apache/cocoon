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

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.validation.Violation;
import org.apache.cocoon.components.xmlform.Form;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Transforms a document with XMLForm
 * elements into a document in the same namespace,
 * but with populated values for the XPath references
 * to the form's model attributes.
 *
 * @author Ivelin Ivanov <ivelin@apache.org>, June 2002
 * @author Andrew Timberlake <andrew@timberlake.co.za>, June 2002
 * @author Michael Ratliff, mratliff@collegenet.com <mratliff@collegenet.com>, May 2002
 * @author Torsten Curdt <tcurdt@dff.st>, March 2002
 * @author Simon Price <price@bristol.ac.uk>, September 2002
 * @version CVS $Id: XMLFormTransformer.java,v 1.9 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public class XMLFormTransformer extends AbstractSAXTransformer {

    // TODO: implements CacheableProcessingComponent {

    public final static String NS = "http://apache.org/cocoon/xmlform/1.0";
    private final static String NS_PREFIX = "xf";

    public final static Attributes NOATTR = new AttributesImpl();
    // private final static String XMLNS_PREFIX = "xmlns";

    /**
     * The main tag in the XMLForm namespace
     * almost all other tags have to appear within the form tag.
     * The id attribute refers to a xmlform.Form object
     * available in the current Request or Session
     *
     * &lt;form id="form-feedback">
     *  &lt;output ref="user/age"/>
     *  &lt;textbox ref="user/name"/>
     * &lt;/form>
     */
    public final static String TAG_FORM = "form";

    public final static String TAG_FORM_ATTR_ID = "id";

    public final static String TAG_FORM_ATTR_VIEW = "view";

    /**
     * The only tag which can be used outside of the form tag
     * with reference to the form id,
     * &lt;output ref="user/age" id="form-feedback"/>
     */
    public final static String TAG_OUTPUT = "output";

    public final static String TAG_OUTPUT_ATTR_FORM = TAG_FORM;

    /**
     * Can be used directly under the form tag
     * to enlist all field violations or
     * within a field tag to enlist only the violations for the field.
     * <br>
     * <pre>
     * &lt;form id="form-feedback">
     *  &lt;violations/>
     *  &lt;textbox ref="user/name">
     *    &lt;violations/>
     *  &lt;/textbox>
     * &lt;/form>
     * </pre>
     *
     * When used under the forms tag it is transformed to a set of:
     * <br>
     * &lt;violation ref="user/age">Age must be a positive number &lt;/violation>
     * <br>
     * and when used within a field it is transformed to a set of:
     * <br>
     * &lt;violation>Age must be a positive number &lt;/violation>
     * <br>
     * The only difference is that the ref tag is used in the first case,
     * while in the second it is omited.
     */
    public final static String TAG_INSERTVIOLATIONS = "violations";

    /** the name of the elements which replace the violations tag */
    public final static String TAG_VIOLATION = "violation";

    /** action buttons */
    public final static String TAG_SUBMIT = "submit";

    public final static String TAG_CANCEL = "cancel";

    public final static String TAG_RESET = "reset";

    public final static String TAG_CAPTION = "caption";

    public final static String TAG_HINT = "hint";

    public final static String TAG_HELP = "help";

    public final static String TAG_TEXTBOX = "textbox";

    public final static String TAG_TEXTAREA = "textarea";

    public final static String TAG_PASSWORD = "password";

    public final static String TAG_SELECTBOOLEAN = "selectBoolean";

    public final static String TAG_SELECTONE = "selectOne";

    public final static String TAG_SELECTMANY = "selectMany";

    public final static String TAG_ITEMSET = "itemset";

    public final static String TAG_ITEM = "item";

    public final static String TAG_VALUE = "value";

    public final static String TAG_HIDDEN = "hidden";

    /**
     * Grouping tag.
     *
     * <pre>
     *  <group ref="address">
     *   <caption>Shipping Address</caption>
     *     <input ref="line_1">
     *       <caption>Address line 1</caption>
     *     </input>
     *     <input ref="line_2">
     *       <caption>Address line 2</caption>
     *     </input>
     *     <input ref="postcode">
     *       <caption>Postcode</caption>
     *     </input>
     * </group>
     * </pre>
     */
    public final static String TAG_GROUP = "group";

    /**
     * Repeat tag.
     *
     * <repeat nodeset="/cart/items/item">
     *    <input ref="." .../><html:br/>
     * </repeat>
     */

    public final static String TAG_REPEAT = "repeat";

    /**
     * This attribute is used within the
     * <code>repeat</code> tag
     * to represent an XPath node set selector from
     * the underlying xmlform model.
     */
    public final static String TAG_REPEAT_ATTR_NODESET = "nodeset";

    /**
     * The current fully expanded reference
     * in the form model.
     */
    private String cannonicalRef = null;

    /**
     * Tracks the current repeat tag depth,
     * when there is one in scope.
     */
    private int repeatTagDepth = -1;

    /**
     * The nodeset selector string of the
     * currently processed repeat tag (if any).
     */
    private String nodeset = null;

    /**
     * The flag annotating if the transformer is
     * working on a repeat tag.
     */
    private boolean isRecording = false;

    /**
     * Flag to let us know if the transformer is working
     * on a hidden tag.
     */
    private boolean isHiddenTag = false;

    /**
     * Flag to let us know that the hidden element contains
     * a value child element.
     */
    private boolean hasHiddenTagValue = false;

    /**
     * The ref value of the current field
     * used by the violations tag.
     */
    private Stack refStack = null;

    /**
     * Tracks the current depth of the XML tree.
     */
    private int currentTagDepth = 0;

    /**
     * this attribute is used within all field tags
     * to represent an XPath reference to the attribute of
     * the underlying model.
     */
    public final static String TAG_COMMON_ATTR_REF = "ref";

    /** 
     * The stack of nested forms.
     * Although nested form tags are not allowed, it is possible
     * that an output tag (with reference to another form) might be nested within a form tag.
     * In this case elements under the output tag (like caption) can reference properties
     * of the form of the enclosing output tag.
     */
    private Stack formStack = null;

    /**
     * Since form elements cannot be nested,
     * at most one possible value for the current form view is available.
     */
    private String currentFormView = null;

    private Object value_;

    /**
     * Setup the next round.
     * The instance variables are initialised.
     * @param resolver The current SourceResolver
     * @param objectModel The objectModel of the environment.
     * @param src The value of the src attribute in the sitemap.
     * @param par The parameters from the sitemap.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src,
                      Parameters par)
                        throws ProcessingException, SAXException,
                               IOException {
        super.setup(resolver, objectModel, src, par);
        if (request==null) {
            getLogger().debug("no request object");
            throw new ProcessingException("no request object");
        }

        // set the XMLForm namespace as the one
        // this transformer is interested to work on
        namespaceURI = NS;

        // init tracking parameters
        formStack = new Stack();
        cannonicalRef = "";
        refStack = new Stack();
        currentTagDepth = 0;
        repeatTagDepth = -1;
        isRecording = false;
        nodeset = null;
    }

    /**
     *  Recycle this component.
     */
    public void recycle() {
        // init tracking parameters
        formStack = null;
        cannonicalRef = null;
        refStack = null;
        currentTagDepth = 0;
        repeatTagDepth = -1;
        isRecording = false;
        nodeset = null;

        super.recycle();
    }

    /**
     * Start processing elements of our namespace.
     * This hook is invoked for each sax event with our namespace.
     * @param uri The namespace of the element.
     * @param name The local name of the element.
     * @param raw The qualified name of the element.
     * @param attributes 
     */
    public void startTransformingElement(String uri, String name, String raw,
                                         Attributes attributes)
                                           throws ProcessingException,
                                               IOException, SAXException {
        try {
            // avoid endless loop for elements in our namespace
            // when outputting the elements themselves
            this.ignoreHooksCount = 1;

            if (this.getLogger().isDebugEnabled()==true) {
                this.getLogger().debug("BEGIN startTransformingElement uri="+
                                       uri+", name="+name+", raw="+raw+
                                       ", attr="+attributes+")");
            }

            // top level element in our namespace
            // set an xmlns:xf="XMLForm namespace..." attribute
            // to explicitely define the prefix to namespace binding
            if (currentTagDepth==0) {
                AttributesImpl atts;

                if ((attributes==null) || (attributes.getLength()==0)) {
                    atts = new AttributesImpl();
                } else {
                    atts = new AttributesImpl(attributes);
                }
                // atts.addAttribute( null, NS_PREFIX, XMLNS_PREFIX + ":" + NS_PREFIX, "CDATA", NS);
                attributes = atts;
            }

            // track the tree depth
            ++currentTagDepth;

            // if within a repeat tag, keep recording
            // when recording, nothing is actively processed
            if (isRecording) {
                // just record the SAX event
                super.startElement(uri, name, raw, attributes);
            }
            // when a new repeat tag is discovered
            // start recording
            // the repeat will be unrolled after the repeat tag ends
            else if (TAG_REPEAT.equals(name)) {
                repeatTagDepth = currentTagDepth;
                isRecording = true;

                // get the nodeset selector string
                nodeset = attributes.getValue(TAG_REPEAT_ATTR_NODESET);

                if (nodeset==null) {
                    throw new SAXException(name+" element should provide a '"+
                                           TAG_REPEAT_ATTR_NODESET+
                                           "' attribute");
                }

                // open the repeat tag in the output document
                super.startElement(uri, name, raw, attributes);
                // and start recording its content
                startRecording();
            }
            // when a new itemset tag (used within select) is discovered
            // start recording
            // the itemset will be unrolled after the tag ends
            // The difference with the repeat tag is that itemset is
            // unrolled in multiple item tags.
            else if (TAG_ITEMSET.equals(name)) {
                repeatTagDepth = currentTagDepth;
                isRecording = true;

                // get the nodeset selector string
                nodeset = attributes.getValue(TAG_REPEAT_ATTR_NODESET);

                if (nodeset==null) {
                    throw new SAXException(name+" element should provide a '"+
                                           TAG_REPEAT_ATTR_NODESET+
                                           "' attribute");
                }

                // start recording its content
                startRecording();
            } else // if not a repeat tag
            {
                // if this tag has a "ref" attribute, then
                // add its value to the refStack
                String aref = attributes.getValue(TAG_COMMON_ATTR_REF);

                if (aref!=null) {
                    // put on top of the ref stack the full ref
                    // append the new ref to the last stack top if not referencing the root
                    if ( !refStack.isEmpty()) {
                        // this is a nested reference
                        cannonicalRef = aref.startsWith("/")
                                        ? aref
                                        : (((Entry) refStack.peek()).getValue()+
                                           "/"+aref);
                    } else {
                        // top level reference
                        cannonicalRef = aref;
                    }
                    Entry entry = new Entry(new Integer(currentTagDepth),
                                            cannonicalRef);

                    refStack.push(entry);

                    // replace the ref attribute's value(path) with its full cannonical form
                    AttributesImpl atts = new AttributesImpl(attributes);
                    int refIdx = atts.getIndex(TAG_COMMON_ATTR_REF);

                    atts.setValue(refIdx, cannonicalRef);
                    attributes = atts;
                }

                // match tag name and apply transformation logic
                if (TAG_FORM.equals(name)) {
                    startElementForm(uri, name, raw, attributes);
                } else if (TAG_OUTPUT.equals(name)) {
                    startElementOutput(uri, name, raw, attributes);
                }  // end if TAG_OUTPUT
                    else if (TAG_INSERTVIOLATIONS.equals(name)) {
                    startElementViolations(uri, name, raw, attributes);
                }  // end if TAG_INSERTVIOLATIONS
                // if we're within a xf:hidden element
                // and a value sub-element has been provided
                // in the markup, then it will be left
                // unchanged. Otherwise we will
                // render the value of the referenced model
                // attribute
                else if (isHiddenTag && TAG_VALUE.equals(name)) {
                    hasHiddenTagValue = true;
                    super.startElement(uri, name, raw, attributes);
                }
                // if we are not within an enclosing form
                // then we can't process the following nested tags
                else if ( !formStack.isEmpty()) {
                    if (TAG_TEXTBOX.equals(name) ||
                        TAG_TEXTAREA.equals(name) ||
                        TAG_PASSWORD.equals(name) ||
                        TAG_SELECTBOOLEAN.equals(name) ||
                        TAG_SELECTONE.equals(name) ||
                        TAG_SELECTMANY.equals(name)) {
                        startElementInputField(uri, name, raw, attributes);
                    } else if (TAG_CAPTION.equals(name) ||
                               TAG_HINT.equals(name) ||
                               TAG_HELP.equals(name) ||
                               TAG_VALUE.equals(name)) {
                        startElementWithOptionalRefAndSimpleContent(uri,
                                                                    name,
                                                                    raw,
                                                                    attributes);
                    } else if (TAG_SUBMIT.equals(name)) {
                        String continuation = attributes.getValue("continuation");

                        if (continuation!=null) {
                            WebContinuation kont = 
				FlowHelper.getWebContinuation(objectModel);

                            if (kont != null) {
                                int level = 0;

                                if (continuation.equals("back")) {
                                    level = 3;
                                }
                                AttributesImpl impl = new AttributesImpl(attributes);
                                int index = impl.getIndex("id");
                                String id = impl.getValue(index);
                                String kId = kont.getContinuation(level).getId();

                                id = kId+":"+id;
                                if (index>=0) {
                                    impl.setValue(index, id);
                                } else {
                                    impl.addAttribute("", "id", "id", "", id);
                                }
                                attributes = impl;
                            }
                        }
                        super.startElement(uri, name, raw, attributes);
                    } else if (TAG_CANCEL.equals(name) ||
                               TAG_RESET.equals(name) ||
                               TAG_ITEM.equals(name)) {
                        super.startElement(uri, name, raw, attributes);
                    } else if (TAG_HIDDEN.equals(name)) {
                        // raise the flag that we're within a hidden element
                        // since there are intricacies in
                        // handling the value sub-element
                        isHiddenTag = true;
                        startElementInputField(uri, name, raw, attributes);
                    } else {
                        getLogger().error("pass through element ["+
                                          String.valueOf(name)+"]");
                        super.startElement(uri, name, raw, attributes);
                    }
                }
            }      // end else (not a repeat tag)
        } finally {
            // reset ignore counter
            this.ignoreHooksCount = 0;
        }

        if (this.getLogger().isDebugEnabled()==true) {
            this.getLogger().debug("END startTransformingElement");
        }
    } // end of startTransformingElement

    protected void startElementForm(String uri, String name, String raw,
                                    Attributes attributes)
                                      throws SAXException {
        String id = attributes.getValue(TAG_FORM_ATTR_ID);

        // currently form elements cannot be nested
        if ( !formStack.isEmpty()) {
            String error = "Form nodes should not be nested ! Current form [id="+
                           formStack.peek()+"], nested form [id="+
                           String.valueOf(id)+"]";

            getLogger().error(error);
            throw new IllegalStateException(error);
        }

        super.startElement(uri, name, raw, attributes);

        // load up the referenced form
        Form currentForm = Form.lookup(objectModel, id);

        // if the form wasn't found, we're in trouble
        if (currentForm==null) {
            String error = "Form is null [id="+String.valueOf(id)+"]";

            getLogger().error(error);
            throw new IllegalStateException(error);
        }

        formStack.push(currentForm);

        // memorize the current form view
        // it will be needed when saving expected references to properties
        currentFormView = attributes.getValue(TAG_FORM_ATTR_VIEW);

        // clear previously saved form state for this view
        resetSavedModelReferences();

    } // end of startElementForm

    protected void startElementViolations(String uri, String name,
                                          String raw,
                                          Attributes attributes)
                                            throws SAXException {

        // we will either use the locally referenced form id
        // or the global id. At least one of the two must be available
        Form form = null;
        String formAttr = attributes.getValue(TAG_OUTPUT_ATTR_FORM);

        if (formAttr==null) {
            if (formStack.isEmpty()) {
                throw new SAXException("When used outside of a form tag, the output tag requires an '"+
                                       TAG_OUTPUT_ATTR_FORM+"' attribute");
            }
            form = (Form) formStack.peek();
        } else {
            form = Form.lookup(objectModel, formAttr);
        }

        SortedSet violations = form.getViolationsAsSortedSet();

        // if there are no violations, there is nothing to show
        if (violations==null) {
            return;
        }

        // if we're immediately under the form tag
        // and parent "ref" attribute is not available
        if (refStack.isEmpty()) {
            for (Iterator it = violations.iterator(); it.hasNext(); ) {
                Violation violation = (Violation) it.next();

                // render <violation> tag

                // set the ref attribute
                AttributesImpl atts;

                if ((attributes==null) || (attributes.getLength()==0)) {
                    atts = new AttributesImpl();
                } else {
                    atts = new AttributesImpl(attributes);
                }
                // atts.addAttribute( NS, TAG_COMMON_ATTR_REF, NS_PREFIX + ":" + TAG_COMMON_ATTR_REF, "CDATA", violation.getPath());
                atts.addAttribute(null, TAG_COMMON_ATTR_REF,
                                  TAG_COMMON_ATTR_REF, "CDATA",
                                  violation.getPath());

                // now start the element
                super.startElement(uri, TAG_VIOLATION,
                                   NS_PREFIX+":"+TAG_VIOLATION, atts);

                // set message
                String vm = violation.getMessage();

                super.characters(vm.toCharArray(), 0, vm.length());

                super.endElement(uri, TAG_VIOLATION,
                                 NS_PREFIX+":"+TAG_VIOLATION);
            }
        } // end if (currentRef_ == null)
            else {
            Entry entry = (Entry) refStack.peek();
            String currentRef = (String) entry.getValue();
            Violation v = new Violation();

            v.setPath(currentRef);
            Collection restViolations = violations.tailSet(v);
            Iterator rviter = restViolations.iterator();

            while (rviter.hasNext()) {
                Violation nextViolation = (Violation) rviter.next();

                // we're only interested in violations
                // with matching reference
                if ( !currentRef.equals(nextViolation.getPath())) {
                    break;
                }

                // render <violation> tag
                super.startElement(uri, TAG_VIOLATION,
                                   NS_PREFIX+":"+TAG_VIOLATION, attributes);
                // set message
                String vm = nextViolation.getMessage();

                super.characters(vm.toCharArray(), 0, vm.length());
                super.endElement(uri, TAG_VIOLATION,
                                 NS_PREFIX+":"+TAG_VIOLATION);
            }
        }
    }     // end of startElementViolations

    /**
     * Since the ouput tag is the only one which can be used
     * outside of a form tag, it needs some special treatment
     */
    protected void startElementOutput(String uri, String name, String raw,
                                      Attributes attributes)
                                        throws SAXException {

        // we will either use the locally referenced form id
        // or the global id. At least one of the two must be available
        Form form = null;
        String formAttr = attributes.getValue(TAG_OUTPUT_ATTR_FORM);

        if (formAttr==null) {
            if (formStack.isEmpty()) {
                throw new SAXException("When used outside of a form tag, the output tag requires an '"+
                                       TAG_OUTPUT_ATTR_FORM+"' attribute");
            }
            form = (Form) formStack.peek();
        } else {
            form = Form.lookup(objectModel, formAttr);
        }
        formStack.push(form);

        startElementSimpleField(uri, name, raw, attributes);

    } // end of startElementOutput

    /**
     * Renders elements, which are used for input.
     *
     * TAG_TEXTBOX, TAG_TEXTAREA, TAG_PASSWORD, TAG_SELECTBOOLEAN,
     * TAG_SELECTONE, TAG_SELECTMANY
     */
    protected void startElementInputField(String uri, String name,
                                          String raw,
                                          Attributes attributes)
                                            throws SAXException {
        startElementSimpleField(uri, name, raw, attributes);

        String ref = attributes.getValue(TAG_COMMON_ATTR_REF);

        if (ref==null) {
            throw new SAXException(name+" element should provide a '"+
                                   TAG_COMMON_ATTR_REF+"' attribute");
        }
        saveModelReferenceForFormView(ref, name);
    }

    protected void startElementSimpleField(String uri, String name,
                                           String raw,
                                           Attributes attributes)
                                             throws SAXException {
        String ref = attributes.getValue(TAG_COMMON_ATTR_REF);

        if (ref==null) {
            throw new SAXException(name+" element should provide a '"+
                                   TAG_COMMON_ATTR_REF+"' attribute");
        }

        if (formStack.isEmpty()) {
            throw new SAXException(name+
                                   " element should be either nested within a form tag or provide a form attribute");
        }

        Form form = getCurrentForm();

        getLogger().debug("["+String.valueOf(name)+
                          "] getting value from form [id="+form.getId()+
                          ", ref="+String.valueOf(ref)+"]");

        // retrieve current value of referenced property
        value_ = form.getValue(ref);

        // we will only forward the SAX event once we know
        // that the value of the tag is available
        super.startElement(uri, name, raw, attributes);

        getLogger().debug("Value of form [id="+form.getId()+", ref="+
                          String.valueOf(ref)+"] = ["+value_+"]");

        // Only render value sub-elements
        // at this point
        // if this is not a xf:hidden element.
        if ( !isHiddenTag) {
            renderValueSubElements();
        }
    } // end of startElementSimpleField

    /**
     * Let the form wrapper know that this reference should be expected
     * when data is submitted by the client for the current form view.
     * The name of the XML tag is also saved to help the form populator
     * find an appropriate default value when one is not provided in the http request.
     */
    protected void saveModelReferenceForFormView(String ref, String name) {
        // the xf:form/@view attribute is not mandatory
        // although it is strongly recommended
        if (currentFormView!=null) {
            Form form = getCurrentForm();

            form.saveExpectedModelReferenceForView(currentFormView, ref,
                                                   name);
        }
    }

    /**
     * When the transformer starts rendering a new form element
     * It needs to reset previously saved references for another
     * transformation of the same view.
     */
    protected void resetSavedModelReferences() {
        if (currentFormView!=null) {
            Form form = getCurrentForm();

            form.clearSavedModelReferences(currentFormView);
        }

    }

    /**
     * Used for elements which are not two directional.
     * They are displayed but cannot be used for submitting new values
     *
     * TAG_CAPTION, TAG_HINT, TAG_HELP, TAG_VALUE
     */
    protected void startElementWithOptionalRefAndSimpleContent(String uri,
        String name, String raw, Attributes attributes) throws SAXException {
        String ref = attributes.getValue(TAG_COMMON_ATTR_REF);

        if (ref==null) // ref attribute is not provided
        {
            super.startElement(uri, name, raw, attributes);
            return;
        }

        if (formStack.isEmpty()) {
            throw new SAXException(name+
                                   " element should be either nested within a form tag or provide a form attribute");
        }

        Form form = (Form) formStack.peek();

        getLogger().debug("["+String.valueOf(name)+
                          "] getting value from form [id="+form.getId()+
                          ", ref="+String.valueOf(ref)+"]");

        Object value = form.getValue(ref);

        // we will only forward the SAX event once we know
        // that the value of the tag is available
        super.startElement(uri, name, raw, attributes);

        getLogger().debug("Value of form [id="+form.getId()+", ref="+
                          String.valueOf(ref)+"] = ["+value_+"]");

        // Now render the character data inside the tag
        String v = String.valueOf(value);

        super.characters(v.toCharArray(), 0, v.length());

    } // end of startElementSimpleFieldWithOptionalRef

    /**
     * Renders one or more xf:value elements
     * depending on whether _value is a
     * collection, array or not.
     */
    private void renderValueSubElements() throws SAXException {
        // render the value subelement(s)
        if (value_ instanceof Collection) {
            Iterator i = ((Collection) value_).iterator();

            while (i.hasNext()) {
                renderValueSubElement(i.next());
            }
        } else if ((value_!=null) && value_.getClass().isArray()) {
            int len = Array.getLength(value_);

            for (int i = 0; i<len; i++) {
                renderValueSubElement(Array.get(value_, i));
            }
        } else {
            renderValueSubElement(value_);
        }
    }

    /**
     * Outputs a <xf:value> element.
     * Used when transforming XMLForm elements
     * with reference to the model
     *
     * @param vobj provides the text content
     * within the <xf:value> element
     */
    protected void renderValueSubElement(Object vobj) throws SAXException {
        super.startElement(NS, "value", NS_PREFIX+":"+"value", NOATTR);
        if (vobj!=null) {
            String v = String.valueOf(vobj);

            super.characters(v.toCharArray(), 0, v.length());
        }
        super.endElement(NS, "value", NS_PREFIX+":"+"value");
    }

    /**
     * Start processing elements of our namespace.
     * This hook is invoked for each sax event with our namespace.
     * @param uri The namespace of the element.
     * @param name The local name of the element.
     * @param raw The qualified name of the element.
     */
    public void endTransformingElement(String uri, String name,
                                       String raw)
                                         throws ProcessingException,
                                                IOException, SAXException {
        if (this.getLogger().isDebugEnabled()==true) {
            this.getLogger().debug("BEGIN endTransformingElement uri="+uri+
                                   ", name="+name+", raw="+raw+")");
        }

        try {
            // avoid endless loop for elements in our namespace
            this.ignoreHooksCount = 1;

            // when the end of an active repeat tag is reached
            // stop recording, unroll the repeat tag content
            // for each node in the node set,
            // then close the repeat tag
            if ((TAG_REPEAT.equals(name)) &&
                (repeatTagDepth==currentTagDepth)) {
                isRecording = false;
                DocumentFragment docFragment = endRecording();

                unrollRepeatTag(docFragment);
                nodeset = null;
                // close the repeat tag
                super.endElement(uri, name, raw);
            }
            // similarly for an itemset tag
            else if ((TAG_ITEMSET.equals(name)) &&
                     (repeatTagDepth==currentTagDepth)) {
                isRecording = false;
                DocumentFragment docFragment = endRecording();

                unrollItemSetTag(docFragment);
                nodeset = null;
            }
            // if within a repeat tag, keep recording
            // when recording, nothing is actively processed
            else if (isRecording) {
                // just record the SAX event
                super.endElement(uri, name, raw);
            } else // if not a repeat tag
            {
                // keep the ref stack in synch with the tree navigation
                if ( !refStack.isEmpty()) {
                    Entry entry = (Entry) refStack.peek();
                    Integer refDepth = (Integer) entry.getKey();

                    if (currentTagDepth<=refDepth.intValue()) {
                        refStack.pop();
                        cannonicalRef = refStack.isEmpty()
                                        ? ""
                                        : (String) ((Entry) (refStack.peek())).getValue();
                    }
                }

                if (TAG_INSERTVIOLATIONS.equals(name)) {
                    // all violations were rendered completely in the startElement method
                } else if (TAG_FORM.equals(name)) {
                    // pop currentForm from stack since we're getting out of its scope
                    formStack.pop();
                    super.endElement(uri, name, raw);
                } else if (TAG_TEXTBOX.equals(name) ||
                           TAG_TEXTAREA.equals(name) ||
                           TAG_PASSWORD.equals(name) ||
                           TAG_SELECTBOOLEAN.equals(name) ||
                           TAG_SELECTONE.equals(name) ||
                           TAG_SELECTMANY.equals(name) ||
                           TAG_SUBMIT.equals(name) ||
                           TAG_CAPTION.equals(name) ||
                           TAG_VALUE.equals(name) || TAG_HINT.equals(name) ||
                           TAG_HELP.equals(name)) {
                    super.endElement(uri, name, raw);
                } else if (TAG_OUTPUT.equals(name)) {
                    formStack.pop();
                    super.endElement(uri, name, raw);
                } else if (TAG_HIDDEN.equals(name)) {
                    isHiddenTag = false;
                    hasHiddenTagValue = false;
                    // if value sub-element was not
                    // provided in the markup
                    // then render the value of the referenced
                    // model attribute, like normally done
                    // for other elements
                    if ( !hasHiddenTagValue) {
                        renderValueSubElements();
                    }
                    super.endElement(uri, name, raw);
                } else {
                    getLogger().error("unknown element ["+
                                      String.valueOf(name)+"]");
                    super.endElement(uri, name, raw);
                }
            }      // else (not in a recording tag)
        } finally {
            // reset ignore hooks counter
            this.ignoreHooksCount = 0;

            // track the tree depth
            --currentTagDepth;
        }

        if (this.getLogger().isDebugEnabled()==true) {
            this.getLogger().debug("END endTransformingElement");
        }

    } // end of endTransformingElement

    /**
     * Unroll the repeat tag.
     * For each node in the repeat tag's nodeset selector result,
     * render a <code>group</code> tag with a <code>ref</code>
     * attribute which points to the location of the current node
     * in the nodeset. Within each <code>group</code> tag,
     * output the content of the repeat tag,
     * by resolving all form model references within nested xmlform tags,
     * relative to the <code>ref</code> attribute of the <code>group</code> element.
     *
     * @param docFragment the content of the repeat tag
     */
    protected void unrollRepeatTag(DocumentFragment docFragment)
      throws SAXException {
        int oldIgnoreHooksCount = ignoreHooksCount;

        try {
            // reset ignore hooks counter
            this.ignoreHooksCount = 0;
            Form currentForm = (Form) formStack.peek();
            Collection locations = currentForm.locate(nodeset);
            Iterator iter = locations.iterator();

            // iterate over each node in the nodeset
            while (iter.hasNext()) {
                String nextNodeLocation = (String) iter.next();

                // set the ref attribute to point to the current node
                AttributesImpl atts = new AttributesImpl();

                atts.addAttribute(null, TAG_COMMON_ATTR_REF,
                                  TAG_COMMON_ATTR_REF, "CDATA",
                                  nextNodeLocation);

                super.startElement(NS, TAG_GROUP, NS_PREFIX+":"+TAG_GROUP,
                                   atts);
                if (value_!=null) {
                    // stream back the recorder repeat content
                    DOMStreamer streamer = new DOMStreamer(this, this);

                    streamer.stream(docFragment);
                }

                super.endElement(NS, TAG_GROUP, NS_PREFIX+":"+TAG_GROUP);

            }
        } finally {
            ignoreHooksCount = oldIgnoreHooksCount;
        }
    } // unrollRepeatTag

    /**
     * Unroll the itemset tag.
     * For each node in the itemset tag's nodeset selector result,
     * render a <code>item</code> tag with a <code>ref</code>
     * attribute which points to the location of the current node
     * in the nodeset.
     * Within each <code>item</code> tag,
     * output the content of the itemset tag,
     * by resolving all model references within nested caption and value tags,
     * relative to the <code>ref</code> attribute of the <code>item</code> element.
     *
     * @param docFragment the content of the repeat tag
     */
    protected void unrollItemSetTag(DocumentFragment docFragment)
      throws SAXException {
        int oldIgnoreHooksCount = ignoreHooksCount;

        try {
            // reset ignore hooks counter
            this.ignoreHooksCount = 0;

            Form currentForm = (Form) formStack.peek();

            Collection locations = currentForm.locate(nodeset);
            Iterator iter = locations.iterator();

            // iterate over each node in the nodeset
            while (iter.hasNext()) {
                String nextNodeLocation = (String) iter.next();

                // set the ref attribute to point to the current node
                AttributesImpl atts = new AttributesImpl();

                atts.addAttribute(null, TAG_COMMON_ATTR_REF,
                                  TAG_COMMON_ATTR_REF, "CDATA",
                                  nextNodeLocation);

                super.startElement(NS, TAG_ITEM, NS_PREFIX+":"+TAG_ITEM,
                                   atts);
                if (value_!=null) {
                    // stream back the recorder repeat content
                    DOMStreamer streamer = new DOMStreamer(this, this);

                    streamer.stream(docFragment);
                }

                super.endElement(NS, TAG_ITEM, NS_PREFIX+":"+TAG_ITEM);

            }
        } finally {
            ignoreHooksCount = oldIgnoreHooksCount;
        }
    } // unrollItemSetTag

    protected Form getCurrentForm() {
        return (Form) formStack.peek();
    }

    /**
     * refStack entry.
     */
    private static class Entry implements Map.Entry {

        Object key;

        Object value;

        Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        // Map.Entry Ops
        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object value) {
            Object oldValue = this.value;

            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if ( !(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry) o;

            return ((key==null) ? e.getKey()==null : key.equals(e.getKey())) &&
                   ((value==null)
                    ? e.getValue()==null : value.equals(e.getValue()));
        }

        public int hashCode() {
            return getKey().hashCode()^((value==null) ? 0 : value.hashCode());
        }

        public String toString() {
            return key+"="+value;
        }
    }
}
