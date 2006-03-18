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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.apache.cocoon.deployer.ArtifactProvider;
import org.apache.cocoon.deployer.DeploymentException;
import org.apache.cocoon.deployer.block.BinaryBlock;
import org.apache.cocoon.deployer.block.Block;
import org.apache.cocoon.deployer.block.LocalBlock;
import org.apache.cocoon.deployer.filemanager.FileManager;
import org.apache.cocoon.deployer.filemanager.FileManagerException;
import org.apache.cocoon.deployer.generated.block.x10.Property;
import org.apache.cocoon.deployer.generated.wiring.x10.Mount;
import org.apache.cocoon.deployer.generated.wiring.x10.Wiring;
import org.apache.cocoon.deployer.logger.Logger;
import org.apache.cocoon.deployer.resolver.VariableResolver;
import org.apache.cocoon.deployer.util.FileUtils;
import org.apache.cocoon.deployer.util.XMLUtils;
import org.apache.cocoon.deployer.util.ZipUtils;
import org.apache.commons.lang.Validate;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.SAXException;

public class BlocksFramework23 implements BlocksFramework {

	public static final String WIRING_10_NAMESPACE = "http://apache.org/cocoon/blocks/wiring/1.0";
	public static final String WIRING_FILE = "WEB-INF/wiring.xml";
	public static final String WEB_INF_LIBS_DIR = "WEB-INF/lib";
	public static final String BLOCKS_DIR = "/blocks/";
	
	private boolean exclusive = false;
	private URI baseDirectory;
	private VariableResolver variableResolver;
	private ArtifactProvider artifactProvider;
	private int lastDir = 0;
	
	/**
	 * Deploy an array of blocks. This operation wrapped by a filesystem transaction so that either
	 * all or any blocks get deployed. Note that the used filesystem transaction implementation only
	 * supports transactions within a single thread. Therefore this method is synchronized.
	 */
	public synchronized boolean deploy(Block[] blocks, String serverArtifact, File[] libraries, Logger log, boolean transactional) {
		Validate.notNull(blocks, "A blocks object (Block[]) has to be passed to this method!");
		Validate.notNull(libraries, "A libraries object (File[]) has to passed to this method!");
		
		// create transaction context    	    
	    FileManager frm = FileUtils.createFileManager(this.baseDirectory, transactional);
	    
	    try {
	    	File baseDirectoryFile = new File(this.baseDirectory);
	    	
			// install the Cocoon server if necessary
			if(baseDirectoryFile.list().length == 0 && serverArtifact != null) {
				deployCocoonServer(frm, "", serverArtifact);
			}

			// get the wiring, in exclusive mode from scratch, else take the existing one
			Wiring wiring = getWiring(log, frm);
			
			// install all passed blocks
			installBlocks(blocks, wiring, frm, baseDirectoryFile);
			
			// install libraries
			installLibraries(libraries, log, frm);
			
			// write the wiring
            writeWiring(wiring, frm);
            
            // committ transaction
			frm.commitTransaction(); 
			
	    } catch(Exception ex) {
	    	try {
				frm.rollbackTransaction(ex);
				String msg = "Filesystem changes have been rolled back. No block has been installed!";
				throw new DeploymentException(msg, ex);
			} catch (FileManagerException e) {
				throw new DeploymentException("A problem occurred when roling back the filesystem changes.");
			}
	    }
			
		return false;
	}

	/**
	 * Write the wiring by marshalling the <code>Wiring</code> object.
	 */
	protected void writeWiring(Wiring wiring, FileManager frm) 
		throws FileManagerException, MarshalException, ValidationException, IOException {
		
		OutputStream out = frm.writeResource(WIRING_FILE);    
		wiring.marshal(new OutputStreamWriter(out));
		out.close();
	}

