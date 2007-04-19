package org.apache.cocoon.servletservice.postable.components;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.impl.AbstractSource;

public class ServletServiceConsumerSource extends AbstractSource {
	
	private Log logger = LogFactory.getLog(getClass());
	
	private InputStream requestBody;
	
	public ServletServiceConsumerSource(HttpServletRequest request) {
		try {
			requestBody = request.getInputStream();
		} catch (Exception e) {
			logger.error("Error during obtaning request's body (POST data)", e);
		}
	}

	public boolean exists() {
		return requestBody != null;
	}
	
	public InputStream getInputStream() throws IOException, SourceException {
		if (!exists()) throw new SourceException("POST data does not exists for request. Make sure you are processing service call.");
		return requestBody;
	}

}
