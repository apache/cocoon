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
import java.io.InputStream;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.blocks.BlockConnection;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.impl.AbstractSource;

/**
 * Implementation of a {@link Source} that gets its content by
 * invoking the Block. 
 *
 * @version $Id$
 */
public class BlockSource extends AbstractSource {
    
    private BlockConnection blockConnection;
    
    public BlockSource(String location, Logger logger) throws IOException {
        // the systemId (returned by getURI()) is by default null
        // using the block uri is a little bit questionable as it only is valid
        // whithin the current block, not globally
        setSystemId(location);
        this.blockConnection = new BlockConnection(location, logger);
        this.blockConnection.connect();
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.impl.AbstractSource#getInputStream()
     */
    public InputStream getInputStream() throws IOException, SourceException {
        return this.blockConnection.getInputStream();
    }

    /**
     * Returns true always.
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        return true;
    }

}
