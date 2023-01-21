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
package org.apache.cocoon.forms.formmodel.tree;

import java.util.List;

import org.apache.excalibur.source.SourceResolver;

/**
 * Definition for {@link SourceTreeModel}
 * 
 * @version $Id$
 */
public class SourceTreeModelDefinition implements TreeModelDefinition {

    private String url;
    private List fileIncludePatterns;
    private List fileExcludePatterns;
    private List dirIncludePatterns;
    private List dirExcludePatterns;
    private SourceResolver resolver;

    public void setURL(String url) {
        this.url = url;
    }

    public void setFilePatterns(List includes, List excludes) {
        this.fileIncludePatterns = includes;
        this.fileExcludePatterns = excludes;
    }

    public void setDirectoryPatterns(List includes, List excludes) {
        this.dirIncludePatterns = includes;
        this.dirExcludePatterns = excludes;
    }
    
    public TreeModel createInstance() {
        return new SourceTreeModel(this);
    }

    public List getDirectoryExcludePatterns() {
        return dirExcludePatterns;
    }

    public List getDirectoryIncludePatterns() {
        return dirIncludePatterns;
    }

    public List getFileExcludePatterns() {
        return fileExcludePatterns;
    }

    public List getFileIncludePatterns() {
        return fileIncludePatterns;
    }

    public void setSourceResolver(SourceResolver resolver) {
        this.resolver = resolver;
    }

    public String getRootURL() {
        return this.url;
    }
    
    public SourceResolver getResolver() {
        return this.resolver;
    }
}
