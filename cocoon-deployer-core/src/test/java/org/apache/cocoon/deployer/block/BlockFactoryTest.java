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

import org.apache.cocoon.deployer.AbstractDeployerTestCase;
import org.apache.cocoon.deployer.generated.deploy.x10.Block;

public class BlockFactoryTest extends AbstractDeployerTestCase {
	
	private static final String VALID_BLOCK_01_JAR = "validBlock-01/valid-block-1.0.jar";
	private static final String VALID_DEPLOY_01 = "validDeploy-01/deploy.xml";
	private static final String VALID_DEPLOY_06 = "validDeploy-06/deploy.xml";

	/**
	 * Test if the passed block is a file and not a directory
	 */
	public void testBlockIsFile() throws Exception {
		File blockAsFile;
		
		// valid deployment descriptor
		org.apache.cocoon.deployer.generated.deploy.x10.Block blockDeployDesc = 
			this.getDeploy(VALID_DEPLOY_01).getBlock(0);
		
		
		// valid block
		blockAsFile = this.getMockArtefact(VALID_BLOCK_01_JAR);
		BlockFactory.createBinaryBlock(blockAsFile, blockDeployDesc);
		
		// block exists
		try {
			blockAsFile = this.getMockArtefact("validBlock-01/valid-block-1.0.ja__");
			BlockFactory.createBinaryBlock(blockAsFile, blockDeployDesc);
			fail("block must exist");
		} catch(IllegalArgumentException iae) {
			// expected
		}
		
		// block not null
		try {
			BlockFactory.createBinaryBlock(null, null);
			fail("null block argument mustn't be allowed");
		} catch(IllegalArgumentException iae) {
			// expected
		}	
		
		// deploydesc not null
		blockAsFile = this.getMockArtefact(VALID_BLOCK_01_JAR);		
		try {
			BlockFactory.createBinaryBlock(blockAsFile, null);
			fail("a block descriptor MUST be provided");
		} catch(IllegalArgumentException iae) {
			// excpected
		}
		
	}
	
	public void testBinaryBlockCreation() throws Exception {
		File blockAsFile = this.getMockArtefact(VALID_BLOCK_01_JAR);
		BinaryBlock block = BlockFactory.createBinaryBlock(blockAsFile, this.getDeploy(VALID_DEPLOY_01).getBlock(0));
		assertNotNull(block);
		assertNotNull(block.getBlockDescriptor());
		Object o = block.getBlockDescriptor();
		org.apache.cocoon.deployer.generated.block.x10.Block b = (org.apache.cocoon.deployer.generated.block.x10.Block) o;
		assertEquals(b.getId(), "http://cocoon.apache.org/blocks/anyblock/1.0");

		assertNotNull(block.getInputStream());
		assertEquals( org.apache.cocoon.deployer.block.Block.BLOCK_NS_10, block.getNamespace());
		assertEquals("http://cocoon.apache.org/blocks/anyblock/1.0", block.getId());
	}

	public void testLocalBlockCreation() throws Exception {
		Block deployBlock = this.getDeploy(VALID_DEPLOY_06).getBlock(0);
		LocalBlock block = BlockFactory.createLocalBlock(absolutizeBlockLocation(deployBlock), "");
		assertNotNull(block);
		assertNotNull(block.getBlockDescriptor());
		assertEquals( org.apache.cocoon.deployer.block.Block.BLOCK_NS_10, block.getNamespace());
		assertEquals("anyblock:anyblock-06:1.0", block.getId());	
		assertTrue(new File(block.getBaseDirectory()).exists());
	}
	
	
	// -------------- test the algorithm that gets calculates the local path
	
	public void testRelativeDirectoryCreationAlgForSameDriverWin() {
		String relDir = BlockFactory.createRelativeLocation("C:\\test\\ax\\server", "C:\\test\\ay\\blockRoot");
		assertEquals("../../ay/blockRoot/", relDir);
	}	
	
	public void testRelativeDirectoryCreationAlgForSameDriverWin1() {
		String relDir = BlockFactory.createRelativeLocation("C:\\test\\ax\\server\\", "C:\\test\\ay\\blockRoot\\");
		assertEquals("../../ay/blockRoot/", relDir);
	}
	
	public void testRelativeDirectoryCreationAlgForSameDriveNix() {
		String relDir = BlockFactory.createRelativeLocation("/home/test/ax/server", "/home/test/ay/blockRoot");
		assertEquals("../../ay/blockRoot/", relDir);
	}	
	
	public void testRelativeDirectoryCreationAlgForSameDriveNix1() {
		String relDir = BlockFactory.createRelativeLocation("/home/test/ax/server/", "/home/test/ay/blockRoot/");
		assertEquals("../../ay/blockRoot/", relDir);
	}		
	
	public void testRelativeDirectoryCreationAlgForDifferent() {
		String relDir = BlockFactory.createRelativeLocation("D:\\server\\", "C:\\test\\ay\\blockRoot\\");
		assertEquals("C:/test/ay/blockRoot/", relDir);
	}	
	
	// ---------------- test relatizing of a path
	 
	public void testPathRelativizing() {
		String relPath = BlockFactory.relativizePath("blah/foo/bar/");
		assertEquals("../../../", relPath);
	}
	
	
}
