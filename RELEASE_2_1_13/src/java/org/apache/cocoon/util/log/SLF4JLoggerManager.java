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
package org.apache.cocoon.util.log;

import org.apache.avalon.excalibur.logger.AbstractLoggerManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.logger.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a LoggerManager with a direct delegation to slf4j.
 * 
 * @author <a href="mailto:cdamioli@apache.org">Cédric Damioli</a>
 * @author <a href="mailto:lmedioni@temenos.com">Laurent Medioni</a>
 * @version $Id$
 */
public class SLF4JLoggerManager extends AbstractLoggerManager implements LoggerManager {

    /**
     * @param prefix
     * @param switchTo
     * @param defaultLoggerOverride
     */
    public SLF4JLoggerManager(String prefix, String switchTo, Logger defaultLoggerOverride) {
        super(prefix, switchTo, defaultLoggerOverride);
    }
    
    public SLF4JLoggerManager(){
        super(null, null, null);
    }
    
    protected Logger doGetLoggerForCategory(String fullCategoryName) {
        org.slf4j.Logger logger = LoggerFactory.getLogger(fullCategoryName);
        return new SLF4JLoggerAdapter(logger);
    }
}
