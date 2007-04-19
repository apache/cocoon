package org.apache.cocoon.servletservice.postable.components;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.cocoon.processing.ProcessInfoProvider;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;

public class ServletServiceConsumerSourceFactory implements SourceFactory {
	
	private ProcessInfoProvider processInfoProvider;

	public Source getSource(String location, Map parameters) throws IOException, MalformedURLException {
		return new ServletServiceConsumerSource(processInfoProvider.getRequest());
		
	}

	public void release(Source source) {
	}

	public ProcessInfoProvider getProcessInfoProvider() {
		return processInfoProvider;
	}

	public void setProcessInfoProvider(ProcessInfoProvider processInfoProvider) {
		this.processInfoProvider = processInfoProvider;
	}

}
