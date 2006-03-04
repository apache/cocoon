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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.cocoon.deployer.DeploymentException;
import org.apache.cocoon.deployer.util.FileUtils;
import org.apache.commons.lang.Validate;

/**
 * This implementation provides non-transaction file system operations.
 */
public class NontransactionalFileManager implements FileManager {

	private File basedir;
	private List fosList = new ArrayList();
	
	public NontransactionalFileManager(URI basedir) {
		Validate.notNull(basedir, "basedir mustn't be null.");
		this.basedir = new File(basedir);
		if(!this.basedir.exists()) {
			if(!this.basedir.mkdirs()) {
				throw new DeploymentException("Can't create the base directory '" + this.basedir + "'.");
			}
		}
	}

	public InputStream readResource(String resource) throws FileManagerException {
		Validate.notNull(resource, "resource mustn't be null.");		
		try {
			return new FileInputStream(new File(this.basedir, cleanResource(resource)));
		} catch (FileNotFoundException e) {
			throw new FileManagerException(e);
		}
	}

	public OutputStream writeResource(String resource) throws FileManagerException {
		Validate.notNull(resource, "resource mustn't be null.");				
		try {
			FileOutputStream fos = new FileOutputStream(FileUtils.createDirectory(new File(this.basedir, resource)));
			fosList.add(fos);
			return fos;
		} catch (FileNotFoundException e) {
			throw new FileManagerException(e);
		} catch (IOException e) {
			throw new FileManagerException(e);
		}
	}

	public void rollbackTransaction(Exception ex) throws FileManagerException {
		closeFileOutputStreams();		
		throw new DeploymentException("Can't rollback when using a nontransactional implementation.", ex);
	}

	public void commitTransaction() throws FileManagerException {
		closeFileOutputStreams();
	}
	
	private void closeFileOutputStreams() throws FileManagerException {
		for(int i = 0; i < fosList.size(); i++) {
			try {
				((FileOutputStream) fosList.get(i)).close();
			} catch (IOException e) {
				throw new FileManagerException(e);
			}
		}
	}
	
	protected String cleanResource(String resource) {
		if(resource.startsWith("\\") || resource.startsWith("/")) {
			return resource.substring(1);
		}
		return resource;
	}

}
