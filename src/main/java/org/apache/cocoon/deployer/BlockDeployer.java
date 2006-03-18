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
package org.apache.cocoon.deployer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.deployer.applicationserver.BlocksFramework;
import org.apache.cocoon.deployer.applicationserver.ApplicationServerFactory;
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
	
	/**
	 * Creates the BlockDeployer object. Pass the artifact provider (How to get access to the binaries
	 * and blocks?), the variable resolver (If you want to use variable resolving in deployment descriptors)
	 * and the logger.
	 * 
	 * @param artifactProvider - provide your implementation how to access the block binaries
	 * @param variableResolver - if you use variables in your deployment descriptor, you have to provide an implementation that resolves them
	 * @param log - a logger for user-orientated messages about the deployment process.
	 */
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
	 * @param transactional - set it 'true' if you want the deployment process being transactional (all or nothing)
	 * @param exclusive - set it 'true' if you want to deploy a complete Cocoon application from scratch. If set to 'false', then the blocks of the descriptor are only added to the application. Currently only 'true' is supported.
	 */
	public void deploy(final Deploy deploymentDescriptor, final boolean transactional, final boolean exclusive) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		// validations
		if(!exclusive) {
			throw new DeploymentException("The block deployer only supports the exclusive mode at the moment.");
		}		
		Validate.notNull(deploymentDescriptor, "A deployment descriptor object has to be passed.");

		// variable resolving
		// TODO implement resolving of variables
		
		// read the deploy script and get a list of all Block objects to be installed
		List blockList = createBlockList(deploymentDescriptor);
		
		// auto-wiring: if a connection is not specified, use the default implementation and add the "new" blocks to the list
		new AutoWiringResolver(this.artifactProvider, this.log).resolve(blockList);
		
		// validate deployment descriptor 
		// TODO (correct dependencies, mount path only used once in exclusive mode, set only available properties)
		
		// get the Cocoon urn
		String cocoonWebappUrn = deploymentDescriptor.getCocoon().getWebappUrn();
		String cocoonBlockFwWebappUrn = deploymentDescriptor.getCocoon().getBlockFwUrn();		
		
		// get all dependant libraries transitivly
		File[] libraries = artifactProvider.getArtifact( 
				this.getAllBlockUrns(blockList, cocoonBlockFwWebappUrn));
		
		// deploy the blocks
		BlocksFramework cocoonServer = ApplicationServerFactory.createServer(
				deploymentDescriptor.getCocoon(), this.variableResolver, this.artifactProvider, exclusive);
		
		Collections.reverse(blockList);
		
		cocoonServer.deploy((Block[]) blockList.toArray(new Block[blockList.size()]), 
				cocoonWebappUrn, libraries, this.log, transactional);
		
		stopWatch.stop();
		this.log.info("SUCESSFULLY deployed in " + stopWatch);
	}

	/**
	 * Reads a deployment descriptor and creates a list of <code>Block</code> objects to be installed.
	 * 
	 * @param deploymentDescriptor
	 * @return a list of <code>Block</code> objects
	 */
	protected List createBlockList(final Deploy deploymentDescriptor) {
		List blockList = new ArrayList();			
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
		return blockList;
	}
	
	/**
	 * @return a list of all urns to be installed
	 */
	protected String[] getAllBlockUrns(List blocks, String urn) {
		List urnList = new ArrayList();
		urnList.add(urn);
		for(Iterator it = blocks.iterator(); it.hasNext();) {
			String blockUrn = ((Block) it.next()).getId();
			urnList.add(blockUrn);
		}
		return (String[]) urnList.toArray(new String[urnList.size()]);
	}

	
}