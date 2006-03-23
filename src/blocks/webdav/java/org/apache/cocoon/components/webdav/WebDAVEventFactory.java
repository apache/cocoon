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
package org.apache.cocoon.components.webdav;

import org.apache.cocoon.caching.validity.Event;
import org.apache.cocoon.caching.validity.EventFactory;
import org.apache.commons.httpclient.HttpURL;

/**
 * Factory interface for constructing Event objects for webdav resources 
 */
public interface WebDAVEventFactory extends EventFactory {
	
	public static final String ROLE = WebDAVEventFactory.class.getName();

	public Event createEvent(String url);
	public Event createEvent(HttpURL url);
}
