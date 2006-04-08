/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.portal.profile;

import org.apache.cocoon.portal.scratchpad.Profile;

/**
 * The behaviour of the used profile manager can be extended by assigning one
 * or more profile manager aspects to the profile manager.
 *
 * @since 2.2
 * @version $Id$
 */
public interface ProfileManagerAspect {

    /** The role to lookup an aspect. */
    String ROLE = ProfileManagerAspect.class.getName();

    void prepare(ProfileManagerAspectContext context,
                 Profile profile);
}
