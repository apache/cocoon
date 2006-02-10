/*
 * Copyright 2005 The Apache Software Foundation.
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
/**
 * 
 */
package org.apache.cocoon.deployer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.deployer.applicationserver.CocoonServer;
import org.apache.cocoon.deployer.applicationserver.CocoonServerFactory;
import org.apache.cocoon.deployer.block.Block;
import org.apache.cocoon.deployer.block.BlockFactory;
import org.apache.cocoon.deployer.generated.deploy.x10.Deploy;
import org.apache.cocoon.deployer.logger.Logger;
import org.apache.cocoon.deployer.resolver.VariableResolver;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.StopWatch;

/**
 * This class provides the services to deploy a block.
 */
public class BlockDeployer {
	
	private final VariableResolver variableResolver;
	private final Logger log;
	private final ArtifactProvider artifactProvider;
	private final List blockList = new ArrayList();	
	
	public BlockDeployer( final ArtifactProvider artifactProvider, 
			final VariableResolver variableResolver, final Logger log) {
		
		Validate.notNull(artifactProvider, "An artifact provider object has to be passed.");
		Validate.notNull(variableResolver, "A variable resolver object has to be passed.");
		
		this.artifactProvider = artifactProvider;
		this.variableResolver = variableResolver;
		this.log = log;
	}

	/**
	 * <p>Use this service method to deploy Cocoon blocks. The deployment descriptor has to describe which blocks
	 * should be deployed and how they are weired.</p> 
	 * 
	 * <p>If any error occurres, the unchecked @link DeploymentException is thrown.</p> 
	 * 
	 * @param deploymentDescriptor - the descriptor that contains all blocks that should be deployed
	 * @param artifactProvider - provide your implementation how to access the block binaries
	 * @param variableResolver - if you use variables in your deployment descriptor, you have to provide an implementation that resolves them
	 * @param log - a logger for user-orientated messages about the deployment process.
	 */
	public void deploy(final Deploy deploymentDescriptor) {
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		Validate.notNull(deploymentDescriptor, "A deployment descriptor object has to be passed.");

		// read the deploy script
		org.apache.cocoon.deployer.generated.deploy.x10.Block[] installBlocks = deploymentDescriptor.getBlock();

		for(int i = 0; i < installBlocks.length; i++) {
			org.apache.cocoon.deployer.generated.deploy.x10.Block installBlock = installBlocks[i];
			log.verbose("Block urn: " + installBlock.getUrn());			
			String blockUrn = installBlock.getUrn();
			File blockArchive = null;
			if(installBlock.getLocation() == null) {
				blockArchive = this.artifactProvider.getArtifact(blockUrn);			
				blockList.add(BlockFactory.createBinaryBlock(blockArchive, installBlock));				
			} else {
				blockList.add(BlockFactory.createLocalBlock(installBlock, 
				    deploymentDescriptor.getCocoon().getTargetUrl()));
			}
		}
		
		// auto-wiring: if a connection is not specified, use the default implementation and
		//              add the "new" blocks to the list
		new AutoWiringResolver(this.artifactProvider, this.log).resolve(this.blockList);
		
		// validate deployment descriptor (correct dependencies, mount path only used once in exclusive mode, 
		// set only available properties)
		
		// get the Cocoon urn
		String cocoonWebappUrn = deploymentDescriptor.getCocoon().getWebappUrn();
		String cocoonBlockFwWebappUrn = deploymentDescriptor.getCocoon().getBlockFwUrn();		
		
		// get all dependant libraries transitivly
		File[] libraries = artifactProvider.getArtifact( 
				this.getAllBlockUrns(this.blockList, cocoonBlockFwWebappUrn));
		
		// deploy the blocks
		CocoonServer cocoonServer = CocoonServerFactory.createServer(
				deploymentDescriptor.getCocoon(), this.variableResolver, this.artifactProvider);
		Collections.reverse(blockList);
		cocoonServer.deploy((Block[]) blockList.toArray(new Block[blockList.size()]), 
				cocoonWebappUrn, libraries, this.log);
		
		stopWatch.stop();
		this.log.info("SUCESSFULLY deployed in " + stopWatch);
		
	}
	
	/**
	 * @return a list of all urns to be installed
	 */
	private String[] getAllBlockUrns(List blocks, String urn) {
		List urnList = new ArrayList();
		urnList.add(urn);
		for(Iterator it = blocks.iterator(); it.hasNext();) {
			String blockUrn = ((Block) it.next()).getId();
			urnList.add(blockUrn);
		}
		return (String[]) urnList.toArray(new String[urnList.size()]);
	}

	
}