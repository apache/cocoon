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
package org.apache.cocoon.blocks.components;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.SourceUtil;
import org.apache.excalibur.source.URIAbsolutizer;

/**
 * This class implements the block: protocol.
 *
 *
 * @version $Id$
 */
public final class BlockSourceFactory
extends AbstractLogEnabled
implements SourceFactory, ThreadSafe, URIAbsolutizer
{

/* (non-Javadoc)
 * @see org.apache.excalibur.source.SourceFactory#getSource(java.lang.String, java.util.Map)
 */
public Source getSource( String location, Map parameters )
    throws MalformedURLException, IOException {
    if( getLogger().isDebugEnabled() ) {
        getLogger().debug( "Creating source object for " + location );
    }

    return new BlockSource(location, getLogger());
}

/* (non-Javadoc)
 * @see org.apache.excalibur.source.SourceFactory#release(org.apache.excalibur.source.Source)
 */
public void release( Source source ) {
    if ( null != source ) {
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Releasing source " + source.getURI());
        }
    }
}

/* (non-Javadoc)
 * @see org.apache.excalibur.source.URIAbsolutizer#absolutize(java.lang.String, java.lang.String)
 */
public String absolutize(String baseURI, String location) {
    return SourceUtil.absolutize(baseURI, location, true);
}

}
