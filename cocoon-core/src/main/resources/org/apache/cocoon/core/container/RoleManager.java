/* 
 * Copyright 2002-2005 The Apache Software Foundation
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
package org.apache.cocoon.core.container;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.components.ComponentInfo;

/**
 * Default RoleManager implementation.  It populates the RoleManager
 * from a configuration file.
 *
 * @version $Id$
 * @since 2.2
 */
public class RoleManager
extends AbstractLogEnabled
implements Configurable {
    
    /** Map for shorthand to role mapping */
    private final Map shorthands = new HashMap();

    /** Map for role to default classname mapping */
    private final Map classNames = new HashMap();

    /** Map for role->key to classname mapping */
    private final Map keyClassNames = new HashMap();

    /** Parent role manager for nested resolution */
    private final RoleManager parent;

    /**
     * Default constructor--this RoleManager has no parent.
     */
    public RoleManager() {
        this.parent = null;
    }

    /**
     * Alternate constructor--this RoleManager has the specified
     * parent.
     *
     * @param parent  The parent <code>RoleManager</code>.
     */
    public RoleManager( RoleManager parent ) {
        this.parent = parent;
    }

    /**
     * Retrieves the real role name from a shorthand name.  Usually
     * the shorthand name refers to a configuration element name.  If
     * this RoleManager does not have the match, and there is a parent
     * RoleManager, the parent will be asked to resolve the role.
     *
     * @param shorthandName  The shortname that is an alias for the role.
     * @return the official role name.
     */
    public final String getRoleForName( final String shorthandName ) {
        final String role = (String)this.shorthands.get( shorthandName );

        if( null == role && null != this.parent ) {
            return this.parent.getRoleForName( shorthandName );
        }

        if( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug( "looking up shorthand " + shorthandName +
                               ", returning " + role );
        }

        return role;
    }

    /**
     * Retrieves the default class name for the specified role.  This
     * is only called when the configuration does not specify the
     * class explicitly.  If this RoleManager does not have the match,
     * and there is a parent RoleManager, the parent will be asked
     * to resolve the class name.
     *
     * @param role  The role that has a default implementation.
     * @return the Fully Qualified Class Name (FQCN) for the role.
     */
    public final ComponentInfo getDefaultServiceInfoForRole( final String role ) {
        final ComponentInfo info = (ComponentInfo)this.classNames.get( role );

        if( info == null && this.parent != null ) {
            return this.parent.getDefaultServiceInfoForRole( role );
        }

        return info;
    }

    /**
     * Retrieves a default class name for a role/key combination.
     * This is only called when a role is mapped to a
     * StandaloneServiceSelector, and the configuration elements use
     * shorthand names for the type of component.  If this RoleManager
     * does not have the match, and there is a parent RoleManager, the
     * parent will be asked to resolve the class name.
     *
     * @param role  The role that this shorthand refers to.
     * @param shorthand  The shorthand name for the type of component
     * @return the FQCN for the role/key combination.
     */
    public final ComponentInfo getDefaultServiceInfoForKey( final String role,
                                                          final String shorthand ) {
        if( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug( "looking up keymap for role " + role );
        }

        final Map keyMap = (Map)this.keyClassNames.get( role );

        if( null == keyMap ) {
            if( null != this.parent ) {
                return this.parent.getDefaultServiceInfoForKey( role, shorthand );
            } 
            return null;
        }

        if( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug( "looking up classname for key " + shorthand );
        }

        final ComponentInfo s = ( ComponentInfo ) keyMap.get( shorthand );

        if( s == null && this.parent != null ) {
            return this.parent.getDefaultServiceInfoForKey( role, shorthand );
        } 
        return s;
    }

    /**
     * Reads a configuration object and creates the role, shorthand,
     * and class name mapping.
     *
     * @param configuration  The configuration object.
     * @throws ConfigurationException if the configuration is malformed
     */
    public final void configure( final Configuration configuration )
    throws ConfigurationException {
        
        // When reading a roles file, we only want "role" elements.
        boolean strictMode = "roles-list".equals(configuration.getName());

        final Configuration[] roles = configuration.getChildren();

        for( int i = 0; i < roles.length; i++ ) {
            Configuration role = roles[i];
            
            if (!"role".equals(role.getName())) {
                if (strictMode) {
                    throw new ConfigurationException("Unexpected '" + role.getName() + "' element at " + role.getLocation());
                }
                // Skip to next one
                continue;
            }
            
            final String roleName = role.getAttribute("name");
            final String shorthand = role.getAttribute("shorthand", null);
            final String defaultClassName = role.getAttribute("default-class", null);

            if (shorthand != null) {
                // Store the shorthand and check that its consistent with any previous one
                Object previous = this.shorthands.put( shorthand, roleName );
                if (previous != null && !previous.equals(roleName)) {
                    throw new ConfigurationException("Shorthand '" + shorthand + "' already used for role " +
                            previous + ": inconsistent declaration at " + role.getLocation());
                }
            }

            if( defaultClassName != null ) {
                ComponentInfo info = (ComponentInfo)this.classNames.get(roleName);
                if (info == null) {
                    // Create a new info and store it
                    info = new ComponentInfo();
                    info.setServiceClassName(defaultClassName);
                    info.fill(role);
                    info.setRole(roleName);
                    this.classNames.put(roleName, info);
                } else {
                    // Check that it's consistent with the existing info
                    if (!defaultClassName.equals(info.getServiceClassName())) {
                        throw new ConfigurationException("Invalid redeclaration: default class already set to " + info.getServiceClassName() +
                                " for role " + roleName + " at " + role.getLocation());
                    }
                    //FIXME: should check also other ServiceInfo members
                }
            }

            final Configuration[] keys = role.getChildren( "hint" );
            if( keys.length > 0 ) {
                Map keyMap = (Map)this.keyClassNames.get(roleName);
                if (keyMap == null) {
                    keyMap = new HashMap();
                    this.keyClassNames.put(roleName, keyMap);
                }

                for( int j = 0; j < keys.length; j++ ) {
                    Configuration key = keys[j];
                    
                    final String shortHand = key.getAttribute( "shorthand" ).trim();
                    final String className = key.getAttribute( "class" ).trim();

                    ComponentInfo info = (ComponentInfo)keyMap.get(shortHand);
                    if (info == null) {       
                        info = new ComponentInfo();
                        info.setServiceClassName(className);
                        info.fill(key);
    
                        keyMap.put( shortHand, info );
                        if( this.getLogger().isDebugEnabled() ) {
                            this.getLogger().debug( "Adding key type " + shortHand +
                                                    " associated with role " + roleName +
                                                    " and class " + className );
                        }
                    } else {
                        // Check that it's consistent with the existing info
                        if (!className.equals(info.getServiceClassName())) {
                            throw new ConfigurationException("Invalid redeclaration: class already set to " + info.getServiceClassName() +
                                    " for hint " + shortHand + " at " + key.getLocation());
                        }
                        //FIXME: should check also other ServiceInfo members
                    }
                }
            }

            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( "added Role " + roleName + " with shorthand " +
                                   shorthand + " for " + defaultClassName );
            }
        }
    }
}
