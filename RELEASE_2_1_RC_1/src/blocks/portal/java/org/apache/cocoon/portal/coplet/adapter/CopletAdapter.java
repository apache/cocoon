/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: CopletAdapter.java,v 1.6 2003/05/28 13:47:29 cziegeler Exp $
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
