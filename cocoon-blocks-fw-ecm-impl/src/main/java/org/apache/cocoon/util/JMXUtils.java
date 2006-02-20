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
package org.apache.cocoon.util;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.ComponentInfo;

import org.apache.cocoon.core.container.CoreServiceManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
 * @version $Id$
 * 
 * @since 2.2
 */
public class JMXUtils {
    //~ Static fields/initializers ----------------------------------------------------------------------

    /** The {@link MBeanServer} first found */
    private static MBeanServer mbeanServer = getInitialMBeanServer();

    //~ Constructors ------------------------------------------------------------------------------------

    /**
     * Private c'tor: its a Utility class
     */
    private JMXUtils() {
        super();
    }

    //~ Methods -----------------------------------------------------------------------------------------

    /**
     * Find a JMX domain name at various places
     *
     * @param pJmxDomain Default JMX domain name
     * @param serviceManager The ServiceManager used
     *
     * @return The JMX domain name
     */
    public static String findJmxDomain(final String pJmxDomain,
                                       final ServiceManager serviceManager) {
        // try to find a JMX domain name first from this component configuration give as parameter
        String jmxDomain = pJmxDomain;

        if(jmxDomain == null) {
            // next from the CoreServiceManager managing this component
            if((serviceManager != null) && serviceManager instanceof CoreServiceManager) {
                // next from the CoreServiceManager managing this component
                jmxDomain = ((CoreServiceManager)serviceManager).getJmxDefaultDomain();
            } else {
                // otherwise use default one
                jmxDomain = CoreServiceManager.JMX_DEFAULT_DOMAIN_NAME;
            }
        }

        return jmxDomain;
    }

    /**
     * Find or construct a JMX object name at various places (excluding the JMX domain)
     *
     * @param pJmxName Default JMX object name
     * @param pClassName Class name to use if JMX object name has to be constructed
     *
     * @return The JMX obejct name (excluding the JMX domain name
     */
    public static String findJmxName(final String pJmxName,
                                     final String pClassName) {
        String jmxName = pJmxName;
        final String className = ((pClassName == null) ? "unknown" : pClassName);

        if(jmxName == null) {
            // otherwise construct one from the service class name
            final StringBuffer sb = new StringBuffer();
            final List groups = new ArrayList();
            int i = className.indexOf('.');
            int j = 0;

            while(i > 0) {
                groups.add(className.substring(j, i));
                j = i + 1;
                i = className.indexOf('.', i + 1);
            }

            groups.add(className.substring(j));

            for(i = 0; i < (groups.size() - 1); i++) {
                sb.append("group");

                if(i > 0) {
                    sb.append(i);
                }

                sb.append('=');
                sb.append(groups.get(i));
                sb.append(',');
            }

            sb.append("item=").append(groups.get(groups.size() - 1));
            jmxName = sb.toString();
        }

        return jmxName;
    }

    /**
     * generate a default JMX object name (excluding JMX domain name) from a FQCN
     *
     * @param clazz The class name
     *
     * @return The generated JMX object name (excluding JMX domain name)
     */
    public static String genDefaultJmxName(final Class clazz) {
        return genDefaultJmxName(clazz.getName());
    }

    /**
     * generate a default JMX object name (excluding JMX domain name) from a FQCN
     *
     * @param className The class name
     *
     * @return The generated JMX object name (excluding JMX domain name)
     */
    public static String genDefaultJmxName(final String className) {
        final StringBuffer nameBuf = new StringBuffer();
        final List groups = new ArrayList();
        int i = className.indexOf('.');
        int j = 0;

        while(i > 0) {
            groups.add(className.substring(j, i));
            j = i + 1;
            i = className.indexOf('.', i + 1);
        }

        groups.add(className.substring(j));

        for(i = 0; i < (groups.size() - 1); i++) {
            nameBuf.append("group");

            if(i > 0) {
                nameBuf.append(i);
            }

            nameBuf.append('=');
            nameBuf.append(groups.get(i));
            nameBuf.append(',');
        }

        nameBuf.append("item=").append(groups.get(groups.size() - 1));

        return nameBuf.toString();
    }

    /**
     * Get the ev. found {@link MBeanServer}
     *
     * @return DOCUMENT ME!
     */
    public static MBeanServer getMBeanServer() {
        return JMXUtils.mbeanServer;
    }

    /**
     * Setup a component for possible JMX managability
     *
     * @param bean The bean to estabilsh JMX for
     * @param info The component info
     *
     * @return DOCUMENT ME!
     */
    public static ObjectInstance setupJmxFor(final Object bean,
                                             final ComponentInfo info) {
        return setupJmxFor(bean, info, new ConsoleLogger(ConsoleLogger.LEVEL_INFO));
    }

    /**
     * Setup a component for possible JMX managability
     *
     * @param bean The bean to estabilsh JMX for
     * @param info The component info
     * @param logger The Logger
     */
    public static ObjectInstance setupJmxFor(final Object bean,
                                             final ComponentInfo info,
                                             final Logger logger) {
        if(getMBeanServer() != null) {
            final Class beanClass = bean.getClass();            
            final String packageName = beanClass.getPackage().getName();
            String cName = beanClass.getName().substring(packageName.length()+1);
            String mName=packageName+"."+cName+"MBean";
            final ObjectInstance oi = setupJmxFor(mName, bean, info, logger);
            if(oi == null) {            
                mName = packageName+".jmx."+cName+"MBean";
                return setupJmxFor(mName, bean, info, logger);
            }
        }
        return null;
    }

