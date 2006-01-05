/*
 * Copyright 2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cocoon.core.osgi;

import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * A <code>ServiceManager</code> that looks up components by getting OSGi services.
 * <p>
 * Roles are supposed to be interface names, which the OSGi framework will use. As some
 * roles may contain hints (e.g. "org.apache.cocoon.generation.Generator/file"), in such
 * cases the interface name is extracted, and the hint is expected to be found in the
 * "<code>component.hint</code>" service property.
 *
 * @version $Id$
 * @since 2.2
 */
public class OSGiServiceManager implements ServiceManager {
    
    /**
     * The property that's used to hold component hints in component selectors.
     */
    public static final String HINT_PROPERTY = "component.hint";
    
    private BundleContext context;

    /** Mapping from service instance to ServiceReference */
    private Map serviceReferences = new IdentityHashMap();

    public OSGiServiceManager(BundleContext context) {
        this.context = context;
    }

    public Object lookup(String role) throws ServiceException {
        // Get the service
        ServiceReference ref;
        try {
            ref = getServiceReference(this.context, role);
        } catch (InvalidSyntaxException e) {
            throw new ServiceException(role, "Cannot lookup OSGi service", e);
        }

        if (ref == null) {
            throw new ServiceException(role, "OSGi service not available");
        }
        
        // Important hypothesis: for a given (role, requesting bundle) value, OSGi
        // always returns the same service instance. This seems to be implied by the
        // specification, and how the knopflerfish implementation behaves.
        Object service = context.getService(ref);
        
        // Keep track of its reference
        synchronized(this) {
            this.serviceReferences.put(service, ref);
        }

        return service;
    }

    public boolean hasService(String role) {
        try {
            return getServiceReference(this.context, role) != null;
        } catch (InvalidSyntaxException e) {
            return false;
        }
   }

    public void release(Object obj) {
        ServiceReference ref = (ServiceReference)this.serviceReferences.get(obj);
        
        if (ref == null) {
            // not handled here
            return;
        }
        
        synchronized(this) {
            if (this.context.ungetService(ref)) {
                // No more used here: remove it from our map.
                this.serviceReferences.remove(obj);
            }
        }
    }
    
    public static ServiceReference getServiceReference(BundleContext ctx, String role) throws InvalidSyntaxException {
        ServiceReference result;

        int pos = role.indexOf('/');

        if (pos == -1) {
            // Single interface role
            result = ctx.getServiceReference(role);
        } else {
            // Hinted role: split it
            String itf = role.substring(0, pos);
            String query = "(" + HINT_PROPERTY + "=" + role.substring(pos + 1) + ")";
            ServiceReference[] results = ctx.getServiceReferences(itf, query);
            result = (results != null && results.length > 1) ? results[0] : null;
        }

        return result;
    }
    
    public static String getServiceInterface(String role) {
        int pos = role.indexOf('/');
        
        return pos == -1 ? role : role.substring(0, pos);
    }
    
    public static String getServiceHint(String role) {
        int pos = role.indexOf('/');
        return pos == -1 ? null : role.substring(pos+1);
    }
}
