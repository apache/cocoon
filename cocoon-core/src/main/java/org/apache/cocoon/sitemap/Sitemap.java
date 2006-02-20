/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.sitemap;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.flow.Interpreter;

/**
 * TODO WORK IN PROGRESS!!
 * FIXME - we should not use the service manager
 * This interface describes the current sitemap. The current sitemap is available using
 * {@link org.apache.cocoon.core.Core#getCurrentSitemap()}.
 *
 * @since 2.2
 * @version $Id$
 */
public interface Sitemap {

    /**
     * Return the locator of the current sitemap.
     * @return The current locator.
     */
    ServiceManager getComponentLocator();

    /**
     * Return the current processor
     */
    Processor getProcessor();

    /**
     * Return the Interpreter for the given language. If no
     * interpreter is found <code>null</code> is returned.
     * @param language The language or <code>null</code> for the default interpreter.
     * @return The interpreter or <code>null</code>.
     */
    Interpreter getInterpreter(String language);
}
