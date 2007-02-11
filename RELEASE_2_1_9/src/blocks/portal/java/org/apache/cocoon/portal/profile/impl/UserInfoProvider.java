/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.profile.impl;

/**
 * Get the information about the current user.
 * This data object is used for loading the profile. It decouples the
 * portal from the used authentication method.
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: MapProfileLS.java 30941 2004-07-29 19:56:58Z vgritsenko $
 */
public interface UserInfoProvider {
    
    /**
     * Return the user info about the current user.
     */
    UserInfo getUserInfo(String portalName, String layoutKey) 
    throws Exception;
}
