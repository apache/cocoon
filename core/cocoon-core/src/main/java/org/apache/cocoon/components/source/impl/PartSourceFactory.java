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
package org.apache.cocoon.components.source.impl;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * A factory for {@link org.apache.cocoon.servlet.multipart.Part} based sources (see {@link PartSource}).
 *
 * @version $Id$
 */
public class PartSourceFactory implements SourceFactory, Contextualizable, ThreadSafe
{
    Context context;
    
    /*
     * Returns a new {@link PartSource} based on the uri.
     *
     * @see org.apache.excalibur.source.SourceFactory#getSource(java.lang.String, java.util.Map)
     */
    public Source getSource(String uri, Map parameters) throws IOException, MalformedURLException
    {
        Map objectModel = ContextHelper.getObjectModel(context);
        return new PartSource(uri, objectModel);
    }

    /**
     * Do nothing, {@link PartSource}s don't need to be released.
     *
     * @see org.apache.excalibur.source.SourceFactory#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source)
    {
        // Nothing to do here
    }

    /**
     * Get the objectModel from the Context
     */
    public void contextualize(org.apache.avalon.framework.context.Context context)
    throws ContextException {
         this.context = context;
    }
}
