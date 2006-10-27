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
package org.apache.cocoon.portal.om;

import org.apache.cocoon.portal.services.LayoutFactory;

/**
 * A link layout references another layout to be used instead. The reference
 * can be changed at runtime using {@link LayoutInstance}.
 *
 * @version $Id$
 */
public class LinkLayout extends Layout {

    /** This is the name of the temporary attribute in the layout instance holding the current layout id. */
    public static final String ATTRIBUTE_LAYOUT_ID = "link-layout-id";

    protected String linkedProfileName;
    protected String linkedLayoutId;

    /**
     * Create a new link layout object.
     * Never create a layout object directly. Use the
     * {@link LayoutFactory} instead.
     * @param id The unique identifier of the layout object or null.
     * @param name The name of the profile.
     */
    public LinkLayout(String id, String name) {
        super(id, name);
    }

    public String getLayoutId() {
        return this.linkedLayoutId;
    }

    public String getProfileName() {
        return linkedProfileName;
    }

    /**
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        LinkLayout clone = (LinkLayout)super.clone();

        clone.linkedLayoutId = this.linkedLayoutId;
        clone.linkedProfileName = this.linkedProfileName;

        return clone;
    }
}
