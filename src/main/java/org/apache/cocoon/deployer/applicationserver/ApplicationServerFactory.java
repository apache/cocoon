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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.cocoon.deployer.ArtifactProvider;
import org.apache.cocoon.deployer.DeploymentException;
import org.apache.cocoon.deployer.generated.deploy.x10.Cocoon;
import org.apache.cocoon.deployer.resolver.VariableResolver;

public class ApplicationServerFactory {

	public static BlocksFramework createServer(Cocoon cocoon, VariableResolver variableResolver, 
		ArtifactProvider artifactProvider, boolean exclusive) {
		
		BlocksFramework cocoonServer = new BlocksFramework23();
		cocoonServer.setExclusive(exclusive);
		cocoonServer.setVariableResolver(variableResolver);
		cocoonServer.setArtifactProvider(artifactProvider);
		
		try {
			cocoonServer.setBaseDirectory(new URI(cocoon.getTargetUrl()));
		} catch (URISyntaxException e) {
			throw new DeploymentException("Invalid Cocoon server URL");
		}
		
		return cocoonServer;
	}

}
