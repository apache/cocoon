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
package org.apache.cocoon.deployer.applicationserver;

import java.io.File;
import java.io.FileReader;
import java.net.URI;

import org.apache.cocoon.deployer.AbstractDeployerTestCase;
import org.apache.cocoon.deployer.ArtifactProvider;
import org.apache.cocoon.deployer.block.BinaryBlock;
import org.apache.cocoon.deployer.block.BlockFactory;
import org.apache.cocoon.deployer.block.impl.Block10;
import org.apache.cocoon.deployer.filemanager.FileManager;
import org.apache.cocoon.deployer.generated.deploy.x10.Deploy;
import org.apache.cocoon.deployer.generated.wiring.x10.Wiring;
import org.apache.cocoon.deployer.util.FileUtils;
import org.easymock.MockControl;

public class BlocksFramework23Test extends AbstractDeployerTestCase {
	
	private static final String deployPath = "target/test/deployServer22";
	private static final String DEFAULT_WEBAPP_ARTIFACT = "org.apache.cocoon:cocoon-minimal-webapp:1.0-SNAPSHOT:war";
	
	/**
	 * Test method for 'org.apache.cocoon.deployer.applicationserver.CocoonServer22.deploy(Block[])'
	 */
	public void testDeploy() throws Exception {
		BlocksFrameworkServer23 cocoonServer = new BlocksFrameworkServer23();
		URI baseUri = new File(deployPath).toURI();
		cocoonServer.setBaseDirectory(baseUri);
		cocoonServer.setArtifactProvider(getArtifactProviderInstance());	
		cocoonServer.setExclusive(true);
		
		// create some blocks for the tests
		BinaryBlock[] blocks = new Block10[1];
		blocks[0] = createBlock10Instance("validBlock-02/valid-block-1.0.jar", "validDeploy-02/deploy.xml", 0);	
		
		cocoonServer.deploy(blocks, DEFAULT_WEBAPP_ARTIFACT, createLibraries(), this.getLogger(), true);
		
	    // assertions: check if setting the properties works correctly
		Wiring wiring = (Wiring) Wiring.unmarshal(new FileReader(new File(deployPath + "/WEB-INF/wiring.xml")));
		assertEquals(wiring.getBlock(0).getProperties().getProperty(0).getValue(), "defaultValue1");
		assertEquals(wiring.getBlock(0).getProperties().getProperty(1).getValue(), "new-value");		
		assertTrue(new File(deployPath + "/WEB-INF/lib/lib-01.jar").exists());
		assertFalse(new File(deployPath + "/WEB-INF/lib/valid-block-1.0.jar").exists());		
	}
	
	public void testDeployWithConnections() throws Exception {
		BlocksFrameworkServer23 cocoonServer = new BlocksFrameworkServer23();
		URI baseUri = new File(deployPath).toURI();
		cocoonServer.setBaseDirectory(baseUri);
		cocoonServer.setArtifactProvider(getArtifactProviderInstance());	
		cocoonServer.setExclusive(true);
		
		// create some blocks for the tests
		BinaryBlock[] blocks = new Block10[3];
		blocks[0] = createBlock10Instance("validBlock-02/valid-block-1.0.jar", "validDeploy-03/deploy.xml", 0);		
		blocks[1] = createBlock10Instance("validBlock-03/valid-block-1.0.jar", "validDeploy-03/deploy.xml", 1);
		blocks[2] = createBlock10Instance("validBlock-03/valid-block-1.0.jar", "validDeploy-03/deploy.xml", 2);
		
		cocoonServer.deploy(blocks, DEFAULT_WEBAPP_ARTIFACT, createLibraries(), this.getLogger(), true);
		
	    // assertions: names, location, connections
		Wiring wiring = (Wiring) Wiring.unmarshal(new FileReader(new File(deployPath + "/WEB-INF/wiring.xml")));
		assertEquals("block02", wiring.getBlock(0).getId());
		assertEquals("db", wiring.getBlock(1).getId());
		
		// check locations
		assertEquals("/blocks/00000001/", wiring.getBlock(0).getLocation());
		assertEquals("/blocks/00000002/", wiring.getBlock(1).getLocation());
		assertEquals("/blocks/00000002/", wiring.getBlock(2).getLocation()); // ensure that a block is unpacked only once
		
		// check connections
		assertEquals("db", wiring.getBlock(0).getConnections().getConnection(0).getName());
		assertEquals("db", wiring.getBlock(0).getConnections().getConnection(0).getName());
		
		// check libraries
		assertTrue(new File(deployPath + "/WEB-INF/lib/lib-01.jar").exists());
		assertTrue(new File(deployPath + "/WEB-INF/lib/lib-02.jar").exists());			
		assertFalse(new File(deployPath + "/WEB-INF/lib/valid-block-1.0.jar").exists());				
	}	

	private BinaryBlock createBlock10Instance(String blockArchive, String deployDescriptorFile, int deployDescPos) 
		throws Exception {
		Deploy deployDescriptor = (Deploy) Deploy.unmarshal(new FileReader(this.getMockArtefact(deployDescriptorFile)));
		return BlockFactory.createBinaryBlock(this.getMockArtefact(blockArchive), deployDescriptor.getBlock(deployDescPos));
	}

	/**
	 * Test method for 'org.apache.cocoon.deployer.applicationserver.CocoonServer22.deployCocoonServer(OutputStream)'
	 */
	public void testDeployCocoonServerTransactionalMode() throws Exception {
		BlocksFrameworkServer23 cocoonServer = new BlocksFrameworkServer23();
		
		// set the basedirectory (JUnit test output dir)
		File outputdir = this.createOutputDir("deployServer22");
		
		cocoonServer.setArtifactProvider(getArtifactProviderInstance());
		
		// test whether the Cocoon server got extracted
		String relativeOutputDir = "x";       
	    FileManager frm = FileUtils.createFileManager(new File(outputdir.getAbsolutePath()).toURI(), true);
	    
	    cocoonServer.deployCocoonServer(frm, relativeOutputDir, DEFAULT_WEBAPP_ARTIFACT);
	    frm.commitTransaction();
	    
	    // ASSERTIONS!!!
		Wiring wiring = (Wiring) Wiring.unmarshal(new FileReader(new File(deployPath + "/x/WEB-INF/wiring.xml")));

	}
	
	/**
	 * @return an ArtifactProvider that returns a valid CocoonServer22 artifact (zip file).
	 */
	private ArtifactProvider getArtifactProviderInstance() {
		MockControl aProviderCtrl = MockControl.createControl(ArtifactProvider.class);
		ArtifactProvider aProvider = (ArtifactProvider) aProviderCtrl.getMock();
		
		aProvider.getArtifact("org.apache.cocoon:cocoon-minimal-webapp:1.0-SNAPSHOT:war");
		File vanillaCocoonServerArtifact = this.getMockArtefact("validVanillaCocoon22App/appRoot.zip");
		assertTrue(vanillaCocoonServerArtifact.exists());		
		aProviderCtrl.setReturnValue(vanillaCocoonServerArtifact);
		aProviderCtrl.replay();
		return aProvider;
	}
	
	private File[] createLibraries() {
		File[] libraries = new File[3];
		libraries[0] = this.getMockArtefact("lib-01/lib-01.jar");
		libraries[1] = this.getMockArtefact("lib-02/lib-02.jar");	
		libraries[2] = this.getMockArtefact("validBlock-02/valid-block-1.0.jar");
		return libraries;
	}

}
