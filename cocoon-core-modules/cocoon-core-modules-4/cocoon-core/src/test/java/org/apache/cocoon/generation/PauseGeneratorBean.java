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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.SAXException;

/**
 * This generator extends the usual FileGenerator with a pause parameter.
 * During generation of the content, this generator pauses for the given
 * amount of time.
 * This is very usefull for caching tests.
 * 
 * @version $Id$
 * @since   2.2
 */
public class PauseGeneratorBean 
    extends FileGeneratorBean {

    protected long secs;

    /**
     * @see org.apache.cocoon.generation.FileGenerator#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        this.secs = par.getParameterAsLong("pause", 60);
    }

    /**
     * @see org.apache.cocoon.generation.FileGenerator#generate()
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Waiting for " + secs + " secs.");
        }
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException ie) {
            // ignore
        }
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Finished waiting.");
        }
        super.generate();
    }


}
