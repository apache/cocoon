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

/**
 * A frame layout holds a source URI. The URI can be changed dynamically through
 * events. The URI may contain any URI that can be resolved by the Cocoon
 * {@link org.apache.cocoon.environment.SourceResolver}.
 *
 * @version $Id$
 */
public class FrameLayout extends Layout {

    /** This is the name of the temporary attribute in the layout instance holding the current link. */
    public static final String ATTRIBUTE_SOURCE_ID = FrameLayout.class.getName() + "/source";

    protected String source;

    /**
     * Create a new frame layout object.
     * Never create a layout object directly. Use the
     * {@link org.apache.cocoon.portal.services.LayoutFactory} instead.
     * @param id The unique identifier of the layout object or null.
     */
    public FrameLayout(String id) {
        super(id);
    }

    /**
     * @return String
     */
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
