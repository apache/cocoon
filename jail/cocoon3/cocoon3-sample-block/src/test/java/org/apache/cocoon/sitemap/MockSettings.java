/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.sitemap;

import java.util.List;

import org.apache.cocoon.configuration.Settings;

public class MockSettings implements Settings {

    public String getCacheDirectory() {
        return null;
    }

    public String getContainerEncoding() {
        return null;
    }

    public long getCreationTime() {
        return 0;
    }

    public String getFormEncoding() {
        return null;
    }

    public List<?> getLoadClasses() {
        return null;
    }

    public String getProperty(String key) {
        return null;
    }

    public String getProperty(String key, String defaultValue) {
        return null;
    }

    public List<?> getPropertyNames() {
        return null;
    }

    public List<?> getPropertyNames(String keyPrefix) {
        return null;
    }

    public long getReloadDelay(String type) {
        return 0;
    }

    public String getRunningMode() {
        return null;
    }

    public String getWorkDirectory() {
        return null;
    }

    public boolean isReloadingEnabled(String type) {
        return false;
    }
}
