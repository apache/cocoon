/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.Roles;
import org.apache.cocoon.components.saxconnector.SAXConnector;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.sitemap.ErrorNotifier;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.xml.AbstractXMLProducer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2001-04-30 14:17:12 $
 */
public class NonCachingEventPipeline extends AbstractEventPipeline {

    public void recycle() {
        getLogger().debug("Recycling of NonCachingEventPipeline");
        super.recycle();
    }
}
