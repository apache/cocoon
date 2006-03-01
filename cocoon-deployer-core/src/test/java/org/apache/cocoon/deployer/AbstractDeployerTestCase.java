package org.apache.cocoon.deployer;

import java.io.File;
import java.io.FileReader;

import org.apache.cocoon.deployer.generated.deploy.x10.Block;
import org.apache.cocoon.deployer.generated.deploy.x10.Deploy;
import org.apache.cocoon.deployer.logger.ConsoleLogger;
import org.apache.cocoon.deployer.logger.Logger;
import org.apache.cocoon.deployer.util.FileUtils;

import junit.framework.TestCase;
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

public abstract class AbstractDeployerTestCase extends TestCase {

	public final static String MOCKS_DIR = "src/test/mocks";
	public final static String OUTPUT_DIR = "target/test";
	
	/**
	 * Code that is executed for each test.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.resetOutputDir();
	}	
	
	/**
	 * Get access to the mock artifacts.
	 * 
	 * @param artifact - the relative path to the artifact
	 * @return the artifact as <code>java.io.File</code>
	 */
	public File getMockArtefact(String artifact) {
		String basedir = System.getProperty("basedir");
		if(basedir != null || "".equals(basedir)) {
			basedir = basedir + File.separator;
		} else {
			basedir = "." + File.separator;
		}
		System.out.println("basedir: " + basedir + MOCKS_DIR + ", artifact=" + artifact);
		return new File(basedir + MOCKS_DIR, artifact);
	}
	
	/**
	 * Create a directory for unit tests.
	 * @param directoryName
	 * @return
	 */
	public File createOutputDir(String directoryName) {	
		File directory = new File(new File(OUTPUT_DIR), directoryName);
		if(!directory.mkdirs()) {
			throw new RuntimeException("Can't create '" + directoryName + "'.");
		}
		return directory;
	}
	
	/**
	 * @return true if the directory with unit tests could be reset
	 */
	public void resetOutputDir() {
		File outputDir = new File(OUTPUT_DIR);
		if(!outputDir.exists()) {
			return;
		}
		if(!FileUtils.deleteDirRecursivly(outputDir)) {
			throw new RuntimeException("Can't reset '" + OUTPUT_DIR + "'.");
		}
	}
    
	/**
	 * A simple method that creates a Deploy object.
	 */
	public org.apache.cocoon.deployer.generated.deploy.x10.Deploy getDeploy(String relativePath) 
		throws Exception {
		return (org.apache.cocoon.deployer.generated.deploy.x10.Deploy) 
			org.apache.cocoon.deployer.generated.deploy.x10.Deploy.unmarshal(
			    new FileReader(getMockArtefact(relativePath)));
	}
	
	/**
	 * Use this, to make the block location absolute. This is required for reactor Maven builds.
	 */
	protected Block absolutizeBlockLocation(Block deployBlock) {
		System.out.println("loc: "+ deployBlock.getLocation());		
		String basedir = System.getProperty("basedir");
		if(basedir != null && deployBlock.getLocation() != null) {
			deployBlock.setLocation(basedir + File.separator + deployBlock.getLocation());
		}		
		return deployBlock;
	}
	
	/**
	 * Change all block objects in a deploy object so that they use absolute paths.
	 */
	protected Deploy absolutizeDeploy(Deploy deploy) {
		for(int i = 0; i < deploy.getBlock().length; i++) {
			deploy.setBlock(i, absolutizeBlockLocation(deploy.getBlock(i)));
		}
		return deploy;
	}	
	
	protected Logger getLogger() {
		return new ConsoleLogger();
	}
	
	public void testDummy() {
		// just exist for Maven surfire ;-)
	}



	
}
