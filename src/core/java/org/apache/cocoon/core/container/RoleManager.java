/* 
 * Copyright 2002-2004 The Apache Software Foundation
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * Default RoleManager implementation.  It populates the RoleManager
 * from a configuration file.
 *
 * @version CVS $Id: RoleManager.java 55144 2004-10-20 12:26:09Z ugo $
 */
public class RoleManager
extends AbstractLogEnabled
implements Configurable {
    
    /** Map for shorthand to role mapping */
    private Map shorthands;

    /** Map for role to default classname mapping */
    private Map classNames;

    /** Map for role->key to classname mapping */
    private Map keyClassNames;

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
    public final String getDefaultClassNameForRole( final String role ) {
        final String className = (String)this.classNames.get( role );

        if( null == className && null != this.parent ) {
            return this.parent.getDefaultClassNameForRole( role );
        }

        return className;
    }

    /**
     * Retrieves a default class name for a role/key combination.
     * This is only called when a role is mapped to a
     * CocoonServiceSelector, and the configuration elements use
     * shorthand names for the type of component.  If this RoleManager
     * does not have the match, and there is a parent RoleManager, the
     * parent will be asked to resolve the class name.
     *
     * @param role  The role that this shorthand refers to.
     * @param shorthand  The shorthand name for the type of component
     * @return the FQCN for the role/key combination.
     */
    public final String getDefaultClassNameForKey( final String role,
                                                   final String shorthand ) {
        if( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug( "looking up keymap for role " + role );
        }

        final Map keyMap = (Map)this.keyClassNames.get( role );

        if( null == keyMap ) {
            if( null != this.parent ) {
                return this.parent.getDefaultClassNameForKey( role, shorthand );
            } 
            return "";
        }

        if( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug( "looking up classname for key " + shorthand );
        }

        final String s = ( String ) keyMap.get( shorthand );

        if( s == null && null != this.parent ) {
            return this.parent.getDefaultClassNameForKey( role, shorthand );
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
        final Map shorts = new HashMap();
        final Map classes = new HashMap();
        final Map keyclasses = new HashMap();

        final Configuration[] roles = configuration.getChildren( "role" );

        for( int i = 0; i < roles.length; i++ ) {
            final String name = roles[ i ].getAttribute( "name" );
            final String shorthand = roles[ i ].getAttribute( "shorthand" );
            final String defaultClassName = roles[ i ].getAttribute( "default-class", null );

            shorts.put( shorthand, name );

            if( null != defaultClassName ) {
                classes.put( name, defaultClassName );
            }

            final Configuration[] keys = roles[ i ].getChildren( "hint" );
            if( keys.length > 0 ) {
                HashMap keyMap = new HashMap();

                for( int j = 0; j < keys.length; j++ ) {
                    final String shortHand = keys[ j ].getAttribute( "shorthand" ).trim();
                    String className = keys[ j ].getAttribute( "class" ).trim();

                    keyMap.put( shortHand, className );
                    if( this.getLogger().isDebugEnabled() ) {
                        this.getLogger().debug( "Adding key type " + shortHand +
                                                " associated with role " + name +
                                                " and class " + className );
                    }
                }

                keyclasses.put( name, Collections.unmodifiableMap( keyMap ) );
            }

            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( "added Role " + name + " with shorthand " +
                                   shorthand + " for " + defaultClassName );
            }
        }

        this.shorthands = Collections.unmodifiableMap( shorts );
        this.classNames = Collections.unmodifiableMap( classes );
        this.keyClassNames = Collections.unmodifiableMap( keyclasses );
    }
}
