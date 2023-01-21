/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.auth.impl;

import org.apache.cocoon.auth.User;

/**
 * Interface for the user dao.
 * 
 * @version $Id$
 */
public interface UserDAO {

    /**
     * Get a user info for the given user name.
     */
    UserInfo getUserInfo(String name);

    /**
     * Store or update a user info in the database.
     */
    void storeUserInfo(UserInfo info);

    /**
     * Get the complete user for the user info.
     */
    User getUser(UserInfo info);
}
