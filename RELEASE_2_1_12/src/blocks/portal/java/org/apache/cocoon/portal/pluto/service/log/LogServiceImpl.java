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
package org.apache.cocoon.portal.pluto.service.log;

import org.apache.pluto.services.log.LogService;
import org.apache.pluto.services.log.Logger;

/**
 * Our own log service logging to an avalon logger
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id$
 */
public class LogServiceImpl 
implements LogService {

    /** The logger to use */
    protected Logger logger;
    
    /** Constructor */
    public LogServiceImpl(org.apache.avalon.framework.logger.Logger logger) {
        this.logger = new LoggerImpl(logger);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#getLogger(java.lang.Class)
     */
    public org.apache.pluto.services.log.Logger getLogger(Class arg0) {
        return this.logger;
    }
    /* (non-Javadoc)
     * @see org.apache.pluto.services.log.LogService#getLogger(java.lang.String)
     */
    public org.apache.pluto.services.log.Logger getLogger(String arg0) {
        return this.logger;
    }
}
