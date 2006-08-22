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
package org.apache.cocoon.auth;

/**
 * This component loads/saves application data for a user. If an application
 * has an application store, the {@link #loadApplicationData(User, Application)}
 * is executed after the user has logged into the application. The loaded
 * data object is stored in the session, see {@link org.apache.cocoon.auth.ApplicationUtil}.
 * Calling the {@link #saveApplicationData(User, Application, Object)} is the
 * task of the application.
 *
 * @version $Id$
*/
public interface ApplicationStore {

    /**
     * Load data for the given user and application.
     * For session replication to work this data should be {@link java.io.Serializable}.
     * @param user The current user.
     * @param app  The current application.
     * @return The loaded data or null.
     */
    Object loadApplicationData(User user, Application app);

    /**
     * Save the data for the given user and application.
     * @param user The current user.
     * @param app  The current application.
     * @param data The user data.
     */
    void saveApplicationData(User user, Application app, Object data);
}
