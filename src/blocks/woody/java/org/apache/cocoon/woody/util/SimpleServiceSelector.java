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
package org.apache.cocoon.woody.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.LifecycleHelper;

/**
 * A very simple ServiceSelector for ThreadSafe services.
 */
public class SimpleServiceSelector extends AbstractLogEnabled implements ServiceSelector, Configurable, LogEnabled,
        Serviceable, Disposable {
    private final String hintShortHand;
    private final Class componentClass;
    private Map components = new HashMap();
    private ServiceManager serviceManager;

    public SimpleServiceSelector(String hintShortHand, Class componentClass) {
        this.hintShortHand = hintShortHand;
        this.componentClass = componentClass;
    }

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        Configuration[] componentConfs = configuration.getChildren(hintShortHand);
        for (int i = 0; i < componentConfs.length; i++) {
            String name = componentConfs[i].getAttribute("name");
            String src = componentConfs[i].getAttribute("src");

            Class clazz = null;
            try {
                clazz = Class.forName(src);
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException("Class not found: " + src + ", declared at " + componentConfs[i].getLocation(), e);
            }

            if (!componentClass.isAssignableFrom(clazz))
                throw new ConfigurationException("The class \"" + src + "\" is of an incorrect type, it should implement or exted " + componentClass.getName());

            Object component = null;
            try {
                component = clazz.newInstance();
                LifecycleHelper lifecycleHelper = new LifecycleHelper(getLogger(), null, serviceManager, null, componentConfs[i]);
                lifecycleHelper.setupComponent(component);
            } catch (Exception e) {
                throw new ConfigurationException("Error creating " + hintShortHand + " declared at " + componentConfs[i].getLocation(), e);
            }

            components.put(name, component);
        }
    }

    public Object select(Object hint) throws ServiceException {
        if (!isSelectable(hint))
            throw new ServiceException((String)hint, "Non-existing component for this hint");
        String stringHint = (String)hint;
        return components.get(stringHint);
    }

    public boolean isSelectable(Object hint) {
        String stringHint = (String)hint;
        return components.containsKey(stringHint);
    }

    public void release(Object o) {
    }

    public void dispose() {
        Iterator serviceIt = components.values().iterator();
        while (serviceIt.hasNext()) {
            Object service = serviceIt.next();
            if (service instanceof Disposable) {
                try {
                    ((Disposable)service).dispose();
                } catch (Exception e) {
                    getLogger().error("Error disposing service " + service, e);
                }
            }
        }
    }
}
