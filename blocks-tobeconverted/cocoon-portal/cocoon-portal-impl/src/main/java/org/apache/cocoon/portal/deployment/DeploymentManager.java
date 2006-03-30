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
package org.apache.cocoon.portal.deployment;

import org.apache.excalibur.source.Source;


/**
 * This component scans for artifacts to be deployed and fires {@link DeploymentEvent}s
 * to deploy/undeploy an artifact.
 *
 * @version $Id$
 */
public interface DeploymentManager {

    /** The role of this component. */
    String ROLE = DeploymentManager.class.getName();

    /**
     * Deploy a new artifact.
     */
    DeploymentStatus deploy(Source source)
    throws DeploymentException;

    /**
     * Undeploy an artifact.
     */
    DeploymentStatus undeploy(String uri)
    throws DeploymentException;

    /**
     * Scan for new artifacts to deploy/old artifacts to undeploy.
     * For each artifact found, a {@link  DeploymentEvent} is fired.
     */
    void scan();
}
