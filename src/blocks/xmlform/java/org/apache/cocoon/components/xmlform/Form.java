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
package org.apache.cocoon.components.xmlform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.cocoon.Constants;
import org.apache.cocoon.components.validation.Validator;
import org.apache.cocoon.components.validation.Violation;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.transformation.XMLFormTransformer;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

/**
 * <p>
 *  Encapsulates a form bean and the validation result
 *  in a single class. It is created automatically by the
 *  FormValidatingAction
 * </p>
 *
 * <b>NOTE: This class is NOT thread safe</b>
 *
 * @author Ivelin Ivanov, ivelin@apache.org
 * @author michael_hampel@sonynetservices.com
 * @version CVS $Id: Form.java,v 1.7 2004/03/01 03:50:57 antonio Exp $
 */
public class Form {

    public static String SCOPE_REQUEST = "request";

    public static String SCOPE_SESSION = "session";

    public static String FORM_VIEW_PARAM = "cocoon-xmlform-view";

    public static String VIOLATION_MESSAGE_DATA_FORMAT_ERROR = "Invalid data format or invalid reference path.";

    /**
     * An XMLForm is only usable when it has an id and an underlying model.
     *
     * @param id         
     * @param model      
     */
    public Form(String id, Object model) {

        if ((id==null) || (model==null)) {
            throw new java.lang.IllegalStateException("Form cannot be created with null id or null model ");
        }
        setId(id);
        setModel(model);
    }

    public String getId() {
        return id_;
    }

    public void setId(String newId) {
        id_ = newId;
    }

    public Object getModel() {
        return model_;
    }

    public void setModel(Object newModel) {
        model_ = newModel;
        jxcontext_ = JXPathContext.newContext(model_);
        jxcontext_.setLenient(false);
    }

    public Validator getValidator() {
        return validator_;
    }

    public void setValidator(Validator newValidator) {
        validator_ = newValidator;
    }

    public List getViolations() {
        return violations_;
    }

    /**
     *  Expose the JXPathContext for the sake of subclasses
     */
    protected JXPathContext getJXContext() {
        return jxcontext_;
    }

    /**
     * This method allows custom validations to be added
     * after population and after a call to validate
     * (either automatic or explicit).
     * Usually used from within the perform method of
     * a concrete XMLFormAction.
     *
     * @param newViolations
     */
    public void addViolations(List newViolations) {

        if (violations_!=null) {
            violations_.addAll(newViolations);
        } else {
            violations_ = newViolations;
        }
        updateViolationsAsSortedSet();

    }

    public SortedSet getViolationsAsSortedSet() {
        return violationsAsSortedSet_;
    }

    public void clearViolations() {
        violations_ = null;
        violationsAsSortedSet_ = null;
    }

    /**
     * Encapsulates access to the model.
     *
     * @param xpath to the model attribute.
     * @param value to be set.
     */
    public void setValue(String xpath, Object value) {
        if (model_==null) {
            throw new IllegalStateException("Form model not set");
        }
        jxcontext_.setValue(xpath, value);
    }

    public void setValue(String xpath, Object[] values) {

        // // Dmitri Plotnikov's patch
        // 
        // // if there are multiple values to set
        // // (like in the selectMany case),
        // // iterate over the array and set individual values
        // if ( values.length > 1  )
        // {
        // Iterator iter = jxcontext_.iteratePointers(xpath);
        // for (int i = 0; i < values.length; i++ )
        // {
        // Pointer ptr = (Pointer)iter.next();
        // ptr.setValue(values[i]);
        // }
        // }
        // else
        // {
        // // This is supposed to do the right thing
        // jxcontext_.setValue(xpath, values);
        // }
        // 

        Pointer pointer = jxcontext_.getPointer(xpath);
        Object property = pointer.getNode();

        // if there are multiple values to set
        // (like in the selectMany case),
        // iterate over the array and set individual values

        // when the instance property is array
        if ((property!=null) && property.getClass().isArray()) {
            Class componentType = property.getClass().getComponentType();

            property = java.lang.reflect.Array.newInstance(componentType,
                values.length);
            java.lang.System.arraycopy(values, 0, property, 0, values.length);
            pointer.setValue(property);
        } else if (property instanceof Collection) {
            Collection cl = (Collection) property;

            cl.clear();
            cl.addAll(java.util.Arrays.asList(values));
        } else if (property instanceof NativeArray) {
            Context.enter();
            try {
                NativeArray arr = (NativeArray) property;

                ScriptableObject.putProperty(arr, "length", new Integer(0));
                ScriptableObject.putProperty(arr, "length",
                                             new Integer(values.length));
                for (int i = 0; i<values.length; i++) {
                    Object val = values[i];

                    if ( !((val==null) || (val instanceof String) ||
                           (val instanceof Number) ||
                           (val instanceof Boolean))) {
                        val = Context.toObject(val, arr);
                    }
                    ScriptableObject.putProperty(arr, i, val);
                }
            } catch (Exception willNotBeThrown) {
                // shouldn't happen
                willNotBeThrown.printStackTrace();
            } finally {
                Context.exit();
            }
        } else {
            jxcontext_.setValue(xpath, values[0]);
        }
    }

