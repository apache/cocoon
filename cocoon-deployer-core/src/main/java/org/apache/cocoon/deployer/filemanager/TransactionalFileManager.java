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
package org.apache.cocoon.deployer.filemanager;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.logging.Logger;

import org.apache.cocoon.deployer.DeploymentException;
import org.apache.cocoon.deployer.util.FileUtils;
import org.apache.commons.transaction.file.FileResourceManager;
import org.apache.commons.transaction.file.ResourceManagerException;
import org.apache.commons.transaction.file.ResourceManagerSystemException;
import org.apache.commons.transaction.util.Jdk14Logger;

/**
 * This implementation provides transactional file system operations.
 */
public class TransactionalFileManager implements FileManager {

    private static final Logger logger = Logger.getLogger(TransactionalFileManager.class.getName());		
    
    /**
     * The FileResourceManager that actually provides transactional filesystem operations
     */
	private FileResourceManager frm;
	
	/**
	 * The transaction id for all operations of this object.
	 */	
	private String txId;
	
	/**
	 * The working directory of the FileResourceManager
	 */
	private File workdir;
	
	public TransactionalFileManager(URI basedir) {	
    	// create the output directory
    	File outputdir = new File(basedir);    	

		if(!outputdir.exists()) {
			if(!outputdir.mkdirs()) {
				throw new DeploymentException("Can't create server directory: " + outputdir.getAbsolutePath());
			}
		}
		
		// create the workdir for the FileResourceManager
		try {
			this.workdir = new File(new URI(basedir + "_WORK"));
		} catch (URISyntaxException ue) {
			throw new DeploymentException("Can't create work directory", ue);
		}
		if(workdir.exists()) {
			FileUtils.deleteDirRecursivly(workdir);
		}

		if(!workdir.mkdirs()) {
			throw new DeploymentException("Can't create work directory");
		}		
		
		// create a random transaction id
		Random rand = new Random();
		txId = Long.toString(rand.nextLong());
		System.out.println("txId: " + txId);		
		
		// create transaction context      	    
	    frm = new FileResourceManager(outputdir.getAbsolutePath(), workdir.getAbsolutePath(), false, new Jdk14Logger(logger));
	    try {
			frm.start();
		    frm.startTransaction(txId);					
		} catch (ResourceManagerSystemException e) {
			throw new DeploymentException("A problem while starting the filesystem transaction manager occurred.");
		} catch (ResourceManagerException e) {
			throw new DeploymentException("A problem while starting the filesystem transaction manager occurred.");
		}
	}

	public InputStream readResource(String resource) throws FileManagerException {
		try {
			return frm.readResource(this.txId, resource);
		} catch (ResourceManagerException e) {
			throw new FileManagerException(e);
		}
	}

	public OutputStream writeResource(String resource) throws FileManagerException {
		try {
			return frm.writeResource(this.txId, resource);
		} catch (ResourceManagerException e) {
			throw new FileManagerException(e);
		}
	}

	public void rollbackTransaction(Exception ex) throws FileManagerException {
		// do nothing with exception
		try {
			frm.rollbackTransaction(this.txId);
			FileUtils.deleteDirRecursivly(this.workdir);
		} catch (ResourceManagerException e) {
			throw new FileManagerException(e);
		}
	}

	public void commitTransaction() throws FileManagerException {
		try {
			frm.commitTransaction(this.txId);
			FileUtils.deleteDirRecursivly(this.workdir);			
		} catch (ResourceManagerException e) {
			throw new FileManagerException(e);
		}
	}

}
