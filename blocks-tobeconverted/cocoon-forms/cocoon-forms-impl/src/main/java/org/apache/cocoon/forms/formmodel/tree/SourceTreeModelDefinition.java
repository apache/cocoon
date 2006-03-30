/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.forms.formmodel.tree;

import org.apache.excalibur.source.SourceResolver;

/**
 * Definition for {@link SourceTreeModel}
 * 
 * @version $Id$
 */
public class SourceTreeModelDefinition implements TreeModelDefinition {

    public static final int[][] NO_PATTERNS = new int[0][];
    private String url;
    private int[][] fileIncludePatterns = NO_PATTERNS;
    private int[][] fileExcludePatterns = NO_PATTERNS;
    private int[][] dirIncludePatterns = NO_PATTERNS;
    private int[][] dirExcludePatterns = NO_PATTERNS;
    private SourceResolver resolver;

    public void setURL(String url) {
        this.url = url;
    }

    public void setFilePatterns(int[][] include, int[][] exclude) {
        this.fileIncludePatterns = include;
        this.fileExcludePatterns = exclude;
    }

    public void setDirectoryPatterns(int[][] include, int[][] exclude) {
        this.dirIncludePatterns = include;
        this.dirExcludePatterns = exclude;
    }
    
    public TreeModel createInstance() {
        return new SourceTreeModel(this);
    }

    public int[][] getDirectoryExcludePatterns() {
        return dirExcludePatterns;
    }

    public int[][] getDirectoryIncludePatterns() {
        return dirIncludePatterns;
    }

    public int[][] getFileExcludePatterns() {
        return fileExcludePatterns;
    }

    public int[][] getFileIncludePatterns() {
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
