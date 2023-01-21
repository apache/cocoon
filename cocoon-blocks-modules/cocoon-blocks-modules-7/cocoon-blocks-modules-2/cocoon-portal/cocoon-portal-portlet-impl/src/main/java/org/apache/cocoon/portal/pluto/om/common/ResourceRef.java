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
package org.apache.cocoon.portal.pluto.om.common;

/**
 *
 * @version $Id$
 */
public class ResourceRef  {

    private String description = "my description";
    private String name = "name";
    private String type = "type";
    private String auth = "container";
    private String sharing = "shareable";

    /**
     * @return description of the resource reference.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return name of the reference.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieve the type of resource referenced by this
     * resource reference.
     * @return the resource type.
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @return the authorization type of the resource.
     */
    public String getAuth() {
        return auth;
    }

    public String getSharing() {
        return sharing;
    }

    /**
     * @param string the resource reference description.
     */
    public void setDescription(String string) {
        description = string;
    }

    /**
     * @param string the name of the resource reference
     */
    public void setName(String string) {
        name = string;
    }

    public void setType(String string) {
        type = string;
    }

    public void setAuth(String string) {
        auth = string;
    }

    public void setSharing(String string) {
        sharing = string;
    }
}
