/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.excalibur.component.ExcaliburComponentManager;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.xml.XMLConsumer;
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
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id$
 */
public final class CocoonComponentManager extends ExcaliburComponentManager
                                          implements SourceResolver {

    /** The key used to store the current process environment */
    private static final String PROCESS_KEY = CocoonComponentManager.class.getName();
    
    /** The environment attribute used to keep track of the actual environment in which the pipeline was built. */
    private static final String PROCESSOR_ATTR = "CocoonComponentManager.processor";

    /** The environment information */
    private static InheritableThreadLocal environmentStack = new CloningInheritableThreadLocal();

    /** The configured {@link SourceResolver} */
    private SourceResolver sourceResolver;

    /** The {@link SitemapConfigurationHolder}s */
    private Map sitemapConfigurationHolders = new HashMap(15);

    /** The parent component manager for implementing parent aware components */
    private ComponentManager parentManager;

    /** Temporary list of parent-aware components.  Will be null for most of
     * our lifecycle. */
    private ArrayList parentAwareComponents = new ArrayList();

    /** has this been disposed? */
    private boolean wasDisposed;

    /** Create the ComponentManager */
    public CocoonComponentManager() {
        super( null, Thread.currentThread().getContextClassLoader() );
    }

    /** Create the ComponentManager with a Classloader */
    public CocoonComponentManager(final ClassLoader loader) {
        super( null, loader );
    }

    /** Create the ComponentManager with a Classloader and parent ComponentManager */
    public CocoonComponentManager(final ComponentManager manager, final ClassLoader loader) {
        super( manager, loader );
        this.parentManager = manager;
    }

    /** Create the ComponentManager with a parent ComponentManager */
    public CocoonComponentManager(final ComponentManager manager) {
        super( manager);
        this.parentManager = manager;
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

        EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
		if (stack == null) {
            stack = new EnvironmentStack();
			environmentStack.set(stack);
		}
		stack.push(new Object[] {env, processor, manager, new Integer(stack.getOffset())});
        stack.setOffset(stack.size()-1);

        env.setAttribute(PROCESSOR_ATTR, processor);
    }

    /**
     * This hook must be called by the sitemap each time a sitemap is left.
     * It's the counterpart to {@link #enterEnvironment(Environment, ComponentManager, Processor)}.
     */
    public static void leaveEnvironment() {
        leaveEnvironment(false);
    }

    /**
     * This hook must be called by the sitemap each time a sitemap is left.
     * It's the counterpart to {@link #enterEnvironment(Environment, ComponentManager, Processor)}.
     * 
     * @param success indicates if the request was successfully handled by the environment that's being left
     */
    public static void leaveEnvironment(boolean success) {
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
                    final Component c = (Component)o[0];
                    ((CocoonComponentManager)o[1]).releaseRLComponent( c );
                }
            }
            env.removeAttribute(GlobalRequestLifecycleComponent.class.getName());
        } else {
            if (!success) {
                // Restore the current environment as being the active one
                getCurrentEnvironment().setAttribute(PROCESSOR_ATTR, getCurrentProcessor());
            }
        }
    }

    /**
     * INTERNAL METHOD. Do not use, can be removed without warning or deprecation cycle.
     */
    public static int markEnvironment() {
        // TODO (CZ): This is only for testing - remove it later on. See also Cocoon.java.
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if (stack != null) {
            return stack.size();
        }

        return 0;
    }

    /**
     * INTERNAL METHOD. Do not use, can be removed without warning or deprecation cycle.
     */
    public static void checkEnvironment(int depth, Logger logger)
    throws Exception {
        // TODO (CZ): This is only for testing - remove it later on. See also Cocoon.java.
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        int currentDepth = stack != null? stack.size() : 0;
        if (currentDepth != depth) {
            logger.error("ENVIRONMENT STACK HAS NOT BEEN CLEANED PROPERLY!");
            throw new ProcessingException("Environment stack has not been cleaned up properly. " +
                                          "Please report this (and if possible, together with a test case) " +
                                          "to the Cocoon developers.");
        }
    }

    /**
     * Create an environment aware xml consumer for the cocoon
     * protocol
     */
    public static XMLConsumer createEnvironmentAwareConsumer(XMLConsumer consumer) {
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
    public static Object startProcessing(Environment env) {
		if (null == env) {
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
        if (null != stack && !stack.isEmpty()) {
            return (Environment) ((Object[])stack.getCurrent())[0];
        }
        return null;
    }

    /**
     * Return the current processor (for the cocoon: protocol)
     */
    public static Processor getCurrentProcessor() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if (null != stack && !stack.isEmpty()) {
            return (Processor) ((Object[])stack.getCurrent())[1];
        }
        return null;
    }

    /**
     * Return the processor that has actually processed the request
     */
    public static Processor getActiveProcessor() {
        return (Processor) getCurrentEnvironment().getAttribute(PROCESSOR_ATTR);
    }

    /**
     * Get the current sitemap component manager.
     * This method return the current sitemap component manager. This
     * is the manager that holds all the components of the currently
     * processed (sub)sitemap.
     */
    static public ComponentManager getSitemapComponentManager() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if (null != stack && !stack.isEmpty()) {
            Object[] o = (Object[]) stack.peek();
            return (ComponentManager) o[2];
        }

        // If we don't have an environment yet, just return null
        return null;
    }

    /**
     * Return an instance of a component based on a Role.  The Role is usually the Interface's
     * Fully Qualified Name(FQN)--unless there are multiple Components for the same Role.  In that
     * case, the Role's FQN is appended with "Selector", and we return a ComponentSelector.
     */
    public Component lookup(final String role)
    throws ComponentException {
        if (null == role) {
            final String message =
                "ComponentLocator Attempted to retrieve component with null role.";
            throw new ComponentException(role, message);
        }

        if (role.equals(SourceResolver.ROLE)) {
            if (null == this.sourceResolver) {
                if(wasDisposed) {
                    // (BD) working on bug 27249: I think we could throw an Exception here, as
                    // the following call fails anyway, but I'm not sure enough ;-)
                    getLogger().warn("Trying to lookup SourceResolver on disposed CocoonComponentManager");
                }
                this.sourceResolver = (SourceResolver) super.lookup( role );
            }
            return this;
        }

        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if ( null != stack && !stack.isEmpty()) {
            final Object[] objects = (Object[])stack.getCurrent();
            final Map objectModel = ((Environment)objects[0]).getObjectModel();
            EnvironmentDescription desc = (EnvironmentDescription)objectModel.get(PROCESS_KEY);
            if ( null != desc ) {
                Component component = desc.getRequestLifecycleComponent(role);
                if (null != component) {
                    return component;
                }
                component = desc.getGlobalRequestLifecycleComponent(role);
                if (null != component) {
                    return component;
                }
            }
        }

        final Component component = super.lookup(role);

        if (component != null && component instanceof RequestLifecycleComponent) {
            if (stack == null || stack.isEmpty()) {
                throw new ComponentException(role, "ComponentManager has no Environment Stack.");
            }

            final Object[] objects = (Object[]) stack.getCurrent();
            final Map objectModel = ((Environment) objects[0]).getObjectModel();
            EnvironmentDescription desc = (EnvironmentDescription) objectModel.get(PROCESS_KEY);
            if (null != desc) {
                // first test if the parent CM has already initialized this component
                if (!desc.containsRequestLifecycleComponent(role)) {
                    try {
                        if (component instanceof Recomposable) {
                            ((Recomposable) component).recompose(this);
                        }
                        ((RequestLifecycleComponent) component).setup((org.apache.cocoon.environment.SourceResolver)objects[0],
                                                                      objectModel);
                    } catch (Exception local) {
                        throw new ComponentException(role, "Exception during setup of RequestLifecycleComponent.", local);
                    }
                    desc.addRequestLifecycleComponent(role, component, this);
                }
            }
        }

        if (component != null && component instanceof GlobalRequestLifecycleComponent) {
            if (stack == null || stack.isEmpty()) {
                throw new ComponentException(role, "ComponentManager has no Environment Stack.");
            }

            final Object[] objects = (Object[]) stack.getCurrent();
            final Map objectModel = ((Environment)objects[0]).getObjectModel();
            EnvironmentDescription desc = (EnvironmentDescription) objectModel.get(PROCESS_KEY);
            if (null != desc) {
                // first test if the parent CM has already initialized this component
                if ( !desc.containsGlobalRequestLifecycleComponent( role ) ) {
                    try {
                        if (component instanceof Recomposable) {
                            ((Recomposable) component).recompose(this);
                        }
                        ((GlobalRequestLifecycleComponent) component).setup((org.apache.cocoon.environment.SourceResolver)objects[0],
                                                                      objectModel);
                    } catch (Exception local) {
                        throw new ComponentException(role, "Exception during setup of RequestLifecycleComponent.", local);
                    }
                    desc.addGlobalRequestLifecycleComponent(role, component, this);
                }
            }
        }

        if (component != null && component instanceof SitemapConfigurable) {
            // FIXME: how can we prevent that this is called over and over again?
            SitemapConfigurationHolder holder;

            holder = (SitemapConfigurationHolder) this.sitemapConfigurationHolders.get(role);
            if (null == holder) {
                // create new holder
                holder = new DefaultSitemapConfigurationHolder(role);
                this.sitemapConfigurationHolders.put(role, holder);
            }

            try {
                ((SitemapConfigurable)component).configure(holder);
            } catch (ConfigurationException ce) {
                throw new ComponentException(role, "Exception during setup of SitemapConfigurable.", ce);
            }
        }

        return component;
    }

    /**
     * Release a Component.  This implementation makes sure it has a handle on the propper
     * ComponentHandler, and let's the ComponentHandler take care of the actual work.
     */
    public void release(final Component component) {
        if (null == component) {
            return;
        }

        if (component instanceof RequestLifecycleComponent
                || component instanceof GlobalRequestLifecycleComponent) {
            return;
        }

        if (component == this) {
            return;
        }

        super.release(component);
    }

    /**
     * Release a RequestLifecycleComponent
     */
    protected void releaseRLComponent(final Component component) {
        super.release(component);
    }

    /**
     * Add an automatically released component
     */
    public static void addComponentForAutomaticRelease(final ComponentSelector selector,
                                                       final Component         component,
                                                       final ComponentManager  manager)
    throws ProcessingException {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if ( null != stack && !stack.isEmpty()) {
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
        if ( null != stack && !stack.isEmpty()) {
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
        if ( null != stack && !stack.isEmpty()) {
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
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("CocoonComponentManager.dispose() called");
        }

        if (null != this.sourceResolver) {
            super.release(this.sourceResolver);
            // We cannot null out sourceResolver here yet as some other not
            // disposed yet components might still have unreleased sources,
            // and they will call {@link #release(Source)} during their
            // dispose().
        }

        super.dispose();

        // All components now are released so sourceResolver should be not
        // needed anymore.
        this.sourceResolver = null;

        // This is used to track bug 27249
        this.wasDisposed = true;
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
            if ( null != stack && !stack.isEmpty()) {
                final Object[] objects = (Object[])stack.getCurrent();
                baseURI = ((Environment)objects[0]).getContext();
            }
        }
        return this.sourceResolver.resolveURI(location, baseURI, parameters);
    }

    /**
     * Releases a resolved resource
     */
    public void release(final Source source) {
        this.sourceResolver.release(source);
    }


    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.component.ExcaliburComponentManager#addComponent(java.lang.String, java.lang.Class, org.apache.avalon.framework.configuration.Configuration)
     */
    public void addComponent(String role, Class clazz, Configuration conf)
    throws ComponentException {
        super.addComponent(role, clazz, conf);
        // Note that at this point, we're not initialized and cannot do
        // lookups, so defer parental introductions to initialize().
        if (ParentAware.class.isAssignableFrom(clazz)) {
            parentAwareComponents.add(role);
        }
    }

    public void initialize() throws Exception {
        super.initialize();
        if (parentAwareComponents == null) {
            throw new ComponentException(null, "CocoonComponentManager already initialized");
        }
        // Set parents for parentAware components
        Iterator iter = parentAwareComponents.iterator();
        while (iter.hasNext()) {
            String role = (String)iter.next();
            getLogger().debug(".. "+role);
            if ( parentManager != null && parentManager.hasComponent( role ) ) {
                // lookup new component
                Component component = null;
                try {
                    component = this.lookup( role );
                    ((ParentAware)component).setParentLocator( new ComponentLocatorImpl(this.parentManager, role ));
                } catch (ComponentException ignore) {
                    // we don't set the parent then
                } finally {
                    this.release( component );
                }
            }
        }
        parentAwareComponents = null;  // null to save memory, and catch logic bugs.
    }
}

