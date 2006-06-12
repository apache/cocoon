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
package org.apache.cocoon.maven.deployer.monolithic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.cocoon.maven.deployer.utils.WildcardHelper;
import org.apache.commons.lang.Validate;
import org.apache.maven.plugin.logging.Log;

/**
 * This class performs the actual deployment based on rules. A rule is mapped to a 
 * <code>FileDeployer</code> and when the rule is executed and returns true, the
 * file deployer is executed.
 * 
 * @version $Id$
 */
public class MonolithicServer22 {

	private Log logger;
	private File basedir;
	private List rules = new ArrayList();
	private Set alreadyDeployedFilesSet =  new HashSet();
	
	public MonolithicServer22(File basedir, Log logger) {
		Validate.notNull(basedir, "The basedir of the server mustn't be null.");
		Validate.notNull(logger, "A logger must be set.");
		this.basedir = basedir;
		this.logger = logger;
		this.logger.debug("Basedir: " + basedir.getAbsolutePath());
	}

	public void addRule(String pattern, FileDeployer fileDeployer) {
		fileDeployer.setBasedir(this.basedir);
		fileDeployer.setLogger(this.logger);
		fileDeployer.setAlreadyDeployedFilesSet(alreadyDeployedFilesSet);
		rules.add(new Rule(pattern, fileDeployer));
	}
	
	public void extract(File zipFile) throws IOException {
		ZipInputStream zipStream = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry document = null;
        try {
            do {
                document = zipStream.getNextEntry();
                if (document != null) {
                    // skip directories (only files have to be written)
                    if (document.isDirectory()) {
                    	zipStream.closeEntry();
                        continue;
                    }
                    OutputStream out = null;
	                    try {               	
	                    	FileDeployer fileDeployer = findFileDeployer(document.getName());
	                    	if(fileDeployer == null) {
	                    		continue;
	                    	}
	                    	
	                    	out = fileDeployer.writeResource(document.getName());
		                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		                    // loop over ZIP entry stream
		                    byte[] buffer = new byte[8192];
		                    int length = -1;
		                    while (zipStream.available() > 0) {
		                        length = zipStream.read(buffer, 0, 8192);
		                        if (length > 0) {
		                            baos.write(buffer, 0, length);
		                        }
		                    }
		                    // write it to the output stream provided by the file resource manager
		                    out.write(baos.toByteArray());
	                    } finally {
	                    	if(out != null) {
	                    		out.close();
	                    	}
	                    }
                    // go to next entry
                    zipStream.closeEntry();
                }
            } while (document != null);
        } finally {
        	zipStream.close();
        }
	}
	
	/**
	 * Loop over all rules and if one matches, the corresponding @link FileDeployer is returned.
	 */
	protected FileDeployer findFileDeployer(String name) {
		for(Iterator it = this.rules.iterator(); it.hasNext();) {
			Rule rule = (Rule) it.next();
			HashMap resultMap = new HashMap();
			if(WildcardHelper.match(resultMap, name, rule.compiledPattern)) {
				logger.debug("findFileDeployer: " + name + " matched with pattern '" + rule.patternString);
				return rule.fileDeployer;
			}
		}
		return null;
	}

	private static class Rule {
		String patternString;
		int[] compiledPattern;
		FileDeployer fileDeployer;
		
		public Rule(String pattern, FileDeployer fileDeployer) {
			this.patternString = pattern;
			this.compiledPattern = WildcardHelper.compilePattern(pattern);
			this.fileDeployer = fileDeployer;
		}
	}

}
