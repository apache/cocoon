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
package org.apache.cocoon.components.classloader;

import java.util.ArrayList;
import java.util.List;

/**
 * The configuration for a {@link ClassLoaderFactory}.
 * @version $Id$
 */
public class ClassLoaderConfiguration {

    protected final List includes = new ArrayList();
    protected final List excludes = new ArrayList();
    protected final List classDirectories = new ArrayList();
    protected final List libDirectories = new ArrayList();

    public void addInclude(String include) {
        this.includes.add(include);
    }

    public void addExclude(String include) {
        this.excludes.add(include);
    }

    public void addClassDirectory(String include) {
        this.classDirectories.add(include);
    }

    public void addLibDirectory(String include) {
        this.libDirectories.add(include);
    }

    public List getClassDirectories() {
        return classDirectories;
    }

    public List getLibDirectories() {
        return libDirectories;
    }
    
    public List getExcludes() {
        return excludes;
    }
    
    public List getIncludes() {
        return includes;
    }

}
