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

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.validation.Validator;
import org.apache.cocoon.components.xmlform.Form;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * This action extends the idea of the AbstractXMLFormAction. It
 * uses a configuration file similar to Jakarta Struts, and uses
 * the concept of the MultiAction.
 *
 * Warning! This classes is an experimental one.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: AbstractControllerAction.java,v 1.4 2004/03/05 13:02:37 bdelacretaz Exp $
 */
public abstract class AbstractControllerAction
  extends AbstractComplementaryConfigurableAction implements ThreadSafe {

    private static final String ACTION_METHOD_PREFIX = "do";

    private HashMap methodIndex;

    private static final String removePrefix(String name) {
        int prefixLen = ACTION_METHOD_PREFIX.length();

        return name.substring(prefixLen, prefixLen+1).toLowerCase()+
               name.substring(prefixLen+1);
    }

    /**
     * Configure the action.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);

        try {
            Method[] methods = this.getClass().getMethods();

            methodIndex = new HashMap();

            for (int i = 0; i<methods.length; i++) {
                String methodName = methods[i].getName();

                if (methodName.startsWith(ACTION_METHOD_PREFIX)) {
                    String actionName = removePrefix(methodName);

                    methodIndex.put(actionName, methods[i]);
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("registered method \""+methodName+
                                          "\" as action \""+actionName+"\"");
                    }
                }
            }
        } catch (Exception e) {
            throw new ConfigurationException("cannot get methods by reflection",
                                             e);
        }
    }

    /**
     * Perform the action.
     */
    public Map act(Redirector redirector, SourceResolver resolver,
                   Map objectModel, String src,
                   Parameters params) throws Exception {

        Request request = (Request) (objectModel.get(ObjectModelHelper.REQUEST_OBJECT));

        // load controller configuration
        Configuration config = getConfiguration(src, resolver, true);

        // current path
        String path = request.getSitemapURI();

        String base = request.getRequestURI().substring(0,
                          request.getRequestURI().length()-path.length());

        // ensure that there is a form available
        // through the rest of the flow
        Form form = getForm(config, path, objectModel);

        if (form==null) {
            throw new IllegalStateException("Action could not obtain the Form");
        }

        // populate form with request parameters
        // population is automatically followed by validation by default.
        // If this is not the desired behaviour, the Form class can be subclassed
        form.populate(objectModel);

        // If the currect form have incorrect values, then process the same page
        if (form.getViolations()!=null) {
            return EMPTY_MAP;
        }

        // find and save the action command
        String command = getCommand(request);

        if ((command==null) || (command.length()==0)) {
            // if no command send, process current page
            return EMPTY_MAP;
        }

        // find the action name for the method, which should be performed.
        String action = getAction(config, path);

        // perform action method
        String forward = command;

        if ((action!=null) && (action.length()>0)) {
            Method method = (Method) methodIndex.get(action);

            if (method!=null) {
                forward = ((String) method.invoke(this, new Object[]{ command,
                                                                      form }));
                if (forward==null) {
                    forward = command;
                }
            }
        }
        if ((command==null) || (command.length()==0)) {
            // if no command send, process current page
            return EMPTY_MAP;
        }

        // if action returns violation, process current form
        if ((forward==null) && (form.getViolations()!=null)) {
            return EMPTY_MAP;
        }

        // process forward
        String newpath = getForward(config, path, forward);

        if (path.equals(newpath)) {
            return EMPTY_MAP;
        } else {
            redirector.redirect(true, base+newpath);
            return null;
        }
    }

    /**
     * Returns name of the action for a given page.
     */
    private String getAction(Configuration config, String path) {

        Configuration[] actions = config.getChild("action-mappings").getChildren("action");

        String action = null;

        for (int i = 0; i<actions.length; i++)
            if (actions[i].getAttribute("path", null).equals(path)) {
                action = actions[i].getAttribute("name", null);
            }

        return (action!=null) ? action.toLowerCase() : null;
    }

    /**
     * Get the command which was submitted with the form.
     * It is extracted from the standard cocoon-action-* request parameter
     */
    private String getCommand(Request request) {
        Enumeration enum = request.getParameterNames();

        while (enum.hasMoreElements()) {
            String paramName = (String) enum.nextElement();

            // search for the command
            if (paramName.startsWith(Constants.ACTION_PARAM_PREFIX)) {
                return paramName.substring(Constants.ACTION_PARAM_PREFIX.length(),
                                           paramName.length());
            }
        }
        return null;
    }

    /**
     * Return the page for a given forward.
     */
    private String getForward(Configuration config, String path,
                              String name) {

        Configuration[] actions = config.getChild("action-mappings").getChildren("action");

        for (int i = 0; i<actions.length; i++)
            if (actions[i].getAttribute("path", null).equals(path)) {
                Configuration[] forwards = actions[i].getChildren("forward");

                for (int j = 0; j<forwards.length; j++)
                    if (forwards[j].getAttribute("name", null).equals(name)) {
                        return forwards[j].getAttribute("path", null);
                    }
            }

        Configuration[] forwards = config.getChild("global-forwards").getChildren("forward");

        for (int i = 0; i<forwards.length; i++)
            if (forwards[i].getAttribute("name", null).equals(name)) {
                return forwards[i].getAttribute("path", null);
            }

        return null;
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
    private Form getForm(Configuration config, String path, Map objectModel) {
        Form form = Form.lookup(objectModel, getFormId(config, path));

        if (form!=null) {
            return form;
        } else {
            // create new form
            form = new Form(getFormId(config, path),
                            getFormModel(config, path));
            Validator v = null; // getFormValidator();

            form.setValidator(v);
            form.save(objectModel, getFormScope(config, path));
            return form;
        }
    }

    /**
     * Extract xmlform-model action parameter and
     * instantiate a new form model it.
     *
     * Subclasses may override this method
     * to use custom model instantiation technique
     *
     * @return Form model
     */
    private Object getFormModel(Configuration config, String path) {

        String formId = getFormId(config, path);

        Configuration[] beans = config.getChild("form-beans").getChildren("form-bean");

        String modelClassName = null;

        for (int i = 0; i<beans.length; i++)
            if (beans[i].getAttribute("name", null).equals(formId)) {
                modelClassName = beans[i].getAttribute("type", null);
            }

        try {
            Class modelClass = Class.forName(modelClassName);
            Object o = modelClass.newInstance();

            return o;
        } catch (Exception e) {
            throw new CascadingRuntimeException(" Failed instantiating form model ",
                                                e);
        }
    }

    /**
     * Returns the form id for a given page.
     */
    private String getFormId(Configuration config, String path) {
        Configuration[] actions = config.getChild("action-mappings").getChildren("action");

        String formId = null;

        for (int i = 0; i<actions.length; i++)
            if (actions[i].getAttribute("path", null).equals(path)) {
                formId = actions[i].getAttribute("form", null);
            }

        if (formId==null) {
            throw new RuntimeException(" xmlform-id not specified ");
        } else {
            return formId;
        }
    }

    /**
     * Returns the scope of the form for a given page.
     */
    private String getFormScope(Configuration config, String path) {

        Configuration[] actions = config.getChild("action-mappings").getChildren("action");

        String formScope = null;

        for (int i = 0; i<actions.length; i++)
            if (actions[i].getAttribute("path", null).equals(path)) {
                formScope = actions[i].getAttribute("scope", null);
            }

        if (formScope==null) {
            // default to request scope
            formScope = Form.SCOPE_REQUEST;
        }

        return formScope;
    }
}
