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
package org.apache.cocoon.kernel.startup;

import org.apache.cocoon.kernel.Installer;
import org.apache.cocoon.kernel.KernelDeployer;
import org.apache.cocoon.kernel.configuration.ConfigurationBuilder;

/**
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.4 $)
 */
public class Main {

    public static void main(String args[]) {
        if (args.length < 1) {
            System.err.println("Usage: " + Main.class.getName() + " <blocks "
                               + "configuration> <deployment configuration>");
            System.exit(1);
        }

        /* Create a logger for startup operations */
        Logger logger = new ConsoleLogger();

        try {
            /* Now let's create our core deployer */
            KernelDeployer deployer = new KernelDeployer();
            deployer.logger(logger);
            deployer.configure(ConfigurationBuilder.parse(args[0]));

            /* Instantiate an installer and process deployment */
            Installer installer = new Installer(deployer);
            installer.process(ConfigurationBuilder.parse(args[1]));

        } catch (Throwable t) {
            logger.fatal("An error occurred initializing", t);
        }
    }
}
