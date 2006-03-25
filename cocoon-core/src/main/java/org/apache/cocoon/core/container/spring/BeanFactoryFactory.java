/*
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.excalibur.source.SourceResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Component to set up a new (per sitemap) bean factory.
 *
 * @since 2.2
 * @version $Id$
 */
public interface BeanFactoryFactory {

    /**
     * Build a bean factory with the contents of the &lt;map:components&gt; element of
     * the tree.
     */
    ConfigurableListableBeanFactory createBeanFactory(Logger         sitemapLogger,
                                                      Configuration  config,
                                                      Context        sitemapContext,
                                                      SourceResolver resolver)
    throws Exception;
}
