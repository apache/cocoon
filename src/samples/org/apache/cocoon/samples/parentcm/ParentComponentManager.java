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
package org.apache.cocoon.samples.parentcm;

import org.apache.avalon.excalibur.component.ExcaliburComponentManager;
import org.apache.avalon.excalibur.naming.memory.MemoryInitialContextFactory;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import javax.naming.Context;
import java.util.Hashtable;

/**
 * A sample parent component manager. This manager will lookup the configuration object
 * given by the initialization parameter in JNDI, use it to configure an ExcaliburComponentManager
 * and delegate any requests to it.
 *
 * @author <a href="mailto:leo.sutic@inspireinfrastructure.com">Leo Sutic</a>
 * @version CVS $Id: ParentComponentManager.java,v 1.1 2003/03/09 00:10:03 pier Exp $
 */
public class ParentComponentManager implements ComponentManager, LogEnabled, Initializable {

    /**
     * Our logger.
     */
    private Logger logger;

    /**
     * The JNDI name where the component manager configuration can be found.
     */
    private final String jndiName;

    /**
     * The delegate that will be configured and provide the
     * functionality for this component manager.
     */
    private final ExcaliburComponentManager delegate;

    public ParentComponentManager(final String jndiName) {
        this.jndiName = jndiName;

        // Initialize it here so we can let it be final.
        this.delegate = new ExcaliburComponentManager();
    }

    public boolean hasComponent(final String role) {
        return delegate.hasComponent(role);
    }

    /**
     * Initializes the CM by looking up the configuration object and using it to
     * configure the delegate.
     */
    public void initialize() throws Exception {
        this.logger.debug("Looking up component manager configuration at : " + this.jndiName);

        Hashtable environment = new Hashtable();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, MemoryInitialContextFactory.class.getName());

        //
        // Yes, this is cheating, but the Excalibur in-memory naming provider
        // is transient. That is, it doesn't store objects persistently and
        // is more like a HashMap.
        //
        // Should be:
        // Context initialContext = new InitialContext(environment);
        //
        Context initialContext = Configurator.initialContext;

        Configuration config = (Configuration) initialContext.lookup(this.jndiName);

        // We ignore the setRoleManager call, as ExcaliburComponentManager handles that
        // in configure().
        this.delegate.enableLogging(logger);
        this.delegate.contextualize(new DefaultContext());
        this.delegate.configure(config);
        this.delegate.initialize();

        this.logger.debug("Component manager successfully initialized.");
    }

    public Component lookup(final String role) throws ComponentException {
        return this.delegate.lookup(role);
    }

    public void release(final Component component) {
        this.delegate.release(component);
    }

    /**
     * Provide component with a logger.
     * 
     * @param logger the logger
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }
}

