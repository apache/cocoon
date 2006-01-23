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
package org.apache.cocoon.deployer;

import java.io.File;

/**
 * The ArtifactProvider is an abstraction of getting access to the actual block. A client using
 * this library can make use of it to plugin its own library mechanism.
 */
public interface ArtifactProvider {

	public File getArtifact(String artifact);
	
	/**
	 * This method is used to resolve all dependencies of the passed artifact ids. It supports
	 * transitive dependencies and makes sure that only one version of an artifact is returned.
	 */
	public File[] getArtifact(String mainArtifactId, String[] artifactIds);
	
}
