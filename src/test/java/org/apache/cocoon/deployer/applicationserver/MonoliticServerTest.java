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
package org.apache.cocoon.deployer.applicationserver;

import java.io.File;

import org.apache.cocoon.deployer.AbstractDeployerTestCase;
import org.apache.cocoon.deployer.logger.ConsoleLogger;
import org.apache.cocoon.deployer.monolithic.FileAlreadyDeployedException;
import org.apache.cocoon.deployer.monolithic.SingleFileDeployer;

public class MonoliticServerTest extends AbstractDeployerTestCase {

	private static final String SERVER_DIR = "target/test/monolithicServer23";
	
	public void testSimpleDeploy() throws Exception {
		MonolithicServer23 monolithicServer = new MonolithicServer23(new File(SERVER_DIR), new ConsoleLogger());
		monolithicServer.addRule("**webdav*.xconf", new SingleFileDeployer("WEB-INF/xconf"));
		monolithicServer.addRule("**legacy**.xmap", new SingleFileDeployer("WEB-INF/sitemap-additions"));
		monolithicServer.extract(this.getMockArtefact("validMonolithicBlock-02/valid-block-1.0.jar"));
		assertTrue(new File("target/test/monolithicServer23/WEB-INF/xconf/my-webdav-server.xconf").exists());
		assertTrue(new File("target/test/monolithicServer23/WEB-INF/sitemap-additions/sa.xmap").exists());		
	}
	
	public void testNotWorkingDeploy() throws Exception {
		MonolithicServer23 monolithicServer = new MonolithicServer23(new File(SERVER_DIR), new ConsoleLogger());
		monolithicServer.addRule("**webdav*.xconf", new SingleFileDeployer("WEB-INF/xconf"));
		monolithicServer.extract(this.getMockArtefact("validMonolithicBlock-02/valid-block-1.0.jar"));
		try {
			monolithicServer.extract(this.getMockArtefact("validMonolithicBlock-02/valid-block-1.0.jar"));		
			fail("If a file with the same name is deployed twice, an exception should be thrown.");
		} catch(FileAlreadyDeployedException fade) {
			// expected
		}
	}
	
}
