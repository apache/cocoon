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
package org.apache.cocoon.servletservice.components;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.servletservice.ServletConnection;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.AbstractSource;

/**
 * Implementation of a {@link Source} that gets its content by
 * invoking the Servlet. 
 *
 * @version $Id$
 */
public class ServletSource extends AbstractSource {
    
    private ServletConnection servletConnection;
    /**
     * <p>This validity must be lazy-initialized as there can occur following situations:</p>
     * <ol>
     * <li>Source is asked for the validity for the first time so the validity won't be asked if it's valid and 
     * <code>getInputStream()</code> will be called to get response body. While making request there is no information that could 
     * be used to set <code>ifModifiedSince</code> property but <i>after</i> fetching response there could be information about 
     * last modification date that has to be set to the <code>ServletValidity</code> instance.</li>
     * <li>It's second (or next) time that Source is asked for the validity object so caller has old validity object. Old validity 
     * object will be asked if it's still valid. New validity object has reference to the {@link ServletConnection} so 
     * <code>ifModifiedSince</code> property can be set by value pulled from <b>old</b> validity object.</li>
     * </ol>
     * 
     * <p>This two different situations demand lazy-initialization.</p>
     */
    private final ServletValidity validity;
    
    public ServletSource(String location) throws IOException {
        // the systemId (returned by getURI()) is by default null
        // using the block uri is a little bit questionable as it only is valid
        // whithin the current block, not globally
        setSystemId(location);
        this.servletConnection = new ServletConnection(location);
        this.validity = new ServletValidity(this.servletConnection);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.impl.AbstractSource#getInputStream()
     */
    public InputStream getInputStream() throws IOException, SourceException {
        try {
        	connect();
            return this.servletConnection.getInputStream();
        } catch (ServletException e) {
            throw new CascadingIOException(e.getMessage(), e);
        }
    }

    public long getLastModified() {
    	try {
			connect();
		} catch (Exception e) {
			return 0;
		}
    	return servletConnection.getLastModified();
	}

	public SourceValidity getValidity() {
		return this.validity;
	}

	/**
     * Returns true always.
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        return true;
    }
    
    private void connect() throws IOException, ServletException {
    	servletConnection.connect();
    	//This way it's guaranteed that validity has proper value set
    	validity.setLastModified(servletConnection.getLastModified());
    }

    private final class ServletValidity implements SourceValidity {
    	
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1793646888814956538L;
		
		private transient ServletConnection servletConnection;
    	private long lastModified;

		public ServletValidity(ServletConnection servletConnection) {
			this.servletConnection = servletConnection;
    	}

		/* (non-Javadoc)
		 * @see org.apache.excalibur.source.SourceValidity#isValid()
		 */
		public int isValid() {
			return 0;
		}

		/* (non-Javadoc)
		 * @see org.apache.excalibur.source.SourceValidity#isValid(org.apache.excalibur.source.SourceValidity)
		 */
		public int isValid(SourceValidity newValidity) {
			if (newValidity instanceof ServletValidity) {
				ServletValidity newServletValidity = (ServletValidity)newValidity;
				
				newServletValidity.servletConnection.setIfModifiedSince(this.getLastModified());
				try {
					newServletValidity.servletConnection.connect();
					switch (newServletValidity.servletConnection.getResponseCode()) {
						case HttpServletResponse.SC_NOT_MODIFIED: return 1;
						case HttpServletResponse.SC_OK: return -1;
						default: return 0; 
					}
				} catch (Exception e) {
					return 0;
				}
			}
			return 0;
		}

		public long getLastModified() {
			return lastModified;
		}

		public void setLastModified(long lastModified) {
			this.lastModified = lastModified;
		}
    	
    }

}
