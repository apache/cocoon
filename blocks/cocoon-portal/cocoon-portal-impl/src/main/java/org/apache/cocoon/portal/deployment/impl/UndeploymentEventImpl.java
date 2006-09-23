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
package org.apache.cocoon.portal.deployment.impl;

import org.apache.cocoon.portal.deployment.UndeploymentEvent;

/**
 * Default implementation of the undeployment event.
 *
 * @version $Id$
 */
public class UndeploymentEventImpl implements UndeploymentEvent {

    /** The corresponding uri. */
    protected String uri;

    /** The deployment status. */
    protected int status = STATUS_EVAL;

    /**
     * 
     */
    public UndeploymentEventImpl(String uri) {
        this.uri = uri;
    }
    
    /**
     * @see org.apache.cocoon.portal.deployment.UndeploymentEvent#getDeploymentUri()
     */
    public String getDeploymentUri() {
        return this.uri;
    }

    /**
     * @see org.apache.cocoon.portal.deployment.DeploymentStatus#getStatus()
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * @see org.apache.cocoon.portal.deployment.DeploymentEvent#setStatus(int)
     */
    public void setStatus(int i) {
        this.status = i;
    }
}