	/**
	 * Install all blocks and adapt wiring.xml. Installing either means copying or just referencing the location
	 * in wiring.xml.
	 */
	protected void installBlocks(Block[] blocks, Wiring wiring, FileManager frm, File baseDirectoryFile) {
	    Map installedBlocks = new HashMap();			
		
		for(int i = 0; i < blocks.length; i++) {
			Block block = blocks[i];
			
			org.apache.cocoon.deployer.generated.deploy.x10.Block deployBlock = 
				(org.apache.cocoon.deployer.generated.deploy.x10.Block) block.getDeployDescriptor();
			org.apache.cocoon.deployer.generated.block.x10.Block blockDesc = 
				(org.apache.cocoon.deployer.generated.block.x10.Block) block.getBlockDescriptor();
			
			// create the wiring object
			org.apache.cocoon.deployer.generated.wiring.x10.Block wiringBlock = 
				createWiring10BlockInstance(deployBlock, blockDesc);			
			
			// in the case of a binary block, unpack it into the blocks directory
			if (block instanceof BinaryBlock) {
				BinaryBlock binaryBlock = (BinaryBlock) block;
				// check if the block has already been unpacked
				if(!installedBlocks.containsKey(block.getId())) {
					String nextDirectory = intDirToStringDirConvert(
							getNextDirectory(new File(baseDirectoryFile, BLOCKS_DIR), this.lastDir));
					this.lastDir = Integer.parseInt(nextDirectory);
					String installDirectory = BLOCKS_DIR + nextDirectory + "/";
					wiringBlock.setLocation(installDirectory);					
					deployBlock(binaryBlock, frm, BLOCKS_DIR + nextDirectory);
					installedBlocks.put(block.getId(), installDirectory);
				} else {
					wiringBlock.setLocation((String) installedBlocks.get(block.getId()));
				}
			} 
			else if(block instanceof LocalBlock) {
				LocalBlock localBlock = (LocalBlock) block;
				wiringBlock.setLocation(localBlock.getBaseDirectory());
			}
			wiring.addBlock(wiringBlock);
		}
	}

	/**
	 * Get the wiring object. Depending on the mode (exclusive/non-exclusive) either a new one is created
	 * or the existing one is returned.
	 */
	protected Wiring getWiring(Logger log, FileManager frm) throws SAXException, IOException, FileManagerException, MarshalException, ValidationException {
		Wiring wiring = null;
		if(this.isExclusive()) {
			wiring = new Wiring();
		} else {
			// check wiring version
			String wiringVersion = XMLUtils.getDocumentNamespace(frm.readResource(WIRING_FILE));
			if(!WIRING_10_NAMESPACE.equals(wiringVersion)) {
				String msg = "The deployer only supports " + WIRING_10_NAMESPACE + " files.";
				log.error(msg);
				throw new DeploymentException(msg);				
			}				
			wiring = (Wiring) Wiring.unmarshal(new InputStreamReader(frm.readResource(WIRING_FILE)));	
		}
		return wiring;
	}

	/*
	 * TODO What happens if two libraries have the same filename by chance ...?
	 */
	protected void installLibraries(File[] libraries, Logger log, FileManager frm) 
		throws IOException, FileNotFoundException, FileManagerException {
		
		for(int i = 0; i < libraries.length; i++) {
			File lib = libraries[i];
			
			// check if a library is a block, if yes, don't add it to WEB_INF_LIBS_DIR
			boolean isBlock = true;
			try {
				ZipUtils.getBlockDescriptorIs(lib);
			} catch(FileNotFoundException fnfe) {
				isBlock = false;
			}
			
			if(!isBlock) {
				String libName = WEB_INF_LIBS_DIR + "/" + lib.getName();
				log.info("Installing library " + libName);		
		        FileUtils.copy(new FileInputStream(lib), frm.writeResource(libName));						
			}
		}
	}

