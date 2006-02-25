/* 
 * Copyright 2005 The Apache Software Foundation
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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.apache.avalon.excalibur.logger.ConsoleLoggerManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.NullLogger;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.MutableSettings;

/**
 * Test cases for {@link CoreServiceManager}.
 * 
 * @version $Id$
 */
public class CoreServiceManagerTestCase extends TestCase {

    protected CoreServiceManager parent;
    protected CoreServiceManager child;

    protected final static String PARENT_ROLE_CONFIG =
        "<role-list cocoon-version = '2.2'>" +
        "  <role name=\"list\" shorthand=\"config-list\"" +
        "        default-class=\"java.util.ArrayList\"/>" +
        "" + 
        "  <role name=\"map\" shorthand=\"config-map\"" +
        "        default-class=\"java.util.HashMap\"/>" +
        "</role-list>";

    protected final static String PARENT_CONFIG =
        "<cocoon version=\"2.2\">" +
        "  <config-map class=\"java.util.Hashtable\"/>" +
        "</cocoon>";

    protected final static String CHILD_ROLE_CONFIG =
        "<role-list cocoon-version = '2.2'>" +
        "  <role name=\"map\"" +
        "        default-class=\"java.util.HashMap\"/>" +
        "</role-list>";

    protected final static String CHILD_CONFIG =
        "<cocoon version=\"2.2\">" +
        "</cocoon>";

    protected Configuration getConfig(String configString)
    throws Exception {
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        return builder.build(new ByteArrayInputStream(configString.getBytes("utf-8")));
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();


        // we setup a hierarchy of service managers
        // let's start with a core component and settings
        MutableSettings settings = new MutableSettings();
        settings.makeReadOnly();
        Core core = new Core(settings, null);

        // and now a context with the core
        DefaultContext context = new DefaultContext();
        context.put(Core.ROLE, core);

        // a role manager for the parent
        RoleManager parentRoleManager = new RoleManager(null);
        parentRoleManager.enableLogging(new ConsoleLogger());
        parentRoleManager.configure(this.getConfig(PARENT_ROLE_CONFIG));

        // the parent service manager
        this.parent = new CoreServiceManager(null);
        this.parent.enableLogging(new ConsoleLogger());
        this.parent.setLoggerManager(new ConsoleLoggerManager());
        this.parent.setRoleManager(parentRoleManager);
        this.parent.contextualize(context);
        this.parent.configure(this.getConfig(PARENT_CONFIG));
        this.parent.initialize();

        // a role manager for the child
        RoleManager childRoleManager = new RoleManager(parentRoleManager);
        childRoleManager.enableLogging(new ConsoleLogger());
        childRoleManager.configure(this.getConfig(CHILD_ROLE_CONFIG));

        // the child service manager
        this.child = new CoreServiceManager(this.parent);
        this.child.enableLogging(new NullLogger());
        this.child.setRoleManager(childRoleManager);
        this.child.contextualize(context);
        this.child.configure(this.getConfig(CHILD_CONFIG));
        this.child.initialize();
    }

    public void testParentLookup()
    throws Exception {
        Object component = this.parent.lookup("list");
        assertTrue(component instanceof ArrayList);

        component = this.parent.lookup("map");
        assertTrue(component instanceof Hashtable);
    }

    public void testChildLookup()
    throws Exception {
        Object component = this.child.lookup("list");
        assertTrue(component instanceof ArrayList);

        component = this.child.lookup("map");
        assertTrue(component instanceof HashMap);
    }
}
