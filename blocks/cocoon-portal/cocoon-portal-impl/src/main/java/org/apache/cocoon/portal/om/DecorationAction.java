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
 * @version $Id$
 */
public final class DecorationAction {

    public static final String WINDOW_STATE_NORMAL = "normal-uri";
    public static final String WINDOW_STATE_MINIMIZED = "minimize-uri";
    public static final String WINDOW_STATE_MAXIMIZED = "maximize-uri";
    public static final String WINDOW_STATE_FULLSCREEN = "fullscreen-uri";

    protected String name;
    protected String url;

    public DecorationAction(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }
}