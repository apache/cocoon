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
package org.apache.cocoon.transformation.helpers;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Can send recorded events and be cloned.
 *
 * @author <a href="mailto:mattam@netcourrier.com">Matthieu Sozeau</a>
 * @version CVS $Id: EventRecorder.java,v 1.2 2004/03/05 13:03:00 bdelacretaz Exp $
 */

public interface EventRecorder { 
    public void send(ContentHandler handler) 
	throws SAXException;

    public Object clone();
}
