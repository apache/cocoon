/*
 * Copyright 2001,2004 The Apache Software Foundation.
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

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.source.SourceFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Source;
import org.apache.cocoon.ProcessingException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A factory for 'file:' sources.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: FileSourceFactory.java,v 1.3 2004/03/05 13:02:40 bdelacretaz Exp $
 * @deprecated Use the new avalon source resolving instead
 */
public class FileSourceFactory extends AbstractLogEnabled
    implements SourceFactory, Composable, ThreadSafe {
    
    private ComponentManager manager;

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }
    
    public Source getSource(Environment environment, String location)
      throws ProcessingException, MalformedURLException, IOException {
        Source result = new FileSource(location, this.manager);
        setupLogger(result);
        return result;
    }

    public Source getSource(Environment environment, URL base, String location)
      throws ProcessingException, MalformedURLException, IOException {
        return getSource(environment, new URL(base, location).toExternalForm());
    }
}
    
