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
package org.apache.cocoon.portal.profile.impl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletBaseData;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.AbstractLayout;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.excalibur.source.SourceValidity;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: StaticProfileManager.java,v 1.3 2003/07/03 08:27:47 cziegeler Exp $
 */
public class StaticProfileManager 
    extends AbstractLogEnabled 
    implements Composable, ProfileManager, ThreadSafe {

    protected ComponentManager componentManager;

    private Mapping layoutMapping;

    private Map layoutStati = new HashMap(100);
    
    /**
     * @see org.apache.avalon.framework.component.Composable#compose(ComponentManager)
     */
    public void compose(ComponentManager componentManager) throws ComponentException {
        this.componentManager = componentManager;
    }

    /**
     * @see ProfileManager#getPortalLayout(String)
     */
    public Layout getPortalLayout(String key) {
        PortalService service = null;
        MapSourceAdapter adapter = null;
        try {
            service = (PortalService) this.componentManager.lookup(PortalService.ROLE);
            
            if ( null == key ) {
                Layout l = (Layout) service.getTemporaryAttribute("DEFAULT_LAYOUT");
                if ( null != l) {
                    return l;
                }
            }

            Object[] objects = (Object[]) service.getAttribute(StaticProfileManager.class.getName() + "/Layout");

            Map map = new HashMap();
            map.put("profile", "layout");
            map.put("portalname", service.getPortalName());

            int valid = SourceValidity.INVALID;
            SourceValidity sourceValidity = null;
            if (objects != null) {
                sourceValidity = (SourceValidity) objects[1];
                valid = sourceValidity.isValid();
                if (valid == SourceValidity.VALID)
                    return (Layout) objects[0];
            }
            adapter = (MapSourceAdapter) this.componentManager.lookup(MapSourceAdapter.ROLE);
            Map param = new HashMap();
            param.put("portalname", service.getPortalName());
            SourceValidity newValidity = adapter.getValidity(param, map);
            if (valid == SourceValidity.UNKNWON) {
                if (sourceValidity.isValid(newValidity) == SourceValidity.VALID)
                    return (Layout) objects[0];
            }
            Layout layout = (Layout) adapter.loadProfile(param, map);
            if (newValidity != null) {
                objects = new Object[] { layout, newValidity };
                service.setAttribute(StaticProfileManager.class.getName() + "/Layout", objects);
            }
            // resolve parents
            resolveParents(layout, null);
            return layout;
        } catch (Exception ce) {
            // TODO
            throw new CascadingRuntimeException("Arg", ce);
        } finally {
            this.componentManager.release(service);
            this.componentManager.release(adapter);
        }
    }

    public CopletInstanceData getCopletInstanceData(String copletID) {
        PortalService service = null;
        String key = null;
        try {
            service = (PortalService) this.componentManager.lookup(PortalService.ROLE);
            key = service.getPortalName() + ":" + copletID;

            Map coplets = (Map) service.getAttribute(StaticProfileManager.class.getName() + "/Coplets");
            if (null == coplets) {
                coplets = new HashMap();
                service.setAttribute(StaticProfileManager.class.getName() + "/Coplets", coplets);
            }

            CopletInstanceData cid = (CopletInstanceData) coplets.get(key);
            if (null == cid) {
                CopletBaseData base = new CopletBaseData();
                base.setId("URICoplet");
                base.setCopletAdapterName("uri");
                cid = new CopletInstanceData();
                CopletData cd = new CopletData();
                cd.setName(copletID);
                cid.setCopletData(cd);
                cid.setId(copletID); // TODO generate unique copletID
                cid.getCopletData().setCopletBaseData(base);
                cid.getCopletData().setAttribute("uri", copletID);
                cid.getCopletData().setTitle(copletID);
                coplets.put(key, cid);

                Marshaller marshaller;
                try {
                    marshaller = new Marshaller(new PrintWriter(System.out));
                    marshaller.setSuppressXSIType(true);
                    marshaller.setMapping(layoutMapping);
                    marshaller.marshal(cid);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            return cid;
        } catch (ComponentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new CascadingRuntimeException("CE", e);
        } finally {
            this.componentManager.release(service);
        }
    }

    public List getCopletInstanceData(CopletData data) {
        List coplets = new ArrayList();
        PortalService service = null;
        String attribute = null;
        try {
            service = (PortalService) this.componentManager.lookup(PortalService.ROLE);

            Map allCoplets = (Map) service.getAttribute(StaticProfileManager.class.getName() + "/Coplets");
            if (null != allCoplets) {

                Iterator iter = allCoplets.values().iterator();
                while ( iter.hasNext() ) {
                    CopletInstanceData current = (CopletInstanceData)iter.next();
                    if ( current.getCopletData().equals(data) ) {
                        coplets.add( current );
                    }
                }
            }
            return coplets;
        } catch (ComponentException e) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", e);
        } finally {
            this.componentManager.release(service);
        }
    }

    public void setDefaultLayout(Layout object) {
        PortalService service = null;
        try {
            service = (PortalService) this.componentManager.lookup(PortalService.ROLE);
            service.setTemporaryAttribute("DEFAULT_LAYOUT", object);
        } catch (ComponentException e) {
            throw new CascadingRuntimeException("Unable to lookup service manager.", e);
        } finally {
            this.componentManager.release(service);
        }
    }

    private void resolveParents(final Layout layout, final Item item) {
        String id = layout.getId();
        if ( id == null ) {
            id = Integer.toString(layout.hashCode());
            ((AbstractLayout)layout).setId(id);
        }
        if (layout instanceof CompositeLayout) {

            final CompositeLayout compositeLayout = (CompositeLayout) layout;

            for (int j = 0; j < compositeLayout.getSize(); j++) {
                final Item layoutItem = (Item) compositeLayout.getItem(j);
                layoutItem.setParent(compositeLayout);
                this.resolveParents(layoutItem.getLayout(), layoutItem);
            }
        }
        if (layout instanceof CopletLayout) {
            final CopletLayout cl = (CopletLayout)layout;
            final CopletInstanceData cid = this.getCopletInstanceData(cl.getId());
            cl.setCopletInstanceData(cid);
        }
        layout.setParent(item);
    }

    public void register(CopletInstanceData coplet) {
    }
    
    public void unregister(CopletInstanceData coplet) {
    }

    public void register(Layout layout) {
    }
    
    public void unregister(Layout layout) {
    }

    public void saveUserProfiles() {
    }
    
    public void login() {
    }
    
    public void logout() {
    }
    
}
