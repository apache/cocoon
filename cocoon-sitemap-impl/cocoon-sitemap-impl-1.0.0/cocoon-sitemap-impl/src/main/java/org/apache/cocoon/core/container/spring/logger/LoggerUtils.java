/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring.logger;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;

import org.apache.cocoon.core.container.spring.avalon.AvalonUtils;

/**
 * Commons Logging logger utilities.
 *
 * @since 2.2
 * @version $Id$
 */
public class LoggerUtils {

    /** Bean name for the logger factory returning commons logging Log. */
    public static final String LOGGER_ROLE = "org.apache.cocoon.core.container.spring.logger.Log";

    /**
     * Create a child logger based on the logger configured in the Spring bean factory.
     *
     * @param factory Current Spring bean factory
     * @param category Desired logger sub category
     * @return Logger for the child category of current bean factory configured logger.
     */
    public static Log getChildLogger(BeanFactory factory, String category) {
        LoggerFactoryBean parent = (LoggerFactoryBean) factory.getBean("&" + LoggerUtils.LOGGER_ROLE);
        return getChildLogger(parent, category);
    }

    /**
     * @deprecated 
     */
    public static Log getChildLogger(ServiceManager manager, String category) throws ServiceException {
        LoggerFactoryBean parent = (LoggerFactoryBean) manager.lookup("&" + LoggerUtils.LOGGER_ROLE);
        return getChildLogger(parent, category);
    }

    private static Log getChildLogger(LoggerFactoryBean parent, String category) {
        return LogFactory.getLog(getChildCategory(parent, category));
    }

    static String getChildCategory(LoggerFactoryBean parent, String category) {
        if (category != null && category.length() > 0) {
            return parent.getCategory() + "." + category;
        }

        return parent.getCategory();
    }
}
