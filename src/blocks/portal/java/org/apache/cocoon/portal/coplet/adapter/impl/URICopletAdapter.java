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
package org.apache.cocoon.portal.coplet.adapter.impl;

import java.io.IOException;
import java.util.HashMap;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This is the adapter to use pipelines as coplets
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: URICopletAdapter.java,v 1.2 2003/05/08 11:54:00 cziegeler Exp $
 */
public class URICopletAdapter 
    extends AbstractLogEnabled
    implements CopletAdapter, ThreadSafe, Composable, Disposable {
	
    /** The component manager */
    protected ComponentManager manager;

    /** The source resolver */
    protected SourceResolver resolver;
    /**
     * @see org.apache.avalon.framework.component.Composable#compose(ComponentManager)
     */
    public void compose(ComponentManager componentManager)
        throws ComponentException {
        this.manager = componentManager;
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    
    public void toSAX(CopletInstanceData coplet, ContentHandler contentHandler)
    throws SAXException {
		String uri = (String)coplet.getCopletData().getAttribute("uri");
		Source copletSource = null;
		PortalService portalService = null;
		try {
			if (uri.startsWith("cocoon:")) {
				portalService = (PortalService)this.manager.lookup(PortalService.ROLE);
				HashMap par = new HashMap();
				par.put(Constants.PORTAL_NAME_KEY, portalService.getPortalName());
				par.put(Constants.COPLET_ID_KEY, coplet.getCopletId());
            
				copletSource = this.resolver.resolveURI(uri, null, par);
			} else {
				copletSource = this.resolver.resolveURI(uri);
			}
			SourceUtil.toSAX(copletSource, contentHandler);
		} catch (IOException ioe) {
			throw new SAXException("IOException", ioe);
		} catch (ProcessingException pe) {
			throw new SAXException("ProcessingException", pe);
		} catch (ComponentException ce) {
			throw new SAXException("ComponentException", ce);
		} finally {
			this.resolver.release(copletSource);
			this.manager.release(portalService);
		}
    }
    
    public void init(CopletInstanceData coplet) {
    }
    
    public void destroy(CopletInstanceData coplet) {
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.resolver );
            this.resolver = null;
            this.manager = null;
        }
    }

}
