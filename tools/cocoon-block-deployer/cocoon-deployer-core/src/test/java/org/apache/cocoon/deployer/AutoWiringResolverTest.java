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

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.deployer.block.BlockFactory;
import org.apache.cocoon.deployer.generated.deploy.x10.Deploy;
import org.apache.cocoon.deployer.logger.ConsoleLogger;
import org.easymock.MockControl;

public class AutoWiringResolverTest extends AbstractDeployerTestCase {

	/*
	 * Test method for 'org.apache.cocoon.deployer.AutoWiringResolver.analyzeBlock(Block)'
	 */
	public void testResolveBlock() throws Exception {
		AutoWiringResolver autoWiringResolver = new AutoWiringResolver(
				this.getArtifactProviderInstance(),
				new ConsoleLogger());
		
		Deploy deploy = (Deploy) Deploy.unmarshal(new FileReader(this.getMockArtefact("validDeploy-05/deploy.xml")));
		
		List blocks = new ArrayList();
		blocks.add(BlockFactory.createBinaryBlock(
				this.getMockArtefact("validBlock-04/valid-block-1.0.jar"), deploy.getBlock(0))
		);
		autoWiringResolver.resolve(blocks);
	}

	private ArtifactProvider getArtifactProviderInstance() {
		MockControl aProviderCtrl = MockControl.createControl(ArtifactProvider.class);
		ArtifactProvider aProvider = (ArtifactProvider) aProviderCtrl.getMock();
		
		aProvider.getArtifact("anyblock:anyblock-05:1.0");
		aProviderCtrl.setReturnValue(this.getMockArtefact("validBlock-05/valid-block-1.0.jar"));
		
		aProvider.getArtifact("anyblock:anyblock-06:1.0");
		aProviderCtrl.setReturnValue(this.getMockArtefact("validBlock-06/valid-block-1.0.jar"));		
		
		aProvider.getArtifact("anyblock:anyblock-07:1.0");
		aProviderCtrl.setReturnValue(this.getMockArtefact("validBlock-07/valid-block-1.0.jar"));	
		
		aProviderCtrl.replay();
		return aProvider;
	}		
	
}
