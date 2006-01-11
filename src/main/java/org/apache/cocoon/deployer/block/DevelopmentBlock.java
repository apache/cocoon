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


/**
 * A block that is under development and available from within the filesystem
 * of the server it is deployed to.
 */
public interface DevelopmentBlock extends Block {

	/**
	 * @return the root directory of the block
	 */
	public File getRootDirectory();
	
	/**
	 * @return the root sitemap of the block
	 */
	public File getSitemap();
	
	/**
	 * @return the directory where all Java classes are compiled to
	 */
	public File getClassesDirectory();
	
}
