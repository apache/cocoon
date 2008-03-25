/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cocoon.core.container.spring.pipeline;

import java.util.Map;
import java.util.StringTokenizer;

import org.apache.cocoon.components.pipeline.impl.PipelineComponentInfo;


/**
 * @version $Id$
 * @since 2.2
 */
public class PipelineComponentInfoInitializer {
    private PipelineComponentInfo info;
    private String componentName;
    private String mimeType;
    private String label;
    private String hint;
    private Map data;
    
    public void init() {
        if (this.mimeType != null)
            this.info.setMimeType(this.componentName, this.mimeType);
        if (this.label != null) {
            StringTokenizer st = new StringTokenizer(this.label, " ,", false);
            String[] labels = new String[st.countTokens()];
            for (int tokenIdx = 0; tokenIdx < labels.length; tokenIdx++) {
                labels[tokenIdx] = st.nextToken();
            }
            this.info.setLabels(this.componentName, labels);
        }
        if (this.hint != null)
            this.info.setPipelineHint(this.componentName, this.hint);
        if (this.data != null)
            this.info.addData(data);
    }

    /**
     * @param info
     */
    public void setInfo(PipelineComponentInfo info) {
        this.info = info;
    }

    /**
     * @param componentName
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
    /**
     * @param hint
     */
    public void setHint(String hint) {
        this.hint = hint;
    }
    /**
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }
    /**
     * @param mimeType
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @param data the data to set
     */
    public void setData(Map data) {
        this.data = data;
    }

}
