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

import java.util.HashSet;
import java.util.Set;

import org.apache.avalon.excalibur.component.ExcaliburComponentSelector;
import org.apache.avalon.excalibur.component.RoleManager;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;

/**
 * An extension of <code>ExcaliburComponentSelector</code> that can have a parent
 * and accepts a wider variety of configurations.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ExtendedComponentSelector.java,v 1.4 2003/08/12 15:48:02 sylvain Exp $
 */

public class ExtendedComponentSelector 
    extends ExcaliburComponentSelector 
    implements ParentAware {

    /** The role manager */
    protected RoleManager roles;

    /** The parent selector, if any */
    protected ComponentSelector parentSelector;

    /** The parent locator, if any */
    protected ComponentLocator parentLocator;

    /** The class loader to use */
    protected ClassLoader classLoader;

    /** The components selected in the parent selector */
    protected Set parentComponents;

    /** The role of this selector. Set in <code>configure()</code>. */
    protected String roleName;

    /** The default hint */
    protected String defaultHint;

    /** This selector's location (used for debugging purposes) */
    private String location;

    public ExtendedComponentSelector()
    {
        super();
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    /** Create the ComponentSelector with a Classloader */
    public ExtendedComponentSelector(ClassLoader loader)
    {
        super(loader);

        if (loader == null) {
            this.classLoader = Thread.currentThread().getContextClassLoader();

        } else {
            this.classLoader = loader;
        }
    }

    /**
     * Get the name for component-instance elements (i.e. components not defined
     * by their role shortcut. If <code>null</code>, any element having a 'class'
     * attribute will be considered as a component instance.
     * <p>
     * The default here is to return <code>null</code>, and subclasses can redefine
     * this method to return particular values.
     *
     * @return <code>null</code>, but can be changed by subclasses
     */
    protected String getComponentInstanceName() {
        return null;
    }

    /**
     * Get the name of the attribute giving the class name of a component.
     * The default here is "class", but this can be overriden in subclasses.
     *
     * @return "<code>class</code>", but can be changed by subclasses
     */
    protected String getClassAttributeName() {
        return "class";
    }

    /**
     * Get the name of the attribute giving the default hint to use if
     * none is given. The default here is "default", but this can be
     * overriden in subclasses. If this method returns <code>null</code>,
     * no default hint can be specified.
     *
     * @return "<code>default</code>", but can be changed by subclasses
     */
    protected String getDefaultHintAttributeName() {
        return "default";
    }

    /**
     * Configure the RoleManager. Redeclared only because parent member is private.
     */
    public void setRoleManager(RoleManager roles) {
        super.setRoleManager(roles);
        this.roles = roles;
    }

    /**
     * Set the parent of this selector. This can be done after the selector is
     * initialized, but <em>only once</em>. This allows this selector to be
     * created by a component manager while still being able to have a parent.
     *
     * @param parent the parent selector
     * @throws IllegalStateException if parent is already set
     */
/*    public void setParentSelector(ComponentSelector parent) {
        if (this.parentSelector != null) {
            throw new IllegalStateException("Parent selector is already set");
        }
        this.parentSelector = parent;
        this.parentComponents = new HashSet();
    }
*/
    /**
     * Get the role name for this selector. This is called by <code>configure()</code>
     * to set the value of <code>this.roleName</code>.
     *
     * @return the role name, or <code>null<code> if it couldn't be determined.
     */
    protected String getRoleName(Configuration config) {
        // Get the role for this selector
        String roleName = config.getAttribute("role", null);
        if (roleName == null && this.roles != null) {
            roleName = this.roles.getRoleForName(config.getName());
        }

        return roleName;
    }

    /**
     * Configure this selector. This is the main difference with the parent class :
     * <ul>
     * <li>if {@link #getComponentInstanceName()} returns <code>null</code>,
     *     any child configurations having a attribute named as the result of
     *     {@link #getClassAttributeName()}, is considered as a component instance.
     * </li>
     * <li>if {@link #getComponentInstanceName()} returns a non-null value,
     *     only child configurations having this name are considered as a
     *     component instance.
     * </li>
     * <li>if other cases, it's name is considered to be a hint in the role manager.
     *     The behaviour is then the same as <code>ExcaliburComponentSelector</code>.
     * </li>
     *
     * @param config the configuration
     * @throws ConfigurationException if some hints aren't defined
     */
    public void configure(Configuration config) throws ConfigurationException {

        // Store location
        this.location = config.getLocation();

        this.roleName = getRoleName(config);

        // Pass a copy of the top-level object to superclass so that
        // our name is properly initialized
        // FIXME : could be avoided if parent m_role was protected or had protected accessors
        DefaultConfiguration temp = new DefaultConfiguration(config.getName(), config.getLocation());
        if (config.getAttribute("role", null) != null) {
            temp.setAttribute("role", this.roleName);
        }
        super.configure(temp);

        // Get default hint
        this.defaultHint = config.getAttribute(this.getDefaultHintAttributeName(), null);

        // Add components
        String compInstanceName = getComponentInstanceName();

        Configuration[] instances = config.getChildren();

        for (int i = 0; i < instances.length; i++) {

            Configuration instance = instances[i];

            Object hint = instance.getAttribute("name").trim();

            String classAttr = instance.getAttribute(getClassAttributeName(), null);
            String className;

            if (compInstanceName == null) {
                // component-instance implicitly defined by the presence of the 'class' attribute
                if (classAttr == null) {
                    className = this.roles.getDefaultClassNameForHint(roleName, instance.getName());
                } else {
                    className = classAttr.trim();
                }

            } else {
                // component-instances names explicitly defined
                if (compInstanceName.equals(instance.getName())) {
                    className = (classAttr == null) ? null : classAttr.trim();
                } else {
                    className = this.roles.getDefaultClassNameForHint(roleName, instance.getName());
                }
            }

            if (className == null) {
                String message = "Unable to determine class name for component named '" + hint +
                    "' at " + instance.getLocation();

                getLogger().error(message);
                throw new ConfigurationException(message);
            }

            try
            {
                Class clazz = this.classLoader.loadClass(className);
                addComponent(hint, clazz, instance);

            } catch(Exception e) {

                String message = "Could not load class " + className + " for component named '" +
                    hint + "' at " + instance.getLocation();

                getLogger().error(message, e);
                throw new ConfigurationException(message, e);
            }
        }
    }

    /**
     * Get the default hint, if any for this selector.
     */
    public String getDefaultHint() {
        return this.defaultHint;
    }

    public Component select(Object hint) throws ComponentException {

        if (hint == null) {
            hint = this.defaultHint;
        }

        if (parentSelector == null) {
            // No parent : default behaviour
            return super.select(hint);

        } else {

            // Try here first
            try {
                return super.select(hint);

            } catch(ComponentException ce) {
                // Doesn't exist here : try in parent selector
                Component component = this.parentSelector.select(hint);
                this.parentComponents.add(component);
                return component;
            }
        }
    }

    public void release(Component component) {
        // Was it selected on the parent ?
        if (this.parentComponents != null && this.parentComponents.remove(component)) {
            this.parentSelector.release(component);

        } else {
            super.release(component);
        }
    }

    /**
     * Does this selector or its parent have the given hint ?
     */
    public boolean hasComponent(Object hint) {
        boolean exists = super.hasComponent( hint );
        if ( !exists && this.parentSelector != null ) {
            exists = this.parentSelector.hasComponent( hint );
        }
        return exists;
    }
    
    /**
     * Does this selector declare a given hint? Check is performed on the components declared for this
     * selector only, and <strong>not</strong> those potentially inherited from the parent selector.
     * 
     * @param hint the hint to check for
     * @return <code>true</code> if this selector has the specified hint
     */
    protected boolean hasDeclaredComponent(Object hint) {
        return super.hasComponent(hint);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.ParentAware#setParentInformation(org.apache.avalon.framework.component.ComponentManager, java.lang.String)
     */
    public void setParentLocator(ComponentLocator locator)
    throws ComponentException {
        if (this.parentSelector != null) {
            throw new ComponentException(null, "Parent selector is already set");
        }
        this.parentLocator = locator;
        this.parentSelector = (ComponentSelector) locator.lookup();
        this.parentComponents = new HashSet();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        super.dispose();
        if ( this.parentLocator != null ) {
            this.parentLocator.release( this.parentSelector );
            this.parentLocator = null;
            this.parentSelector = null;
        }
    }

}