	/**
	 * Create the wired block information.
	 * 
	 * @return a wired block descriptor
	 */
	protected org.apache.cocoon.deployer.generated.wiring.x10.Block createWiring10BlockInstance(
			org.apache.cocoon.deployer.generated.deploy.x10.Block deployBlock, 
			org.apache.cocoon.deployer.generated.block.x10.Block blockDesc) {
		
		org.apache.cocoon.deployer.generated.wiring.x10.Block wiringBlock = 
			new org.apache.cocoon.deployer.generated.wiring.x10.Block();
		
		// id
		wiringBlock.setId(deployBlock.getId());
		
		// location
		wiringBlock.setLocation(deployBlock.getLocation());
		
		// mount
		Mount wiringMount = new Mount();
		org.apache.cocoon.deployer.generated.deploy.x10.Mount deployMount = deployBlock.getMount();
		if(deployMount != null) {
			wiringMount.setPath(deployMount.getPath());
			wiringBlock.setMount(wiringMount);			
		}

		
		// connections
		org.apache.cocoon.deployer.generated.wiring.x10.Connections wiringConnections = 
			new org.apache.cocoon.deployer.generated.wiring.x10.Connections();
		
		if(deployBlock.getConnections() != null) {
			org.apache.cocoon.deployer.generated.deploy.x10.Connection[] deployConnections = 
				deployBlock.getConnections().getConnection();
			
			for(int i = 0; i < deployConnections.length; i++) {
				org.apache.cocoon.deployer.generated.deploy.x10.Connection deployConnection = deployConnections[i];
				org.apache.cocoon.deployer.generated.wiring.x10.Connection wiringConnection = 
					new org.apache.cocoon.deployer.generated.wiring.x10.Connection();
				wiringConnection.setBlock(deployConnection.getBlock());
				wiringConnection.setName(deployConnection.getName());
				wiringConnections.addConnection(wiringConnection);
			}
			wiringBlock.setConnections(wiringConnections);
		}
			
		// properties
		if(blockDesc.getProperties() != null) {
			Property[] properties = blockDesc.getProperties().getProperty();
			org.apache.cocoon.deployer.generated.wiring.x10.Properties wiringProperties = 
				new org.apache.cocoon.deployer.generated.wiring.x10.Properties();
			
			for(int i = 0; i < properties.length; i++) {
				Property property = properties[i];
				String name = property.getName();
				
				org.apache.cocoon.deployer.generated.wiring.x10.Property wiringProperty = 
					new org.apache.cocoon.deployer.generated.wiring.x10.Property();
				wiringProperty.setName(name);
				
				// investigate deploy properties if the default value has been overwritten		
				String deployValue = null;
				if(deployBlock.getProperties() != null) {
					deployValue = getDeployDescPropertyValue(deployBlock.getProperties().getProperty(), name);
				}
				if(deployValue == null) {
					// wiringProperty.setValue(variableResolver.resolve(property.getDefault()));
					wiringProperty.setValue(property.getDefault());
				} else {
					wiringProperty.setValue(deployValue);
				}
				wiringProperties.addProperty(wiringProperty);
			}
			
			if(wiringProperties.getPropertyCount() > 0) {
				wiringBlock.setProperties(wiringProperties);
			}
		}
		
		return wiringBlock;
	}	
	
	/**
	 * Get the value of a deployment descriptor property
	 * 
	 * @param deployProperties
	 * @param name of the propertiy
	 * @return the value of the property or null if not available
	 */
	protected String getDeployDescPropertyValue(org.apache.cocoon.deployer.generated.deploy.x10.Property[] deployProperties, String name) {
		for(int i = 0; i < deployProperties.length; i++) {
			String curName = deployProperties[i].getName();
			if(name.equals(curName)) {
				return deployProperties[i].getValue();
			}
		}
		return null;
	}

	/**
	 * Extract a block to the filesystem using the FileResourceManager
	 */
	protected void deployBlock(BinaryBlock binaryBlock, FileManager frm, String relativeOutputDir) {
		try {
			ZipUtils.extractZip(new ZipInputStream(binaryBlock.getInputStream()), frm, relativeOutputDir);
		} catch (IOException e) {
			throw new DeploymentException("A problem while extracting a block occurred");
		} catch (FileManagerException e) {
			throw new DeploymentException("A problem while extracting a block occurred");
		}
	}
	
