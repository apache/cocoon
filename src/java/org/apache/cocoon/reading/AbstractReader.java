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

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * A reader can be used to generate binary output for a request. This
 * abstract class helps in implementing a custom reader.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Id: AbstractReader.java,v 1.3 2004/03/12 15:14:19 cziegeler Exp $
 */
public abstract class AbstractReader
  extends AbstractLogEnabled
  implements Reader, Recyclable {

    /** The current <code>SourceResolver</code>. */
    protected SourceResolver resolver;
    /** The current <code>Map</code> of the object model. */
    protected Map objectModel;
    /** The current <code>Parameters</code>. */
    protected Parameters parameters;
    /** The source URI associated with the request or <b>null</b>. */
    protected String source;
    /** The <code>OutputStream</code> to write on. */
    protected OutputStream out;

    /**
     * Set the <code>SourceResolver</code> the object model <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        this.resolver=resolver;
        this.objectModel=objectModel;
        this.source=src;
        this.parameters=par;
    }

    /**
     * Set the <code>OutputStream</code>
     */
    public void setOutputStream(OutputStream out) {
        if ( out instanceof BufferedOutputStream 
             || out instanceof org.apache.cocoon.util.BufferedOutputStream ) {
            this.out = out;
        } else {
            this.out = new BufferedOutputStream(out, 1536);
        }
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
        this.resolver = null;
        this.source = null;
        this.parameters = null;
        this.objectModel = null;
    }

    /**
     * Test if the component wants to set the content length
     */
    public boolean shouldSetContentLength() {
        return false;
    }

}
