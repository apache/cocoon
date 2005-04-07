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
package org.apache.cocoon.reading;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.sitemap.impl.AbstractVirtualSitemapComponent;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;


public class VirtualPipelineReader extends AbstractVirtualSitemapComponent
    implements Reader {

    /** The <code>OutputStream</code> to write on. */
    protected OutputStream out;

    protected String getTypeName() {
        return "reader";
    }

    /**
     * Set the <code>OutputStream</code>
     */
    // The output stream from
    // EnvironmentHelper.getCurrentEnvironment() is used instead. Is
    // it always the same?
    public void setOutputStream(OutputStream out) {
	this.out = out;
    }

    /**
     * Get the mime-type of the output of this <code>Reader</code>
     * This default implementation returns null to indicate that the
     * mime-type specified in the sitemap is to be used
     */
    public String getMimeType() {
        return null;
    }

    /**
     * @return the time the read source was last modified or 0 if it is not
     *         possible to detect
     */
    public long getLastModified() {
        return 0;
    }

    /**
     * Recycle the component
     */
    public void recycle() {
        this.out = null;
    }

    /**
     * Test if the component wants to set the content length
     */
    public boolean shouldSetContentLength() {
        return false;
    }

    public void generate()
    throws IOException, SAXException, ProcessingException {

        // Should use SourceResolver of the this components' sitemap, not caller sitemap
        // Have to switch to another environment...
        Environment env = EnvironmentHelper.getCurrentEnvironment();
        String oldPrefix = env.getURIPrefix();
        String oldURI    = env.getURI();

        // save callers resolved sources if there are any
        Map oldSourceMap = (Map)env.getAttribute(this.sourceMapName);
        // place for resolved sources
        env.setAttribute(this.sourceMapName, this.sourceMap);

        try {
            try {
                String uri = (String) this.context.get(Constants.CONTEXT_ENV_URI);
                String prefix = (String) this.context.get(Constants.CONTEXT_ENV_PREFIX);
                env.setURI(prefix, uri);
                
                this.pipeline.prepareInternal(env);
            } catch (Exception e) {
                throw new ProcessingException("Oops", e);
            } finally {
                // Restore context
                env.setURI(oldPrefix, oldURI);
            }

            this.pipeline.process(env);

        } finally {
            // restore sourceMap
            if (oldSourceMap != null)
                env.setAttribute(this.sourceMapName, oldSourceMap);
            else
                env.removeAttribute(this.sourceMapName);
        }
    }
 }
