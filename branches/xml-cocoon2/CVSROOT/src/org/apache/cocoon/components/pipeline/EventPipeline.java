/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.pipeline;

import org.apache.avalon.component.Component;
import org.apache.avalon.component.Composable;
import org.apache.avalon.parameters.Parameters;
import org.apache.cocoon.Processor;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.sitemap.Sitemap;
import org.apache.excalibur.pool.Recyclable;

/**
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-04-25 17:06:20 $
 */
public interface EventPipeline extends Component, Composable, Recyclable, Processor {
    public void setGenerator (String role, String source, Parameters param, Exception e) throws Exception;
    public void setGenerator (String role, String source, Parameters param) throws Exception;
    public Generator getGenerator ();
    public void addTransformer (String role, String source, Parameters param) throws Exception;
    public boolean process(Environment environment) throws Exception;
}
