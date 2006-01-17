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
package org.apache.cocoon.spring.samples;

import java.util.Date;

import org.apache.cocoon.core.Core;

/** Simple Bean for testing the use of a Spring container
 *
 * @version $Id$
 */
public class SpringTest {

    protected String message;

    protected Core core;

    protected String version;
    
    public SpringTest() {
        this.message = "Hello World from Spring! Bean created "
                + new Date() + " (should be recreated if sitemap is touched).";
    }

    public String getMessage() {
        return this.message;
    }
    
    public void setCore(Core core) {
        this.core = core;
    }
    
    public Core getCore() {
        return this.core;
    }
    
    public void setJavaVersion(String version) {
        this.version = version;
    }
    
    public String getJavaVersion() {
        return this.version;
    }
    
}
