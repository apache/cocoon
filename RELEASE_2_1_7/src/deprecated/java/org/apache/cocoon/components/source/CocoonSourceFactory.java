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
package org.apache.cocoon.components.source;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Source;
import org.apache.cocoon.Processor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class implements the cocoon: protocol.
 * It cannot be configured like the other source factories
 * as it needs the current <code>Sitemap</code> as input.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CocoonSourceFactory.java,v 1.2 2004/03/05 13:02:40 bdelacretaz Exp $
 */
public final class CocoonSourceFactory
extends AbstractLogEnabled
implements SourceFactory {

    /** The component manager */
    private ComponentManager  manager;

    public CocoonSourceFactory(Processor processor,
                               ComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Resolve the source
     */
    public Source getSource(Environment environment, String location)
    throws ProcessingException, IOException, MalformedURLException {
        if (environment == null)
            throw new ProcessingException("CocoonSourceFactory: environment is required.");
        return new SitemapSource(this.manager,
                                 location,
                                 this.getLogger());
    }

    /**
     * Resolve the source
     */
    public Source getSource(Environment environment, URL base, String location)
    throws ProcessingException, IOException, MalformedURLException {
        if (environment == null)
            throw new ProcessingException("CocoonSourceFactory: environment is required.");
        return this.getSource(environment, base.toExternalForm() + location);
    }
}