final class EnvironmentDescription {

    Environment environment;
    Map         objectModel;
    Map         requestLifecycleComponents;
    List        autoreleaseComponents      = new ArrayList(4);

    /**
     * Constructor
     */
    EnvironmentDescription(Environment env) {
        this.environment = env;
        this.objectModel = env.getObjectModel();
    }

    Map getGlobalRequestLifcecycleComponents() {
        Map m = (Map)environment.getAttribute(GlobalRequestLifecycleComponent.class.getName());
        if ( m == null ) {
            m = new HashMap();
            environment.setAttribute(GlobalRequestLifecycleComponent.class.getName(), m);
        }
        return m;
    }

    /**
     * Release all components of this environment
     * All RequestLifecycleComponents and autoreleaseComponents are
     * released.
     */
    synchronized void release() {
        if ( this.requestLifecycleComponents != null ) {
            final Iterator iter = this.requestLifecycleComponents.values().iterator();
            while (iter.hasNext()) {
                final Object[] o = (Object[])iter.next();
                final Component component = (Component)o[0];
                ((CocoonComponentManager)o[1]).releaseRLComponent( component );
            }
            this.requestLifecycleComponents.clear();
        }

        for (int i = 0; i < autoreleaseComponents.size(); i++) {
            final Object[] o = (Object[])autoreleaseComponents.get(i);
            final Component component = (Component)o[0];
            if (o[1] instanceof ComponentManager) {
                ((ComponentManager)o[1]).release( component );
            } else {
                ((ComponentSelector) o[1]).release(component);
                if (o[2] != null) {
                    ((ComponentManager) o[2]).release((Component) o[1]);
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
    void addRequestLifecycleComponent(final String role,
                                      final Component co,
                                      final ComponentManager manager) {
        if ( this.requestLifecycleComponents == null ) {
            this.requestLifecycleComponents = new HashMap();
        }
        this.requestLifecycleComponents.put(role, new Object[] {co, manager});
    }

    /**
     * Add a GlobalRequestLifecycleComponent to the environment
     */
    void addGlobalRequestLifecycleComponent(final String role,
                                      final Component co,
                                      final ComponentManager manager) {
        this.getGlobalRequestLifcecycleComponents().put(role, new Object[] {co, manager});
    }

    /**
     * Do we already have a request lifecycle component
     */
    boolean containsRequestLifecycleComponent(final String role) {
        if ( this.requestLifecycleComponents == null ) {
            return false;
        }
        return this.requestLifecycleComponents.containsKey( role );
    }

    /**
     * Do we already have a global request lifecycle component
     */
    boolean containsGlobalRequestLifecycleComponent(final String role) {
        return this.getGlobalRequestLifcecycleComponents().containsKey( role );
    }

    /**
     * Search a RequestLifecycleComponent
     */
    Component getRequestLifecycleComponent(final String role) {
        if ( this.requestLifecycleComponents == null ) {
            return null;
        }
        final Object[] o = (Object[])this.requestLifecycleComponents.get(role);
        if ( null != o ) {
            return (Component)o[0];
        }
        return null;
    }

    /**
     * Search a GlobalRequestLifecycleComponent
     */
    Component getGlobalRequestLifecycleComponent(final String role) {
        final Object[] o = (Object[])this.getGlobalRequestLifcecycleComponents().get(role);
        if ( null != o ) {
            return (Component)o[0];
        }
        return null;
    }

    /**
     * Add an automatically released component
     */
    synchronized void addToAutoRelease(final ComponentSelector selector,
                                       final Component         component,
                                       final ComponentManager  manager) {
        this.autoreleaseComponents.add(new Object[] {component, selector, manager});
    }

    /**
     * Add an automatically released component
     */
    synchronized void addToAutoRelease(final ComponentManager manager,
                                       final Component        component) {
        this.autoreleaseComponents.add(new Object[] {component, manager});
    }

    /**
     * Remove from automatically released components
     */
    synchronized void removeFromAutoRelease(final Component component)
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