    /**
     * Setup a component for possible JMX managability
     *
     * @param mbeanClassName The class name to look after as MBean
     * @param bean The bean to estabilsh JMX for
     * @param info The component info
     * @param logger The Logger
     */
    private static ObjectInstance setupJmxFor(final String mbeanClassName,
                                             final Object bean,
                                             final ComponentInfo info,
                                             final Logger logger) {
        final Class beanClass = bean.getClass();
        ObjectName on = null;

        try {
            // try to find a MBean for bean
            final Class mbeanClass = beanClass.getClassLoader().loadClass(mbeanClassName);
            Constructor ctor = null;
            Object mbean = null;

            try {
                ctor = mbeanClass.getConstructor(new Class[] {beanClass, ComponentInfo.class});
                mbean = ctor.newInstance(new Object[] {bean, info});
            } catch(final Exception e) {
                // ignore this
            }

            if(ctor == null) {
                ctor = mbeanClass.getConstructor(new Class[] {beanClass});
                mbean = ctor.newInstance(new Object[] {bean});
            }

            // see if MBean supplies some JMX ObjectName parts
            final String mBeanSuppliedJmxDomain = callGetter(mbean, "getJmxDomain", logger);
            final String mBeanSuppliedJmxName = callGetter(mbean, "getJmxName", logger);
            final String mBeanSuppliedJmxNameAdditions =
                callGetter(mbean, "getJmxNameAddition", logger);

            // construct a JMX ObjectName instance
            final StringBuffer objectNameBuf = new StringBuffer();

            if(mBeanSuppliedJmxDomain != null) {
                objectNameBuf.append(mBeanSuppliedJmxDomain);
            } else {
                objectNameBuf.append(info.getJmxDomain());
            }

            objectNameBuf.append(':');

            if(mBeanSuppliedJmxName != null) {
                objectNameBuf.append(mBeanSuppliedJmxName);
            } else if(info.getConfiguration()
                          .getAttribute(CoreServiceManager.JMX_NAME_ATTR_NAME, null) != null) {
                objectNameBuf.append(info.getConfiguration()
                                         .getAttribute(CoreServiceManager.JMX_NAME_ATTR_NAME, null));
            } else {
                // if we do not have the name parts we'll construct one from the bean class name           
                objectNameBuf.append(genDefaultJmxName(beanClass));
            }

            if(mBeanSuppliedJmxNameAdditions != null) {
                objectNameBuf.append(',');
                objectNameBuf.append(mBeanSuppliedJmxNameAdditions);
            }

            on = new ObjectName(objectNameBuf.toString());

            int instance = 1;

            while(mbeanServer.isRegistered(on)) {
                instance++;
                on = new ObjectName(objectNameBuf.toString() + ",instance=" + instance);
            }

            return mbeanServer.registerMBean(mbean, on);
        } catch(final ClassNotFoundException cnfe) {
            // happens if a component doesn't have a MBean to support it for management
            if(logger.isDebugEnabled()) {
                logger.debug("Class " + beanClass.getName() +
                             " doesn't have a supporting MBean called " + mbeanClassName);
            }
        } catch(final NoSuchMethodException nsme) {
            logger.warn("MBean " + mbeanClassName +
                        " doesn't have a constructor that accepts an instance of " +
                        info.getServiceClassName(), nsme);
        } catch(final InvocationTargetException ite) {
            logger.warn("Cannot invoke constructor on class " + mbeanClassName, ite);
        } catch(final InstantiationException ie) {
            logger.warn("Cannot instantiate class " + mbeanClassName, ie);
        } catch(final IllegalAccessException iae) {
            logger.warn("Cannot access class " + mbeanClassName, iae);
        } catch(final MalformedObjectNameException mone) {
            logger.warn("Invalid ObjectName '" + on + "' for MBean " + mbeanClassName, mone);
        } catch(final InstanceAlreadyExistsException iaee) {
            logger.warn("Instance for MBean " + mbeanClassName + "already exists", iaee);
        } catch(final NotCompliantMBeanException ncme) {
            logger.warn("Not compliant MBean " + mbeanClassName, ncme);
        } catch(final MBeanRegistrationException mre) {
            logger.warn("Cannot register MBean " + mbeanClassName, mre);
        } catch(final SecurityException se) {
            logger.warn("Instantiation of MBean " + mbeanClassName +
                        " is prevented by a SecurityManager", se);
        }

        return null;
    }

    /**
     * Call a getter method on a bean
     *
     * @param mbean The bean to call a method from
     * @param name The name of the method to call
     * @param logger Logger for diagnostic messages
     *
     * @return DOCUMENT ME!
     */
    private static String callGetter(final Object mbean,
                                     final String name,
                                     final Logger logger) {
        final Method[] methods = mbean.getClass().getMethods();

        for(int i = 0; i < methods.length; i++) {
            if(methods[i].getName().equals(name)) {
                try {
                    return methods[i].invoke(mbean, null).toString();
                } catch(final Exception e) {
                    logger.warn("Method '" + name + "' cannot be accessed on MBean " +
                                mbean.getClass().getName());

                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Try to find a JMX Agent (MBeanServer)
     *
     * @return The first MBeanServer found or null
     */
    private static MBeanServer getInitialMBeanServer() {
        final List servers = MBeanServerFactory.findMBeanServer(null);

        if(servers.size() > 0) {
            return (MBeanServer)servers.get(0);
        }

        return null;
    }
}
