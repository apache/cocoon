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
package org.apache.cocoon.samples;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.validity.EventValidity;
import org.apache.cocoon.caching.validity.NamedEvent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.template.JXTemplateGenerator;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

/**
 * This is a sample generator to demonstrate the event aware caching. We simply extend the JXTG.
 * 
 * @version $Id$
 */
public class EventAwareGenerator extends JXTemplateGenerator {

    private Map oldObjectModel;

    /**
     * Generate the unique key for the cache.
     * 
     * This key must be unique inside the space of this XSP page, it is used to find the page contents in the cache (if
     * getValidity says that the contents are still valid).
     * 
     * This method will be invoked before the getValidity() method.
     * 
     * @return The generated key or null if the component is currently not cacheable.
     */
    public Serializable getKey() {
        final Request request = ObjectModelHelper.getRequest(oldObjectModel);
        // for our test, pages having the same value of "pageKey" will share
        // the same cache location
        String key = request.getParameter("pageKey");
        return ((key == null || "".equals(key)) ? "one" : key);
    }

    /**
     * Generate the validity object, tells the cache how long to keep contents having this key around. In this case, it
     * will be until an Event is retrieved matching the NamedEvent created below.
     * 
     * Before this method can be invoked the getKey() method will be invoked.
     * 
     * @return The generated validity object or null if the component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        final Request request = ObjectModelHelper.getRequest(this.oldObjectModel);
        String key = request.getParameter("pageKey");
        return new EventValidity(new NamedEvent((key == null || "".equals(key)) ? "one" : key));
    }
    
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
            throws ProcessingException, SAXException, IOException {
        this.oldObjectModel = objectModel;
        super.setup(resolver, objectModel, src, parameters);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate() throws IOException, SAXException, ProcessingException {
        super.generate();
        // slowdown page generation.
        long DELAY_SECS = this.parameters.getParameterAsLong("DELAY_SECS", 2);
        try {
            Thread.sleep(DELAY_SECS * 1000L);
        } catch (InterruptedException ie) {
            // Not much that can be done...
        }
    }
}
