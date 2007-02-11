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
package org.apache.garbage.tree;

import org.apache.commons.jxpath.JXPathContext;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: DocType.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public class DocType extends LocatedEvent {

    /** Our root element name. */
    private String name = null;

    /** Our public id. */
    private String dtd_public_id = null;

    /** Our system id. */
    private String dtd_system_id = null;

    /**
     * Create a new <code>DocType</code> instance.
     *
     * @param name The name of the root element.
     * @param pub The public identifier.
     * @param sys The system identifier.
     */
    public DocType(String name, String pub, String sys) {
        this(null, name, pub, sys);
    }

    /**
     * Create a new <code>DocType</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param name The name of the root element.
     * @param pub The public identifier.
     * @param sys The system identifier.
     */
    public DocType(Locator locator, String name, String pub, String sys) {
        super(locator);

        if (name == null) {
            throw new TreeException(locator, "No name specified");
        }

        if ((pub != null) && (sys == null)) {
            throw new TreeException(locator, "No system identifier specified");
        }

        this.name = name;
        this.dtd_public_id = pub;
        this.dtd_system_id = sys;
    }

    /**
     * Process this event in the context of the specified <code>Runtime</code>.
     *
     * @param runtime The <code>Runtime</code> receiving events notifications.
     * @throws SAXException In case of error processing this event.
     */
    public void process(Runtime runtime, JXPathContext context)
    throws SAXException {
        runtime.doctype(name, dtd_public_id, dtd_system_id);
    }
}
