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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * Utility class for setting up Avalon components. Similar to Excalibur's
 * <code>DefaultComponentFactory</code>, but on existing objects.
 * <p>
 * To be moved to Avalon ?
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: LifecycleHelper.java,v 1.9 2003/10/22 15:37:50 bloritsch Exp $
 */

// FIXME : need to handle also LogEnabled.

public class LifecycleHelper {
    /** The Logger for the component
     */
    final private Logger m_logger;

    /** The Context for the component
     */
    final private Context m_context;

    /** The service manager for this component.
     */
    final private ServiceManager m_serviceManager;

    /** The configuration for this component.
     */
    final private Configuration m_configuration;

    /**
     * Construct a new <code>LifecycleHelper</code> that can be used repeatedly to
     * setup several components.
     * <p>
     * <b>Note</b> : if a parameter is <code>null</code>,
     * the corresponding method isn't called (e.g. if <code>configuration</code> is
     * <code>null</code>, <code>configure()</code> isn't called).
     *
     * @param logger the <code>Logger</code> to pass to <code>LogEnabled</code>s, unless there is
     *        a <code>LogKitManager</code> and the configuration specifies a logger name.
     * @param context the <code>Context</code> to pass to <code>Contexutalizable</code>s.
     * @param serviceManager the component manager to pass to <code>Composable</code>s.
     * @param configuration the <code>Configuration</code> object to pass to new instances.
     */
    public LifecycleHelper(final Logger logger,
                            final Context context,
                            final ServiceManager serviceManager,
                           final Configuration configuration) {
        m_logger = logger;
        m_context = context;
        m_serviceManager = serviceManager;
        m_configuration = configuration;
    }

    /**
     * Setup a component, including initialization and start.
     *
     * @param component the component to setup.
     * @return the component passed in, to allow function chaining.
     * @throws Exception if something went wrong.
     */
    public Object setupComponent(Object component) throws Exception {
        return setupComponent(component, true);
    }

    /**
     * Setup a component, and optionnaly initializes (if it's <code>Initializable</code>)
     * and starts it (if it's <code>Startable</code>).
     *
     * @param component the component to setup.
     * @param initializeAndStart if true, <code>intialize()</code> and <code>start()</code>
     *        will be called.
     * @return the component passed in, to allow function chaining.
     * @throws Exception if something went wrong.
     */
    public Object setupComponent(Object component, boolean initializeAndStart)
    throws Exception {
        return setupComponent(
            component,
            m_logger,
            m_context,
            m_serviceManager,
            m_configuration,
            initializeAndStart);
    }

    /**
     * Alternative setupComponent method that takes a ServiceManager instead of a ComponentManger.
     */
    public static Object setupComponent(final Object component,
                                         final Logger logger,
                                         final Context context,
                                         final ServiceManager serviceManager,
                                        final Configuration configuration)
    throws Exception {
        return setupComponent(
            component,
            logger,
            context,
            serviceManager,
            configuration,
            true);
    }

    /**
     * Alternative setupComponent method that takes a ServiceManager instead of a ComponentManger.
     */
    public static Object setupComponent(final Object component,
                                         final Logger logger,
                                         final Context context,
                                         final ServiceManager serviceManager,
                                        final Configuration configuration,
                                         final boolean initializeAndStart)
    throws Exception {
        if (component instanceof LogEnabled) {
            ((LogEnabled) component).enableLogging(logger);
        }

        if (null != context && component instanceof Contextualizable) {
            ((Contextualizable) component).contextualize(context);
        }

        if (null != serviceManager && component instanceof Serviceable) {
            ((Serviceable) component).service(serviceManager);
        }

        if (null != configuration && component instanceof Configurable) {
            ((Configurable) component).configure(configuration);
        }

        if (null != configuration && component instanceof Parameterizable) {
            ((Parameterizable) component).parameterize(
                Parameters.fromConfiguration(configuration));
        }

        if (initializeAndStart && component instanceof Initializable) {
            ((Initializable) component).initialize();
        }

        if (initializeAndStart && component instanceof Startable) {
            ((Startable) component).start();
        }

        return component;
    }

    /**
     * Decomission a component, by stopping (if it's <code>Startable</code>) and
     * disposing (if it's <code>Disposable</code>) a component.
     */
    public static final void decommission(final Object component)
    throws Exception {
        if (component instanceof Startable) {
            ((Startable) component).stop();
        }

        dispose(component);
    }

    /**
     * Dispose a component if it's <code>Disposable</code>. Otherwhise, do nothing.
     */
    public static final void dispose(final Object component) {
        if (component instanceof Disposable) {
            ((Disposable) component).dispose();
        }
    }
}
