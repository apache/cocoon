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
package org.apache.cocoon.util;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.core.container.CoreServiceManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * Utility methods for JMX
 *
 */
public class JMXUtils {
    
    /** The {@link MBeanServer} first found */
    private static MBeanServer mbeanServer = getInitialMBeanServer();
    
    /**
     * Private c'tor: its a Utility class
     */
    private JMXUtils() {
        super();
    }

    /** Get the ev. found {@link MBeanServer} */
    public static MBeanServer getMBeanServer() {
        return JMXUtils.mbeanServer;
    }
    
    /**
     * Setup a component for possible JMX managability
     * @param bean The bean to estabilsh JMX for
     * @param info The component info
     */
    public static ObjectInstance setupJmxFor(final Object bean,
                                             final ComponentInfo info)
    {
        return setupJmxFor(bean, info, new ConsoleLogger(ConsoleLogger.LEVEL_INFO));
    }

    /**
     * Setup a component for possible JMX managability
     * @param bean The bean to estabilsh JMX for
     * @param info The component info
     * @param logger The Logger
     * @return
     */
    public static ObjectInstance setupJmxFor(final Object bean,
                                             final ComponentInfo info,
                                             final Logger logger)
    {
        if (getMBeanServer() != null) {
            final Class clazz = bean.getClass();
            final String mbeanClassName = clazz.getName() + "MBean";
            ObjectName on = null;
            Object comp = null;
            try {
                final Class mbeanClass = clazz.getClassLoader().loadClass(mbeanClassName);
                final Constructor ctor = mbeanClass.getConstructor(new Class[] {clazz});
                final Object mbean = ctor.newInstance( new Object[] {bean});
                final String jmxDomain = info.getJmxDomain();
                String jmxGroup = info.getConfiguration().getAttribute( CoreServiceManager.JMX_NAME_ATTR_NAME, null );
                if (jmxGroup == null ) {
                    // otherwise construct one from the service class name
                    final StringBuffer sb = new StringBuffer();
                    final List groups = new ArrayList();
                    int i = info.getServiceClassName().indexOf('.');
                    int j = 0;
                    while(i > 0) {
                        groups.add(info.getServiceClassName().substring(j,i));
                        j = i+1;
                        i = info.getServiceClassName().indexOf('.', i+1);
                    }
                    groups.add(info.getServiceClassName().substring(j));
                    for (i = 0; i < groups.size()-1; i++) {
                        sb.append("group");
                        if (i > 0) {
                            sb.append(i);
                        }
                        sb.append('=');
                        sb.append(groups.get(i));
                        sb.append(',');
                    }
                    sb.append("item=").append(groups.get(groups.size()-1));
                    jmxGroup = sb.toString();
                }
                on = new ObjectName( jmxDomain + ":" + jmxGroup );
                return mbeanServer.registerMBean(mbean,on);
            } catch (final ClassNotFoundException cnfe) {
                // happens if a component doesn't have a MBean to support it for management
                logger.debug( "Class "+info.getServiceClassName()+" doesn't have a supporting MBean called " + mbeanClassName );
            } catch (final NoSuchMethodException nsme) {
                logger.warn( "MBean " + mbeanClassName + " doesn't have a constructor that accepts an instance of " + info.getServiceClassName(), nsme);
            } catch (final InvocationTargetException ite) {
                logger.warn( "Cannot instantiate class " + mbeanClassName, ite);
            } catch (final InstantiationException ie) {
                logger.warn( "Cannot instantiate class " + mbeanClassName, ie);
            } catch (final IllegalAccessException iae) {
                logger.warn( "Cannot instantiate class " + mbeanClassName, iae);
            } catch (final MalformedObjectNameException mone) {
                logger.warn( "Invalid ObjectName '" + on + "' for MBean " + mbeanClassName, mone);
            } catch (final InstanceAlreadyExistsException iaee) {
                logger.warn( "Instance for MBean " + mbeanClassName + "already exists", iaee);
            } catch (final NotCompliantMBeanException ncme) {
                logger.warn( "Not compliant MBean " + mbeanClassName, ncme);
            } catch (final MBeanRegistrationException mre) {
                logger.warn( "Cannot register MBean " + mbeanClassName, mre);
            }
        }
        return null;
    }

    public static String findJmxDomain(final String pJmxDomain, final ServiceManager serviceManager) {
        // try to find a JMX domain name first from this component configuration give as parameter
        String jmxDomain = pJmxDomain;
        if( jmxDomain == null )
        {
            // next from the CoreServiceManager managing this component
            if( serviceManager != null && serviceManager instanceof CoreServiceManager )
            {
                // next from the CoreServiceManager managing this component
                jmxDomain = ((CoreServiceManager)serviceManager).getJmxDefaultDomain();
            } else {
                // otherwise use default one
                jmxDomain = CoreServiceManager.JMX_DEFAULT_DOMAIN_NAME;
            }
        }
        return jmxDomain;
    }

    public static String findJmxName(final String pJmxName, final String pClassName) {
        String jmxName = pJmxName;
        final String className = (pClassName == null ? "unknown" : pClassName);
        if (jmxName == null ) {
            // otherwise construct one from the service class name
            final StringBuffer sb = new StringBuffer();
            final List groups = new ArrayList();
            int i = className.indexOf('.');
            int j = 0;
            while(i > 0) {
                groups.add(className.substring(j,i));
                j = i+1;
                i = className.indexOf('.', i+1);
            }
            groups.add(className.substring(j));
            for (i = 0; i < groups.size()-1; i++) {
                sb.append("group");
                if (i > 0) {
                    sb.append(i);
                }
                sb.append('=');
                sb.append(groups.get(i));
                sb.append(',');
            }
            sb.append("item=").append(groups.get(groups.size()-1));
            jmxName = sb.toString();
        }
        return jmxName;
    }
    
    private static MBeanServer getInitialMBeanServer() {
        final List servers = MBeanServerFactory.findMBeanServer(null);
        if( servers.size() > 0 ) {
            return (MBeanServer)servers.get(0);
        }
        return null;
    }
}
