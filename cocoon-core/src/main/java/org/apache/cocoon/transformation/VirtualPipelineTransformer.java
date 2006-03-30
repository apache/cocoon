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
package org.apache.cocoon.transformation;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.sitemap.impl.AbstractVirtualSitemapComponent;
import org.apache.cocoon.xml.XMLConsumer;

import org.xml.sax.SAXException;

public class VirtualPipelineTransformer extends AbstractVirtualSitemapComponent
    implements Transformer {

    /** Exception that might occur during setConsumer */
    private SAXException exceptionDuringSetConsumer;

    protected String getTypeName() {
        return "transformer";
    }

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     * And set up the internal pipeline for processing.
     */
    public void setConsumer(XMLConsumer consumer) {
        // Should use SourceResolver and context of the this
        // components' sitemap, not caller sitemap
        try {
            EnvironmentHelper.enterEnvironment(this.getVPCEnvironment());
            this.getPipeline().prepareInternal(this.getVPCEnvironment());
        } catch (Exception e) {
            this.exceptionDuringSetConsumer =
                new SAXException("VirtualPipelineTransformer: couldn't create internal pipeline ", e);
            return;
        } finally {
            EnvironmentHelper.leaveEnvironment();
        }

        try {
            // Remove the current environment before calling next pipeline component
            XMLConsumer outConsumer =
                EnvironmentHelper.createPopEnvironmentConsumer(consumer);
            // Call the internal VPC transformer pipeline
            XMLConsumer transformConsumer =
                this.getPipeline().getXMLConsumer(this.getMappedSourceEnvironment(),
                                                  outConsumer);
            // Add the current environment
            XMLConsumer inConsumer =
                EnvironmentHelper.createPushEnvironmentConsumer(transformConsumer,
                                                                this.getMappedSourceEnvironment());
            super.setConsumer(inConsumer);
        } catch (ProcessingException e) {
            this.exceptionDuringSetConsumer =
                new SAXException("VirtualPipelineSerializer: couldn't get xml consumer from the pipeline ", e);
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        // did an exception occur during setConsumer?
        // if so, throw it here
        if ( this.exceptionDuringSetConsumer != null ) {
            throw this.exceptionDuringSetConsumer;
        }
        super.startDocument();
    }
 }
