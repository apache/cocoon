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
package org.apache.cocoon.components.slide.impl;

import java.io.File;
import java.util.Hashtable;

import org.apache.slide.common.Domain;
import org.apache.slide.common.ServiceParameterErrorException;
import org.apache.slide.common.ServiceParameterMissingException;
import org.apache.slide.store.txfile.TxFileContentStore;

/**
 * Specialized version of the TxFileContentStore from the
 * Jakarta Slide project, which respects the context path and work directory.
 *
 * @version $Id$
 */
public class ContextTxFileContentStore extends TxFileContentStore {
    
    public ContextTxFileContentStore() {
    }
    
    public void setParameters(Hashtable parameters)
        throws ServiceParameterErrorException, ServiceParameterMissingException {
        
        // resolve the rootpath parameter relative to the webapp context path
        String rootpath = (String) parameters.get(STORE_DIR_PARAMETER);
        rootpath = new File(Domain.getParameter("contextpath"),rootpath).toString();
        parameters.put(STORE_DIR_PARAMETER,rootpath);
        
        // resolve the workpath parameter relative to the cocoon work directory
        String workpath = (String) parameters.get(WORK_DIR_PARAMETER);
        workpath = new File(Domain.getParameter("workdir"),workpath).toString();
        parameters.put(WORK_DIR_PARAMETER,workpath);
        
        super.setParameters(parameters);
    }

}
