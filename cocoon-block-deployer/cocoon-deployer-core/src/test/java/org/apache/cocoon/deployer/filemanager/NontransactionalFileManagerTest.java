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
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.cocoon.deployer.AbstractDeployerTestCase;
import org.apache.cocoon.deployer.util.FileUtils;

public class NontransactionalFileManagerTest extends AbstractDeployerTestCase {

	private static final String EXISTING_RESOURCE = "lib-01/lib-01.jar";
	private static final String NOT_EXISTING_RESOURCE = "lib-01/lib-01.jar_______";	

	public void testWrite() throws Exception {
		File basedir = new File(AbstractDeployerTestCase.OUTPUT_DIR, "/xyz/abc");
		String fileName = "x.txt";		
		
		FileManager fm = new NontransactionalFileManager(basedir.toURI());

		OutputStream os = fm.writeResource(fileName);
		InputStream is = new FileInputStream(this.getMockArtefact(EXISTING_RESOURCE));
		FileUtils.copy(is, os);
		fm.commitTransaction();
		
		assertTrue(new File(basedir, fileName).exists());
	}	
	
	public void testRead() throws Exception {
		File basedir = new File(this.MOCKS_DIR);
		InputStream is = new NontransactionalFileManager(basedir.toURI()).readResource(EXISTING_RESOURCE);
	}
	
	public void testReadNotExistingFile() throws Exception {
		File basedir = new File(this.MOCKS_DIR);
		try {
			InputStream is = new NontransactionalFileManager(basedir.toURI()).readResource(NOT_EXISTING_RESOURCE);
			fail("A non-existing file has to throw an exception.");
		} catch(FileManagerException fme) {
			// expected
		}
		
	}	
	
	public void testNontransactionFileManagerWithNullBasedir() throws Exception {
		try {
			FileManager fm = new NontransactionalFileManager(null);
			fail("Null basedir mustn't be allowed.");
		} catch(IllegalArgumentException iae) {
			// expected
		}
	}

}
