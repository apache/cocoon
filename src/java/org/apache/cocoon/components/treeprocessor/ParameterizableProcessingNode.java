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
 * A <code>ProcessingNode</code> that has parameters.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ParameterizableProcessingNode.java,v 1.2 2004/03/05 13:02:51 bdelacretaz Exp $
 */

public interface ParameterizableProcessingNode extends ProcessingNode {

    /**
     * Set the parameters of this node as a <code>Map</code> of <code>VariableResolver</code>s
     * that will be resolved at process-time.
     */
    void setParameters(Map parameterMap);
}
