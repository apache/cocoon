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

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.xml.XMLConsumer;

import java.util.*;

/**
 * RequestLifecycleHelper Encapsulates all the static processing that is needed
 * for handling RequestLifecycle components.
 *
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @version CVS $ Revision: 1.1 $
 */
class RequestLifecycleHelper
{
    /** The key used to store the current process environment */
    static final String PROCESS_KEY = CocoonComponentManager.class.getName();
    /** The environment information */
    private static InheritableThreadLocal environmentStack = new CloningInheritableThreadLocal();

    static EnvironmentStack getTopEnvironmentStack()
    {
        return (EnvironmentStack)environmentStack.get();
    }

    /**
     * This hook must be called by the sitemap each time a sitemap is entered
     * This method should never raise an exception, except when the
     * parameters are not set!
     */
    static void enterEnvironment(Environment      env,
                                        ServiceManager manager,
                                        Processor        processor) {
        if ( null == env || null == manager || null == processor) {
            throw new IllegalArgumentException("CocoonComponentManager.enterEnvironment: all parameters must be set: " + env + " - " + manager + " - " + processor);
        }

        EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
		if (stack == null) {
            stack = new EnvironmentStack();
			environmentStack.set(stack);
		}
		stack.push(new Object[] {env, processor, manager, new Integer(stack.getOffset())});
        stack.setOffset(stack.size()-1);

        env.setAttribute("CocoonComponentManager.processor", processor);
    }

    /**
     * This hook must be called by the sitemap each time a sitemap is left.
     * It's the counterpart to {@link #enterEnvironment(Environment, ServiceManager, Processor)}.
     */
    static void leaveEnvironment() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        final Object[] objs = (Object[])stack.pop();
        stack.setOffset(((Integer)objs[3]).intValue());
        if ( stack.isEmpty() ) {
            final Environment env = (Environment)objs[0];
            final Map globalComponents = (Map)env.getAttribute(GlobalRequestLifecycleComponent.class.getName());
            if ( globalComponents != null) {

                final Iterator iter = globalComponents.values().iterator();
                while ( iter.hasNext() ) {
                    final Object[] o = (Object[])iter.next();
                    final Object c = o[0];
                    ((CocoonComponentManager)o[1]).releaseRLComponent( c );
                }
            }
            env.removeAttribute(GlobalRequestLifecycleComponent.class.getName());
        }
    }

    static void checkEnvironment(Logger logger)
    throws Exception {
        // TODO (CZ): This is only for testing - remove it later on
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if (stack != null && !stack.isEmpty() ) {
            logger.error("ENVIRONMENT STACK HAS NOT BEEN CLEANED PROPERLY");
            throw new ProcessingException("Environment stack has not been cleaned up properly. "
                                          +"Please report this (if possible together with a test case) "
                                          +"to the Cocoon developers.");
        }
    }

    /**
     * Create an environment aware xml consumer for the cocoon
     * protocol
     */
    static XMLConsumer createEnvironmentAwareConsumer(XMLConsumer consumer) {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        final Object[] objs = (Object[])stack.getCurrent();
        return stack.getEnvironmentAwareConsumerWrapper(consumer, ((Integer)objs[3]).intValue());
    }

    /**
     * This hook has to be called before a request is processed.
     * The hook is called by the Cocoon component and by the
     * cocoon protocol implementation.
     * This method should never raise an exception, except when
     * the environment is not set.
     *
     * @return A unique key within this thread.
     */
    static Object startProcessing(Environment env) {
		if ( null == env) {
			throw new RuntimeException("CocoonComponentManager.startProcessing: environment must be set.");
		}
        final EnvironmentDescription desc = new EnvironmentDescription(env);
        env.getObjectModel().put(PROCESS_KEY, desc);
		env.startingProcessing();
        return desc;
    }

    /**
     * This hook has to be called before a request is processed.
     * The hook is called by the Cocoon component and by the
     * cocoon protocol implementation.
     * @param key A unique key within this thread return by
     *         {@link #startProcessing(Environment)}.
     */
    static void endProcessing(Environment env, Object key) {
		env.finishingProcessing();
        final EnvironmentDescription desc = (EnvironmentDescription)key;
        desc.release();
        env.getObjectModel().remove(PROCESS_KEY);
    }

    /**
     * Return the current environment (for the cocoon: protocol)
     */
    static Environment getCurrentEnvironment() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if (null != stack && !stack.empty()) {
            return (Environment) ((Object[])stack.getCurrent())[0];
        }
        return null;
    }

    /**
     * Return the current processor (for the cocoon: protocol)
     */
    static Processor getCurrentProcessor() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if (null != stack && !stack.empty()) {
            return (Processor) ((Object[])stack.getCurrent())[1];
        }
        return null;
    }

    /**
     * Return the processor that is actually processing the request
     */
    static Processor getLastProcessor(Environment env) {
        return (Processor)env.getAttribute("CocoonComponentManager.processor");
    }

    /**
     * Get the current sitemap component manager.
     * This method return the current sitemap component manager. This
     * is the manager that holds all the components of the currently
     * processed (sub)sitemap.
     */
    static public ServiceManager getSitemapComponentManager() {
		final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();

		if ( null != stack && !stack.empty()) {
			Object[] o = (Object[])stack.peek();
			return (ServiceManager)o[2];
		}

        // if we don't have an environment yet, just return null
        return null;
    }

    /**
     * Add an automatically released component
     */
    static void addComponentForAutomaticRelease(final ServiceSelector selector,
                                                       final Object         component,
                                                       final ServiceManager  manager)
    throws ProcessingException {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if ( null != stack && !stack.empty()) {
            final Object[] objects = (Object[])stack.get(0);
            final Map objectModel = ((Environment)objects[0]).getObjectModel();
            EnvironmentDescription desc = (EnvironmentDescription)objectModel.get(PROCESS_KEY);
            if ( null != desc ) {
                desc.addToAutoRelease(selector, component, manager);
            }
        } else {
            throw new ProcessingException("Unable to add component for automatic release: no environment available.");
        }
    }

    /**
     * Add an automatically released component
     */
    static void addComponentForAutomaticRelease(final ServiceManager manager,
                                                       final Object        component)
    throws ProcessingException {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if ( null != stack && !stack.empty()) {
            final Object[] objects = (Object[])stack.get(0);
            final Map objectModel = ((Environment)objects[0]).getObjectModel();
            EnvironmentDescription desc = (EnvironmentDescription)objectModel.get(PROCESS_KEY);
            if ( null != desc ) {
                desc.addToAutoRelease(manager, component);
            }
        } else {
            throw new ProcessingException("Unable to add component for automatic release: no environment available.");
        }
    }

    /**
     * Remove from automatically released components
     */
    public static void removeFromAutomaticRelease(final Object component)
    throws ProcessingException {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if ( null != stack && !stack.empty()) {
            final Object[] objects = (Object[])stack.get(0);
            final Map objectModel = ((Environment)objects[0]).getObjectModel();
            EnvironmentDescription desc = (EnvironmentDescription)objectModel.get(PROCESS_KEY);
            if ( null != desc ) {
                desc.removeFromAutoRelease(component);
            }
        } else {
            throw new ProcessingException("Unable to remove component from automatic release: no environment available.");
        }
    }
}

