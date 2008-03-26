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

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.javascript.JavaScriptFlowHelper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.apache.cocoon.spring.configurator.WebAppContextUtils;
import org.apache.commons.io.IOUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.springframework.web.context.WebApplicationContext;
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
public class PipelineUtil {

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
        final Map objectModel = getObjectModel();
        final ObjectModel newObjectModel = getNewObjectModel();
        final SourceResolver resolver = getSourceResolver();

        // Keep the previous view data, if any (is it really necessary?), and set the new one
        Object oldViewData = FlowHelper.getContextObject(objectModel);
        FlowHelper.setContextObject(objectModel, newObjectModel, JavaScriptFlowHelper.unwrap(viewData));

        Source src = null;
        InputStream input = null;
        try {
            src = resolver.resolveURI("cocoon:/" + uri);
            input = src.getInputStream();
            IOUtils.copy(input, output);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {}
            }

            // Restore the previous view data
            FlowHelper.setContextObject(objectModel, newObjectModel, oldViewData);
            resolver.release(src);
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
        final Map objectModel = getObjectModel();
        final ObjectModel newObjectModel = getNewObjectModel();
        final SourceResolver resolver = getSourceResolver();

        Object oldViewData = FlowHelper.getContextObject(objectModel);
        FlowHelper.setContextObject(objectModel, newObjectModel, JavaScriptFlowHelper.unwrap(viewData));

        Source src = null;
        try {
            src = resolver.resolveURI("cocoon:/" + uri);
            SourceUtil.toSAX(src, handler);
        } finally {
            FlowHelper.setContextObject(objectModel, newObjectModel, oldViewData);
            resolver.release(src);
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
        final Map objectModel = getObjectModel();
        final ObjectModel newObjectModel = getNewObjectModel();
        final SourceResolver resolver = getSourceResolver();
        Object oldViewData = FlowHelper.getContextObject(objectModel);
        FlowHelper.setContextObject(objectModel, newObjectModel, JavaScriptFlowHelper.unwrap(viewData));

        Source src = null;

        try {
            src = resolver.resolveURI("cocoon:/" + uri);
            return SourceUtil.toDOM(src);
        } finally {
            FlowHelper.setContextObject(objectModel, newObjectModel, oldViewData);
            resolver.release(src);
        }
    }

    /**
     * Helper method to get the current source resolver.
     */
    protected static SourceResolver getSourceResolver() {
        final WebApplicationContext webAppContext = WebAppContextUtils.getCurrentWebApplicationContext();
        return (SourceResolver)webAppContext.getBean(SourceResolver.ROLE);        
    }

    /**
     * Helper method to get the current object model.
     */
    protected static Map getObjectModel() {
        final WebApplicationContext webAppContext = WebAppContextUtils.getCurrentWebApplicationContext();
        final ProcessInfoProvider infoProvider =
            (ProcessInfoProvider)webAppContext.getBean(ProcessInfoProvider.ROLE);
        return infoProvider.getObjectModel();
    }
    
    protected static ObjectModel getNewObjectModel() {
        final WebApplicationContext webAppContext = WebAppContextUtils.getCurrentWebApplicationContext();
        return (ObjectModel)webAppContext.getBean(ObjectModel.ROLE);
    }
}
