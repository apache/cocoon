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
package org.apache.cocoon.deployer.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.apache.cocoon.deployer.DeploymentException;
import org.apache.commons.transaction.file.FileResourceManager;
import org.apache.commons.transaction.file.ResourceManagerException;
import org.apache.commons.transaction.file.ResourceManagerSystemException;
import org.apache.commons.transaction.util.Jdk14Logger;

/**
 * Utitily class to handle ZIP archives.
 */
public class FileUtils {
	
    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());		

	/**
	 * Delete a directory recursivly
	 * @param directory
	 * @return true if deletation went okay
	 */
    public static boolean deleteDirRecursivly(File directory) {
        if (directory.isDirectory()) {
            String[] children = directory.list();
            for (int i=0; i < children.length; i++) {
                boolean success = deleteDirRecursivly(new File(directory, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return directory.delete();
    }	
    
    /**
     * A factory method that creates a @link FileResourceManager using the passed base directory and transaction id.
     * @return the intialized resource manager
     */
    public static FileResourceManager createFileResourceManager(String txId, URI basedir) {

    	// create the output directory
    	File outputdir = new File(basedir);    	

		if(!outputdir.exists()) {
			if(!outputdir.mkdirs()) {
				throw new DeploymentException("Can't create server directory: " + outputdir.getAbsolutePath());
			}
		}
		
		// create the workdir for the FileResourceManager
		File workdir = null;
		try {
			workdir = new File(new URI(basedir + "_WORK"));
		} catch (URISyntaxException ue) {
			throw new DeploymentException("Can't create work directory", ue);
		}
		if(workdir.exists()) {
			FileUtils.deleteDirRecursivly(workdir);
		}

		if(!workdir.mkdirs()) {
			throw new DeploymentException("Can't create work directory");
		}		
		
		// create transaction context      	    
	    FileResourceManager frm = new FileResourceManager(outputdir.getAbsolutePath(), workdir.getAbsolutePath(), false, new Jdk14Logger(logger));
	    try {
			frm.start();
		    frm.startTransaction(txId);					
		} catch (ResourceManagerSystemException e) {
			throw new DeploymentException("A problem while starting the filesystem transaction manager occurred.");
		} catch (ResourceManagerException e) {
			throw new DeploymentException("A problem while starting the filesystem transaction manager occurred.");
		}
		return frm;
    }
	
}
