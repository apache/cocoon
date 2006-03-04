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
/**
 * 
 */
package org.apache.cocoon.deployer.applicationserver;

import java.io.File;
import java.net.URI;

import org.apache.cocoon.deployer.ArtifactProvider;
import org.apache.cocoon.deployer.block.Block;
import org.apache.cocoon.deployer.logger.Logger;
import org.apache.cocoon.deployer.resolver.VariableResolver;

public interface CocoonServer {

	public boolean deploy(Block[] blocks, String serverArtifact, File[] libraries, Logger log, boolean transactional);
	
	public boolean isExclusive();
	
	public void setExclusive(boolean exclusive);
	
	public void setBaseDirectory(URI uri);
	
	public void setVariableResolver(VariableResolver resolver);

	public void setArtifactProvider(ArtifactProvider artifactProvider);
	
}
