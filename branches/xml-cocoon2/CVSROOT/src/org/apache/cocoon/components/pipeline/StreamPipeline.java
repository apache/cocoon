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
import org.apache.excalibur.pool.Recyclable;
import org.apache.avalon.parameters.Parameters;

import org.apache.cocoon.Processor;

/** A <CODE>StreamPipeline</CODE> either
 * <UL>
 *  <LI>collects a <CODE>Reader</CODE> and let it produce a character stream</LI>
 *  <LI>or connects a <CODE>EventPipeline</CODE> with a
 *  <CODE>Serializer</CODE> and let them produce the character stream
 * </UL>
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-04-20 20:50:00 $
 */
public interface StreamPipeline extends Component, Composable, Recyclable, Processor {

    public void setEventPipeline (EventPipeline eventPipeline) throws Exception;
    public EventPipeline getEventPipeline ();
    public void setReader (String role, String source, Parameters param) throws Exception;
    public void setReader (String role, String source, Parameters param, String mimeType) throws Exception;
    public void setSerializer (String role, String source, Parameters param) throws Exception;
    public void setSerializer (String role, String source, Parameters param, String mimeType) throws Exception;
}
