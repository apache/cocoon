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
 * A coplet layout holds a coplet.
 *
 * @version $Id$
 */
public final class CopletLayout extends Layout {

    /** The coplet instance id. */
    protected String copletInstanceId;

    /**
     * Create a new coplet layout object.
     * Never create a layout object directly. Use the
     * {@link org.apache.cocoon.portal.services.LayoutFactory} instead.
     * @param id The unique identifier of the layout object or null.
     */
    public CopletLayout(String id) {
        super(id);
    }

    public void setCopletInstanceId(String cid) {
        this.copletInstanceId = cid;
    }

    public String getCopletInstanceId() {
        return this.copletInstanceId;
    }
}
