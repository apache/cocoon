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
package org.apache.cocoon.portal.layout.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.layout.*;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFactory;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: DefaultLayoutFactory.java,v 1.3 2003/05/19 12:50:58 cziegeler Exp $
 */
public class DefaultLayoutFactory
	extends AbstractLogEnabled
    implements ThreadSafe, Component, LayoutFactory, Configurable {

    protected Map layouts = new HashMap();
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) 
    throws ConfigurationException {
        final Configuration[] layoutsConf = configuration.getChild("layouts").getChildren("layout");
        if ( layoutsConf != null ) {
            for(int i=0; i < layoutsConf.length; i++ ) {
                DefaultLayoutDescription desc = new DefaultLayoutDescription();
                // TODO unique name test
                desc.setName(layoutsConf[i].getAttribute("name"));
                desc.setClassName(layoutsConf[i].getAttribute("class"));        
                desc.setRendererName(layoutsConf[i].getAttribute("renderer")); 
                
                // and now the aspects
                final Configuration[] aspectsConf = layoutsConf[i].getChild("aspects").getChildren("aspect");
                if (aspectsConf != null) {
                    for(int m=0; m < aspectsConf.length; m++) {
                        DefaultLayoutAspectDescription adesc = new DefaultLayoutAspectDescription();
                        adesc.setClassName(aspectsConf[m].getAttribute("class"));
                        adesc.setName(aspectsConf[m].getAttribute("name"));
                        adesc.setPersistence(aspectsConf[m].getAttribute("persistence"));
                        desc.addAspect( adesc );
                    }
                }
                LayoutAspectDataHandler handler = new LayoutAspectDataHandler(desc);
                this.layouts.put(desc.getName(), new Object[] {desc, handler});
            }
        }
    }

    public void prepareLayout(Layout layout) 
    throws ProcessingException {
        if ( layout != null ) {
     
            final String layoutName = layout.getName();
            if ( layoutName == null ) {
                throw new ProcessingException("Layout "+layout.getId()+" has no associated name.");
            }
            Object[] o = (Object[]) this.layouts.get( layoutName );
            
            if ( o == null ) {
                throw new ProcessingException("LayoutDescription with name " + layoutName + " not found.");
            }
            DefaultLayoutDescription layoutDescription = (DefaultLayoutDescription)o[0];

            // TODO do something here 
            // we have to set the aspect data handler
            layout.setAspectDataHandler((LayoutAspectDataHandler)o[1]);
            
            // recursive
            if ( layout instanceof CompositeLayout ) {
                CompositeLayout composite = (CompositeLayout)layout;
                Iterator items = composite.getItems().iterator();
                while ( items.hasNext() ) {
                    this.prepareLayout( ((Item)items.next()).getLayout() );
                }
            }
        }
    }

}