	/**
	 * Extract the Cocoon server to the filesystem using the FileResourceManager
	 */
	protected void deployCocoonServer(FileManager frm, String relativeOutputDir, String serverArtifact) {
		File zip = this.artifactProvider.getArtifact(serverArtifact);
		try {
			ZipUtils.extractZip(new ZipInputStream(new FileInputStream(zip)), frm, relativeOutputDir);
		} catch (FileNotFoundException e) {
			throw new DeploymentException("Can't get the Cocoon server artifact.");
		} catch (IOException e) {
			throw new DeploymentException("A problem while extracting the Cocoon server occurred");
		} catch (FileManagerException e) {
			throw new DeploymentException("A problem while extracting the Cocoon server occured");
		}
	}
	
	/**
     * Create a directory following the naming scheme: 00000001, 00000002, 00000003, 00000004, ...
     * This method is synchronized.
     * 
	 * @param file - the base directory where the new directory should be created
	 * @return the new directory as @link File.
	 */
	public static synchronized int getNextDirectory(File dir, int lastDir) {
		if(lastDir == 0) {
			return getHighestDirectoryNumber(dir);
		}
		return lastDir++;
	}
	
	/**
	 * Convert an int to a string with leading "0" so that the output is a string containing 8 characters.
	 * @param intDir
	 * @return
	 */
	public static String intDirToStringDirConvert(int intDir) {
        // maximum length of direcotries
        int maxDirLength = 8;
        
        // create a string representation of the next available directory
        String nextDir = Integer.toString(intDir + 1);
        StringBuffer nextDirSB = new StringBuffer();
        for(int i = nextDir.length(); i < maxDirLength; i++) {
        	nextDirSB.append("0");
        }
        return nextDirSB.append(nextDir).toString();
	}
	
	/**
	 * Scan a directory for directories that only consist of numbers, find the higest and return it.
	 * @param dir
	 * @return the highest number of an "int"-directory
	 */
	public static int getHighestDirectoryNumber(File dir) {
		if(!dir.exists()) {
			return 0;
		}
        File[] dirContent = dir.listFiles();
        List allDirectories = new ArrayList();
        // filter all directories
        for(int i = 0; i < dirContent.length; i++ ) {
            if(dirContent[i].isDirectory()) {
            	allDirectories.add(dirContent[i]);
            }
        }
        int highestDirNumber = 0;
        // iterate over all directories and get highest number
        Iterator fileIterator = allDirectories.iterator();
        while(fileIterator.hasNext()) {
            String fileName = ((File) fileIterator.next()).getName();
            int fileNameAsInteger;
            try {
                fileNameAsInteger = (new Integer(fileName)).intValue();
                if(fileNameAsInteger > highestDirNumber) {
                	highestDirNumber = fileNameAsInteger;
                }
            } catch(NumberFormatException nfe) {
            	// ignore it because directories with alpha characters are no problem
                // as they don't conflict with the namespace
            }
        }
        return highestDirNumber;
	}	
	
	// ----------------------------------------- setters and getters ------------------------------------------
	
	public boolean isExclusive() {
		return this.exclusive;
	}

	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}

	public void setBaseDirectory(URI baseDirectory) {
		if(!baseDirectory.isAbsolute()) {
			try {
				this.baseDirectory = new URI(new File("").toURI().toString()).resolve(baseDirectory);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("The passed root directory can't be resolved correctly.");
			}
		} else {
			this.baseDirectory = baseDirectory;			
		}
		
	}
	
	public URI getBaseDirectory() {
		return this.baseDirectory;
	}

	public void setVariableResolver(VariableResolver resolver) {
		this.variableResolver = resolver;
	}
	
	public VariableResolver getVariableResolver() {
		return variableResolver;
	}

	public void setArtifactProvider(ArtifactProvider artifactProvider) {
		this.artifactProvider = artifactProvider;
	}
	
	public ArtifactProvider getArtifactProvider() {
		return this.artifactProvider;
	}

}
