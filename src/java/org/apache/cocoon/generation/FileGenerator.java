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

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

/**
 *
 * The <code>FileGenerator</code> is a class that reads XML from a source
 * and generates SAX Events.
 * The FileGenerator implements the <code>CacheableProcessingComponent</code> interface.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: FileGenerator.java,v 1.10 2004/03/08 14:02:45 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=Generator
 * @x-avalon.lifestyle type=pooled
 * @x-avalon.info name=file-generator
 */
public class FileGenerator extends ServiceableGenerator
implements CacheableProcessingComponent {

    /** The input source */
    protected Source inputSource;

    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        if (null != this.inputSource) {
            super.resolver.release(this.inputSource);
            this.inputSource = null;
        }
        super.recycle();
    }

    /**
     * Setup the file generator.
     * Try to get the last modification date of the source for caching.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, src, par);
        try {
            this.inputSource = super.resolver.resolveURI(src);
        } catch (SourceException se) {
            throw SourceUtil.handle("Error during resolving of '" + src + "'.", se);
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public java.io.Serializable getKey() {
        return this.inputSource.getURI();
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return this.inputSource.getValidity();
    }

    /**
     * Generate XML data.
     */
    public void generate()
        throws IOException, SAXException, ProcessingException {

        try {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Source " + super.source +
                                  " resolved to " + this.inputSource.getURI());
            }
            SourceUtil.parse(this.manager, this.inputSource, super.xmlConsumer);

        } catch (SAXException e) {
            final Exception cause = e.getException();
            if (cause != null) {
                if (cause instanceof ProcessingException) {
                    throw (ProcessingException)cause;
                }
                if (cause instanceof IOException) {
                    throw (IOException)cause;
                }
                if (cause instanceof SAXException) {
                    throw (SAXException)cause;
                }
                throw new ProcessingException("Could not read resource " +
                                              this.inputSource.getURI(), cause);
            }
            throw e;
        }
    }
}
