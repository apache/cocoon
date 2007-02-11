/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.treeprocessor;

import java.util.Map;

/**
 *
 * @author <a href="mailto:Michael.Melhem@dresdner-bank.com">Michael Melhem</a>
 * @version CVS $Id: PipelineEventComponentProcessingNode.java,v 1.3 2004/03/05 13:02:51 bdelacretaz Exp $
 */
public abstract class PipelineEventComponentProcessingNode extends AbstractProcessingNode {

    protected Map views;
    protected Map pipelineHints;

    public void setViews(Map views) {
        this.views = views;
    }

    // Set any pipeline-hint parameters
    public void setPipelineHints(Map parameterMap) {
        this.pipelineHints = parameterMap;
    }

    public boolean hasViews() {
        return this.views != null;
    }
}
