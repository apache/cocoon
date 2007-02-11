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
package org.apache.cocoon.portal.coplet.adapter;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This is the "portlet" implementation.
 * A coplet adapter is the interface between the portal engine and
 * the implementation of a coplet.
 * Usually there is only one instance of an adapter (= singleton). 
 * Whenever an instance of this coplet is rendered, the
 * adapter is invoked to render the coplet.
 * 
 * The behaviour of the adapter can be controlled by a set of
 * parameters. In general, the coplet base data defines the default
 * for all coplets, but the value can be overriden by the coplet
 * data. The coplet base data stores the information in the coplet
 * config map whereas the coplet data stores them in the attributes.
 * Apart from that the keys and the data types are the same.
 * 
 * Configuration:
 * 
 * buffer - A boolean value (default is false) that defines if the
 *          xml data stream from the coplet is buffered. If the stream
 *          is not buffered and an exception occurs then the whole
 *          portal will be rendered invalid.
 * timeout - An integer value (default is endless) that defines the
 *           maximum time (in seconds) the coplet has to deliver it's content.
 *           If the timeout is reached the content is assumed as not
 *           gettable. If you set a timeout, the content is automatically
 *           buffered.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: CopletAdapter.java,v 1.7 2004/03/05 13:02:10 bdelacretaz Exp $
 */
public interface CopletAdapter 
    extends Component {

    String ROLE = CopletAdapter.class.getName();
    
    /**
     * Initialize the coplet
     * This method is called immediately after a new instance is created.
     * For each coplet, this method is only invoked once.
     * @param coplet The coplet
     */
    void init(CopletInstanceData coplet);
    
    /**
     * Destroy the coplet
     * This method is invoked when a coplet instance will be destroyed
     * For each coplet, this method is only invoked once.
     * @param coplet
     */
    void destroy(CopletInstanceData coplet);

    /**
     * Stream the content of the coplet
     */
    void toSAX(CopletInstanceData coplet, ContentHandler contentHandler)
    throws SAXException;
    
    /**
     * User logs in to a coplet
     * This method is invoked when a user logs in for each coplet instance
     * of the user
     */
    void login(CopletInstanceData coplet);
        
    /**
     * User logs out from a coplet
     * This method is invoked when a user logs out for each coplet instance
     * of this user.
     */
    void logout(CopletInstanceData coplet);
}
