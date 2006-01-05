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
package org.apache.cocoon.serialization;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.sitemap.impl.AbstractVirtualSitemapComponent;

import org.xml.sax.SAXException;

import java.io.OutputStream;

public class VirtualPipelineSerializer extends AbstractVirtualSitemapComponent
    implements Serializer {

    protected String getTypeName() {
        return "serializer";
    }

    /**
     * Set the <code>OutputStream</code>
     */
    public void setOutputStream(OutputStream out) {
        this.getMappedSourceEnvironment().setOutputStream(out);
    }

    /**
     * Get the mime-type of the output of this <code>Reader</code>
     */
    public String getMimeType() {
        return this.getPipeline().getMimeType();
    }

    /**
     * Test if the component wants to set the content length
     */
    public boolean shouldSetContentLength() {
        return this.getPipeline().shouldSetContentLength();
    }

    /**
     *  Process the SAX event. A new document is processed. The
     *  internal pipeline is prepared.
     *
     *  @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        // Should use SourceResolver and context of the this
        // components' sitemap, not caller sitemap
        try {
            EnvironmentHelper.enterEnvironment(this.getVPCEnvironment());
            this.getPipeline().prepareInternal(this.getVPCEnvironment());
        } catch (Exception e) {
            throw new SAXException("VirtualPipelineSerializer: couldn't create internal pipeline ", e);
        } finally {
            EnvironmentHelper.leaveEnvironment();
        }

        try {
            super.setConsumer(EnvironmentHelper
                              .createPushEnvironmentConsumer(this.getPipeline().getXMLConsumer(this.getMappedSourceEnvironment()),
                                                             this.getMappedSourceEnvironment()));
        } catch (ProcessingException e) {
            throw new SAXException("VirtualPipelineSerializer: couldn't get xml consumer from the pipeline ", e);
        }

        super.startDocument();
    }
 }
