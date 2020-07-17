/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.faces.context;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the Java Server Faces Context
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class FacesContextImpl extends FacesContext {
    private ExternalContextImpl extContext;

    private boolean released;
    private boolean renderResponse;
    private boolean responseComplete;

    private Application application;
    private UIViewRoot viewRoot;
    private Map messages;

    private ResponseStream responseStream;
    private ResponseWriter responseWriter;


    FacesContextImpl(ExternalContextImpl extContext) {
        this.extContext = extContext;
        FacesContext.setCurrentInstance(this);
    }

    private void checkReleased() {
        if (released) {
            throw new IllegalStateException("Context is released.");
        }
    }

    public Application getApplication() {
        checkReleased();

        if (application == null) {
            ApplicationFactory aFactory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
            this.application = aFactory.getApplication();
        }

        return this.application;
    }

    public Iterator getClientIdsWithMessages() {
        checkReleased();

        if (this.messages == null) {
            return Collections.EMPTY_LIST.iterator();
        } else {
            return this.messages.keySet().iterator();
        }
    }

    public ExternalContext getExternalContext() {
        checkReleased();
        return this.extContext;
    }

    public Severity getMaximumSeverity() {
        throw new UnsupportedOperationException();
    }

    public Iterator getMessages() {
        checkReleased();
        if (this.messages == null) {
            return Collections.EMPTY_LIST.iterator();
        }

        List messages = new ArrayList();
        for (Iterator i = this.messages.values().iterator(); i.hasNext();) {
            final List list = (List) i.next();
            messages.addAll(list);
        }

        if (messages.size() > 0) {
            return messages.iterator();
        }

        return Collections.EMPTY_LIST.iterator();
    }

    public Iterator getMessages(String clientID) {
        checkReleased();
        if (this.messages != null) {
            final List list = (List) this.messages.get(clientID);
            if (list != null) {
                return list.iterator();
            }
        }

        return Collections.EMPTY_LIST.iterator();
    }

    public RenderKit getRenderKit() {
        checkReleased();

        UIViewRoot viewRoot = getViewRoot();
        if (viewRoot == null) {
            return null;
        }

        String renderKitId = viewRoot.getRenderKitId();
        if (renderKitId == null) {
            return null;
        } else {
            RenderKitFactory rkFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            return rkFactory.getRenderKit(this, renderKitId);
        }
    }

    public boolean getRenderResponse() {
        checkReleased();
        return this.renderResponse;
    }

    public boolean getResponseComplete() {
        checkReleased();
        return this.responseComplete;
    }

    public ResponseStream getResponseStream() {
        checkReleased();
        return this.responseStream;
    }

    public void setResponseStream(ResponseStream responseStream) {
        checkReleased();
        if (responseStream == null) {
            throw new NullPointerException("ResponseStream can't be null.");
        }

        this.responseStream = responseStream;
    }

    public ResponseWriter getResponseWriter() {
        checkReleased();
        return this.responseWriter;
    }

    public void setResponseWriter(ResponseWriter responseWriter) {
        checkReleased();
        if (responseWriter == null) {
            throw new NullPointerException("ResponseWriter can't be null.");
        }

        this.responseWriter = responseWriter;
    }

    public UIViewRoot getViewRoot() {
        checkReleased();
        return this.viewRoot;
    }

    public void setViewRoot(UIViewRoot viewRoot) {
        checkReleased();
        this.viewRoot = viewRoot;
    }

    public void addMessage(String clientID, FacesMessage message) {
        checkReleased();
        if (message == null) {
            throw new NullPointerException("Message can't be null");
        }

        if (messages == null) {
            messages = new HashMap();
        }

        List list = (List) messages.get(clientID);
        if (list == null) {
            list = new ArrayList();
            messages.put(clientID, list);
        }

        list.add(message);
    }

    public void release() {
        this.released = true;
        this.extContext = null;

        FacesContext.setCurrentInstance(null);

        this.application = null;
        this.viewRoot = null;
        this.messages = null;

        this.responseStream = null;
        this.responseWriter = null;
    }

    public void renderResponse() {
        checkReleased();
        this.renderResponse = true;
    }

    public void responseComplete() {
        checkReleased();
        this.responseComplete = true;
    }
}
