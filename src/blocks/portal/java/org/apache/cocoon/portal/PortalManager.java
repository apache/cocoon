/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This is the main component that has currently two actions:
 * Invoke the request processing using the {@link #process()} method,
 * this starts the event handling for the current request (evaluating
 * request parameters etc.)
 * The section method {@link #showPortal(ContentHandler, Parameters)}
 * starts rendering the portal.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: PortalManager.java,v 1.3 2004/03/05 13:02:08 bdelacretaz Exp $
 */
public interface PortalManager extends Component {

    String ROLE = PortalManager.class.getName();
    
    void process()
    throws ProcessingException;
    
    void showPortal(ContentHandler ch,
                     Parameters     parameters)
    throws SAXException;
    
}
