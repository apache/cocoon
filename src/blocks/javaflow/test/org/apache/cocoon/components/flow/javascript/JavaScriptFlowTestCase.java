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

package org.apache.cocoon.components.flow.javascript;

import java.util.*;

import org.apache.avalon.framework.component.*;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.cocoon.components.flow.*;
import org.apache.excalibur.event.Queue;
import org.apache.excalibur.event.command.CommandManager;
import org.apache.excalibur.source.SourceResolver;

/**
 *
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels </a>
 * @version CVS $Id: JavaScriptFlowTestCase.java,v 1.1 2004/06/04 14:02:38 stephan Exp $
 */
public class JavaScriptFlowTestCase extends SitemapComponentTestCase {

    public JavaScriptFlowTestCase(String name) {
        super(name);
    }
    
    /*public void setUp() {
        CommandManager commands = new CommandManager();
        ContainerUtil.enableLogging(commands, (Logger) getLogger());
        //this.threads.register(this.commands);
        
        getContext(). put(Queue.ROLE, commands.getCommandSink());
    }*/

    public void testCalculator() throws Exception {
        String source = "resource://org/apache/cocoon/components/flow/javascript/calc.js";
        callFunction("java", source, "calculator", new ArrayList());
    }

    public void callFunction(String type, String source, String function, List params) throws Exception {
        
        ComponentSelector selector = null;
        Interpreter interpreter = null;
        SourceResolver resolver = null;

        try {
            selector = (ComponentSelector) this.manager.lookup(Interpreter.ROLE);
            assertNotNull("Test lookup of interpreter selector", selector);

            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            assertNotNull("Test if interpreter name is not null", type);
            interpreter = (Interpreter) selector.select(type);
            assertNotNull("Test lookup of interpreter", interpreter);

            ((AbstractInterpreter)interpreter).register(source);
            interpreter.callFunction(function, params, getRedirector());

        } catch (ComponentException ce) {
            getLogger().error("Could not retrieve interpeter", ce);
            fail("Could not retrieve interpreter: " + ce.toString());
        } finally {
            if (interpreter != null) {
                selector.release((Component) interpreter);
            }
            this.manager.release(selector);
            this.manager.release(resolver);
        }
    }
    
    public void handleContinuation(String type, String source, String id, List params) throws Exception {
        
        ComponentSelector selector = null;
        Interpreter interpreter = null;
        SourceResolver resolver = null;

        try {
            selector = (ComponentSelector) this.manager.lookup(Interpreter.ROLE);
            assertNotNull("Test lookup of interpreter selector", selector);

            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            assertNotNull("Test lookup of source resolver", resolver);

            assertNotNull("Test if interpreter name is not null", type);
            interpreter = (Interpreter) selector.select(type);
            assertNotNull("Test lookup of interpreter", interpreter);

            ((AbstractInterpreter)interpreter).register(source);
            interpreter.handleContinuation(id, params, getRedirector());

        } catch (ComponentException ce) {
            getLogger().error("Could not retrieve interpreter", ce);
            fail("Could not retrieve interpreter: " + ce.toString());
        } finally {
            if (interpreter != null) {
                selector.release((Component) interpreter);
            }
            this.manager.release(selector);
            this.manager.release(resolver);
        }
    }
}
