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
 * @version CVS $Id: ProcessingInstruction.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public class ProcessingInstruction extends LocatedEvent {

    /** Our target. */
    private String target = null;

    /** Our data. */
    private String data = null;

    /**
     * Create a new <code>ProcessingInstruction</code> instance.
     *
     * @param target The processing instruction target.
     * @param data The data of this processing instruction.
     */
    public ProcessingInstruction(String target, String data) {
        this(null, target, data);
    }

    /**
     * Create a new <code>ProcessingInstruction</code> instance.
     *
     * @param locator The <code>Locator</code> instance where location
     *                information should be read from.
     * @param target The processing instruction target.
     * @param data The data of this processing instruction.
     */
    public ProcessingInstruction(Locator locator, String target, String data) {
        super(locator);
        if (target == null) {
            throw new TreeException(locator, "No target specified");
        }
        this.target = target;
        this.data = data;
    }

    /**
     * Process this event in the context of the specified <code>Runtime</code>.
     *
     * @param runtime The <code>Runtime</code> receiving events notifications.
     * @throws SAXException In case of error processing this event.
     */
    public void process(Runtime runtime, JXPathContext context)
    throws SAXException {
        runtime.processingInstruction(target, data);
    }
}
