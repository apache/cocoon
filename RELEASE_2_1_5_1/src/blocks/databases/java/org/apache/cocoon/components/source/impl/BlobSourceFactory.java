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
package org.apache.cocoon.components.source.impl;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * A factory for 'blob:' sources.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: BlobSourceFactory.java,v 1.5 2004/03/05 13:01:55 bdelacretaz Exp $
 */
public class BlobSourceFactory
  extends AbstractLogEnabled
  implements Serviceable, SourceFactory, ThreadSafe {
    
    /** The ServiceManager instance */
    protected ServiceManager manager;

    /**
     * Get a <code>Source</code> object.
     * @param parameters This is optional.
     */
    public Source getSource(String location, Map parameters)
        throws MalformedURLException, IOException, SourceException {
        BlobSource blob = new BlobSource(location);
        this.setupLogger(blob);
        blob.service(this.manager);
        return blob;
    }

    /**
     * Release a {@link Source} object.
     */
    public void release( Source source ) {
        // Nothing to do
    }
    
	/**
	 * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
	 */
	public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
	}

}
    
