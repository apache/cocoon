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
package org.apache.cocoon.components;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Cocoon Component Manager.
 * This manager extends the ///////
 * by a special lifecycle handling for a {@link RequestLifecycleComponent}
 *
 * WARNING: This is a "private" Cocoon core class - do NOT use this class
 * directly - and do not assume that a {@link org.apache.avalon.framework.service.ServiceManager} you get
 * via the compose() method is an instance of CocoonComponentManager.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CocoonComponentManager.java,v 1.27 2003/10/24 07:29:56 cziegeler Exp $
 */
public final class CocoonComponentManager
implements ServiceManager
{

    /** The parent component manager for implementing parent aware components */
    private ServiceManager parentManager;

    /** Temporary list of parent-aware components.  Will be null for most of
     * our lifecycle. */
    private ArrayList parentAwareComponents = new ArrayList();

    /** Create the ComponentManager */
    public CocoonComponentManager() {
    }

    /** Create the ComponentManager with a parent ComponentManager */
    public CocoonComponentManager(final ServiceManager manager) {
        this.parentManager = manager;
    }

    public boolean hasService(String role)
    {
        return parentManager.hasService(role);
    }

    /**
     * Return an instance of a component based on a Role.  The Role is usually the Interface's
     * Fully Qualified Name(FQN)--unless there are multiple Components for the same Role.  In that
     * case, the Role's FQN is appended with "Selector", and we return a ComponentSelector.
     */
    public Object lookup( final String role )
    throws ServiceException {
        return parentManager.lookup(role);
    }

    /**
     * Release a Component.  This implementation makes sure it has a handle on the propper
     * ComponentHandler, and let's the ComponentHandler take care of the actual work.
     */
    public void release( final Object component ) {
        parentManager.release( component);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.component.ExcaliburComponentManager#addComponent(java.lang.String, java.lang.Class, org.apache.avalon.framework.configuration.Configuration)
     */
    public void addComponent(String role, Class clazz, Configuration conf)
        throws ServiceException {
//        super.addComponent(role, clazz, conf);
        // Note that at this point, we're not initialized and cannot do
        // lookups, so defer parental introductions to initialize().
        if ( ParentAware.class.isAssignableFrom( clazz ) ) {
            parentAwareComponents.add(role);
        }
    }

    public void initialize()
        throws Exception
    {
//        super.initialize();
        if (parentAwareComponents == null) {
            throw new ServiceException(null, "CocoonComponentManager already initialized");
        }
        // Set parents for parentAware components
        Iterator iter = parentAwareComponents.iterator();
        while (iter.hasNext()) {
            String role = (String)iter.next();
//            getLogger().debug(".. "+role);
            if ( parentManager != null && parentManager.hasService( role ) ) {
                // lookup new component
                Object component = null;
                try {
                    component = this.lookup( role );
                    ((ParentAware)component).setParentLocator( new ComponentLocatorImpl(this.parentManager, role ));
                } catch (ServiceException ignore) {
                    // we don't set the parent then
                } finally {
                    this.release( component );
                }
            }
        }
        parentAwareComponents = null;  // null to save memory, and catch logic bugs.
    }
    
    /**
     * Get the current sitemap component manager.
     * This method return the current sitemap component manager. This
     * is the manager that holds all the components of the currently
     * processed (sub)sitemap.
     */
    static public ServiceManager getSitemapComponentManager() {
        return RequestLifecycleHelper.getSitemapComponentManager();
    }
    
}


