/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.deployer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.deployer.applicationserver.MonolithicServer23;
import org.apache.cocoon.deployer.logger.Logger;
import org.apache.cocoon.deployer.monolithic.SingleFileDeployer;

/**
 * Deploy blocks to a monolithic Cocoon web application.
 */
public class MonolithicCocoonDeployer {

	public static void deploy(Map libraries, File basedir, Logger logger) {
		
        MonolithicServer23 zipExtractor = new MonolithicServer23(basedir, logger);
        zipExtractor.addRule("**legacy**.xconf", new SingleFileDeployer("WEB-INF/xconf"));
        zipExtractor.addRule("**legacy**.xmap", new SingleFileDeployer("WEB-INF/sitemap-additions"));  

        for(Iterator it = libraries.keySet().iterator(); it.hasNext();) {
        	File lib = (File) libraries.get(it.next());  	
        	try {
        		// extract all configurations files
				zipExtractor.extract(lib);
			} catch (IOException e) {
				throw new DeploymentException("Can't deploy '" + lib.getAbsolutePath() + "'.", e);
			}
        }
	}
	
}
