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
package org.apache.cocoon.components.pipeline.spring;

import java.util.Map;

public interface PipelineComponentScopeHolder {
    //FIXME: This interface needs redesign

    public Map<String, Object> getBeans();

    public void setBeans(Map<String, Object> beans);

    public Map<String, Object> getParentBeans();

    public void setParentBeans(Map<String, Object> parentBeans);

    public Map<String, Object> getDestructionCallbacks();

    public void setDestructionCallbacks(Map<String, Object> destructionCallbacks);

    public Map<String, Object> getParentDestructionCallbacks();

    public void setParentDestructionCallbacks(Map<String, Object> destructionCallbacks);

    public void setInScope(boolean inScope);

    public boolean getInScope();

}
