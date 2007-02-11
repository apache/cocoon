/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.deployer.logger;

/**
 * A very simple logger that prints out all messages to the console using some prefixes.
 */
public class ConsoleLogger implements Logger {

	public void verbose(String msg) {
		System.out.println("[DEBUG] " + msg);
	}

	public void info(String msg) {
		System.out.println("[INFO] " + msg);
	}

	public void error(String msg) {
		System.err.println("[ERROR] " + msg);
	}

}
