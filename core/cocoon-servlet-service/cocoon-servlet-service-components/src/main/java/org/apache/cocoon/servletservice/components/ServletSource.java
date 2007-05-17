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
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.servletservice.ServletConnection;
import org.apache.cocoon.servletservice.postable.PostableSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.AbstractSource;
import org.apache.excalibur.store.Store;

/**
 * Implementation of a {@link Source} that gets its content by invoking the Servlet.
 * 
 * @version $Id$
 */
public class ServletSource extends AbstractSource implements PostableSource {

	private transient Log logger = LogFactory.getLog(getClass());

	private ServletConnection servletConnection;
	private String location;
	
	/**
	 * The store is used to store values of Last-Modified header (if it exists). This store is required because in
	 * {@link #getValidity()} we need value of Last-Modified header of previous response in order to perform conditional
	 * GET.
	 * 
	 * @see Broken caching of servlet: source in some cases thread
	 *      (http://news.gmane.org/find-root.php?group=gmane.text.xml.cocoon.devel&article=72801)
	 */
	private Store store;

	private boolean connected;

	public ServletSource(String location, Store store) throws IOException {
		// the systemId (returned by getURI()) is by default null
		// using the block uri is a little bit questionable as it only is valid
		// whithin the current block, not globally
		this.store = store;
		setSystemId(location);
		this.location = location;
		this.servletConnection = new ServletConnection(location);
		connected = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.excalibur.source.impl.AbstractSource#getInputStream()
	 */
	public InputStream getInputStream() throws IOException, SourceException {
		try {
			connect();
			//FIXME: This is not the most elegant solution
			if (servletConnection.getResponseCode() != HttpServletResponse.SC_OK) {
				//most probably, servlet returned 304 (not modified) and we need to perform second request to get data
				servletConnection = new ServletConnection(location);
				servletConnection.connect();
			}
			return this.servletConnection.getInputStream();
		} catch (ServletException e) {
			throw new CascadingIOException(e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.excalibur.source.impl.AbstractSource#getValidity()
	 */
	public SourceValidity getValidity() {
		try {
			connect();
			return servletConnection.getLastModified() > 0 ? new ServletValidity(servletConnection.getResponseCode()) : null;
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.debug("Exception occured while making servlet request", e);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.excalibur.source.impl.AbstractSource#getLastModified()
	 */
	public long getLastModified() {
		try {
			connect();
			return servletConnection.getLastModified() > 0 ? servletConnection.getLastModified() : 0;
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.debug("Exception occured while making servlet request", e);
			return 0;
		}
	}
	
    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be null.
     */
    public String getMimeType() {
    	try {
			connect();
			return servletConnection.getContentType();
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.debug("Exception occured while making servlet request", e);
			return null;
		}
    }

	/**
	 * Returns true always.
	 * 
	 * @see org.apache.excalibur.source.Source#exists()
	 */
	public boolean exists() {
		return true;
	}

	public OutputStream getOutputStream() throws IOException {
		return servletConnection.getOutputStream();
	}

	private void connect() throws IOException, ServletException {
		if (connected)
			return;
		long lastModified = getStoredLastModified();
		if (lastModified > 0)
			servletConnection.setIfModifiedSince(lastModified);
		servletConnection.connect();
		connected = true;
		// If header is present, Last-Modified value will be stored for further
		// use in conditional gets
		setStoredLastModified(servletConnection.getLastModified());
	}

	/**
	 * Returns Last-Modified value from previous response if present. Otherwise 0 is returned.
	 * 
	 * @return Last-Modified value from previous response if present. Otherwise 0 is returned.
	 */
	private long getStoredLastModified() {
		Long lastModified = (Long) store.get(calculateInternalKey());
		return lastModified != null ? lastModified.longValue() : 0;
	}

	/**
	 * Stores value of Last-Modified header in {@link #store}.
	 * 
	 * @param lastModified
	 *            value that will be stored in {@link #store}. Only positive values will be stored.
	 * @throws IOException
	 *             if exception occured while storing value
	 */
	private void setStoredLastModified(long lastModified) throws IOException {
		String key = calculateInternalKey();
		store.remove(key);
		if (lastModified > 0)
			store.store(key, new Long(lastModified));
	}

	/**
	 * @return key that will be used to store value of Last-Modified header
	 */
	private String calculateInternalKey() {
		return ServletSource.class.getName() + "$" + getURI();
	}

	private final class ServletValidity implements SourceValidity {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1793646888814956538L;

		private int responseCode;

		public ServletValidity(int responseCode) {
			setResponseCode(responseCode);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.excalibur.source.SourceValidity#isValid()
		 */
		public int isValid() {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.excalibur.source.SourceValidity#isValid(org.apache.excalibur.source.SourceValidity)
		 */
		public int isValid(SourceValidity newValidity) {
			if (newValidity instanceof ServletValidity) {
				ServletValidity newServletValidity = (ServletValidity) newValidity;

				switch (newServletValidity.getResponseCode()) {
				case HttpServletResponse.SC_NOT_MODIFIED:
					return SourceValidity.VALID;
				case HttpServletResponse.SC_OK:
					return SourceValidity.INVALID;
				default:
					return 0;
				}
			}
			return SourceValidity.UNKNOWN;
		}

		public int getResponseCode() {
			return responseCode;
		}

		public void setResponseCode(int responseCode) {
			this.responseCode = responseCode;
		}
	}

}
