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
package org.apache.cocoon.samples.parentcm;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

/**
 * Generator for the parent component manager sample. The generator outputs
 * a single tag <code>&lt;time&gt;<i>current time</i>&lt;/time&gt;</code>.
 * Where <code><i>current time</i></code> is the current time as obtained from the
 * <code>Time</code> component.
 *
 * @author <a href="mailto:leo.sutic@inspireinfrastructure.com">Leo Sutic</a>
 * @version $Id$
 */
public class Generator extends ServiceableGenerator {

    /**
     * Current time.
     */
    private Date time;

    /**
     * Looks up a <code>Time</code> component and obtains the current time.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {

        Time timeGiver = null;
        try {
            timeGiver = (Time) this.manager.lookup(Time.ROLE);
            this.time = timeGiver.getTime ();
        } catch (ServiceException ce) {
            throw new ProcessingException ("Could not obtain current time.", ce);
        } finally {
            manager.release(timeGiver);
        }
    }

    /**
     * Generate XML data.
     */
    public void generate()
    throws SAXException, ProcessingException {
        contentHandler.startDocument();
        contentHandler.startElement("", "time", "time", XMLUtils.EMPTY_ATTRIBUTES);

        char[] text = this.time.toString().toCharArray();
        contentHandler.characters(text, 0, text.length);

        contentHandler.endElement("", "time", "time");
        contentHandler.endDocument();
    }

    /**
     * Prepare this object for another cycle.
     */
    public void recycle () {
        this.time = null;
    }
}


