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
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.AbstractXMLProducer;
import org.xml.sax.SAXException;

/**
 * An abstract class that can be used to implement an own generator.
 * If you need other components, use the {@link ServiceableGenerator}
 * instead.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @version CVS $Id: AbstractGenerator.java,v 1.7 2004/03/08 14:02:43 cziegeler Exp $
 */
public abstract class AbstractGenerator
    extends AbstractXMLProducer
    implements Generator {

    /** The current <code>SourceResolver</code>. */
    protected SourceResolver resolver;
    /** The current <code>Map</code> objectModel. */
    protected Map objectModel;
    /** The current <code>Parameters</code>. */
    protected Parameters parameters;
    /** The source URI associated with the request or <b>null</b>. */
    protected String source;

    /**
     * Set the <code>SourceResolver</code>, object model <code>Map</code>,
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
     * Recycle the generator by removing references
     */
    public void recycle() {
        super.recycle();
        this.resolver = null;
        this.objectModel = null;
        this.source = null;
        this.parameters = null;
    }

}