    /**
     * Encapsulates access to the model.
     *
     * @param xpath of the model attribute
     */
    public Object getValue(String xpath) {
        if (model_==null) {
            throw new IllegalStateException("Form model not set");
        }
        Object result = jxcontext_.getValue(xpath);

        if (result instanceof NativeArray) {
            // Convert JavaScript array to Collection
            NativeArray arr = (NativeArray) result;
            int len = (int) arr.jsGet_length();
            List list = new ArrayList(len);

            for (int i = 0; i<len; i++) {
                Object obj = arr.get(i, arr);

                if (obj==Context.getUndefinedValue()) {
                    obj = null;
                }
                list.add(obj);
            }
            result = list;
        }
        return result;
    }

    /**
     * Resolves a nodeset selector
     * into a list of concrete node locations.
     * @param xpathSelector the nodeset selector
     *
     * @return a Set of XPath strings pointing to
     * each nodeset satisfying the nodeset selector
     *
     * <p>
     * TODO: the Collection return type should be replaced with a Set.
     * LinkedHashSet implementation should be used. All resolved
     * nodes are unique in the resulting set, therefore Set is more appropriate.
     * Since LinkedHashSet is only available in JDK 1.4 or later, it is not
     * appropriate to make the change immediately.
     */
    public Collection locate(String xpathSelector) {
        if (model_==null) {
            throw new IllegalStateException("Form model not set");
        }
        List nodeset = new LinkedList();
        Iterator iter = jxcontext_.iteratePointers(xpathSelector);

        while (iter.hasNext()) {
            Pointer nextPointer = (Pointer) iter.next();
            String path = nextPointer.asPath();

            nodeset.add(path);
        }
        return nodeset;
    }

    /**
     * Performs complete validation
     * of the form model.
     *
     */
    public boolean validate() {
        return validate(null);
    }

    /**
     *
     * @param phase the validation phase
     *
     * @return If validation finishes without any violations,
     *         return true otherwise return false and save 
     *         all violations.
     */
    public boolean validate(String phase) {
        if (validator_==null) {
            return true;
        }

        validator_.setProperty(Validator.PROPERTY_PHASE, phase);
        List vs = validator_.validate(model_);

        if (vs!=null) {
            if (violations_!=null) {
                violations_.addAll(vs);
            } else {
                if ( !vs.isEmpty()) {
                    violations_ = vs;
                }
            }
        }
        if (violations_==null) {
            return true;
        } else {
            updateViolationsAsSortedSet();
            return false;
        }
    }

