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
package org.apache.cocoon.components.jxforms.flow.javascript;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon;
import org.apache.cocoon.components.flow.javascript.fom.FOM_WebContinuation;
import org.apache.cocoon.components.jxforms.validation.Schema;
import org.apache.cocoon.components.jxforms.validation.SchemaFactory;
import org.apache.cocoon.components.jxforms.validation.Violation;
import org.apache.cocoon.components.jxforms.xmlform.Form;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.continuations.Continuation;
import org.xml.sax.InputSource;

public class JXForm extends ScriptableObject {

    FOM_Cocoon cocoon;
    Object model;
    Form form;
    JXPathContext context;
    String id;
    String validatorNamespace;
    String validatorDocument;
    String submitId;

    private FOM_Cocoon getCocoon() {
        if (cocoon == null) {
            cocoon = (FOM_Cocoon)getProperty(getTopLevelScope(this), "cocoon");
        }
        return cocoon;
    }

    public JXForm() {
    }

    public static Scriptable jsConstructor(Context cx, Object[] args,
                                           Function ctorObj, 
                                           boolean inNewExpr)
        throws Exception {
        String id;
        String validatorNS = null;
        String validatorDoc = null;
        if (args.length < 1) {
            throw new JavaScriptException("Expected an argument");
        }
        id = org.mozilla.javascript.Context.toString(args[0]);
        if (args.length > 1) {
            validatorNS = (args[1] == null || args[1] == Undefined.instance) ?
                null : 
                org.mozilla.javascript.Context.toString(args[1]);
            if (args.length > 2) {
                validatorDoc = (args[2] == null || args[2] == Undefined.instance) ? 
                    null : 
                    org.mozilla.javascript.Context.toString(args[2]);
            }
        }
        JXForm result = new JXForm();
        result.id = id;
        result.validatorNamespace = validatorNS;
        result.validatorDocument = validatorDoc;
        return result;
    }

    public String getClassName() {
        return "JXForm";
    }

    public Object jsFunction_getModel() {
        return model;
    }

    public void jsFunction_setModel(Object obj) throws Exception {
        model = unwrap(obj);
        form = new Form(id, model);
        context = JXPathContext.newContext(model);
        form.setAutoValidate(false);
        if (validatorNamespace != null && validatorDocument != null) {
            SourceResolver resolver = (SourceResolver)
                getCocoon().getServiceManager().lookup(SourceResolver.ROLE);
            Source schemaSrc = resolver.resolveURI(validatorDocument);
            InputSource is = SourceUtil.getInputSource(schemaSrc);
            SchemaFactory schf = SchemaFactory.lookup(validatorNamespace);
            Schema sch = schf.compileSchema(is);
            form.setValidator(sch.newValidator());
        }
    }

    public String jsFunction_getSubmitId() {
        //return submitId;
        return (String)cocoon.getRequest().getAttribute("jxform-submit-id");
    }

    public void jsSet_submitId(String value) {
        submitId = value;
    }

    public String jsGet_submitId() {
        //return submitId;
        return (String)cocoon.getRequest().getAttribute("jxform-submit-id");
    }

    public void jsFunction_forwardTo(String uri,
                                     Object bizData,
                                     Object continuation) 
        throws Exception {
        FOM_Cocoon cocoon = getCocoon();
        FOM_WebContinuation fom_wk = 
            (FOM_WebContinuation)unwrap(continuation);
        cocoon.forwardTo(uri,
                         unwrap(bizData),
                         fom_wk);
                         
    }

    public void jsFunction_addViolation(String xpath, String message) 
        throws Exception {
        Violation violation = new Violation();
        violation.setPath(xpath);
        violation.setMessage(message);
        List list = new LinkedList();
        list.add(violation);
        form.addViolations(list);
    }

    public boolean jsFunction_hasViolations() {
        Set set = form.getViolationsAsSortedSet();
        return set != null && set.size() > 0;
    }

    public Object jsFunction_getValue(String expr) {
        return context.getValue(expr);
    }

    public static void jsStaticFunction_handleContinuation(String kontId,
                                                           Object cocoon_) 
        throws Exception {
        FOM_Cocoon cocoon = (FOM_Cocoon)unwrap(cocoon_);
        cocoon.handleContinuation(kontId, null);
    }

    public Object jsFunction_iterate(String expr) {
        return context.iterate(expr);
    }

    public FOM_WebContinuation jsFunction_makeWebContinuation(Object k, 
                                                              Object lastContinuation,
                                                              int ttl) 
        throws Exception {
        Continuation kont = (Continuation)unwrap(k);
        FOM_WebContinuation fom_wk = 
            (FOM_WebContinuation)unwrap(lastContinuation);
        FOM_Cocoon cocoon = getCocoon();
        return cocoon.makeWebContinuation(kont, fom_wk, ttl);
    }

    // unwrap Wrapper's and convert undefined to null
    private static Object unwrap(Object obj) {
        if (obj instanceof Wrapper) {
            obj = ((Wrapper)obj).unwrap();
        } else if (obj == Undefined.instance) {
            obj = null;
        }
        return obj;
    }

    public void jsFunction_removeForm() {
        FOM_Cocoon cocoon = getCocoon();

        Form.remove(cocoon.getObjectModel(), id);
        cocoon.getRequest().removeAttribute(this.id);
    }

    public void jsFunction_saveForm() {
        FOM_Cocoon cocoon = getCocoon();
        form.save(cocoon.getObjectModel(), "request");
    }

    public void jsFunction_populateForm() {
        FOM_Cocoon cocoon = getCocoon();
        form.populate(cocoon.getObjectModel());
    }

    public void jsFunction_validateForm(String view) {
        form.validate(view);
    }

    public void jsFunction_clearFormViolations() {
        form.clearViolations();
    }

}
