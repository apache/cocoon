/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Contextualizable;

import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.components.pipeline.StreamPipeline;
import org.apache.cocoon.components.pipeline.EventPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.Processor;

/**
 * Base interface for generated <code>Sitemap</code> classes
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.4.17 $ $Date: 2001-04-19 11:30:32 $
 */
public interface Sitemap extends CompiledComponent, Configurable, Contextualizable, Processor {
    int GENERATOR = 1;
    int TRANSFORMER = GENERATOR << 1;
    int SERIALIZER = TRANSFORMER << 1;
    int READER = SERIALIZER << 1;
    int ACTION = READER << 1;
    int MATCHER = ACTION << 1;
    int SELECTOR = MATCHER << 1;

    /**
     * Process the given <code>Environment</code> 
     */
    boolean process(Environment environment) throws Exception;

    /**
     * Process the given <code>Environment</code> assebling 
     * a <code>StreamPipeline</code> and an <code>EventPipeline</code>
     */
    boolean process(Environment environment, StreamPipeline pipeline, EventPipeline eventPipeline)
    throws Exception;
}
