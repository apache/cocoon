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
package org.apache.cocoon.components.flow.java;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.util.PipelineUtil;
import org.apache.cocoon.environment.Request;
import org.apache.excalibur.source.SourceUtil;

import java.io.OutputStream;

/**
 * Abstract class to add basic methods for flow handling.
 *
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: AbstractCocoonFlow.java,v 1.2 2004/03/31 20:32:45 vgritsenko Exp $
 */
public abstract class AbstractCocoonFlow implements Continuable {

    public Logger getLogger() {
        return getContext().getLogger();
    }

    private ContinuationContext getContext() {
        return (ContinuationContext) Continuation.currentContinuation().getContext();
    }

    public void sendPageAndWait(String uri) {
        sendPageAndWait(uri, new VarMap());
    }

    public void sendPageAndWait(String uri, Object bizdata) {

        System.out.println("send page and wait '" + uri + "'");
        if (Continuation.currentContinuation()!=null) {
            ContinuationContext context = getContext();

            FlowHelper.setContextObject(ContextHelper.getObjectModel(context.getAvalonContext()), bizdata);

            if (SourceUtil.indexOfSchemeColon(uri) == -1) {
                uri = "cocoon:/" + uri;
                if (getContext().getRedirector().hasRedirected()) {
                    throw new IllegalStateException("Pipeline has already been processed for this request");
                }
                try {
                    context.getRedirector().redirect(false, uri);
                } catch (Exception e) {
                    throw new CascadingRuntimeException("Cannot redirect to '"+uri+"'", e);
                }
            } else {
                throw new IllegalArgumentException("uri is not allowed to contain a scheme (cocoon:/ is always automatically used)");
            }

            Continuation.suspend();
        }
    }

    public void sendPage(String uri) {
        sendPage(uri, new VarMap());
    }

    public void sendPage(String uri, Object bizdata) {

        System.out.println("send page '" + uri + "'");
        ContinuationContext context = getContext();

        FlowHelper.setContextObject(ContextHelper.getObjectModel(context.getAvalonContext()), bizdata);

        if (SourceUtil.indexOfSchemeColon(uri) == -1) {
            uri = "cocoon:/" + uri;
            if (getContext().getRedirector().hasRedirected()) {
                throw new IllegalStateException("Pipeline has already been processed for this request");
            }
            try {
                context.getRedirector().redirect(false, uri);
            } catch (Exception e) {
                throw new CascadingRuntimeException("Cannot redirect to '"+uri+"'", e);
            }
        } else {
            throw new IllegalArgumentException("uri is not allowed to contain a scheme (cocoon:/ is always automatically used)");
        }
    }

    public Request getRequest() {
        return ContextHelper.getRequest(getContext().getAvalonContext());
    }

    public void processPipelineTo(String uri, Object bizdata, OutputStream out) {

        ContinuationContext context = getContext();

        try {
            PipelineUtil pipeUtil = new PipelineUtil();
            pipeUtil.contextualize(context.getAvalonContext());
            pipeUtil.service(context.getServiceManager());
            pipeUtil.processToStream(uri, bizdata, out);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Cannot process pipeline to '"+uri+"'", e);
        }
    }

    public void redirectTo(String uri) {
        try {
            getContext().getRedirector().redirect(false, uri);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Cannot redirect to '"+uri+"'", e);
        }
    }

    public void sendStatus(int sc) {
        getContext().getRedirector().sendStatus(sc);
    }

    /**
     * Access components.
     */
    public Object getComponent(String id) {
        try {
            return getContext().getServiceManager().lookup(id);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Cannot lookup component '"+id+"'", e);
        }
    }

    /**
     * Release pooled components.
     *
     * @param component a component
     */
    public void releaseComponent( Object component ) {
        if (component != null) {
            getContext().getServiceManager().release(component);
        }
    }
}
