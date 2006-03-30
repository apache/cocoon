/* 
 * Copyright 2006 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring;

import org.apache.cocoon.core.Settings;

/**
 * This is a simple test bean.
 * $Id$
 */
public class TestBean {

    protected String message;

    protected Settings settings;

    protected String javaVersion;

    public TestBean() {
        this.message = "Hello World from Spring";    
    }
    public String getMessage() {
        return this.message;
    }
    public String getJavaVersion() {
        return javaVersion;
    }
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }
    public Settings getSettings() {
        return settings;
    }
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

}
