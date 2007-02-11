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
package org.apache.cocoon.components.language.programming.javascript;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;

import org.apache.avalon.excalibur.component.ComponentHandler;
import org.apache.avalon.excalibur.component.RoleManager;
import org.apache.avalon.excalibur.component.LogkitLoggerManager;

import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.components.language.programming.Program;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class represents program in the Javascript language.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: JavascriptProgram.java,v 1.2 2003/07/19 15:06:38 joerg Exp $
 */
public class JavascriptProgram implements Program {

    protected File file;
    protected Class clazz;
    protected DefaultConfiguration config;

    public JavascriptProgram(File file, Class clazz, Collection dependecies) {
        DefaultConfiguration child;

        this.file = file;
        this.clazz = clazz;

        config = new DefaultConfiguration("", "GeneratorSelector");
        child = new DefaultConfiguration("file", "");
        child.setValue(file.toString());
        config.addChild(child);

        for (Iterator i = dependecies.iterator(); i.hasNext(); ) {
            child = new DefaultConfiguration("dependency", "");
            child.setValue(i.next().toString());
            config.addChild(child);
        }
    }

    public String getName() {
        return file.toString();
    }

    public ComponentHandler getHandler(ComponentManager manager,
                                       Context context,
                                       RoleManager roles,
                                       LogkitLoggerManager logKitManager)
            throws Exception {

        return ComponentHandler.getComponentHandler(
                clazz, config, manager, context, roles, logKitManager, null, "N/A");
    }

    public CompiledComponent newInstance() throws Exception {
        CompiledComponent instance = (CompiledComponent) clazz.newInstance();
        if (instance instanceof Configurable)
            ((Configurable) instance).configure(config);
        return instance;
    }
}
