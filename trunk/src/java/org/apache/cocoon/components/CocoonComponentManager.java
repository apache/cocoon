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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.avalon.excalibur.component.ExcaliburComponentManager;
import org.apache.avalon.excalibur.component.RoleManager;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.environment.Environment;

import java.net.MalformedURLException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;

/**
 * Cocoon Component Manager.
 * This manager extends the {@link ExcaliburComponentManager}
 * by a special lifecycle handling for a {@link RequestLifecycleComponent}
 * and by handling the lookup of the {@link SourceResolver}.
 * WARNING: This is a "private" Cocoon core class - do NOT use this class
 * directly - and do not assume that a {@link ComponentManager} you get
 * via the compose() method is an instance of CocoonComponentManager.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CocoonComponentManager.java,v 1.1 2003/03/09 00:08:46 pier Exp $
 */
public final class CocoonComponentManager
extends ExcaliburComponentManager
implements SourceResolver
{
 
    /** The key used to store the current process environment */
    private static final String PROCESS_KEY =
               "org.apache.cocoon.components.CocoonComponentManager";
         
    
    /** The environment information */
    private static InheritableThreadLocal environmentStack = new CloningInheritableThreadLocal();

    /** The configured <code>SourceResolver</code> */
    private SourceResolver sourceResolver;

    /** The {@link RoleManager} */
    private RoleManager roleManager;

    /** The root component manager */
    private static ComponentManager rootManager;
    
    /** Create the ComponentManager */
    public CocoonComponentManager(){
        super( null, Thread.currentThread().getContextClassLoader() );
        if ( null == rootManager ) rootManager = this;
    }

    /** Create the ComponentManager with a Classloader */
    public CocoonComponentManager( final ClassLoader loader ) {
        super( null, loader );
		if ( null == rootManager ) rootManager = this;
    }

    /** Create the ComponentManager with a Classloader and parent ComponentManager */
    public CocoonComponentManager( final ComponentManager manager, final ClassLoader loader ) {
        super(manager, loader);
		if ( null == rootManager ) rootManager = this;
    }

    /** Create the ComponentManager with a parent ComponentManager */
    public CocoonComponentManager(final ComponentManager manager) {
        super(manager);
		if ( null == rootManager ) rootManager = this;
    }

    /**
     * This hook must be called by the sitemap each time a sitemap is entered
     * This method should never raise an exception, except when the
     * parameters are not set!
     */
    public static void enterEnvironment(Environment      env,
                                          ComponentManager manager,
                                          Processor        processor) {
        if ( null == env || null == manager || null == processor) {                                       	
            throw new RuntimeException("CocoonComponentManager.enterEnvironment: all parameters must be set: " + env + " - " + manager + " - " + processor);
        }
        
		if (environmentStack.get() == null) {
			environmentStack.set(new EnvironmentStack());
		}
		final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
		stack.push(new Object[] {env, processor, manager});
        final EnvironmentDescription desc = (EnvironmentDescription)env.getObjectModel().get(PROCESS_KEY);
        desc.addSitemapConfiguration(processor.getComponentConfigurations());
    }

    /**
     * This hook must be called by the sitemap each time a sitemap is left
     */
    public static void leaveEnvironment() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        final Object[] objects = (Object[])stack.pop();
        final EnvironmentDescription desc = (EnvironmentDescription)((Environment)objects[0]).getObjectModel().get(PROCESS_KEY);
        desc.removeLastSitemapConfiguration();
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
    public static Object startProcessing(Environment env) {
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
    public static void endProcessing(Environment env, Object key) {
		env.finishingProcessing();
        final EnvironmentDescription desc = (EnvironmentDescription)key;
        desc.release();
        env.getObjectModel().remove(PROCESS_KEY);
    }

    /**
     * Return the current environment (for the cocoon: protocol)
     */
    public static Environment getCurrentEnvironment() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if (null != stack && !stack.empty()) {
            return (Environment) ((Object[])stack.getCurrent())[0];
        }
        return null;
    }

    /**
     * Return the current processor (for the cocoon: protocol)
     */
    public static Processor getCurrentProcessor() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if (null != stack && !stack.empty()) {
            return (Processor) ((Object[])stack.getCurrent())[1];
        }
        return null;
    }

    /**
     * Return the current environment stack (for the cocoon: protocol)
     */
    public static EnvironmentStack getCurrentEnvironmentStack() {
        return (EnvironmentStack)environmentStack.get();
    }

    /**
     * Get the current sitemap component manager.
     * This method return the current sitemap component manager. This
     * is the manager that holds all the components of the currently
     * processed (sub)sitemap.
     * FIXME: Do we really want to expose this?
     */
    static public ComponentManager getSitemapComponentManager() {
		final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();

		if ( null != stack && !stack.empty()) {
			Object[] o = (Object[])stack.peek();
			return (ComponentManager)o[2];
		}

        // if we don't have an environment yet, just return the root manager
        return rootManager;
    }
    
    /**
     * Configure the RoleManager
     */
    public void setRoleManager( final RoleManager roles ) {
        super.setRoleManager( roles );
        this.roleManager = roles;
    }

    /**
     * Return an instance of a component based on a Role.  The Role is usually the Interface's
     * Fully Qualified Name(FQN)--unless there are multiple Components for the same Role.  In that
     * case, the Role's FQN is appended with "Selector", and we return a ComponentSelector.
     */
    public Component lookup( final String role )
    throws ComponentException {
        if( null == role ) {
            final String message =
                "ComponentLocator Attempted to retrieve component with null role.";

            throw new ComponentException( role, message );
        }
        if ( role.equals(SourceResolver.ROLE) ) {
            if ( null == this.sourceResolver ) {
                this.sourceResolver = (SourceResolver) super.lookup( role );
            }
            return this;
        }

        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if ( null != stack && !stack.empty()) {
            final Object[] objects = (Object[])stack.getCurrent();
            final Map objectModel = ((Environment)objects[0]).getObjectModel();
            EnvironmentDescription desc = (EnvironmentDescription)objectModel.get(PROCESS_KEY);
            if ( null != desc ) {
                final Component component = desc.getRequestLifecycleComponent(role);
                if (null != component) {
                    return component;
                }
            }
        }

        final Component component = super.lookup( role );
        if (null != component && component instanceof RequestLifecycleComponent) {
            if (stack == null || stack.empty()) {
                throw new ComponentException(role, "ComponentManager has no Environment Stack.");
            }
            final Object[] objects = (Object[]) stack.getCurrent();
            final Map objectModel = ((Environment)objects[0]).getObjectModel();
            EnvironmentDescription desc = (EnvironmentDescription)objectModel.get(PROCESS_KEY);
            if ( null != desc ) {

                // first test if the parent CM has already initialized this component
                if ( !desc.containsRequestLifecycleComponent( role ) ) {
                    try {
                        if (component instanceof Recomposable) {
                            ((Recomposable) component).recompose(this);
                        }
                        ((RequestLifecycleComponent) component).setup((org.apache.cocoon.environment.SourceResolver)objects[0],
                                                                      objectModel);
                        if (component instanceof SitemapConfigurable) {
                            List configs = desc.getSitemapConfigurations();
                            for(int i=0; i < configs.size(); i++) {
                                Configuration parent = (Configuration)configs.get(i);
                                Configuration cc = parent.getChild( role, false );
                                if ( null != cc ) {
                                    ((SitemapConfigurable) component).setSitemapConfiguration(cc);
                                } else if ( null != this.roleManager) {

                                    // check for hint
                                    Configuration[] childs = parent.getChildren();
                                    if ( null != childs ) {
                                        for(int m = 0; m < childs.length; m++) {
                                            final String r = this.roleManager.getRoleForName(childs[m].getName());
                                            if ( role.equals(r) ) {
                                                ((SitemapConfigurable) component).setSitemapConfiguration(childs[m]);
                                                m = childs.length;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception local) {
                        throw new ComponentException(role, "Exception during setup of RequestLifecycleComponent.", local);
                    }
                    desc.addRequestLifecycleComponent(role, component, this);
                }
            }
        }
        return component;
    }

    /**
     * Release a Component.  This implementation makes sure it has a handle on the propper
     * ComponentHandler, and let's the ComponentHandler take care of the actual work.
     */
    public void release( final Component component ) {
        if( null == component ) {
            return;
        }

        if ( component instanceof RequestLifecycleComponent) {
            return;
        }
        if ( component == this ) {
            return;
        }
        super.release( component);
    }

    /**
     * Add an automatically released component
     */
    public static void addComponentForAutomaticRelease(final ComponentSelector selector,
                                                       final Component         component,
                                                       final ComponentManager  manager)
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
    public static void addComponentForAutomaticRelease(final ComponentManager manager,
                                                       final Component        component)
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
    public static void removeFromAutomaticRelease(final Component component)
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

    /**
     * Dispose
     */
    public void dispose() {
        if ( null != this.sourceResolver ) {
            super.release( this.sourceResolver );
            this.sourceResolver = null;
        }
        super.dispose();
    }

    /**
     * Get a <code>Source</code> object.
     */
    public Source resolveURI(final String location)
    throws MalformedURLException, IOException, SourceException {
        return this.resolveURI(location, null, null);
    }

    /**
     * Get a <code>Source</code> object.
     */
    public Source resolveURI(final String location,
                             String baseURI,
                             final Map    parameters)
    throws MalformedURLException, IOException, SourceException {
        if (baseURI == null) {
            final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
            if ( null != stack && !stack.empty()) {
                final Object[] objects = (Object[])stack.getCurrent();
                baseURI = ((Environment)objects[0]).getContext().toExternalForm();
            }
        }
        return this.sourceResolver.resolveURI(location, baseURI, parameters);
    }

    /**
     * Releases a resolved resource
     */
    public void release( final Source source ) {
        this.sourceResolver.release( source );
    }

}

final class EnvironmentDescription {
    
    private static final Configuration EMPTY_CONFIGURATION = 
            new DefaultConfiguration("config", "");
    
    Environment environment;
    Map         objectModel;
    Map         requestLifecycleComponents = new HashMap(5);
    List        autoreleaseComponents      = new ArrayList(2);
    List        sitemapConfigurations      = new ArrayList(4);    
    /**
     * Constructor
     */
    EnvironmentDescription(Environment env) {
        this.environment = env;
        this.objectModel = env.getObjectModel();
    }
    
    /**
     * Release all components of this environment
     * All RequestLifecycleComponents and autoreleaseComponents are
     * released.
     */
    void release() {
        final Iterator iter = this.requestLifecycleComponents.values().iterator();
        while (iter.hasNext()) {
            final Object[] o = (Object[])iter.next();
            final Component component = (Component)o[0];
            ((ComponentManager)o[1]).release( component );
        }
        
        for(int i = 0; i < autoreleaseComponents.size(); i++) {
            final Object[] o = (Object[])autoreleaseComponents.get(i);
            final Component component = (Component)o[0];
            if (o[1] instanceof ComponentManager) {
                ((ComponentManager)o[1]).release( component );
            } else {
                ((ComponentSelector)o[1]).release( component );
                if (o[2] != null) {
                    ((ComponentManager)o[2]).release( (Component)o[1] );
                }
            }
        }
        this.requestLifecycleComponents.clear();
        this.autoreleaseComponents.clear();
        this.sitemapConfigurations.clear();
        this.environment = null;
        this.objectModel = null;
    }
  

    /**
     * Add a RequestLifecycleComponent to the environment
     */
    void addRequestLifecycleComponent(final String role, 
                                      final Component co, 
                                      final ComponentManager manager) {
        this.requestLifecycleComponents.put(role, new Object[] {co, manager});
    }
    
    boolean containsRequestLifecycleComponent(final String role) {
        return this.requestLifecycleComponents.containsKey( role );
    }
    
    /**
     * Search a RequestLifecycleComponent
     */
    Component getRequestLifecycleComponent(final String role) {
        final Object[] o = (Object[])this.requestLifecycleComponents.get(role);
        if ( null != o ) {
            return (Component)o[0];
        }
        return null;
    }

    /**
     * Add an automatically released component
     */
    void addToAutoRelease(final ComponentSelector selector,
                          final Component         component,
                          final ComponentManager  manager) {
        this.autoreleaseComponents.add(new Object[] {component, selector, manager});
    }

    /**
     * Add an automatically released component
     */
    void addToAutoRelease(final ComponentManager manager,
                          final Component        component) {
        this.autoreleaseComponents.add(new Object[] {component, manager});
    }

    /**
     * Remove from automatically released components
     */
    void removeFromAutoRelease(final Component component)
    throws ProcessingException {
        int i = 0;
        boolean found = false;
        while (i < this.autoreleaseComponents.size() && !found) {
            final Object[] o = (Object[])this.autoreleaseComponents.get(i);
            if (o[0] == component) {
                found = true;
                if (o[1] instanceof ComponentManager) {
                    ((ComponentManager)o[1]).release( component );
                } else {
                    ((ComponentSelector)o[1]).release( component );
                    if (o[2] != null) {
                        ((ComponentManager)o[2]).release( (Component)o[1] );
                    }
                }
                this.autoreleaseComponents.remove(i);
            } else {
                i++;
            }
        }
        if (!found) {
            throw new ProcessingException("Unable to remove component from automatic release: component not found.");
        }
    }
    
          
    void addSitemapConfiguration(Configuration conf) {
        if (conf != null) {
            this.sitemapConfigurations.add(conf);
        } else {
            this.sitemapConfigurations.add(EMPTY_CONFIGURATION);
        }
    }
      
    List getSitemapConfigurations() {
        return this.sitemapConfigurations;
    }
 
    void removeLastSitemapConfiguration() {
        this.sitemapConfigurations.remove(this.sitemapConfigurations.size()-1);
    }               
}

final class CloningInheritableThreadLocal 
    extends InheritableThreadLocal {

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
    protected Object childValue(Object parentValue) {
        if ( null != parentValue) {
            return ((EnvironmentStack)parentValue).clone();
        } else {
            return null;
        }
    }
}