    /**
     * Populates an HTML Form POST into the XMLForm model (JavaBean or DOM node).
     *
     * <p>
     * Expects that all request parameter names are XPath expressions
     * to attributes of the model.
     * For each request parameter, finds and assigns its value to the
     * JavaBean property corresponding to the parameter's name
     * </p>
     *
     * TODO: provide a more sophisticated examples with checkboxes, multi choice,
     * radio button, text area, file upload, etc.
     *
     * @param sitemapObjectModel
     */
    public void populate(Map sitemapObjectModel) {
        // clean violations_ set
        clearViolations();

        // let listeners know that
        // population is about to start
        reset();

        // data format violations
        // gathered during population
        // For example when
        // a request parameter value is "saymyname"
        // while the request parameter name points to an int attribute
        List pviolations = new ArrayList();

        Map filteredParameters = getFilteredRequestParameters(sitemapObjectModel);
        Iterator iter = filteredParameters.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();

            String path = (String) entry.getKey();

            // filter custom request parameter
            // not refering to the model
            if (filterRequestParameter(path)) {
                continue;
            }

            Object[] values = (Object[]) entry.getValue();

            try {
                setValue(path, values);
            } catch (JXPathException ex) {
                Violation v = new Violation();

                v.setPath(path);
                v.setMessage(ex.getMessage());
                pviolations.add(v);
            }
        } // while

        // validate form model
        autoValidate(sitemapObjectModel);

