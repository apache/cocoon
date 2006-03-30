package org.apache.cocoon.maven.deployer;

import org.apache.cocoon.deployer.logger.Logger;
import org.apache.maven.plugin.logging.Log;

/**
 * This logger can be used within the cocoon-deployer library to send all 
 * logging messages of the library to the Maven logger.
 */
public class MavenLoggingWrapper implements Logger {
	
	private Log logger;

	public MavenLoggingWrapper(Log logger) {
		this.logger = logger;
	}

	public void verbose(String msg) {
		this.logger.debug(msg);		
	}

	public void info(String msg) {
		this.logger.info(msg);
	}

	public void error(String msg) {
		this.logger.error(msg);
	}

}
