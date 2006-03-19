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
import java.io.IOException;
import java.io.OutputStream;

import org.apache.cocoon.deployer.logger.Logger;

/**
 * Use this interface for classes that are used by the @link org.apache.cocoon.maven.deployer.monolithic.ZipExtractor
 * to extract ZIP files. Classes implementing this interface can be added together with a rule to the
 * @link org.apache.cocoon.maven.deployer.monolithic.ZipExtractor and when the rule matches, the execute method
 * is called.
 */
public interface FileDeployer {

	public OutputStream writeResource(String documentName) throws IOException;

	public void setBasedir(File file);

	public void setLogger(Logger logger);
	
}
