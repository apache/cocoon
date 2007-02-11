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

package org.apache.cocoon.components.modules.input;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.blocks.Block;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.blocks.BlockEnvironmentHelper;
import org.apache.cocoon.environment.internal.EnvironmentHelper;

/**
 * BlockPathModule returns the absolute path of a block protocol path.
 *
 * @version $Id$
 */
public class BlockPathModule implements InputModule, ThreadSafe {

    public Object getAttribute( String name, Configuration modeConf, Map objectModel )
    throws ConfigurationException {
        Environment env = EnvironmentHelper.getCurrentEnvironment();
        Block block = BlockEnvironmentHelper.getCurrentBlock();
        String absoluteURI = null;
        String baseURI = env.getURIPrefix();
        if (baseURI.length() == 0 || !baseURI.startsWith("/"))
            baseURI = "/" + baseURI;
        try {
            URI uri = block.absolutizeURI(new URI(name), new URI(null, null, baseURI, null));
            absoluteURI = uri.toString();
        } catch (URISyntaxException e) {
            throw new ConfigurationException("Couldn't absolutize " + name);
        }
        return absoluteURI;
    }

    public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
        throws ConfigurationException {
        throw new UnsupportedOperationException();
    }

    public Iterator getAttributeNames(Configuration modeConf, Map objectModel)
        throws ConfigurationException {
        throw new UnsupportedOperationException();
    }
}
