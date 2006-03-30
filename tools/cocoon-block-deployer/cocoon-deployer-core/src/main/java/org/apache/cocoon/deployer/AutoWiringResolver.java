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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.cocoon.deployer.block.Block;
import org.apache.cocoon.deployer.block.BlockFactory;
import org.apache.cocoon.deployer.generated.block.x10.Requires;
import org.apache.cocoon.deployer.generated.deploy.x10.Connection;
import org.apache.cocoon.deployer.logger.Logger;
import org.apache.commons.lang.Validate;

/**
 * The AutoWiringResolver analyzes a block and resolves all connections that are
 * not explizitly declared in the deploy script.
 */
public class AutoWiringResolver {

	public static final String DEPLOY_BLOCK_ID_PREFIX = "auto_wired_";
	
	private ArtifactProvider artifactProvider;
	private Logger log;
	private int autoCreatedBlockCounter = 0;
	List autowiredBlockList = new ArrayList();	
	
	public AutoWiringResolver(final ArtifactProvider artifactProvider, final Logger log) {
		Validate.notNull(artifactProvider, "artifactProvider mustn't be null.");
		Validate.notNull(log, "log  mustn't be null ");
		this.artifactProvider = artifactProvider;
		this.log = log;
	}
	
	public void resolve(final List blockList) {
		Validate.notNull(blockList, "The list of blocks mustn't be null.");
		Validate.allElementsOfType(blockList, Block.class);			
		
		// iterator over all blocks		
		for(Iterator it = blockList.iterator(); it.hasNext();) {
			Block block = (Block) it.next();
			analyzeBlock(block);
		}
		
		blockList.addAll(this.autowiredBlockList);
		
	}

	/**
	 * Analyze a block and find all unset connections. If an unset connection is found, the default
	 * connection is read out and autowired by creating a new block. 
	 * 
	 * @param block to be analyzed
	 * @return a list of all blocks that have to be installed
	 */
	protected void analyzeBlock(Block block) {
		Validate.notNull(block, "Null value is not allowed for block.");

		Set allExplicitConnections = getAllExplicitConnections(block);
		
		// if the block has no requirements, the block is an endpoint
		if(block.getBlockDescriptor().getRequirements() == null) {
			return;
		}
		
		// iterate over all requirements and find out if connections are set in the deployment script		
		Requires[] requires = block.getBlockDescriptor().getRequirements().getRequires();
		for(int i = 0; i < requires.length; i++) {
			
			// if no explicit connection is set in the deployment script --> autowire			
			if(!allExplicitConnections.contains(requires[i].getName())){
				
				// create a new block
				String newBlockDeployId = getNextDeployBlockId();
				Block autoWiredBlock = BlockFactory.createAutowiredBlock(
			        artifactProvider.getArtifact(requires[i].getDefault()), newBlockDeployId);
				this.log.info("auto-wiring: create new block [" + newBlockDeployId + "].");
				
				// add the connection to the deployer
				Connection newConnection = new Connection();
				newConnection.setBlock(newBlockDeployId);
				newConnection.setName(requires[i].getName());
				if(block.getDeployDescriptor().getConnections() == null) {
					block.getDeployDescriptor().setConnections(new org.apache.cocoon.deployer.generated.deploy.x10.Connections());
				}
				block.getDeployDescriptor().getConnections().addConnection(newConnection);
			        
				// analyze the new block too
				analyzeBlock(autoWiredBlock);
				
				// add block to the list
				this.autowiredBlockList.add(autoWiredBlock);
			}
		}		
	}
	
	/**
	 * Get a set of names of all connections of the block.
	 * 
	 * @param the block to be analyzed
	 * @return a set of names
	 */
	protected Set getAllExplicitConnections(Block block) {
		Set allExplicitConnections = new HashSet();
		if(block.getDeployDescriptor().getConnections() != null) {
			Connection[] connections = block.getDeployDescriptor().getConnections().getConnection();
			for(int i = 0; i < connections.length; i++) {
				allExplicitConnections.add(connections[i].getName());
			}
		}
		return allExplicitConnections;
	}
	
	/**
	 * @return the next available name
	 */
	protected String getNextDeployBlockId() {
		return DEPLOY_BLOCK_ID_PREFIX + this.autoCreatedBlockCounter++;
	}

}
