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
import java.io.FileReader;

import org.apache.cocoon.deployer.generated.deploy.x10.Deploy;
import org.apache.cocoon.deployer.logger.ConsoleLogger;
import org.apache.cocoon.deployer.resolver.NullVariableResolver;
import org.easymock.MockControl;

public class BlockDeployerTest extends AbstractDeployerTestCase {

	public void testDeploy() throws Exception {
		BlockDeployer blockDeployer = new BlockDeployer(
				this.getArtifactProviderInstance(),
				new NullVariableResolver(),
				new ConsoleLogger());
		blockDeployer.deploy(
			(Deploy) Deploy.unmarshal(new FileReader(this.getMockArtefact("validDeploy-04/deploy.xml"))));
		System.out.println("x");
		// assertions: ...
		
	}
	
	private ArtifactProvider getArtifactProviderInstance() {
		MockControl aProviderCtrl = MockControl.createControl(ArtifactProvider.class);
		ArtifactProvider aProvider = (ArtifactProvider) aProviderCtrl.getMock();
		
		// server
		aProvider.getArtifact("org.apache.cocoon:cocoon-minimal-webapp:2.0-SNAPSHOT:war");
		File vanillaCocoonServerArtifact = this.getMockArtefact("validVanillaCocoon22App/appRoot.zip");
		assertTrue(vanillaCocoonServerArtifact.exists());		
		aProviderCtrl.setReturnValue(vanillaCocoonServerArtifact);
		
		aProvider.getArtifact("validBlock-03:validBlock-03:1.0");
		aProviderCtrl.setReturnValue(this.getMockArtefact("validBlock-03/valid-block-1.0.jar"));
		
		aProvider.getArtifact("validBlock-04:validBlock-04:1.0");
		aProviderCtrl.setReturnValue(this.getMockArtefact("validBlock-04/valid-block-1.0.jar"));		
		
		aProvider.getArtifact("anyblock:anyblock-05:1.0");
		aProviderCtrl.setReturnValue(this.getMockArtefact("validBlock-05/valid-block-1.0.jar"));
		
		aProvider.getArtifact("anyblock:anyblock-06:1.0");
		aProviderCtrl.setReturnValue(this.getMockArtefact("validBlock-06/valid-block-1.0.jar"));		
		
		aProvider.getArtifact("anyblock:anyblock-07:1.0");
		aProviderCtrl.setReturnValue(this.getMockArtefact("validBlock-07/valid-block-1.0.jar"));		
		
		aProvider.getArtifact("anyblock:anyblock-05:1.0");
		aProviderCtrl.setReturnValue(this.getMockArtefact("validBlock-05/valid-block-1.0.jar"));
		
		aProvider.getArtifact("anyblock:anyblock-06:1.0");
		aProviderCtrl.setReturnValue(this.getMockArtefact("validBlock-06/valid-block-1.0.jar"));		
		
		aProvider.getArtifact("anyblock:anyblock-07:1.0");
		aProviderCtrl.setReturnValue(this.getMockArtefact("validBlock-07/valid-block-1.0.jar"));			
		
		aProvider.getArtifact((String[]) null);
		aProviderCtrl.setDefaultReturnValue(new File[0]);
		
		aProviderCtrl.replay();
		return aProvider;
	}	
	
	public void testAutoWiringResolving() {

	}
	
}
