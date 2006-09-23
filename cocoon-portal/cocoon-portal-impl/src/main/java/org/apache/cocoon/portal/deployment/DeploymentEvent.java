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

import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.Receiver;

/**
 * A deployment event is fired when a new artifact is found for deployment.
 * A {@link Receiver} for this event should first check the status before performing
 * an action. If the receiver could deploy the artifact, it should set the status
 * accordingly. If the receiver is not responsible for deploying this type of artifact
 * or if the artifact has already been deployed, the receiver should just ignore the
 * event. If an error occurs during deployment the status should be updated by the
 * receiver as well.
 *
 * @version $Id$
 */
public interface DeploymentEvent
    extends DeploymentStatus, Event {

    /**
	 * Get the corresponding deployment object.
	 */
	DeploymentObject getDeploymentObject();

	/**
	 * Sets the status of this event. @see getEvent()
	 */
	void setStatus(int status);
}
