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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.aspect.AspectDataHandler;
import org.apache.cocoon.portal.aspect.AspectDataStore;
import org.apache.cocoon.portal.aspect.AspectDescription;
import org.apache.cocoon.portal.aspect.impl.DefaultAspectDataHandler;
import org.apache.cocoon.portal.aspect.impl.DefaultAspectDescription;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Filter;
import org.apache.cocoon.portal.event.LayoutEvent;
import org.apache.cocoon.portal.event.Subscriber;
import org.apache.cocoon.portal.event.impl.LayoutRemoveEvent;
import org.apache.cocoon.portal.layout.*;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.util.ClassUtils;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: DefaultLayoutFactory.java,v 1.12 2003/06/14 17:55:43 cziegeler Exp $
 */
public class DefaultLayoutFactory
	extends AbstractLogEnabled
    implements ThreadSafe, 
                 Component, 
                 LayoutFactory, 
                 Configurable, 
                 Disposable, 
                 Composable,
                 Initializable,
                 Subscriber {

    protected Map layouts = new HashMap();
    
    protected List descriptions = new ArrayList();
    
    protected ComponentSelector storeSelector;
    
    protected ComponentManager manager;
    
    /** 
     * Configure a layout
     */
    protected void configureLayout(Configuration layoutConf) 
    throws ConfigurationException {
        DefaultLayoutDescription desc = new DefaultLayoutDescription();
        final String name = layoutConf.getAttribute("name");
                
        // unique test
        if ( this.layouts.get(name) != null) {
            throw new ConfigurationException("Layout name must be unique. Double definition for " + name);
        }
        desc.setName(name);
        desc.setClassName(layoutConf.getAttribute("class"));        
        desc.setCreateId(layoutConf.getAttributeAsBoolean("create-id", false));
                
        // the renderers
        final String defaultRenderer = layoutConf.getChild("renderers").getAttribute("default");
        desc.setDefaultRendererName(defaultRenderer); 
                                
        final Configuration[] rendererConfs = layoutConf.getChild("renderers").getChildren("renderer");
        if ( rendererConfs != null ) {
            boolean found = false;
            for(int m=0; m < rendererConfs.length; m++) {
                final String rName = rendererConfs[m].getAttribute("name");
                desc.addRendererName(rName);
                if ( defaultRenderer.equals(rName) ) {
                    found = true;
                }
            }
            if ( !found ) {
                throw new ConfigurationException("Default renderer '" + defaultRenderer + "' is not configured for layout '" + name + "'");
            }
        } else {
            throw new ConfigurationException("Default renderer '" + defaultRenderer + "' is not configured for layout '" + name + "'");
        }
        
        // and now the aspects
        final Configuration[] aspectsConf = layoutConf.getChild("aspects").getChildren("aspect");
        if (aspectsConf != null) {
            for(int m=0; m < aspectsConf.length; m++) {
                AspectDescription adesc = DefaultAspectDescription.newInstance(aspectsConf[m]);
                desc.addAspectDescription( adesc );
            }
        }
        // now query all configured renderers for their aspects
        ComponentSelector rendererSelector = null;
        try {
            rendererSelector = (ComponentSelector)this.manager.lookup(Renderer.ROLE+"Selector");
            
            Iterator rendererIterator = desc.getRendererNames();
            while (rendererIterator.hasNext()) {
                final String rendererName = (String)rendererIterator.next();
                Renderer renderer = null;
                try { 
                    renderer = (Renderer) rendererSelector.select( rendererName );
                    
                    Iterator aspectIterator = renderer.getAspectDescriptions();
                    while (aspectIterator.hasNext()) {
                        final AspectDescription adesc = (AspectDescription) aspectIterator.next();
                        desc.addAspectDescription( adesc );
                    }
                } catch (ComponentException ce ) {
                    throw new ConfigurationException("Unable to lookup renderer '" + rendererName + "'", ce);
                } finally {
                    rendererSelector.release( renderer );
                }
            }
        } catch (ComponentException ce ) {
            throw new ConfigurationException("Unable to lookup renderer selector.", ce);
        } finally {
            this.manager.release( rendererSelector );
        }
        
        // set the aspect data handler
        DefaultAspectDataHandler handler = new DefaultAspectDataHandler(desc, this.storeSelector);
        this.layouts.put(desc.getName(), new Object[] {desc, handler});
        this.descriptions.add(desc);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) 
    throws ConfigurationException {
        final Configuration[] layoutsConf = configuration.getChild("layouts").getChildren("layout");
        if ( layoutsConf != null ) {
            for(int i=0; i < layoutsConf.length; i++ ) {
                this.configureLayout( layoutsConf[i] );
            }
        }
    }

    public void prepareLayout(Layout layout) 
    throws ProcessingException {
        if ( layout != null ) {
     
            final String layoutName = layout.getName();
            if ( layoutName == null ) {
                throw new ProcessingException("Layout '"+layout.getId()+"' has no associated name.");
            }
            Object[] o = (Object[]) this.layouts.get( layoutName );
            
            if ( o == null ) {
                throw new ProcessingException("LayoutDescription with name '" + layoutName + "' not found.");
            }
            DefaultLayoutDescription layoutDescription = (DefaultLayoutDescription)o[0];

            layout.setDescription( layoutDescription );
            layout.setAspectDataHandler((AspectDataHandler)o[1]);
            
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

    public Layout newInstance(String layoutName) 
    throws ProcessingException {
        Object[] o = (Object[]) this.layouts.get( layoutName );
            
        if ( o == null ) {
            throw new ProcessingException("LayoutDescription with name '" + layoutName + "' not found.");
        }
        DefaultLayoutDescription layoutDescription = (DefaultLayoutDescription)o[0];
        
        Layout layout = null;
        try {
            Class clazz = ClassUtils.loadClass( layoutDescription.getClassName() );
            layout = (Layout)clazz.newInstance();
            
        } catch (Exception e) {
            throw new ProcessingException("Unable to create new instance", e );
        }
        
        String id = null;
        if ( layoutDescription.createId() ) {
            // TODO - set unique id
            id = layoutName + '-' + System.currentTimeMillis();
        }
        layout.initialize( layoutName, id ); 
        layout.setDescription( layoutDescription );
        layout.setAspectDataHandler((AspectDataHandler)o[1]);

        ProfileManager profileManager = null;
        try {
            profileManager = (ProfileManager)this.manager.lookup(ProfileManager.ROLE);
            profileManager.register(layout);
        } catch (ComponentException ce) {
            throw new ProcessingException("Unable to lookup profile manager.", ce);
        } finally {
            this.manager.release( profileManager );
        }
        return layout;
    }
    
    public List getLayoutDescriptions() {
        return this.descriptions;
    }


    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            EventManager eventManager = null;
            try { 
                eventManager = (EventManager)this.manager.lookup(EventManager.ROLE);
                eventManager.getRegister().unsubscribe( this );
            } catch (Exception ignore) {
            } finally {
                this.manager.release( eventManager ); 
            }
            this.manager.release( this.storeSelector );
            this.storeSelector = null;
            this.manager = null;
        }

    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        this.storeSelector = (ComponentSelector)this.manager.lookup( AspectDataStore.ROLE+"Selector" );
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        EventManager eventManager = null;
        try { 
            eventManager = (EventManager)this.manager.lookup(EventManager.ROLE);
            eventManager.getRegister().subscribe( this );
        } finally {
            this.manager.release( eventManager );
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getFilter()
     */
    public Filter getFilter() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getEventType()
     */
    public Class getEventType() {
        return LayoutEvent.class;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#inform(org.apache.cocoon.portal.event.Event)
     */
    public void inform(Event e) {
        // event dispatching
        if ( e instanceof LayoutRemoveEvent ) {
            LayoutRemoveEvent event = (LayoutRemoveEvent)e;
            Layout layout = (Layout)event.getTarget();
            try {
                this.remove( layout );
            } catch (ProcessingException pe) {
                throw new CascadingRuntimeException("Exception during removal.", pe);
            }
        }
    }

    public void remove(Layout layout) 
    throws ProcessingException {
        if ( layout != null ) {
            if ( layout instanceof CompositeLayout ) {
                Iterator itemIterator = ((CompositeLayout)layout).getItems().iterator();
                while ( itemIterator.hasNext() ) {
                    this.remove( ((Item)itemIterator.next()).getLayout());               
                }
            }
            Item parent = layout.getParent();
            if ( parent != null && parent.getParent() != null) {
                parent.getParent().removeItem( parent );
            }
            
            if ( layout instanceof CopletLayout ) {
                CopletFactory factory = null;
                try {
                    factory = (CopletFactory)this.manager.lookup(CopletFactory.ROLE);
                    factory.remove( ((CopletLayout)layout).getCopletInstanceData());
                } catch (ComponentException ce) {
                    throw new ProcessingException("Unable to lookup coplet factory.", ce);
                } finally {
                    this.manager.release( (Component)factory );
                }
            }
            ProfileManager profileManager = null;
            try {
                profileManager = (ProfileManager)this.manager.lookup(ProfileManager.ROLE);
                profileManager.unregister(layout);
            } catch (ComponentException ce) {
                throw new ProcessingException("Unable to lookup profile manager.", ce);
            } finally {
                this.manager.release( profileManager );
            }
        }
    }
}
