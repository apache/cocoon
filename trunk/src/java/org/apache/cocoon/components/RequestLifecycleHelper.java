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

 4. The names "Jakarta", "Avalon", "Excalibur" and "Apache Software Foundation"
    must not be used to endorse or promote products derived from this  software
    without  prior written permission. For written permission, please contact
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
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.fortress.impl.handler.ComponentHandler;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.EnvironmentContext;
import org.apache.cocoon.environment.EnvironmentHelper;

/**
 * RequestLifecycleHelper Encapsulates all the static processing that is needed
 * for handling RequestLifecycle components.
 *
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @version CVS $ Revision: 1.1 $
 */
class RequestLifecycleHelper {

    static final String KEY = RequestLifecycleHelper.class.getName();
    
    static EnvironmentDescription getEnvironmentDescription() {
        final EnvironmentContext context = EnvironmentHelper.getCurrentContext();
        EnvironmentDescription desc = (EnvironmentDescription) context.getAttribute(KEY);
        if ( desc == null ) {
            desc = new EnvironmentDescription(context.getEnvironment());
            context.addAttribute(KEY, desc);
        }
        return desc;
    }
    
    /**
     * Add an automatically released component
     */
    static void addComponentForAutomaticRelease(final ComponentHandler manager,
                                                       final Object        component)
    throws ProcessingException {
        EnvironmentDescription desc = getEnvironmentDescription();
        if ( null != desc ) {
            desc.addToAutoRelease(manager, component);
        }
    }

    /**
     * Remove from automatically released components
     */
    public static void removeFromAutomaticRelease(final Object component)
    throws ProcessingException {
        EnvironmentDescription desc = getEnvironmentDescription();
        if ( null != desc ) {
            desc.removeFromAutoRelease(component);
        }
    }
}

final class EnvironmentDescription
implements Disposable
{
    Environment environment;
    Map objectModel;
    Map requestLifecycleComponents;
    List autoreleaseComponents = new ArrayList( 4 );

    /**
     * Constructor
     */
    EnvironmentDescription( Environment env )
    {
        this.environment = env;
        this.objectModel = env.getObjectModel();
    }

    Map getGlobalRequestLifcecycleComponents()
    {
        Map m = (Map) environment.getAttribute( GlobalRequestLifecycleComponent.class.getName() );
        if ( m == null )
        {
            m = new HashMap();
            environment.setAttribute( GlobalRequestLifecycleComponent.class.getName(), m );
        }
        return m;
    }

    /**
     * Release all components of this environment
     * All RequestLifecycleComponents and autoreleaseComponents are
     * released.
     */
    public void dispose()
    {
        if ( this.requestLifecycleComponents != null )
        {
            final Iterator iter = this.requestLifecycleComponents.values().iterator();
            while ( iter.hasNext() )
            {
                final Object[] o = (Object[]) iter.next();
                final Object component = o[0];
                ((ComponentHandler)o[1]).put(component);
            }
            this.requestLifecycleComponents.clear();
        }

        for ( int i = 0; i < autoreleaseComponents.size(); i++ )
        {
            final Object[] o = (Object[]) autoreleaseComponents.get( i );
            final Object component = o[0];
            final ComponentHandler handler = (ComponentHandler)o[1];
            handler.put(component);
        }
        this.autoreleaseComponents.clear();
        this.environment = null;
        this.objectModel = null;
    }


    /**
     * Add a RequestLifecycleComponent to the environment
     */
    void addRequestLifecycleComponent( final String role,
                                       final Object co,
                                       final ComponentHandler manager )
    {
        if ( this.requestLifecycleComponents == null )
        {
            this.requestLifecycleComponents = new HashMap();
        }
        this.requestLifecycleComponents.put( role, new Object[]{co, manager} );
    }

    /**
     * Add a GlobalRequestLifecycleComponent to the environment
     */
    void addGlobalRequestLifecycleComponent( final String role,
                                             final Object co,
                                             final ComponentHandler manager )
    {
        this.getGlobalRequestLifcecycleComponents().put( role, new Object[]{co, manager} );
    }

    /**
     * Do we already have a request lifecycle component
     */
    boolean containsRequestLifecycleComponent( final String role )
    {
        if ( this.requestLifecycleComponents == null )
        {
            return false;
        }
        return this.requestLifecycleComponents.containsKey( role );
    }

    /**
     * Do we already have a global request lifecycle component
     */
    boolean containsGlobalRequestLifecycleComponent( final String role )
    {
        return this.getGlobalRequestLifcecycleComponents().containsKey( role );
    }

    /**
     * Search a RequestLifecycleComponent
     */
    Object getRequestLifecycleComponent( final String role )
    {
        if ( this.requestLifecycleComponents == null )
        {
            return null;
        }
        final Object[] o = (Object[]) this.requestLifecycleComponents.get( role );
        if ( null != o )
        {
            return o[0];
        }
        return null;
    }

    /**
     * Search a GlobalRequestLifecycleComponent
     */
    Object getGlobalRequestLifecycleComponent( final String role )
    {
        final Object[] o = (Object[]) this.getGlobalRequestLifcecycleComponents().get( role );
        if ( null != o )
        {
            return o[0];
        }
        return null;
    }

    /**
     * Add an automatically released component
     */
    void addToAutoRelease( final ComponentHandler manager,
                           final Object component )
    {
        this.autoreleaseComponents.add( new Object[]{component, manager} );
    }

    /**
     * Remove from automatically released components
     */
    void removeFromAutoRelease( final Object component )
            throws ProcessingException
    {
        int i = 0;
        boolean found = false;
        while ( i < this.autoreleaseComponents.size() && !found )
        {
            final Object[] o = (Object[]) this.autoreleaseComponents.get( i );
            if ( o[0] == component )
            {
                found = true;
                final ComponentHandler handler = (ComponentHandler)o[1];
                handler.put(component);
                this.autoreleaseComponents.remove( i );
            }
            else
            {
                i++;
            }
        }
        if ( !found )
        {
            throw new ProcessingException( "Unable to remove component from automatic release: component not found." );
        }
    }
}
