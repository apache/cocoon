/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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

*/
package org.apache.cocoon.portal.pluto.om;

import java.util.HashMap;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.om.window.PortletWindowList;
import org.apache.pluto.om.window.PortletWindowListCtrl;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletWindowListImpl.java,v 1.3 2004/03/01 03:50:57 antonio Exp $
 */
public class PortletWindowListImpl implements PortletWindowList, PortletWindowListCtrl {

    
    HashMap windows = null;

    public PortletWindowListImpl() {
        windows = new HashMap();
    }

    // PortletWindowList implementation.

    /**
     * Returns the elements of this set
     * 
     * @return An iterator containg all elements
     */
    public java.util.Iterator iterator() {

        return windows.values().iterator();
    }


    /**
     * Returns the portlet window object of the given id
     *
     * @param id  id of the portlet window object
     *
     * @return the portlet window object or null if the list does not
     *         contain a portlet window with the given id
     **/
    public PortletWindow get(ObjectID id)
    {
        return (PortletWindow)windows.get(id.toString());
    }

    // PortletWindowListCtrl implementation.
    
    /**
     * Add a portlet window to the list
     * 
     * @param window the porlet window to add
     **/
    public void add(PortletWindow window) {
        if(window != null) {
            windows.put(window.getId().toString(), window);
        }
    }

    /**
     * Remove the portlet window with the given Id from the list
     * 
     * @param id the Id of the portlet window which should be removed
     **/
    public void remove(ObjectID id){
        if(id != null) {
            windows.remove(id.toString());
        }
    }
}
