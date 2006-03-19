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
package org.apache.cocoon.deployer.monolithic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.cocoon.deployer.logger.Logger;
import org.apache.commons.lang.Validate;

/**
 * Deploy a single file. 
 */
public class SingleFileDeployer implements FileDeployer {

	private File basedir;
	private Logger logger;
	private String outputDir;
	
	public SingleFileDeployer(String outputDir) {
		Validate.notNull(outputDir, "An outputDir has to be set.");
		this.outputDir = outputDir;
	}

	public void setBasedir(final File basedir) {
		this.basedir = basedir;
	}

	protected File getBasedir() {
		return this.basedir;
	}

	public void setLogger(final Logger logger) {
		this.logger = logger;
	}
	
	protected Logger getLogger() {
		return this.logger;
	}
	
	protected String getFileName(String documentName) { 
		return documentName.substring(documentName.lastIndexOf('/') + 1);
	}

	protected String getOutputDir() {
		return this.outputDir;
	}
	
	public OutputStream writeResource(String documentName) throws IOException {
		File outDir = new File(this.getBasedir(), getOutputDir());
		if(!outDir.exists()) {
			outDir.mkdirs();
		}
		File targetFile = new File(outDir, this.getFileName(documentName));
		if(targetFile.exists()) {
			throw new FileAlreadyDeployedException("File '" + targetFile + "' already exists!");
		}
		return new FileOutputStream(targetFile);
	}
	
}
