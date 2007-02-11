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
package org.apache.cocoon.acting;

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.SchemaFactory;
import org.apache.cocoon.components.validation.Validator;
import org.apache.cocoon.components.xmlform.Form;
import org.apache.cocoon.components.xmlform.FormListener;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.xml.sax.InputSource;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the base action class for
 * xmlform handling.
 *
 * This action is Poolable which means that
 * subclasses of this class should not be
 * concerned about thread safety.
 * The framework ensures that only one thread
 * has access to a concrete instance at any time.
 *
 * However once an action is complete, the instance
 * will be recycled and reused for another request.
 *
 *
 * Several ideas are borrowed from the original work of
 * Torsten Curdt.
 *
 * @author Ivelin Ivanov <ivelin@apache.org>
 * @version CVS $Id: AbstractXMLFormAction.java,v 1.6 2004/03/05 13:02:37 bdelacretaz Exp $
 */
public abstract class AbstractXMLFormAction
  extends ConfigurableServiceableAction
  implements Poolable, Recyclable, FormListener {

    public static final String OBJECT_MAP_NEXT_PAGE = "page";

    protected static final Map PREPARE_RESULT_CONTINUE = null;

    // action state objects
    private Redirector redirector_;
    private SourceResolver resolver_;
    private Map objectModel_;
    private Parameters params_;
    private String src_;
    private String command_;

    /**
     * The first method which is called
     * when an action is invoked.
     *
     * It is called before population.
     *
     * @return null if the Action is prepared to continue.
     * an objectModel map which will be immediately returned by the action.
     *
     * This method is a good place to handle buttons with Cancel
     * kind of semantics. For example
     * <pre>return page("input")</pre>
     */
    protected Map prepare() {
        // by default, assume that there is
        // no preparation needed
        return PREPARE_RESULT_CONTINUE;
    }

    /**
     * FormListener callback
     * called in the beginning of Form.populate()
     * before population starts.
     *
     * This is the place to intialize the model for this request.
     *
     * This method should not handle unchecked check boxes
     * when the form is session scope, which is the most common case.
     * It should only do so, if the form is request scoped.
     *
     * @param form       
     */
    public void reset(Form form) {
        // Do Nothing by default
        return;
    }

    /**
     * FormListener callback.
     *
     * Invoked during Form.populate();
     *
     * It is invoked before a request parameter is mapped to
     * an attribute of the form model.
     *
     * It is appropriate to use this method for filtering
     * custom request parameters which do not reference
     * the model.
     *
     * Another appropriate use of this method is for graceful filtering of invalid
     * values, in case that knowledge of the system state or
     * other circumstainces make the standard validation
     * insufficient. For example if a registering user choses a username which
     * is already taken - the check requires database transaction, which is
     * beyond the scope of document validating schemas.
     * Of course customized Validators can be implemented to do
     * this kind of domain specific validation
     * instead of using this method.
     *
     * @param form       
     * @param parameterName
     * @return false if the request parameter should not be filtered.
     * true otherwise.
     */
    public boolean filterRequestParameter(Form form, String parameterName) {
        // in this example we do not expect "custom" parameters
        return false;
    }

    /**
     * Invoked during the form population process
     *
     * Provides default implementation, which
     * can be extended or replaced by subclasses
     *
     * Implementations of this method are responsible
     * for creating and
     * returning the Form object which the action
     * is working on.
     *
     * @return Form the form object this action works with
     */
    protected Form getForm() {
        Form form = Form.lookup(getObjectModel(), getFormId());

        if (form!=null) {
            return form;
        } else {
            // create new form
            form = new Form(getFormId(), getFormModel());
            Validator v = getFormValidator();

            form.setValidator(v);
            form.save(getObjectModel(), getFormScope());
            return form;
        }
    }

    public Map act(Redirector redirector, SourceResolver resolver,
                   Map objectModel, String src,
                   Parameters params) throws Exception {
        // populate action state objects
        redirector_ = redirector;
        resolver_ = resolver;
        objectModel_ = objectModel;
        src_ = src;
        params_ = params;

        // ensure that there is a form available
        // through the rest of the flow
        Form form = getForm();

        if (form==null) {
            throw new IllegalStateException("Action could not obtain the Form");
        }

        // find and save the action command
        findCommand();

        // call the subclass prepare()
        // give it a chance to get ready for action
        Map prepareResult = prepare();

        if (prepareResult!=null) {
            return prepareResult;
        }

        // attache callback hooks to the form
        // in case the action subclasses are interested in
        // form events
        getForm().addFormListener(this);
        Map result = null;

        try {
            // populate form with request parameters
            // population is automatically followed by validation by default.
            // If this is not the desired behaviour, the Form class can be subclassed
            form.populate(objectModel);

            result = perform();
        } finally {
            // since the action may be recycled immediately after
            // the request. It is important that it's callback hooks
            // are removed from the Form.
            getForm().removeFormListener(this);
        }

        return result;
    }

    /**
     * Get the command which was submitted with the form.
     * It is extracted from the standard cocoon-action-* request parameter
     *
     */
    public String getCommand() {
        return command_;
    }

    protected void findCommand() {
        command_ = null;
        Enumeration enum = getRequest().getParameterNames();

        while (enum.hasMoreElements()) {
            String paramName = (String) enum.nextElement();

            // search for the command
            if (paramName.startsWith(Constants.ACTION_PARAM_PREFIX)) {
                command_ = paramName.substring(Constants.ACTION_PARAM_PREFIX.length(),
                                               paramName.length());
            }
        }
    }

    /**
     * @return the @view attribute of the xmlform form tag.
     * This attribute is used to identify the part(or view)
     * of the model which is used in the specific xmlform
     * document.
     *
     */
    public String getFormView() {
        return getForm().getFormView(getObjectModel());
    }

    /**
     * Called to determine the exit point of an action.
     * The pageName is made available in the objectMap,
     * which can be then referenced in the pipeline

     * @param pageName logical name for a next page
     * @return Map a pipeline objectMap containing the pageName
     */
    protected Map page(String pageName) {
        Map objectModel = new HashMap();

        objectModel.put(OBJECT_MAP_NEXT_PAGE, pageName);
        return objectModel;
    }

    /**
     * Invoked after form population
     * unless a Cancel button was pressed,
     * in which case population is skipped and this method
     * is invoked immediately.
     *
     * Semanticly similar to Struts Action.perform()
     *
     * Take appropriate action based on the command.
     */
    public abstract Map perform();

    protected SourceResolver getSourceResolver() {
        return resolver_;
    }

    protected Redirector getRedirector() {
        return redirector_;
    }

    protected Map getObjectModel() {
        return objectModel_;
    }

    protected Parameters getParameters() {
        return params_;
    }

    protected String getSrc() {
        return src_;
    }

    protected Request getRequest() {
        return (Request) (getObjectModel().get(ObjectModelHelper.REQUEST_OBJECT));
    }

    protected Session getSession(boolean shouldCreateNew) {
        return getRequest().getSession(shouldCreateNew);
    }

    protected Session getSession() {
        return getSession(true);
    }

    /**
     * Extract action parameters and
     * instantiate a new validator based on them.
     *
     * xmlform-validator-schema-ns
     * xmlform-validator-schema
     *
     * Subclasses may override this method
     * to use custom validators
     *
     */
    protected Validator getFormValidator() {
        try {
            // initialize the Validor with a schema file
            String schNS = getParameters().getParameter("xmlform-validator-schema-ns",
                                                        null);
            String schDoc = getParameters().getParameter("xmlform-validator-schema",
                                null);

            // if validator params are not specified, then
            // there is no validation by default
            if ((schNS==null) || (schDoc==null)) {
                return null;
            }

            Source schemaSrc = getSourceResolver().resolveURI(schDoc);

            try {
                InputSource is = SourceUtil.getInputSource(schemaSrc);
                SchemaFactory schf = SchemaFactory.lookup(schNS);
                Schema sch = schf.compileSchema(is);

                return sch.newValidator();
            } finally {
                getSourceResolver().release(schemaSrc);
            }
        } catch (Exception e) {
            // couldn't load the validator
            throw new CascadingRuntimeException(" Failed loading validating schema ",
                                                e);
        }
    }

    /**
     * Extract xmlform-model action parameter and
     * instantiate a new form model it.
     *
     * Subclasses may override this method
     * to use custom model instantiation technique
     *
     */
    protected Object getFormModel() {
        try {
            String modelClassName = getParameters().getParameter("xmlform-model",
                                        null);
            Class modelClass = Class.forName(modelClassName);
            Object o = modelClass.newInstance();

            return o;
        } catch (Exception e) {
            throw new CascadingRuntimeException(" Failed instantiating form model ",
                                                e);
        }
    }

    protected String getFormId() {
        String formId = getParameters().getParameter("xmlform-id", null);

        if (formId==null) {
            throw new RuntimeException(" xmlform-id not specified ");
        } else {
            return formId;
        }
    }

    protected String getFormScope() {
        String formScope = getParameters().getParameter("xmlform-scope",
                               null);

        if (formScope==null) {
            // default to request scope
            formScope = Form.SCOPE_REQUEST;
        }

        return formScope;
    }

    /**
     * Recycle this component.
     */
    public void recycle() {
        redirector_ = null;
        resolver_ = null;
        objectModel_ = null;
        params_ = null;
        src_ = null;
        command_ = null;
    }
}
