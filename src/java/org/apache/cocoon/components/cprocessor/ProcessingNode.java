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
package org.apache.cocoon.components.cprocessor;

import org.apache.cocoon.environment.Environment;

/**
 * The ProcessingNode is used to represent the interface for using a processing tree.
 * Each node will recursively call its child nodes until the entire processing tree is
 * executed for the particular request.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ProcessingNode.java,v 1.6 2004/03/08 13:57:39 cziegeler Exp $
 */
public interface ProcessingNode extends Node {

    public static final String ROLE = ProcessingNode.class.getName();

    /**
     * Process this node with the supplied environment information. Will recursively
     * process child nodes.
     *
     * @param env     The Environment object used to process a request.
     * @param context The runtime context information for this request.
     * @return <code>true</code> if a redirect has been issued.
     * @throws Exception if the processing was not successful.
     */
    boolean invoke(Environment env, InvokeContext context) throws Exception;

}
