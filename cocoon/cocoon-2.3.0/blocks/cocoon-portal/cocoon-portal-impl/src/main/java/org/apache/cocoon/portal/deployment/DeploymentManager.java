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

/**
 * This component scans for artifacts to be deployed and fires {@link DeploymentEvent}s
 * to deploy/undeploy an artifact.
 *
 * @version $Id$
 */
public interface DeploymentManager {

    /**
     * Deploy a new artifact.
     */
    DeploymentStatus deploy(String uri)
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
