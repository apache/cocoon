/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.portal.layout;

import java.util.Map;

import org.apache.cocoon.portal.factory.impl.AbstractProducible;
import org.apache.commons.collections.map.LinkedMap;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: AbstractLayout.java,v 1.13 2004/01/27 14:58:05 cziegeler Exp $
 */
public abstract class AbstractLayout 
    extends AbstractProducible 
    implements Layout, Parameters {
    
    protected String rendererName;
    
    protected Item parent;
    
    protected Map parameters = new LinkedMap();
     
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.Parameters#getParameters()
     */
    public final Map getParameters() {
        return parameters;
    }

    /**
     * @see org.apache.cocoon.portal.layout.Layout#getRendererName()
     */
    public String getRendererName() {
        if ( this.rendererName == null ) {
            return ((LayoutDescription)this.description).getDefaultRendererName();
        }
        return this.rendererName;
    }

    public void setLayoutRendererName(String value) {
		this.rendererName = value;
	}
    
    public Item getParent() {
        return this.parent;
    }
    
    public void setParent(Item item) {
        this.parent = item;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.Layout#getLayoutRendererName()
     */
    public String getLayoutRendererName() {
        return this.rendererName;
    }

}
