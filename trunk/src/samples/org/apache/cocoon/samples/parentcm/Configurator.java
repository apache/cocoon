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

import org.apache.avalon.excalibur.naming.memory.MemoryInitialContextFactory;
import org.apache.avalon.framework.configuration.DefaultConfiguration;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;

/**
 * This class sets up the configuration used by the ParentComponentManager sample.
 * The class also holds a reference to the initial context in which the configuration
 * is available.
 * <p>
 * The configuration is bound to <code>org/apache/cocoon/samples/parentcm/ParentCMConfiguration</code>.
 *
 * @author <a href="mailto:leo.sutic@inspireinfrastructure.com">Leo Sutic</a>
 * @version CVS $Id: Configurator.java,v 1.1 2003/03/09 00:10:03 pier Exp $
 */
public class Configurator  {

    /**
     * The Excalibur in-memory JNDI directory. Since the directory doesn't
     * provide any persistence we must keep a reference to the initial context
     * as a static member to avoid passing it around.
     */
    public static Context initialContext = null;

    static {
        try {
            //
            // Create a new role.
            //
            DefaultConfiguration config = new DefaultConfiguration("roles", "");
            DefaultConfiguration timeComponent = new DefaultConfiguration("role", "roles");
            timeComponent.addAttribute("name", Time.ROLE);
            timeComponent.addAttribute("default-class", TimeComponent.class.getName());
            timeComponent.addAttribute("shorthand", "samples-parentcm-time");
            config.addChild(timeComponent);

            //
            // Bind it - get an initial context.
            //
            Hashtable environment = new Hashtable();
            environment.put(Context.INITIAL_CONTEXT_FACTORY, MemoryInitialContextFactory.class.getName());
            initialContext = new InitialContext(environment);

            //
            // Create subcontexts and bind the configuration.
            //
            Context ctx = initialContext.createSubcontext("org");
            ctx = ctx.createSubcontext("apache");
            ctx = ctx.createSubcontext("cocoon");
            ctx = ctx.createSubcontext("samples");
            ctx = ctx.createSubcontext("parentcm");
            ctx.rebind("ParentCMConfiguration", config);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}

