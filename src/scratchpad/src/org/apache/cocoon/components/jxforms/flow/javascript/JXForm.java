/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-

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
package org.apache.cocoon.components.jxforms.flow.javascript;
import org.apache.cocoon.components.jxforms.validation.*;
import org.apache.cocoon.components.jxforms.xmlform.*;
import org.mozilla.javascript.*;
import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon;
import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon.FOM_WebContinuation;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.source.Source;
import org.xml.sax.InputSource;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;

public class JXForm extends ScriptableObject {

    FOM_Cocoon cocoon;
    Object model;
    Form form;
    JXPathContext context;
    String id;
    String validatorNamespace;
    String validatorDocument;
    String scope;
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
        String validatorNS;
        String validatorDoc;
        String scope;
        id = org.mozilla.javascript.Context.toString(args[0]);
        validatorNS = args[1] == null ? null : org.mozilla.javascript.Context.toString(args[1]);
        validatorDoc = args[2] == null ? null : org.mozilla.javascript.Context.toString(args[2]);
        scope = org.mozilla.javascript.Context.toString(args[3]);
        JXForm result = new JXForm();
        result.id = id;
        result.validatorNamespace = validatorNS;
        result.validatorDocument = validatorDoc;
        result.scope = scope;
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
            SourceResolver resolver = 
                getCocoon().getEnvironment();
            Source schemaSrc = resolver.resolveURI(validatorDocument);
            InputSource is = SourceUtil.getInputSource(schemaSrc);
            SchemaFactory schf = SchemaFactory.lookup(validatorNamespace);
            Schema sch = schf.compileSchema(is);
            form.setValidator(sch.newValidator());
        }
    }

    public String jsFunction_getSubmitId() {
        return submitId;
    }

    public void jsSet_submitId(String value) {
        submitId = value;
    }

    public String jsGet_submitId() {
        return submitId;
    }

    public void jsFunction_forwardTo(String uri,
                                     Object bizData,
                                     Object continuation) 
        throws Exception {
        FOM_Cocoon cocoon = getCocoon();
        FOM_WebContinuation fom_wk = 
            (FOM_WebContinuation)unwrap(continuation);
        String redUri = "cocoon://" + 
            cocoon.getEnvironment().getURIPrefix() + uri;
        cocoon.getInterpreter().forwardTo(getTopLevelScope(cocoon),
                                          cocoon,
                                          redUri, 
                                          unwrap(bizData),
                                          fom_wk == null ? null: fom_wk.getWebContinuation(),
                                          cocoon.getEnvironment());
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
        cocoon.getInterpreter().handleContinuation(kontId, null, 
                                                   cocoon.getEnvironment());
    }

    public Object jsFunction_iterate(String expr) {
        return context.iterate(expr);
    }

    public FOM_WebContinuation jsFunction_makeWebContinuation(Object k, 
                                                              Object lastContinuation,
                                                              int ttl) 
        throws Exception {
        FOM_Cocoon cocoon = getCocoon();
        WebContinuation wk;
        ContinuationsManager contMgr;
        contMgr = (ContinuationsManager)
            cocoon.getComponentManager().lookup(ContinuationsManager.ROLE);
        FOM_WebContinuation fom_wk = 
            (FOM_WebContinuation)unwrap(lastContinuation);
        wk = contMgr.createWebContinuation(unwrap(k),
                                           (WebContinuation)(fom_wk == null ? null : fom_wk.getWebContinuation()),
                                           ttl);
        return cocoon.makeWebContinuation(wk);
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
        Form.remove(cocoon.getEnvironment().getObjectModel(), id);
        cocoon.getRequest().removeAttribute(this.id);
    }

    public void jsFunction_saveForm() {
        FOM_Cocoon cocoon = getCocoon();
        form.save(cocoon.getEnvironment().getObjectModel(), "request");
    }

    public void jsFunction_populateForm() {
        FOM_Cocoon cocoon = getCocoon();
        form.populate(cocoon.getEnvironment().getObjectModel());
    }

    public void jsFunction_validateForm(String view) {
        form.validate(view);
    }

    public void jsFunction_clearFormViolations() {
        form.clearViolations();
    }

}
