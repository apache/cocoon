/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components;

import java.util.Map;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Collections;

import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;

import org.apache.avalon.AbstractLoggable;

import org.apache.avalon.configuration.ConfigurationException;

/** Default RoleInfo implementation
 * @author <a href="mailto:ricardo@apache,org">Ricardo Rocha</a>
 * @author <a href="mailto:giacomo@apache,org">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-04-05 15:40:36 $
 */
public class DefaultRoleInfo extends AbstractLoggable implements RoleInfo, Configurable {
    private Map shorthands = new Hashtable();
    private Map classNames = new Hashtable();

    public String lookup(String shorthandName) {
        getLogger().debug("looking up role " + shorthandName + ", returning " + (String) this.shorthands.get(shorthandName));
        return (String) this.shorthands.get(shorthandName);
    }

    public Iterator shorthandNames() {
        return Collections.unmodifiableMap(this.shorthands).keySet().iterator();
    }


    public String defaultClass(String role) {
        return (String) this.classNames.get(role);
    }

    public void addRole(String name, String shorthand, String defaultClassName) {
        this.shorthands.put(shorthand, name);

        if (defaultClassName != null) {
            this.classNames.put(name, defaultClassName);
        }
    }

    public void configure(Configuration conf) throws ConfigurationException {
        Configuration[] roles = conf.getChildren("role");

        for (int i = 0; i < roles.length; i++) {
            String name = roles[i].getAttribute("name");
            String shorthand = roles[i].getAttribute("shorthand");
            String defaultClassName = roles[i].getAttribute("default-class", null);

            this.addRole(name, shorthand, defaultClassName);
            getLogger().debug("added Role " + name + " with shorthand " + shorthand + " for " + defaultClassName);
        }
    }
}