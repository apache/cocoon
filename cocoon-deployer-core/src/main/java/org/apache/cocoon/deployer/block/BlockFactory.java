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
package org.apache.cocoon.deployer.block;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.cocoon.deployer.DeploymentException;
import org.apache.cocoon.deployer.block.impl.Block10;
import org.apache.cocoon.deployer.generated.block.x10.Block;
import org.apache.cocoon.deployer.util.XMLUtils;
import org.apache.cocoon.deployer.util.ZipUtils;
import org.apache.commons.lang.Validate;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.SAXException;

public class BlockFactory {

	public static BinaryBlock createBinaryBlock(File blockArchive, org.apache.cocoon.deployer.generated.deploy.x10.Block deploymentDescriptor) {
		Validate.notNull(blockArchive, "You have to pass a block");
		Validate.isTrue(blockArchive.exists(), "The block must exist as file (" + blockArchive.getAbsolutePath() + ").");
		Validate.notNull(deploymentDescriptor, "You have to provide a deployment descriptor.");
	
		Block10 block = new Block10();
		
		// set the install descriptor
		block.setDeployDescriptor(deploymentDescriptor);
		
		// get and set the descriptor
		Block blockDescriptor = null;		
		try {
			Unmarshaller unmarshaller = new Unmarshaller(Block.class);
			// unmarshaller.setDebug(true);
			blockDescriptor = (Block) unmarshaller.unmarshal(new InputStreamReader(ZipUtils.getBlockDescriptorIs(blockArchive)));
			block.setBlockDescriptor(blockDescriptor);
			block.setInputStream(new FileInputStream(blockArchive));
			block.setNameSpace(XMLUtils.getDocumentNamespace(ZipUtils.getBlockDescriptorIs(blockArchive)));
			block.setId(blockDescriptor.getId());
		} catch (FileNotFoundException e) {
			throw new DeploymentException("The block archive file can't be found.");
		} catch (IOException e) {
			throw new DeploymentException("The block archive or the block descriptor can't be read correctly.");
		} catch (MarshalException e) {
			throw new DeploymentException("The block descriptor of the archive can't be read correctly.");
		} catch (ValidationException e) {
			throw new DeploymentException("The block descriptor of the archive can't be read correctly.");
		} catch (SAXException e) {
			throw new DeploymentException("The block descriptor of the archive can't be read correctly.");
		}
		
		return block;
	}

	public static BinaryBlock createAutowiredBlock(File artifact, String deployBlockId) {
		Validate.notNull(deployBlockId, "You have to pass the deployBlockId.");
		
		org.apache.cocoon.deployer.generated.deploy.x10.Block deployBlock = new org.apache.cocoon.deployer.generated.deploy.x10.Block();
		deployBlock.setId(deployBlockId);
		
		BinaryBlock block = BlockFactory.createBinaryBlock(artifact, deployBlock);
		deployBlock.setUrn(block.getBlockDescriptor().getId());
		
		return block;
	}
	
}