        // merge violation sets
        if (violations_!=null) {
            violations_.addAll(pviolations);
        } else {
            if ( !pviolations.isEmpty()) {
                violations_ = pviolations;
            }
        }
        if (violations_!=null) {
            updateViolationsAsSortedSet();
        }
    }

    /**
     * Filters request parameters which are not references to model properties.
     * Sets default values for parameters which were expected in the request,
     * but did not arrive (e.g. check boxes).
     *
     * @param sitemapObjectModel
     * @return filtered request parameters
     */
    protected Map getFilteredRequestParameters(Map sitemapObjectModel) {

        Request request = getRequest(sitemapObjectModel);

        Map filteredParameters = new HashMap();

        // first filter out request parameters which do not refer to model properties
        Enumeration enum = request.getParameterNames();

        while (enum.hasMoreElements()) {
            String path = (String) enum.nextElement();

            // filter custom request parameter
            // not refering to the model
            if (filterRequestParameter(path)) {
                continue;
            }

            Object[] values = request.getParameterValues(path);

            filteredParameters.put(path, values);
        }

        // now, find expected parameters which did not arrive
        // and set default values for them
        String viewName = getFormView(sitemapObjectModel);
        Map expectedReferences = getFormViewState(viewName).getModelReferenceMap();

        Iterator iter = expectedReferences.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String propertyReference = (String) entry.getKey();

            // check if the expected parameter actually arrived in the request
            if (filteredParameters.get(propertyReference)==null) {
                // Since it is not there, try to provide a default value
                String inputType = (String) entry.getValue();

                Object defaultValue = null;

                if (inputType.equals(XMLFormTransformer.TAG_SELECTBOOLEAN)) {
                    // false for boolean type (usually, single check-box)
                    defaultValue = new Object[]{ Boolean.FALSE };
                } else if (inputType.equals(XMLFormTransformer.TAG_SELECTMANY)) {
                    // empty array for select many (usually, multi check-box)
                    defaultValue = new Object[0];
                } else {
                    // for all the rest, use a blank value and hope for the best
                    defaultValue = new Object[]{ "" };
                }

                filteredParameters.put(propertyReference, defaultValue);

            }

        } // iterate over expectedReferences.entrySet()

        return filteredParameters;

    } // getFilteredRequestParameters

    /** 
     * Create a SortedSet view of the violations collection
     * for convenience of processors down the pipeline
     * protected void updateViolationsAsSortedSet()
     */
    protected void updateViolationsAsSortedSet() {
        violationsAsSortedSet_ = new TreeSet(violations_);
    }

    /**
     * Convenience method invoked after populate()
     * By default it performs Form model validation.
     *
     * <br>
     * - If default validation is not necessary
     * setAutoValidate( false ) should be used
     *
     * <br>
     * If the validation
     * criteria needs to be different, subclasses can override
     * this method to change the behaviour.
     *
     * @param sitemapObjectModel
     */
    protected void autoValidate(Map sitemapObjectModel) {
        if ( !autoValidateEnabled_) {
            return;
        }
        // perform validation for the phase
        // which matches the name of the current form view
        // if one is available
        String formView = getFormView(sitemapObjectModel);

        if (formView!=null) {
            validate(formView);
        }
    }

    /**
     * Filters custom request parameter not refering to the model.
     *
     * TODO: implement default filtering
     * for standard Cocoon parameters
     * like cocoon-action[-suffix]
     *
     * @param name       
     *
     */
    protected boolean filterRequestParameter(String name) {
        // filter standard cocoon-* parameters
        if (filterDefaultRequestParameter(name)) {
            return true;
        }

        // then consult with FormListeners
        Set ls = new HashSet();

        ls.addAll(Collections.synchronizedSet(formListeners_));
        Iterator iter = ls.iterator();

        while (iter.hasNext()) {
            FormListener fl = (FormListener) iter.next();

            // if any of the listeners wants this parameter filtered
            // then filter it (return true)
            if (fl.filterRequestParameter(this, name)) {
                return true;
            }
        }
        // if none of the listeners wants this parameter filtered
        // then don't filter it
        return false;
    }

    /**
     * Filters the standard cocoon request parameters.
     * If default filtering needs to be different,
     * subclasses can override this method.
     * It is invoked before all listeners are asked to filter the parameter
     *
     * @param paramName  
     *
     */
    protected boolean filterDefaultRequestParameter(String paramName) {
        // Forbid parameters containing parenthesis to avoid method-call injection
        if (paramName.indexOf('(') != -1) {
            return true;
        }
        
        if (paramName.startsWith(Constants.ACTION_PARAM_PREFIX) ||
            paramName.startsWith(Constants.VIEW_PARAM)) {
            return true;
        }
        if (paramName.equals(FORM_VIEW_PARAM)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Try to extract from the request
     * and return the current form view
     *
     * @param sitemapObjectModel
     *
     */
    public String getFormView(Map sitemapObjectModel) {
        return getRequest(sitemapObjectModel).getParameter(Form.FORM_VIEW_PARAM);
    }

    /**
     * This method is called before
     * the form is populated with request parameters.
     *
     * Semantically similar to that of the
     * ActionForm.reset() in Struts
     *
     * Can be used for clearing checkbox fields,
     * because the browser will not send them when
     * not checked.
     *
     * Calls reset on all FormListeners
     */
    protected void reset() {
        // notify FormListeners
        Set ls = new HashSet();

        ls.addAll(Collections.synchronizedSet(formListeners_));
        Iterator iter = ls.iterator();

        while (iter.hasNext()) {
            FormListener fl = (FormListener) iter.next();

            fl.reset(this);
        }
        return;
    }

    /**
     * Loads a form from the request or session
     *
     * @param sitemapObjectModel
     * @param id the form id
     *
     */
    public static Form lookup(Map sitemapObjectModel, String id) {
        Request request = getRequest(sitemapObjectModel);
        Form form = (Form) request.getAttribute(id);

        if (form!=null) {
            return form;
        } else {
            Session session = request.getSession(false);

            if (session!=null) {
                form = (Form) session.getAttribute(id);
            }
            return form;
        }
    }

    /**
     * Removes a form from the request and session.
     * This method will remove the attribute bindings
     * correspoding to the form id from both request
     * and session to ensure that a subsequent
     * Form.lookup will not succeed.
     *
     * @param sitemapObjectModel
     * @param id the form id
     */
    public static void remove(Map sitemapObjectModel, String id) {
        Request request = getRequest(sitemapObjectModel);

        request.removeAttribute(id);

        Session session = request.getSession(false);

        if (session!=null) {
            session.removeAttribute(id);
        }
    }

    /**
     * Saves the form in the request or session.
     *
     * @param sitemapObjectModel
     * @param scope if true the form will be bound in the session, otherwise request
     */
    public void save(Map sitemapObjectModel, String scope) {
        Request request = getRequest(sitemapObjectModel);

        if (lookup(sitemapObjectModel, id_)!=null) {
            throw new java.lang.IllegalStateException("Form [id="+id_+
                                                      "] already bound in request or session ");
        }

        if (SCOPE_REQUEST.equals(scope)) {
            request.setAttribute(id_, this);
        } else // session scope
        {
            Session session = request.getSession(true);

            session.setAttribute(id_, this);
        }

    }

    /**
     * Add another FormListener.
     */
    public synchronized void addFormListener(FormListener formListener) {
        formListeners_.add(formListener);
    }

    /**
     * Add another FormListener
     */
    public synchronized void removeFormListener(FormListener formListener) {
        formListeners_.remove(formListener);
    }

    protected final static Request getRequest(Map sitemapObjectModel) {
        return (Request) sitemapObjectModel.get(ObjectModelHelper.REQUEST_OBJECT);
    }

    public void setAutoValidate(boolean newAVFlag) {
        autoValidateEnabled_ = newAVFlag;
    }

    /**
     * <pre>
     * When the transformer renders a form view,
     * it lets the form wrapper know about each referenced model property.
     * This allows a precise tracking and can be used for multiple reasons:
     *   1) Verify that the client does not temper with the input fields as specified by the
     *      form view author
     *   2) Allow default values to be used for properties which were expected to be send by the client,
     *      but for some reason were not. A typical example is a check box. When unchecked, the browser
     *      does not send any request parameter, leaving it to the server to handle the situation.
     *      This proves to be a very error prone problem when solved on a case by case basis.
     *      By having a list of expected property references, the model populator can detect
     *      a checkbox which was not send and set the property value to false.
     *
     * NOTE: This added functionality is ONLY useful for SESSION scope forms.
     * Request scope forms are constructed anew for every request and therefore
     * cannot benefit from this extra feature.
     * With the high performance CPUs and cheap memory used in today's servers,
     * session scope forms are a safe choice.
     * </pre>
     *
     * @param currentFormView
     * @param ref        
     * @param inputType  
     */
    public void saveExpectedModelReferenceForView(String currentFormView,
        String ref, String inputType) {
        // if the form view is null, we are not interested in saving any references
        if (currentFormView==null) {
            return;
        }

        FormViewState formViewState = getFormViewState(currentFormView);

        formViewState.addModelReferenceAndInputType(ref, inputType);
    }

    /**
     * When the transformer starts rendering a new form element.
     * It needs to reset previously saved references for another
     * transformation of the same view.
     *
     * @param currentFormView
     */
    public void clearSavedModelReferences(String currentFormView) {
        FormViewState formViewState = getFormViewState(currentFormView);

        formViewState.clear();
    }

    /**
     * We keep a map of ViewState objects which store
     * all references to model properties in a particular form view
     * which were rendered by the
     * XMLFormTansformer in the most recent transformation.
     *
     * @param viewName   
     *
     */
    protected FormViewState getFormViewState(String viewName) {
        FormViewState formViewState = (FormViewState) viewStateMap_.get(viewName);

        if (formViewState==null) {
            formViewState = new FormViewState();
            viewStateMap_.put(viewName, formViewState);
        }
        return formViewState;
    }

    /**
     * Internal class used for keeping state information
     * during the life cycle of a form.
     *
     * <p>Used only for session scoped forms
     */
    class FormViewState {
        private Map modelReferences_ = new HashMap();

        FormViewState() {
        }

        /**
         *
         * @return Map of (String modelPropertyReference, String inputType) pairs
         */
        Map getModelReferenceMap() {
            return modelReferences_;
        }

        void addModelReferenceAndInputType(String modelPropertyReference,
                                           String inputType) {
            modelReferences_.put(modelPropertyReference, inputType);
        }

        void clear() {
            modelReferences_.clear();
        }
    }

    /** the set of violations the model commited during validation */
    private List violations_ = null;

    /** another view of the violations_ collection */
    private SortedSet violationsAsSortedSet_ = null;

    /** flag allowing control over automatic validation on populate() */
    private boolean autoValidateEnabled_ = true;

    /** The data model this form encapsulates */
    private Object model_ = null;

    /** The list of FormListeners */
    private Set formListeners_ = new HashSet();

    /**
     * The unique identifier for this form. Used when form is stored in request
     * or session for reference by other components
     *
     * <p>
     * TODO: a centralized form registry would be helpful to prevent from id collision
     */
    private String id_ = null;

    /**
     * The JXPath context associated with the model.
     * Used to traverse the model with XPath expressions
     */
    private JXPathContext jxcontext_ = null;

    /**
     * Used to validate the content of the model
     * at various phases.
     */
    private Validator validator_ = null;

    /**
     * Keeps a state information for
     * each form view that has been processed.
     */
    private Map viewStateMap_ = new HashMap();
}
