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
public class TagDefinition {

    private String uri ="http://java.sun.com/portlet";
    private String location = "/WEB-INF/tld/portlet.tld";

    /**
     *
     */
    public String getLocation() {
        return this.location;
    }

    /**
     *
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * @param string
     */
    public void setLocation(String string) {
        this.location = string;
    }

    /**
     * @param string
     */
    public void setUri(String string) {
        this.uri = string;
    }
}
