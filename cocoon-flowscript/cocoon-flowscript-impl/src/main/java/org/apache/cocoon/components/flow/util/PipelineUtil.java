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
package org.apache.cocoon.components.flow.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Utility class to process a pipeline to various destinations.
 * This class must be setup from the flowscript before being used. This means that instances must
 * be created with <code>cocoon.createObject(Packages.org.apache.cocoon.components.flow.util.PipelineUtil);
 *
 * @version $Id$
 */
public class PipelineUtil implements Contextualizable, Serviceable, Disposable {

    private Context context;
    private ServiceManager manager;
    private SourceResolver resolver;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.resolver);
            this.manager = null;
            this.resolver = null;
        }
    }

    public void contextualize(Context context) throws ContextException {
        this.context = context;

    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
    }

    /**
     * Check that this object has been correctly set up.
     *
     * @throws IllegalStateException if not already set up.
     */
    private void checkSetup() {
        if (this.manager == null) {
            throw new IllegalStateException("Instances of " + getClass().getName() +
                                            " must be setup using either cocoon.createObject() or cocoon.setupObject().");
        }
    }

    /**
     * Process a pipeline to a stream.
     *
     * @param uri the pipeline URI
     * @param viewData the view data object
     * @param output the stream where pipeline result is output. Note: this stream is not closed.
     * @throws IOException
     */
    public void processToStream(String uri, Object viewData, OutputStream output)
    throws IOException {
        checkSetup();

        Map objectModel = ContextHelper.getObjectModel(this.context);

        // Keep the previous view data, if any (is it really necessary?), and set the new one
        Object oldViewData = FlowHelper.getContextObject(objectModel);
        FlowHelper.setContextObject(objectModel, PipelineUtil.unwrap(viewData));

        Source src = null;
        InputStream input = null;
        try {
            src = this.resolver.resolveURI("cocoon:/" + uri);
            input = src.getInputStream();
            IOUtils.copy(input, output);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {}
            }

            // Restore the previous view data
            FlowHelper.setContextObject(objectModel, oldViewData);

            if (src != null) {
                this.resolver.release(src);
            }
        }
    }

    /**
     * Process a pipeline to a SAX <code>ContentHandler</code>
     *
     * @param uri the pipeline URI
     * @param viewData the view data object
     * @param handler where the pipeline should be streamed to.
     */
    public void processToSAX(String uri, Object viewData, ContentHandler handler)
    throws SAXException, IOException, ProcessingException {
        checkSetup();

        Map objectModel = ContextHelper.getObjectModel(this.context);
        Object oldViewData = FlowHelper.getContextObject(objectModel);
        FlowHelper.setContextObject(objectModel, PipelineUtil.unwrap(viewData));

        Source src = null;
        try {
            src = this.resolver.resolveURI("cocoon:/" + uri);
            SourceUtil.toSAX(src, handler);
        } finally {
            FlowHelper.setContextObject(objectModel, oldViewData);
            if (src != null) {
                this.resolver.release(src);
            }
        }
    }

    /**
     * Process a pipeline and gets is result as a DOM <code>Document</code>
     *
     * @param uri the pipeline URI
     * @param viewData the view data object
     * @return the document
     */
    public Document processToDOM(String uri, Object viewData) throws ProcessingException, SAXException, IOException  {
        checkSetup();

        Map objectModel = ContextHelper.getObjectModel(this.context);
        Object oldViewData = FlowHelper.getContextObject(objectModel);
        FlowHelper.setContextObject(objectModel, PipelineUtil.unwrap(viewData));

        Source src = null;

        try {
            src = this.resolver.resolveURI("cocoon:/" + uri);
            return SourceUtil.toDOM(src);
        } finally {
            FlowHelper.setContextObject(objectModel, oldViewData);
            if (src != null) {
                this.resolver.release(src);
            }
        }
    }

    /**
     * Unwrap a Rhino object (getting the raw java object) and convert undefined to null
     */
    public static Object unwrap(Object obj) {
        if (obj instanceof Wrapper) {
            obj = ((Wrapper)obj).unwrap();
        } else if (obj == Undefined.instance) {
            obj = null;
        }
        return obj;
    }
}
