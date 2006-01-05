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
package org.apache.cocoon.generation;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.sitemap.impl.AbstractVirtualSitemapComponent;

import org.xml.sax.SAXException;

import java.io.IOException;

public class VirtualPipelineGenerator extends AbstractVirtualSitemapComponent
    implements Generator {

    protected String getTypeName() {
        return "generator";
    }

    /**
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {

        // Should use SourceResolver and context of the this
        // components' sitemap, not caller sitemap
        EnvironmentHelper.enterEnvironment(this.getVPCEnvironment());
        try {
            this.getPipeline().prepareInternal(this.getVPCEnvironment());
        } finally {
            EnvironmentHelper.leaveEnvironment();
        }

        // Should use SourceResolver of the this components' sitemap, not caller sitemap
        EnvironmentHelper.enterEnvironment(this.getMappedSourceEnvironment());
        try {
            this.getPipeline().process(this.getMappedSourceEnvironment(),
                                  EnvironmentHelper.createPopEnvironmentConsumer(this.xmlConsumer));
        } finally {
            EnvironmentHelper.leaveEnvironment();
        }
    }
 }
