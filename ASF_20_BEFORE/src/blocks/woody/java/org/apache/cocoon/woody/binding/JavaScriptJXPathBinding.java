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
package org.apache.cocoon.woody.binding;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.cocoon.woody.util.JavaScriptHelper;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.mozilla.javascript.Script;

/**
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptJXPathBinding.java,v 1.8 2004/02/27 13:32:18 antonio Exp $
 */
public class JavaScriptJXPathBinding extends JXPathBindingBase {

    private final String id;
    private final String path;
    private final Script loadScript;
    private final Script saveScript;

    public JavaScriptJXPathBinding(
            JXPathBindingBuilderBase.CommonAttributes commonAtts, String id,
            String path, Script loadScript, Script saveScript) {
        super(commonAtts);
        this.id = id;
        this.path = path;
        this.loadScript = loadScript;
        this.saveScript = saveScript;
    }

    public void doLoad(Widget frmModel, JXPathContext jctx) {
        if (this.loadScript != null) {
            Widget widget = frmModel.getWidget(this.id);
    
            // Move to widget context
            Pointer pointer = jctx.getPointer(this.path);
    
            // FIXME: remove this ugly hack and get the request from the
            // Avalon context once binding builder are real components
            Request request = ObjectModelHelper.getRequest(CocoonComponentManager.getCurrentEnvironment().getObjectModel());

            try {
                Map values = new HashMap(3);
                values.put("widget", widget);
                values.put("jxpathPointer", pointer);
                if (pointer.getNode() != null) {
                    values.put("jxpathContext", jctx.getRelativeContext(pointer));
                }

                JavaScriptHelper.execScript(this.loadScript, values, request);
    
            } catch(RuntimeException re) {
                // rethrow
                throw re;
            } catch(Exception e) {
                throw new CascadingRuntimeException("Error invoking JavaScript event handler", e);
            }
        } else {
            if (this.getLogger().isInfoEnabled()) {
                this.getLogger().info("[Javascript Binding] - loadForm: No javascript code avaliable. Widget id=" + this.getId());
            }
        }
    }

    public void doSave(Widget frmModel, JXPathContext jctx) throws BindingException {
        if (this.saveScript != null) {
            Widget widget = frmModel.getWidget(this.id);

            // Move to widget context and create the path if needed
            Pointer pointer = jctx.createPath(this.path);
            JXPathContext widgetCtx = jctx.getRelativeContext(pointer);
            try {
                // FIXME: remove this ugly hack and get the request from the Avalon context once
                // binding builder are real components
                Request request = ObjectModelHelper.getRequest(CocoonComponentManager.getCurrentEnvironment().getObjectModel());

                Map values = new HashMap();
                values.put("widget", widget);
                values.put("jxpathContext", widgetCtx);
                values.put("jxpathPointer", pointer);

                JavaScriptHelper.execScript(this.saveScript, values, request);

            } catch(RuntimeException re) {
                // rethrow
                throw re;
            } catch(Exception e) {
                throw new CascadingRuntimeException("Error invoking JavaScript event handler", e);
            }
        } else {
            if (this.getLogger().isInfoEnabled()) {
                this.getLogger().info("[Javascript Binding] - saveForm: No javascript code avaliable. <wb:javascript id=" + this.getId() + ">");
            }
        }
    }
}
