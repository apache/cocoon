/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.pipeline;


import org.apache.avalon.Component;
import org.apache.avalon.Composer;
import org.apache.avalon.Recyclable;
import org.apache.avalon.configuration.Parameters;

import org.apache.cocoon.Processor;
import org.apache.cocoon.environment.Environment;

/**
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-04-04 15:42:42 $
 */
public interface EventPipeline extends Component, Composer, Recyclable, Processor {
    public void setGenerator (String role, String source, Parameters param, Exception e) throws Exception;
    public void setGenerator (String role, String source, Parameters param) throws Exception;
    public void addTransformer (String role, String source, Parameters param) throws Exception;
    public boolean process(Environment environment) throws Exception;
}