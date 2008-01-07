/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.deployment;

import java.io.IOException;
import java.io.InputStream;

/**
 * This object describes an artifact to be deployed.
 *
 * @version $Id$
 */
public interface DeploymentObject {

	/**
	 * Closes any resources that may have been opend during the use
	 * of this object.
     * Usually this method is called by the {@link DeploymentManager}.
	 * @throws IOException
	 */
	void close() throws IOException;

	/**
	 * Retreives the configuration for this deployment artifact
	 * based on the artifact-relative configuration path.
	 * @param configPath artifact-relative path to the confiuration file
	 * @return Configuration of this artificat or <code>null</code> if the
	 * configuration is not present in the artifact.
	 * @throws IOException error opening the configuration
	 */
	InputStream getConfiguration(String configPath) throws IOException;

	/**
     * Returns the name of the artifact.
	 */
	String getName();

	/**
	 * The corresponding uri of the deployment unit.
	 */
	String getUri();
}
