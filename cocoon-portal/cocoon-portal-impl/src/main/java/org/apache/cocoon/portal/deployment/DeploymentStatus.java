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
 * Status of a deployment or undeployment action.
 *
 * @version $Id$
 */
public interface DeploymentStatus {

    /** The operation could be performed successfully. */
    int STATUS_OKAY   = 1;
    /** The operation hasn't been performed yet. */
    int STATUS_EVAL   = 0;
    /** An error occured during the operation. */
    int STATUS_FAILED = -1;

    /**
     * Return the status of the operation (deployment or undeployment)
     */
    int getStatus();
}