final class CloningInheritableThreadLocal
        extends InheritableThreadLocal
{

    /**
     * Computes the child's initial value for this InheritableThreadLocal
     * as a function of the parent's value at the time the child Thread is
     * created.  This method is called from within the parent thread before
     * the child is started.
     * <p>
     * This method merely returns its input argument, and should be overridden
     * if a different behavior is desired.
     *
     * @param parentValue the parent thread's value
     * @return the child thread's initial value
     */
    protected Object childValue( Object parentValue )
    {
        if ( null != parentValue )
        {
            return ( (EnvironmentStack) parentValue ).clone();
        }
        else
        {
            return null;
        }
    }
}


final class EnvironmentDescription
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
    void release()
    {
        if ( this.requestLifecycleComponents != null )
        {
            final Iterator iter = this.requestLifecycleComponents.values().iterator();
            while ( iter.hasNext() )
            {
                final Object[] o = (Object[]) iter.next();
                final Object component = o[0];
                ( (CocoonComponentManager) o[1] ).releaseRLComponent( component );
            }
            this.requestLifecycleComponents.clear();
        }

        for ( int i = 0; i < autoreleaseComponents.size(); i++ )
        {
            final Object[] o = (Object[]) autoreleaseComponents.get( i );
            final Object component = o[0];
            if ( o[1] instanceof ServiceManager )
            {
                ( (ServiceManager) o[1] ).release( component );
            }
            else
            {
                ( (ServiceSelector) o[1] ).release( component );
                if ( o[2] != null )
                {
                    ( (ServiceManager) o[2] ).release( o[1] );
                }
            }
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
                                       final ServiceManager manager )
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
                                             final ServiceManager manager )
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
    void addToAutoRelease( final ServiceSelector selector,
                           final Object component,
                           final ServiceManager manager )
    {
        this.autoreleaseComponents.add( new Object[]{component, selector, manager} );
    }

    /**
     * Add an automatically released component
     */
    void addToAutoRelease( final ServiceManager manager,
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
                if ( o[1] instanceof ServiceManager )
                {
                    ( (ServiceManager) o[1] ).release( component );
                }
                else
                {
                    ( (ServiceSelector) o[1] ).release( component );
                    if ( o[2] != null )
                    {
                        ( (ServiceManager) o[2] ).release( o[1] );
                    }
                }
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
