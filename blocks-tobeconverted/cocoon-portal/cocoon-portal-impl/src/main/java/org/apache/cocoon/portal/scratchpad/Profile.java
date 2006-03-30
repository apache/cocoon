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
package org.apache.cocoon.portal.scratchpad;

import java.util.Collection;

import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.Layout;

/**
 * The profile for a single user.
 * WORK IN PROGRESS
 *
 * @version $Id$
 * @since 2.2
 */
public interface Profile {

    String getProfileName();

    Collection getCopletInstanceDataObjects();
    Collection getLayoutObjects();

    Layout searchLayout(String layoutId);
    Layout getRootLayout();

    CopletInstanceData searchCopletInstanceData(String copletId);
    Collection searchCopletInstanceDataObjects(String copletDataId);
    Collection searchCopletInstanceDataObjects(CopletData copletData);
}
